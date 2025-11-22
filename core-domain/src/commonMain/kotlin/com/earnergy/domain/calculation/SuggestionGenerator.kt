package com.earnergy.domain.calculation

import com.earnergy.domain.model.FocusMetrics
import com.earnergy.domain.model.HealthMetrics
import com.earnergy.domain.model.Priority
import com.earnergy.domain.model.Suggestion
import com.earnergy.domain.model.SuggestionType
import java.util.UUID

/**
 * Generates smart suggestions based on user metrics.
 */
object SuggestionGenerator {
    
    /**
     * Generate suggestions based on current metrics.
     * Returns a list of suggestions ordered by priority.
     */
    fun generateSuggestions(
        healthMetrics: HealthMetrics?,
        focusMetrics: FocusMetrics?,
        investedMinutes: Int,
        driftMinutes: Int
    ): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        val now = System.currentTimeMillis()
        
        // Health-based suggestions
        if (healthMetrics != null) {
            // Urgent: High eye strain
            if (healthMetrics.eyeStrainScore >= 70) {
                suggestions.add(
                    Suggestion(
                        id = UUID.randomUUID().toString(),
                        type = SuggestionType.TAKE_BREAK,
                        title = "⚠️ High Eye Strain Detected",
                        description = "Your eye strain score is ${healthMetrics.eyeStrainScore.toInt()}/100. Take a break immediately to protect your eyes.",
                        priority = Priority.URGENT,
                        manualSteps = listOf(
                            "Look away from your screen",
                            "Focus on something 20 feet away for 20 seconds",
                            "Blink several times to refresh your eyes",
                            "Take a 5-minute walk if possible"
                        ),
                        autoActionAvailable = false,
                        timestamp = now
                    )
                )
            }
            // High priority: Continuous screen time
            else if (healthMetrics.continuousScreenTimeMinutes >= 40) {
                suggestions.add(
                    Suggestion(
                        id = UUID.randomUUID().toString(),
                        type = SuggestionType.TAKE_BREAK,
                        title = "Time for a Break",
                        description = "You've been using your phone for ${healthMetrics.continuousScreenTimeMinutes} minutes straight. Follow the 20-20-20 rule.",
                        priority = Priority.HIGH,
                        manualSteps = listOf(
                            "Look at something 20 feet away",
                            "Hold your gaze for 20 seconds",
                            "Repeat every 20 minutes"
                        ),
                        autoActionAvailable = false,
                        timestamp = now
                    )
                )
            }
            // Medium: Poor break compliance
            else if (healthMetrics.breakComplianceRate < 0.5 && healthMetrics.breaksRecommended > 0) {
                suggestions.add(
                    Suggestion(
                        id = UUID.randomUUID().toString(),
                        type = SuggestionType.TAKE_BREAK,
                        title = "Improve Break Habits",
                        description = "You've only taken ${healthMetrics.breaksTaken} of ${healthMetrics.breaksRecommended} recommended breaks today.",
                        priority = Priority.MEDIUM,
                        manualSteps = listOf(
                            "Set a timer for every 20 minutes",
                            "Take short breaks regularly",
                            "Use the 'Take a Break' button in the app"
                        ),
                        autoActionAvailable = false,
                        timestamp = now
                    )
                )
            }
        }
        
        // Focus-based suggestions
        if (focusMetrics != null) {
            // High: Low focus score
            if (focusMetrics.focusScore < 50) {
                suggestions.add(
                    Suggestion(
                        id = UUID.randomUUID().toString(),
                        type = SuggestionType.IMPROVE_FOCUS,
                        title = "Boost Your Focus",
                        description = "Your focus score is ${focusMetrics.focusScore.toInt()}/100. You switched apps ${focusMetrics.appSwitchCount} times today.",
                        priority = Priority.HIGH,
                        manualSteps = listOf(
                            "Enable Do Not Disturb mode",
                            "Close unnecessary apps",
                            "Focus on one task at a time",
                            "Use app timers to limit distractions"
                        ),
                        autoActionAvailable = false,
                        timestamp = now
                    )
                )
            }
            // Medium: Excessive app switching
            else if (focusMetrics.appSwitchCount > 50) {
                suggestions.add(
                    Suggestion(
                        id = UUID.randomUUID().toString(),
                        type = SuggestionType.IMPROVE_FOCUS,
                        title = "Reduce App Switching",
                        description = "You've switched apps ${focusMetrics.appSwitchCount} times today. This hurts your productivity.",
                        priority = Priority.MEDIUM,
                        manualSteps = listOf(
                            "Batch similar tasks together",
                            "Turn off non-essential notifications",
                            "Use focus mode or app blockers"
                        ),
                        autoActionAvailable = false,
                        timestamp = now
                    )
                )
            }
        }
        
        // Drift time suggestions
        val totalMinutes = investedMinutes + driftMinutes
        if (totalMinutes > 0) {
            val driftRatio = driftMinutes.toDouble() / totalMinutes
            
            // High: More drift than invested
            if (driftRatio > 0.6 && driftMinutes > 60) {
                suggestions.add(
                    Suggestion(
                        id = UUID.randomUUID().toString(),
                        type = SuggestionType.REDUCE_DRIFT_TIME,
                        title = "Too Much Drift Time",
                        description = "You've spent ${driftMinutes} minutes on drift apps today. That's ${(driftRatio * 100).toInt()}% of your time.",
                        priority = Priority.HIGH,
                        manualSteps = listOf(
                            "Set daily limits for social media apps",
                            "Replace scrolling with productive activities",
                            "Use grayscale mode to reduce phone appeal",
                            "Schedule specific times for leisure apps"
                        ),
                        autoActionAvailable = false,
                        timestamp = now
                    )
                )
            }
        }
        
        // Low priority: Enable grayscale (if drift is moderate)
        if (driftMinutes > 30 && driftMinutes < 120) {
            suggestions.add(
                Suggestion(
                    id = UUID.randomUUID().toString(),
                    type = SuggestionType.ENABLE_GRAYSCALE,
                    title = "Try Grayscale Mode",
                    description = "Grayscale mode can reduce phone addiction by making apps less appealing.",
                    priority = Priority.LOW,
                    manualSteps = listOf(
                        "Go to Settings → Digital Wellbeing",
                        "Tap Bedtime mode",
                        "Enable Grayscale",
                        "Or use Quick Settings to toggle"
                    ),
                    autoActionAvailable = false,
                    timestamp = now
                )
            )
        }
        
        // Sort by priority (URGENT > HIGH > MEDIUM > LOW)
        return suggestions.sortedByDescending { it.priority.ordinal }
    }
}
