package com.earnergy.domain.model

/**
 * Represents a device unlock event.
 */
data class UnlockEvent(
    val timestamp: Long,
    val dateEpochDay: Long,
    val wasNotificationLed: Boolean,
    val triggeringPackage: String? = null
)
