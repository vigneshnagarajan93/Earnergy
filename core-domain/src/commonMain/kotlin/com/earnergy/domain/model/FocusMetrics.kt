package com.earnergy.domain.model

/**
 * Metrics related to user focus and concentration quality.
 */
data class FocusMetrics(
    val focusScore: Double, // 0-100, higher is better
    val appSwitchCount: Int, // Number of times user switched apps
    val longestFocusSessionMinutes: Int, // Longest uninterrupted session
    val averageFocusSessionMinutes: Double, // Average session length
    val distractionIndex: Double, // 0-1, higher means more distracted
    val deepWorkSessionCount: Int = 0, // Number of deep work sessions (25+ min uninterrupted)
    val totalDeepWorkMinutes: Int = 0, // Total time spent in deep work
    val peakProductivityHour: Int? = null // Hour of day (0-23) with best focus, null if no data
)
