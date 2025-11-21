package com.earnergy.domain.model

/**
 * Represents focus metrics for a specific day, used for trend analysis.
 */
data class FocusTrend(
    val dateEpochDay: Long,
    val focusScore: Double,
    val appSwitchCount: Int,
    val deepWorkMinutes: Int
)
