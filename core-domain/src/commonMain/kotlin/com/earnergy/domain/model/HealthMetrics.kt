package com.earnergy.domain.model

/**
 * Metrics related to user health and wellbeing.
 */
data class HealthMetrics(
    val totalScreenTimeMinutes: Int, // Total screen time for the day
    val continuousScreenTimeMinutes: Int, // Longest continuous screen time without break
    val eyeStrainScore: Double, // 0-100, higher means more strain
    val breaksTaken: Int, // Number of breaks user has taken
    val breaksRecommended: Int, // Number of breaks recommended (based on 20-20-20 rule)
    val minutesSinceLastBreak: Int // Time since last break
)
