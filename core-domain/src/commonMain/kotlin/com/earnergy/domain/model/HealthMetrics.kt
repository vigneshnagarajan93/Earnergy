package com.earnergy.domain.model

/**
 * Metrics related to user health and wellbeing.
 */
data class HealthMetrics(
    val eyeStrainScore: Double, // 0-100, higher means more strain (deprecated in favor of status)
    val status: EyeHealthStatus,
    val dailyStrainMinutes: Int, // Σ (Session Duration - 20 minutes)
    val maxContinuousSessionMinutes: Int,
    val currentSessionMinutes: Int,
    val totalScreenTimeMinutes: Int, // Total screen time for the day
    val continuousScreenTimeMinutes: Int, // Time since last break (legacy, renamed/refined to currentSessionMinutes)
    val breaksRecommended: Int, // Number of breaks recommended (based on 20-20-20 rule)
    val breaksTaken: Int, // Number of breaks user has taken
    val breakComplianceRate: Double, // 0-1, percentage of breaks taken
    val lastBreakTimestamp: Long? // Null if no breaks today
)
