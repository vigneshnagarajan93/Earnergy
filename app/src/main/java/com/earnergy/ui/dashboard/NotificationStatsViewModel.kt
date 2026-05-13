package com.earnergy.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earnergy.core.data.repository.UsageRepository
import com.earnergy.domain.model.AppRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class NotificationStatsViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val notificationEventDao: com.earnergy.core.data.local.NotificationEventDao,
    private val clock: Clock
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(NotificationStatsUiState(isLoading = true))
    val uiState: StateFlow<NotificationStatsUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        val today = LocalDate.now(clock).toEpochDay()

        viewModelScope.launch {
            combine(
                usageRepository.observeAllApps(today),
                notificationEventDao.observeForDay(today),
                _searchQuery
            ) { allApps, notificationEntities, query ->
                val notificationCounts = notificationEntities
                    .groupBy { it.packageName }
                    .mapValues { it.value.size }

                val stats = allApps.mapNotNull { app ->
                    val count = notificationCounts[app.packageName] ?: 0
                    if (count > 0 || app.packageName.contains(query, ignoreCase = true) || app.displayName.contains(query, ignoreCase = true)) {
                         AppNotificationStat(
                            packageName = app.packageName,
                            displayName = app.displayName,
                            count = count,
                            role = app.role
                        )
                    } else null
                }.sortedByDescending { it.count }

                val filteredStats = if (query.isBlank()) {
                    stats.filter { it.count > 0 }
                } else {
                    stats.filter {
                        it.displayName.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
                    }
                }

                val driftStats = stats.filter { it.role == AppRole.DRIFT && it.count > 0 }

                NotificationStatsUiState(
                    totalNotifications = notificationEntities.size,
                    appNotificationStats = filteredStats,
                    driftNotificationStats = driftStats,
                    searchQuery = query,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
