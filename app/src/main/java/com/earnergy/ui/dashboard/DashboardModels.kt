package com.earnergy.ui.dashboard

data class DashboardUiState(
    val investedMinutes: Int = 0,
    val driftMinutes: Int = 0,
    val valueOfInvestedTime: Double = 0.0,
    val costOfDriftTime: Double = 0.0,
    val netValue: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
