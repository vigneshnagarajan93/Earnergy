package com.earnergy.ui.charts

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ChartsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ChartsUiState())
    val uiState: StateFlow<ChartsUiState> = _uiState.asStateFlow()
}

data class ChartsUiState(
    val investedHoursLabel: String = "12h",
    val driftHoursLabel: String = "5h",
    val investedRatio: Float = 0.7f,
    val driftRatio: Float = 0.3f,
    val summary: String = "You're investing most of your time wisely.",
    val isLoading: Boolean = false
)
