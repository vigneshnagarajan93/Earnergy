package com.earnergy.domain.model

/**
 * Represents a screen session for health tracking.
 */
data class ScreenSession(
    val startTime: Long, // Epoch millis
    val endTime: Long, // Epoch millis
    val durationMinutes: Int
)
