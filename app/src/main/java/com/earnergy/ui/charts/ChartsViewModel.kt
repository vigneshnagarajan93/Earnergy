package com.earnergy.ui.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earnergy.core.data.repository.UsageRepository
import com.earnergy.domain.calculation.EarningCalculator
import com.earnergy.domain.model.FocusTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChartsUiState())
    val uiState: StateFlow<ChartsUiState> = _uiState.asStateFlow()
    
    init {
        loadWeeklyData()
    }
    
    fun refresh() {
        loadWeeklyData()
    }
    
    private fun loadWeeklyData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val today = LocalDate.now(clock)
                val weekStart = today.minusDays(6) // Last 7 days including today
                
                var totalInvestedSeconds = 0L
                var totalDriftSeconds = 0L
                
                // Load each day's data
                for (i in 0..6) {
                    val date = weekStart.plusDays(i.toLong())
                    val summary = usageRepository.loadDay(date.toEpochDay())
                    
                    if (summary != null) {
                        val impact = EarningCalculator.computeImpact(summary)
                        totalInvestedSeconds += impact.productiveSeconds
                        totalDriftSeconds += impact.passiveSeconds
                    }
                }
                
                val totalSeconds = totalInvestedSeconds + totalDriftSeconds
                val investedRatio = if (totalSeconds > 0) {
                    totalInvestedSeconds.toFloat() / totalSeconds
                } else 0f
                val driftRatio = if (totalSeconds > 0) {
                    totalDriftSeconds.toFloat() / totalSeconds
                } else 0f
                
                val investedHours = totalInvestedSeconds / 3600
                val driftHours = totalDriftSeconds / 3600
                
                val summary = when {
                    totalSeconds == 0L -> "No data yet. Start classifying your apps!"
                    investedRatio >= 0.7f -> "Excellent! You're investing most of your time wisely."
                    investedRatio >= 0.5f -> "Good balance. Keep focusing on productive activities."
                    investedRatio >= 0.3f -> "You're drifting more than investing. Time to refocus!"
                    else -> "Most of your time is drifting. Let's turn this around!"
                }
                
                // Load focus trends
                val focusTrends = usageRepository.loadWeeklyFocusTrends(
                    weekStart.toEpochDay(),
                    today.toEpochDay()
                )
                
                // Calculate focus analytics
                val weeklyAverageFocusScore = if (focusTrends.isNotEmpty()) {
                    focusTrends.map { it.focusScore }.average()
                } else 0.0
                
                val totalDeepWorkMinutes = focusTrends.sumOf { it.deepWorkMinutes }
                val totalDeepWorkHours = totalDeepWorkMinutes / 60
                
                // Find most common peak productivity hour across the week
                val peakHours = focusTrends.mapNotNull { trend ->
                    // We'd need to enhance this - for now just show if we have data
                    null
                }
                val peakProductivityHour = null // Will be enhanced in future
                
                _uiState.update {
                    it.copy(
                        investedHoursLabel = "${investedHours}h",
                        driftHoursLabel = "${driftHours}h",
                        investedRatio = investedRatio,
                        driftRatio = driftRatio,
                        summary = summary,
                        focusTrends = focusTrends,
                        weeklyAverageFocusScore = weeklyAverageFocusScore,
                        totalDeepWorkHours = totalDeepWorkHours,
                        peakProductivityHour = peakProductivityHour,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        summary = "Error loading insights: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
}

data class ChartsUiState(
    val investedHoursLabel: String = "0h",
    val driftHoursLabel: String = "0h",
    val investedRatio: Float = 0f,
    val driftRatio: Float = 0f,
    val summary: String = "Loading...",
    val isLoading: Boolean = false,
    // Focus analytics
    val focusTrends: List<FocusTrend> = emptyList(),
    val weeklyAverageFocusScore: Double = 0.0,
    val totalDeepWorkHours: Int = 0,
    val peakProductivityHour: String? = null
)
