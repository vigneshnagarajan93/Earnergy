package com.earnergy.domain.calculation

import com.earnergy.domain.model.AppSwitchEvent
import com.earnergy.domain.model.AppUsage
import com.earnergy.domain.model.FocusMetrics
import kotlin.math.max

object FocusCalculator {
    /**
     * Threshold for compulsive unlock detection.
     * If an unlock occurs within this duration of the previous lock, it's considered compulsive.
     */
    private const val COMPULSIVE_UNLOCK_THRESHOLD_MS = 3 * 60 * 1000L // 3 minutes

    fun computeFocusMetrics(
        usages: List<AppUsage>,
        appSwitchEvents: List<AppSwitchEvent>,
        unlockEvents: List<com.earnergy.domain.model.UnlockEvent> = emptyList(),
        notificationEvents: List<com.earnergy.domain.model.NotificationEvent> = emptyList(),
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
        
        // Calculate unlock metrics
        val unlocksOnly = unlockEvents.filter { !it.isLockEvent }
        val unlockCount = unlocksOnly.size

        var compulsiveUnlockCount = 0
        var lastLockTimestamp: Long? = null

        // unlockEvents are sorted by timestamp ASC
        for (event in unlockEvents) {
            if (event.isLockEvent) {
                lastLockTimestamp = event.timestamp
            } else {
                // It's an unlock
                if (lastLockTimestamp != null) {
                    val timeSinceLock = event.timestamp - lastLockTimestamp
                    if (timeSinceLock < COMPULSIVE_UNLOCK_THRESHOLD_MS) {
                        compulsiveUnlockCount++
                    }
                }
            }
        }

        // Drift notification count
        val driftPackageNames = usages
            .filter { it.role == com.earnergy.domain.model.AppRole.DRIFT }
            .map { it.packageName }
            .toSet()

        val driftNotificationCount = notificationEvents
            .count { it.packageName in driftPackageNames }

        val totalNotificationCount = notificationEvents.size

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
            peakProductivityHour = peakProductivityHour,
            unlockCount = unlockCount,
            compulsiveUnlockCount = compulsiveUnlockCount,
            driftNotificationCount = driftNotificationCount,
            totalNotificationCount = totalNotificationCount
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
