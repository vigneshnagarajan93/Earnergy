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

import com.earnergy.core.data.local.AppSwitchEventDao
import com.earnergy.core.data.local.AppSwitchEventEntity
import com.earnergy.domain.calculation.FocusCalculator
import com.earnergy.domain.model.AppSwitchEvent
import kotlinx.coroutines.flow.combine

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val appSwitchEventDao: AppSwitchEventDao,
    private val clock: Clock,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val todayEpochDay: Long = LocalDate.now(clock).toEpochDay()


    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeData()
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

    private fun observeData() {
        viewModelScope.launch {
            combine(
                usageRepository.observeDaySummary(todayEpochDay),
                appSwitchEventDao.observeForDay(todayEpochDay)
            ) { summary, switchEntities ->
                val switches = switchEntities.map { it.toDomain() }
                val focusMetrics = FocusCalculator.computeFocusMetrics(
                    usages = summary.usages,
                    appSwitchEvents = switches,
                    dateEpochDay = todayEpochDay
                )
                
                Pair(summary, focusMetrics)
            }.collect { (summary, focusMetrics) ->
                _uiState.update {
                    it.withSummary(summary).copy(
                        focusMetrics = focusMetrics,
                        isLoading = false, 
                        errorMessage = null
                    )
                }
            }
        }
    }
    
    private fun AppSwitchEventEntity.toDomain() = AppSwitchEvent(
        timestamp = timestamp,
        fromPackage = fromPackage,
        toPackage = toPackage,
        dateEpochDay = dateEpochDay
    )

    private fun DashboardUiState.withSummary(summary: DaySummary): DashboardUiState {
        val impact = EarningCalculator.computeImpact(summary)
        return copy(
            investedMinutes = (impact.productiveSeconds / 60).toInt(),
            driftMinutes = (impact.passiveSeconds / 60).toInt(),
            valueOfInvestedTime = impact.potentialEarningsUsd,
            costOfDriftTime = impact.potentialLossUsd,
            netValue = impact.potentialEarningsUsd - impact.potentialLossUsd
        )
    }
}
