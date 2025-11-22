package com.earnergy.domain.calculation

import com.earnergy.domain.model.AppUsage
import com.earnergy.domain.model.BreakEvent
import com.earnergy.domain.model.HealthMetrics
import kotlin.math.min

object HealthCalculator {
    
    private const val BREAK_INTERVAL_MINUTES = 20 // 20-20-20 rule
    private const val EVENING_HOUR_START = 20 // 8 PM
    private const val NIGHT_HOUR_START = 22 // 10 PM
    
    /**
     * Compute health metrics for a given day.
     */
    fun computeHealthMetrics(
        usages: List<AppUsage>,
        breakEvents: List<BreakEvent>,
        dateEpochDay: Long,
        currentTimeMillis: Long
    ): HealthMetrics {
        val totalScreenTimeMinutes = usages.sumOf { it.totalForeground.inWholeMinutes }.toInt()
        
        if (totalScreenTimeMinutes == 0) {
            return HealthMetrics(
                eyeStrainScore = 0.0,
                totalScreenTimeMinutes = 0,
                continuousScreenTimeMinutes = 0,
                breaksRecommended = 0,
                breaksTaken = 0,
                breakComplianceRate = 0.0,
                lastBreakTimestamp = null
            )
        }
        
        // Calculate breaks recommended (one per 20 minutes)
        val breaksRecommended = (totalScreenTimeMinutes / BREAK_INTERVAL_MINUTES).coerceAtLeast(1)
        val breaksTaken = breakEvents.size
        
        // Calculate break compliance rate
        val breakComplianceRate = if (breaksRecommended > 0) {
            (breaksTaken.toDouble() / breaksRecommended).coerceIn(0.0, 1.0)
        } else 0.0
        
        // Calculate continuous screen time since last break
        val lastBreakTimestamp = breakEvents.maxOfOrNull { it.timestamp }
        val continuousScreenTimeMinutes = if (lastBreakTimestamp != null) {
            ((currentTimeMillis - lastBreakTimestamp) / 60000).toInt()
        } else {
            totalScreenTimeMinutes
        }
        
        // Calculate eye strain score
        val eyeStrainScore = calculateEyeStrainScore(
            totalScreenTimeMinutes = totalScreenTimeMinutes,
            continuousScreenTimeMinutes = continuousScreenTimeMinutes,
            breakComplianceRate = breakComplianceRate,
            currentTimeMillis = currentTimeMillis
        )
        
        return HealthMetrics(
            eyeStrainScore = eyeStrainScore,
            totalScreenTimeMinutes = totalScreenTimeMinutes,
            continuousScreenTimeMinutes = continuousScreenTimeMinutes,
            breaksRecommended = breaksRecommended,
            breaksTaken = breaksTaken,
            breakComplianceRate = breakComplianceRate,
            lastBreakTimestamp = lastBreakTimestamp
        )
    }
    
    /**
     * Calculate eye strain score (0-100).
     * Higher score = more strain.
     */
    private fun calculateEyeStrainScore(
        totalScreenTimeMinutes: Int,
        continuousScreenTimeMinutes: Int,
        breakComplianceRate: Double,
        currentTimeMillis: Long
    ): Double {
        // Base score from total screen time (0-40 points)
        // 0 min = 0, 120 min = 20, 240+ min = 40
        val baseScore = min(totalScreenTimeMinutes / 6.0, 40.0)
        
        // Penalty for continuous screen time without breaks (0-30 points)
        // 0 min = 0, 20 min = 10, 40 min = 20, 60+ min = 30
        val continuousTimePenalty = min(continuousScreenTimeMinutes / 2.0, 30.0)
        
        // Penalty for poor break compliance (0-20 points)
        // 100% compliance = 0, 50% = 10, 0% = 20
        val breakCompliancePenalty = (1.0 - breakComplianceRate) * 20.0
        
        // Time of day multiplier
        val hourOfDay = getHourOfDay(currentTimeMillis)
        val timeMultiplier = when {
            hourOfDay >= NIGHT_HOUR_START -> 1.3 // Night: 30% increase
            hourOfDay >= EVENING_HOUR_START -> 1.15 // Evening: 15% increase
            else -> 1.0 // Day: no increase
        }
        
        val rawScore = (baseScore + continuousTimePenalty + breakCompliancePenalty) * timeMultiplier
        
        return rawScore.coerceIn(0.0, 100.0)
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
