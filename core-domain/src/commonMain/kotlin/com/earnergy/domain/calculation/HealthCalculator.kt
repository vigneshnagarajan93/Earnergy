package com.earnergy.domain.calculation

import com.earnergy.domain.model.AppUsage
import com.earnergy.domain.model.BreakEvent
import com.earnergy.domain.model.HealthMetrics
import com.earnergy.domain.model.EyeHealthStatus
import com.earnergy.domain.model.UnlockEvent
import kotlin.math.max
import kotlin.math.min

object HealthCalculator {
    
    private const val BREAK_INTERVAL_MINUTES = 20 // 20-20-20 rule
    private const val EVENING_HOUR_START = 20 // 8 PM
    private const val NIGHT_HOUR_START = 22 // 10 PM
    private const val SESSION_RESET_THRESHOLD_MS = 60 * 1000L // 60 seconds
    
    /**
     * Compute health metrics for a given day.
     */
    fun computeHealthMetrics(
        usages: List<AppUsage>,
        breakEvents: List<BreakEvent>,
        unlockEvents: List<UnlockEvent>,
        dateEpochDay: Long,
        currentTimeMillis: Long
    ): HealthMetrics {
        val totalScreenTimeMinutes = usages.sumOf { it.totalForeground.inWholeMinutes }.toInt()
        
        val sessions = reconstructSessions(unlockEvents, currentTimeMillis)
        val maxContinuousSessionMinutes = if (sessions.isEmpty()) 0 else (sessions.maxOf { it.getDuration(currentTimeMillis) } / 60000).toInt()
        val currentSessionMinutes = if (sessions.isEmpty()) 0 else {
            val lastSession = sessions.last()
            if (lastSession.isActive(currentTimeMillis)) {
                (lastSession.getDuration(currentTimeMillis) / 60000).toInt()
            } else 0
        }

        val dailyStrainMinutes = sessions.sumOf { session ->
            val durationMinutes = (session.getDuration(currentTimeMillis) / 60000).toInt()
            max(0, durationMinutes - 20)
        }

        val status = determineStatus(maxContinuousSessionMinutes, totalScreenTimeMinutes)

        // Legacy/Compatibility fields
        val breaksRecommended = (totalScreenTimeMinutes / BREAK_INTERVAL_MINUTES).coerceAtLeast(1)
        val breaksTaken = breakEvents.size
        val breakComplianceRate = if (breaksRecommended > 0) {
            (breaksTaken.toDouble() / breaksRecommended).coerceIn(0.0, 1.0)
        } else 0.0
        val lastBreakEvent = breakEvents.maxByOrNull { it.timestamp }
        
        // eyeStrainScore is kept for compatibility but we can derive it from status or keep a simple version
        val eyeStrainScore = when (status) {
            EyeHealthStatus.EXCELLENT -> 10.0
            EyeHealthStatus.GOOD -> 35.0
            EyeHealthStatus.FAIR -> 65.0
            EyeHealthStatus.POOR -> 90.0
        }

        return HealthMetrics(
            eyeStrainScore = eyeStrainScore,
            status = status,
            dailyStrainMinutes = dailyStrainMinutes,
            maxContinuousSessionMinutes = maxContinuousSessionMinutes,
            currentSessionMinutes = currentSessionMinutes,
            totalScreenTimeMinutes = totalScreenTimeMinutes,
            continuousScreenTimeMinutes = currentSessionMinutes, // Refined mapping
            breaksRecommended = breaksRecommended,
            breaksTaken = breaksTaken,
            breakComplianceRate = breakComplianceRate,
            lastBreakTimestamp = lastBreakEvent?.timestamp
        )
    }

    private data class Session(val startTime: Long, var endTime: Long?) {
        val durationMs: Long get() = (endTime ?: startTime) - startTime
        fun getDuration(now: Long): Long = (endTime ?: now) - startTime
        fun isActive(now: Long): Boolean = endTime == null || (now - endTime!!) < SESSION_RESET_THRESHOLD_MS
    }

    private fun reconstructSessions(unlockEvents: List<UnlockEvent>, currentTimeMillis: Long): List<Session> {
        if (unlockEvents.isEmpty()) return emptyList()
        
        val sortedEvents = unlockEvents.sortedBy { it.timestamp }
        val sessions = mutableListOf<Session>()
        var currentSession: Session? = null

        for (event in sortedEvents) {
            if (!event.isLockEvent) { // Unlock
                if (currentSession == null) {
                    currentSession = Session(event.timestamp, null)
                } else {
                    val gap = event.timestamp - (currentSession.endTime ?: event.timestamp)
                    if (gap >= SESSION_RESET_THRESHOLD_MS) {
                        sessions.add(currentSession)
                        currentSession = Session(event.timestamp, null)
                    } else {
                        // Resume session (timer resumes from previous value is effectively continuing the session)
                        currentSession.endTime = null
                    }
                }
            } else { // Lock
                currentSession?.endTime = event.timestamp
            }
        }

        currentSession?.let { sessions.add(it) }
        return sessions
    }

    private fun determineStatus(maxContinuousMinutes: Int, totalDailyMinutes: Int): EyeHealthStatus {
        val continuousRating = when {
            maxContinuousMinutes <= 20 -> EyeHealthStatus.EXCELLENT
            maxContinuousMinutes <= 40 -> EyeHealthStatus.GOOD
            maxContinuousMinutes <= 60 -> EyeHealthStatus.FAIR
            else -> EyeHealthStatus.POOR
        }

        val totalUsageRating = when {
            totalDailyMinutes <= 120 -> EyeHealthStatus.EXCELLENT // <= 2 hours
            totalDailyMinutes <= 180 -> EyeHealthStatus.GOOD      // 2.1 - 3 hours
            totalDailyMinutes <= 240 -> EyeHealthStatus.FAIR      // 3.1 - 4 hours
            else -> EyeHealthStatus.POOR                         // > 4 hours
        }

        // Lower-bound wins (stricter rating)
        return if (continuousRating.ordinal > totalUsageRating.ordinal) continuousRating else totalUsageRating
    }
    
    /**
     * Get hour of day (0-23) from timestamp.
     */
    private fun getHourOfDay(timestampMillis: Long): Int {
        // Get hour from timestamp (simplified - assumes local time)
        val hourMillis = timestampMillis % (24 * 60 * 60 * 1000)
        return (hourMillis / (60 * 60 * 1000)).toInt()
    }
}
