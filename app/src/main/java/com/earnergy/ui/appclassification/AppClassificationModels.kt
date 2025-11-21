package com.earnergy.ui.appclassification



import com.earnergy.domain.model.AppRole

data class AppClassificationItem(
    val appName: String,
    val packageName: String,
    val todayMinutes: Int = 0,
    val role: AppRole,
    val isSystemApp: Boolean = false
)

data class AppClassificationUiState(
    val apps: List<AppClassificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
