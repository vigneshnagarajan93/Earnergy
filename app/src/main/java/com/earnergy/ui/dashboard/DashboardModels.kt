package com.earnergy.ui.dashboard

import com.earnergy.domain.model.FocusMetrics
import com.earnergy.domain.model.HealthMetrics

data class DashboardUiState(
    val investedMinutes: Int = 0,
    val driftMinutes: Int = 0,
    val valueOfInvestedTime: Double = 0.0,
    val costOfDriftTime: Double = 0.0,
    val netValue: Double = 0.0,
    val focusMetrics: FocusMetrics? = null,
    val healthMetrics: HealthMetrics? = null,
    val suggestions: List<com.earnergy.domain.model.Suggestion> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
