package com.earnergy.domain.model

/**
 * Represents a smart suggestion for the user.
 */
data class Suggestion(
    val id: String,
    val type: SuggestionType,
    val title: String,
    val description: String,
    val priority: Priority,
    val manualSteps: List<String> = emptyList(), // Step-by-step instructions for manual actions
    val autoActionAvailable: Boolean = false, // Whether app can perform action automatically
    val timestamp: Long
)

/**
 * Types of suggestions the app can generate.
 */
enum class SuggestionType {
    TAKE_BREAK,           // Suggest taking a break for eye health
    ENABLE_GRAYSCALE,     // Suggest enabling grayscale mode
    ENABLE_DND,           // Suggest enabling Do Not Disturb
    REDUCE_DRIFT_TIME,    // Suggest reducing time on drift apps
    IMPROVE_FOCUS,        // Suggest ways to improve focus score
    ENERGY_BOOST          // Suggest activities to boost energy
}

/**
 * Priority levels for suggestions.
 */
enum class Priority {
    LOW,      // Nice to have
    MEDIUM,   // Recommended
    HIGH,     // Important
    URGENT    // Critical (e.g., eye strain is very high)
}
