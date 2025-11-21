package com.earnergy.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing user suggestions.
 * Suggestions are generated based on usage patterns and stored for display.
 */
@Entity(tableName = "suggestions")
data class SuggestionEntity(
    @PrimaryKey val id: String, // UUID
    val type: String, // SuggestionType.name
    val title: String,
    val description: String,
    val timestamp: Long, // Epoch millis when suggestion was generated
    val dismissed: Boolean = false, // User swiped away
    val completed: Boolean = false // User acted on suggestion
)
