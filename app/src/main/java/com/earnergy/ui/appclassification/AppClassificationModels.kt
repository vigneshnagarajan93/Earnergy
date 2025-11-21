package com.earnergy.ui.appclassification

import androidx.compose.ui.graphics.painter.Painter

enum class AppRole(val label: String) {
    INVESTED("Invested"),
    DRIFT("Drift"),
    IGNORED("Ignored")
}

data class AppClassificationItem(
    val appName: String,
    val packageName: String,
    val icon: Painter? = null,
    val todayMinutes: Int = 0,
    val role: AppRole
)

data class AppClassificationUiState(
    val apps: List<AppClassificationItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
