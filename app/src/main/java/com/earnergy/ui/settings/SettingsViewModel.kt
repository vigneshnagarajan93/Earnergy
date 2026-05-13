package com.earnergy.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earnergy.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Currency

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    companion object {
        private val cachedAvailableCurrencies by lazy {
            try {
                Currency.getAvailableCurrencies().sortedBy { it.currencyCode }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private val _uiState = MutableStateFlow(SettingsUiState(
        hourlyRateInput = "",
        availableCurrencies = cachedAvailableCurrencies
    ))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.hourlyRate,
                settingsRepository.currencyCode
            ) { rate, currencyCode ->
                rate to currencyCode
            }.collectLatest { (rate, currencyCode) ->
                val formatted = String.format("%.2f", rate)
                _uiState.update {
                    it.copy(
                        hourlyRateInput = formatted,
                        currencyCode = currencyCode,
                        isSaving = false,
                        errorMessage = null
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.healthFeaturesEnabled.collectLatest { enabled ->
                _uiState.update { it.copy(healthFeaturesEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.brightnessWarningEnabled.collectLatest { enabled ->
                _uiState.update { it.copy(brightnessWarningEnabled = enabled) }
            }
        }
    }

    fun onHourlyRateChanged(value: String) {
        _uiState.update { it.copy(hourlyRateInput = value, errorMessage = null) }
    }

    fun onCurrencyChanged(code: String) {
        _uiState.update { it.copy(currencyCode = code) }
    }

    fun onHealthFeaturesToggled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHealthFeaturesEnabled(enabled)
        }
    }

    fun onBrightnessWarningToggled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBrightnessWarningEnabled(enabled)
        }
    }

    fun onSaveClicked(force: Boolean = false) {
        val input = _uiState.value.hourlyRateInput.trim()
        val value = input.toDoubleOrNull()
        val currencyCode = _uiState.value.currencyCode
        val errorMessage = "Please enter a valid hourly rate"

        if (value == null || value <= 0) {
            _uiState.update { it.copy(errorMessage = errorMessage) }
            viewModelScope.launch { _events.emit(SettingsEvent.SaveError(errorMessage)) }
            return
        }

        if (!force) {
            // Check if rate or currency changed mid-day to show confirmation
            viewModelScope.launch {
                val currentRate = settingsRepository.hourlyRate.first()
                val currentCurrency = settingsRepository.currencyCode.first()
                if (currentRate != value || currentCurrency != currencyCode) {
                    _uiState.update { it.copy(showConfirmationDialog = true) }
                    return@launch
                }
                saveSettings(value, currencyCode)
            }
        } else {
            _uiState.update { it.copy(showConfirmationDialog = false) }
            saveSettings(value, currencyCode)
        }
    }

    private fun saveSettings(rate: Double, currencyCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                settingsRepository.setHourlyRate(rate)
                settingsRepository.setCurrencyCode(currencyCode)
                _events.emit(SettingsEvent.SaveSuccess)
            } catch (throwable: Throwable) {
                val failureMessage = "Failed to save hourly rate"
                _uiState.update { it.copy(errorMessage = failureMessage) }
                _events.emit(SettingsEvent.SaveError(failureMessage))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun onDismissConfirmation() {
        _uiState.update { it.copy(showConfirmationDialog = false) }
    }
}

data class SettingsUiState(
    val hourlyRateInput: String = "",
    val currencyCode: String = "USD",
    val availableCurrencies: List<Currency> = emptyList(),
    val currencySearchQuery: String = "",
    val healthFeaturesEnabled: Boolean = true,
    val brightnessWarningEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val showConfirmationDialog: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SettingsEvent {
    object SaveSuccess : SettingsEvent
    data class SaveError(val message: String) : SettingsEvent
}
