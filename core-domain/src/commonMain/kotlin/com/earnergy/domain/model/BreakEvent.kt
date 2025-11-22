package com.earnergy.domain.model

/**
 * Represents a break event taken by the user.
 * Used to track break compliance and calculate health metrics.
 */
data class BreakEvent(
    val timestamp: Long, // Epoch millis when break occurred
    val dateEpochDay: Long, // Date for grouping
    val durationSeconds: Int, // How long the break lasted
    val wasManual: Boolean // True if user manually logged, false if auto-detected
)
