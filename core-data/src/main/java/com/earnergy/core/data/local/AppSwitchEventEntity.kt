package com.earnergy.core.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for tracking app switch events.
 * Used to calculate focus score by analyzing app switching frequency.
 */
@Entity(
    tableName = "app_switch_events",
    indices = [Index(value = ["dateEpochDay"])] // Index for fast date-based queries
)
data class AppSwitchEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long, // Epoch millis when switch occurred
    val fromPackage: String, // Package name of previous app
    val toPackage: String, // Package name of new app
    val dateEpochDay: Long // Date for grouping (LocalDate.toEpochDay())
)
