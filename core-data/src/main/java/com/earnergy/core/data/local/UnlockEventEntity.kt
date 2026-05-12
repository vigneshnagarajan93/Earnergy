package com.earnergy.core.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for tracking device unlock events.
 */
@Entity(
    tableName = "unlock_events",
    indices = [Index(value = ["dateEpochDay"])]
)
data class UnlockEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val dateEpochDay: Long,
    val wasNotificationLed: Boolean,
    val triggeringPackage: String? = null
)
