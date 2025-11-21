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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(hourlyRateInput = ""))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    init {
        observeHourlyRate()
    }

    private fun observeHourlyRate() {
        viewModelScope.launch {
            settingsRepository.hourlyRate.collectLatest { rate ->
                val formatted = String.format("%.2f", rate)
                _uiState.update {
                    it.copy(
                        hourlyRateInput = formatted,
                        isSaving = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun onHourlyRateChanged(value: String) {
        _uiState.update { it.copy(hourlyRateInput = value, errorMessage = null) }
    }

    fun onSaveClicked() {
        val input = _uiState.value.hourlyRateInput.trim()
        val value = input.toDoubleOrNull()
        val errorMessage = "Please enter a valid hourly rate"
        if (value == null || value <= 0) {
            _uiState.update { it.copy(errorMessage = errorMessage) }
            viewModelScope.launch { _events.emit(SettingsEvent.SaveError(errorMessage)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                settingsRepository.setHourlyRate(value)
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
}

data class SettingsUiState(
    val hourlyRateInput: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SettingsEvent {
    object SaveSuccess : SettingsEvent
    data class SaveError(val message: String) : SettingsEvent
}
