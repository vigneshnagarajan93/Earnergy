package com.earnergy.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for tracking user breaks.
 * Used to calculate health metrics and eye strain score.
 */
@Entity(
    tableName = "break_events",
    indices = [androidx.room.Index(value = ["dateEpochDay"])]
)
data class BreakEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long, // Epoch millis when break started
    val dateEpochDay: Long, // Date for grouping (LocalDate.toEpochDay())
    val durationSeconds: Int, // How long the break lasted
    val wasManual: Boolean = false // True if user manually logged, false if auto-detected
)
