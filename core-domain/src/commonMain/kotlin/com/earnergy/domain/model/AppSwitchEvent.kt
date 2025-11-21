package com.earnergy.domain.model

/**
 * Represents an app switch event for focus tracking.
 */
data class AppSwitchEvent(
    val timestamp: Long, // Epoch millis when switch occurred
    val fromPackage: String, // Package name of previous app
    val toPackage: String, // Package name of new app
    val dateEpochDay: Long // Date for grouping
)
