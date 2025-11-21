package com.earnergy.domain.calculation

import com.earnergy.domain.model.AppSwitchEvent
import com.earnergy.domain.model.AppUsage
import com.earnergy.domain.model.FocusMetrics
import kotlin.math.max

object FocusCalculator {
    fun computeFocusMetrics(
        usages: List<AppUsage>,
        appSwitchEvents: List<AppSwitchEvent>,
        dateEpochDay: Long
    ): FocusMetrics {
        val totalMinutes = usages.sumOf { it.totalForeground.inWholeMinutes }
        
        if (totalMinutes == 0L) {
            return FocusMetrics(
                focusScore = 0.0,
                appSwitchCount = 0,
                longestFocusSessionMinutes = 0,
                averageFocusSessionMinutes = 0.0,
                distractionIndex = 0.0
            )
        }
        
        // Calculate app switch count
        val switchCount = appSwitchEvents.size
        
        // Calculate distraction index
        // Ideal: <1 switch per 10 minutes
        val idealSwitches = totalMinutes / 10.0
        val distractionIndex = if (idealSwitches > 0) {
            (switchCount / idealSwitches).coerceIn(0.0, 1.0)
        } else {
            0.0
        }
        
        // Calculate session lengths
        val sessions = calculateSessions(appSwitchEvents, usages)
        val longestSession = sessions.maxOfOrNull { it.durationMinutes } ?: 0
        val avgSession = if (sessions.isNotEmpty()) {
            sessions.map { it.durationMinutes }.average()
        } else 0.0
        
        // Identify deep work sessions (25+ minutes)
        val deepWorkSessions = sessions.filter { it.durationMinutes >= 25 }
        val deepWorkSessionCount = deepWorkSessions.size
        val totalDeepWorkMinutes = deepWorkSessions.sumOf { it.durationMinutes }
        
        // Calculate peak productivity hour
        val peakProductivityHour = calculatePeakProductivityHour(appSwitchEvents)
        
        // Calculate focus score
        // Base score from distraction index
        val baseScore = (1.0 - distractionIndex) * 100.0
        
        // Bonus for longer sessions (up to +20 points)
        val sessionBonus = (avgSession / 30.0).coerceIn(0.0, 1.0) * 20.0
        
        val focusScore = (baseScore + sessionBonus).coerceIn(0.0, 100.0)
        
        return FocusMetrics(
            focusScore = focusScore,
            appSwitchCount = switchCount,
            longestFocusSessionMinutes = longestSession,
            averageFocusSessionMinutes = avgSession,
            distractionIndex = distractionIndex,
            deepWorkSessionCount = deepWorkSessionCount,
            totalDeepWorkMinutes = totalDeepWorkMinutes,
            peakProductivityHour = peakProductivityHour
        )
    }
    
    private data class Session(
        val packageName: String,
        val startTime: Long,
        val endTime: Long,
        val durationMinutes: Int
    )
    
    private fun calculateSessions(
        switches: List<AppSwitchEvent>,
        usages: List<AppUsage>
    ): List<Session> {
        // Group consecutive time in same app as a session
        val sessions = mutableListOf<Session>()
        
        if (switches.isEmpty()) return emptyList()

        for (i in switches.indices) {
            val current = switches[i]
            val next = switches.getOrNull(i + 1)
            
            val durationMillis = if (next != null) {
                next.timestamp - current.timestamp
            } else {
                // Last session - estimate based on total usage or just default to 0 if unknown
                // For simplicity and safety, we'll cap it or use a default if we can't determine end
                // A better approach might be to look at the total usage for that app
                // But here we are just looking at switch intervals
                0L 
            }
            
            val durationMinutes = (durationMillis / 60000).toInt()

            if (durationMinutes > 0) {
                sessions.add(
                    Session(
                        packageName = current.toPackage,
                        startTime = current.timestamp,
                        endTime = current.timestamp + durationMillis,
                        durationMinutes = durationMinutes
                    )
                )
            }
        }
        
        return sessions
    }
    
    /**
     * Calculate the hour of day (0-23) with the best focus (fewest app switches).
     * Returns null if there's insufficient data.
     */
    private fun calculatePeakProductivityHour(switches: List<AppSwitchEvent>): Int? {
        if (switches.size < 5) return null // Need at least 5 switches for meaningful analysis
        
        // Group switches by hour of day
        val switchesByHour = switches.groupBy { event ->
            // Convert timestamp to hour of day (0-23)
            val hourMillis = event.timestamp % (24 * 60 * 60 * 1000)
            (hourMillis / (60 * 60 * 1000)).toInt()
        }
        
        // Find hour with fewest switches (best focus)
        // Only consider hours with at least one switch
        val peakHour = switchesByHour
            .filter { it.value.isNotEmpty() }
            .minByOrNull { it.value.size }
            ?.key
        
        return peakHour
    }
}
