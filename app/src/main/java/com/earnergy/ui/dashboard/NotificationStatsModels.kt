package com.earnergy.ui.dashboard

import com.earnergy.domain.model.AppRole

data class NotificationStatsUiState(
    val totalNotifications: Int = 0,
    val appNotificationStats: List<AppNotificationStat> = emptyList(),
    val driftNotificationStats: List<AppNotificationStat> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

data class AppNotificationStat(
    val packageName: String,
    val displayName: String,
    val count: Int,
    val role: AppRole
)
