package com.earnergy.domain.model

/**
 * Represents a device unlock or lock event.
 */
data class UnlockEvent(
    val timestamp: Long,
    val dateEpochDay: Long,
    val isLockEvent: Boolean
)
