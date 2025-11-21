package com.earnergy.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for tracking user breaks.
 * Used to calculate health metrics and eye strain score.
 */
@Entity(tableName = "break_events")
data class BreakEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long, // Epoch millis when break started
    val durationMinutes: Int, // How long the break lasted
    val dateEpochDay: Long // Date for grouping (LocalDate.toEpochDay())
)
