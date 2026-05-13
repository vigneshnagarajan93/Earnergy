package com.earnergy.domain.model

enum class EyeHealthStatus(
    val label: String,
    val message: String
) {
    EXCELLENT(
        label = "EXCELLENT",
        message = "Great pacing! You are maintaining the 20-20-20 habit."
    ),
    GOOD(
        label = "GOOD",
        message = "Good job! Try to keep your sessions under 20 minutes."
    ),
    FAIR(
        label = "FAIR",
        message = "You're spending a bit too much time on screen. Consider a short break."
    ),
    POOR(
        label = "POOR",
        message = "Your eyes have been locked in 'near-vision' for over an hour. Take a 5-minute break now."
    )
}
