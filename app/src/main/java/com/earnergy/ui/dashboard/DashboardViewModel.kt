package com.earnergy.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earnergy.core.data.repository.UsageRepository
import com.earnergy.domain.calculation.EarningCalculator
import com.earnergy.domain.model.DaySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val clock: Clock,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val todayEpochDay: Long = LocalDate.now(clock).toEpochDay()
    private val productivePackages = setOf(
        "com.google.android.apps.docs",
        "com.google.android.apps.keep",
        "com.microsoft.todos",
        "com.notion.android",
        "com.slack"
    )

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDaySummary()
        refreshNow()
    }

    fun refresh() {
        refreshNow()
    }

    fun refreshNow() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                usageRepository.refreshToday()
            } catch (throwable: Throwable) {
                _uiState.update { it.copy(errorMessage = throwable.message ?: "Unable to refresh") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun observeDaySummary() {
        viewModelScope.launch {
            usageRepository.observeDaySummary(todayEpochDay).collect { summary ->
                _uiState.update {
                    it.withSummary(summary).copy(isLoading = false, errorMessage = null)
                }
            }
        }
    }

    private fun DashboardUiState.withSummary(summary: DaySummary): DashboardUiState {
        val impact = EarningCalculator.computeImpact(summary, productivePackages)
        return copy(
            investedMinutes = (impact.productiveSeconds / 60).toInt(),
            driftMinutes = (impact.passiveSeconds / 60).toInt(),
            valueOfInvestedTime = impact.potentialEarningsUsd,
            costOfDriftTime = impact.potentialLossUsd
        )
    }
}
