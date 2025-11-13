package com.earnergy.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earnergy.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeHourlyRate()
    }

    private fun observeHourlyRate() {
        viewModelScope.launch {
            settingsRepository.hourlyRate.collectLatest { rate ->
                _uiState.update { it.copy(hourlyRateInput = rate.toString(), isSaving = false, errorMessage = null) }
            }
        }
    }

    fun onHourlyRateChanged(value: String) {
        _uiState.update { it.copy(hourlyRateInput = value) }
    }

    fun onSaveClicked() {
        val input = _uiState.value.hourlyRateInput.trim()
        val value = input.toDoubleOrNull()
        if (value == null || value < 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid hourly rate") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            settingsRepository.setHourlyRate(value)
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}

data class SettingsUiState(
    val hourlyRateInput: String = "25.0",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
