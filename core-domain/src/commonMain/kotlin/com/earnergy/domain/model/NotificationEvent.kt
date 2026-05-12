package com.earnergy.domain.model

/**
 * Represents a notification event.
 */
data class NotificationEvent(
    val timestamp: Long,
    val packageName: String,
    val dateEpochDay: Long
)
