package com.earnergy.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earnergy.core.data.repository.UsageRepository
import com.earnergy.domain.calculation.EarningCalculator
import com.earnergy.domain.model.DaySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.time.Clock
import java.time.LocalDate
import java.util.Locale
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
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val productivePackages = setOf(
        "com.google.android.apps.docs",
        "com.google.android.apps.keep",
        "com.microsoft.todos",
        "com.notion.android",
        "com.slack"
    )

    init {
        observeDaySummary()
        refreshNow()
    }

    fun refreshNow() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                usageRepository.refreshToday()
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun observeDaySummary() {
        viewModelScope.launch {
            usageRepository.observeDaySummary(todayEpochDay).collect { summary ->
                _uiState.update { it.updateFromSummary(summary, currencyFormatter, productivePackages) }
            }
        }
    }
}

data class DashboardUiState(
    val productiveTime: String = "00:00",
    val passiveTime: String = "00:00",
    val potentialEarnings: String = "\$0.00",
    val potentialLoss: String = "\$0.00",
    val hourlyRate: Double = 25.0,
    val isRefreshing: Boolean = false
) {
    fun updateFromSummary(
        summary: DaySummary,
        formatter: NumberFormat,
        productivePackages: Set<String>
    ): DashboardUiState {
        val impact = EarningCalculator.computeImpact(summary, productivePackages)
        return copy(
            productiveTime = formatDuration(impact.productiveSeconds),
            passiveTime = formatDuration(impact.passiveSeconds),
            potentialEarnings = formatter.format(impact.potentialEarningsUsd),
            potentialLoss = formatter.format(impact.potentialLossUsd),
            hourlyRate = summary.hourlyRate
        )
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }
}
