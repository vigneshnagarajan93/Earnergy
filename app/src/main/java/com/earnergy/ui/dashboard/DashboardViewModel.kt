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
import kotlinx.coroutines.flow.flatMapLatest
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
    private val unlockEventDao: com.earnergy.core.data.local.UnlockEventDao,
    private val notificationEventDao: com.earnergy.core.data.local.NotificationEventDao,
    private val clock: Clock,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _todayEpochDay = MutableStateFlow(LocalDate.now(clock).toEpochDay())

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentCurrencyCode = "USD"

    init {
        observeData()
        refreshNow()
    }

    fun refresh() {
        refreshNow()
    }

    fun refreshNow() {
        val currentDay = LocalDate.now(clock).toEpochDay()
        if (_todayEpochDay.value != currentDay) {
            _todayEpochDay.value = currentDay
        }

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

    fun onSuggestionClicked(suggestion: com.earnergy.domain.model.Suggestion) {
        // Implementation for suggestion actions
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeData() {
        viewModelScope.launch {
            _todayEpochDay.flatMapLatest { epochDay ->
                combine(
                    usageRepository.observeDaySummary(epochDay),
                    appSwitchEventDao.observeForDay(epochDay),
                    unlockEventDao.observeForDay(epochDay),
                    usageRepository.observeHealthMetrics(epochDay),
                    usageRepository.observeActiveSuggestions(),
                    notificationEventDao.observeForDay(epochDay),
                    usageRepository.settingsDataStore.currencyCode
                ) { args: Array<*> ->
                    val summary = args[0] as DaySummary
                    val switchEntities = args[1] as List<AppSwitchEventEntity>
                    val unlockEntities = args[2] as List<com.earnergy.core.data.local.UnlockEventEntity>
                    val healthMetrics = args[3] as com.earnergy.domain.model.HealthMetrics
                    val suggestions = args[4] as List<com.earnergy.domain.model.Suggestion>
                    val notificationEntities = args[5] as List<com.earnergy.core.data.local.NotificationEventEntity>
                    val currencyCode = args[6] as String
                    currentCurrencyCode = currencyCode

                    val switches = switchEntities.map { it.toDomain() }
                    val unlocks = unlockEntities.map { it.toDomain() }
                    val notifications = notificationEntities.map { it.toDomain() }
                    val focusMetrics = FocusCalculator.computeFocusMetrics(
                        usages = summary.usages,
                        appSwitchEvents = switches,
                        unlockEvents = unlocks,
                        notificationEvents = notifications,
                        dateEpochDay = epochDay
                    )

                    Quintuple(summary, focusMetrics, healthMetrics, suggestions, null)
                }
            }.collect { (summary, focusMetrics, healthMetrics, suggestions, _) ->
                _uiState.update {
                    it.withSummary(summary).copy(
                        focusMetrics = focusMetrics,
                        healthMetrics = healthMetrics,
                        suggestions = suggestions,
                        isLoading = false, 
                        errorMessage = null
                    )
                }
            }
        }
    }
    
    fun logBreak(durationSeconds: Int = 20) {
        viewModelScope.launch(ioDispatcher) {
            try {
                usageRepository.logBreak(durationSeconds, wasManual = true)
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }
    
    fun dismissSuggestion(id: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                usageRepository.dismissSuggestion(id)
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    private fun AppSwitchEventEntity.toDomain() = AppSwitchEvent(
        timestamp = timestamp,
        fromPackage = fromPackage,
        toPackage = toPackage,
        dateEpochDay = dateEpochDay
    )

    private fun com.earnergy.core.data.local.UnlockEventEntity.toDomain() = com.earnergy.domain.model.UnlockEvent(
        timestamp = timestamp,
        dateEpochDay = dateEpochDay,
        isLockEvent = isLockEvent
    )

    private fun com.earnergy.core.data.local.NotificationEventEntity.toDomain() = com.earnergy.domain.model.NotificationEvent(
        timestamp = timestamp,
        packageName = packageName,
        dateEpochDay = dateEpochDay
    )

    private fun DashboardUiState.withSummary(summary: DaySummary): DashboardUiState {
        val impact = EarningCalculator.computeImpact(summary)
        return copy(
            investedMinutes = (impact.productiveSeconds / 60).toInt(),
            driftMinutes = (impact.passiveSeconds / 60).toInt(),
            valueOfInvestedTime = impact.potentialEarnings,
            costOfDriftTime = impact.potentialLoss,
            netValue = impact.potentialEarnings - impact.potentialLoss,
            currencyCode = currentCurrencyCode
        )
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
