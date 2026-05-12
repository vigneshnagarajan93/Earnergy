package com.earnergy.core.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for tracking notification events.
 */
@Entity(
    tableName = "notification_events",
    indices = [Index(value = ["dateEpochDay"])]
)
data class NotificationEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val packageName: String,
    val dateEpochDay: Long
)
