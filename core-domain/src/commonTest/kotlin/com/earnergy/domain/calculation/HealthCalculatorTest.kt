package com.earnergy.domain.calculation

import com.earnergy.domain.model.AppUsage
import com.earnergy.domain.model.EyeHealthStatus
import com.earnergy.domain.model.UnlockEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class HealthCalculatorTest {

    @Test
    fun testDetermineStatus_Excellent() {
        // Continuous <= 20 and Total <= 120
        val metrics = HealthCalculator.computeHealthMetrics(
            usages = listOf(createUsage(100)),
            breakEvents = emptyList(),
            unlockEvents = listOf(
                UnlockEvent(1000, 0, false),
                UnlockEvent(1000 + 15 * 60000L, 0, true)
            ),
            dateEpochDay = 0,
            currentTimeMillis = 20 * 60000L
        )
        assertEquals(EyeHealthStatus.EXCELLENT, metrics.status)
        assertEquals(15, metrics.maxContinuousSessionMinutes)
        assertEquals(0, metrics.dailyStrainMinutes)
    }

    @Test
    fun testDetermineStatus_PoorByContinuous() {
        // Continuous > 60, Total <= 120 (Excellent) -> Poor wins
        val metrics = HealthCalculator.computeHealthMetrics(
            usages = listOf(createUsage(100)),
            breakEvents = emptyList(),
            unlockEvents = listOf(
                UnlockEvent(1000, 0, false),
                UnlockEvent(1000 + 65 * 60000L, 0, true)
            ),
            dateEpochDay = 0,
            currentTimeMillis = 70 * 60000L
        )
        assertEquals(EyeHealthStatus.POOR, metrics.status)
        assertEquals(65, metrics.maxContinuousSessionMinutes)
        assertEquals(45, metrics.dailyStrainMinutes) // 65 - 20 = 45
    }

    @Test
    fun test60SecondResetRule() {
        // Unlock -> Lock -> Unlock within 30s should count as one session
        val metrics = HealthCalculator.computeHealthMetrics(
            usages = listOf(createUsage(30)),
            breakEvents = emptyList(),
            unlockEvents = listOf(
                UnlockEvent(1000, 0, false),
                UnlockEvent(1000 + 10 * 60000L, 0, true), // Lock after 10m
                UnlockEvent(1000 + 10 * 60000L + 30000L, 0, false), // Unlock after 30s
                UnlockEvent(1000 + 25 * 60000L, 0, true) // Lock after another 15m (Total 25m)
            ),
            dateEpochDay = 0,
            currentTimeMillis = 30 * 60000L
        )
        // One continuous session of ~25 minutes -> GOOD (21-40)
        assertEquals(25, metrics.maxContinuousSessionMinutes)
        assertEquals(EyeHealthStatus.GOOD, metrics.status)
        assertEquals(5, metrics.dailyStrainMinutes) // 25 - 20 = 5
    }

    @Test
    fun testMoreThan60SecondReset() {
        // Unlock -> Lock -> Unlock after 90s should count as two sessions
        val metrics = HealthCalculator.computeHealthMetrics(
            usages = listOf(createUsage(30)),
            breakEvents = emptyList(),
            unlockEvents = listOf(
                UnlockEvent(1000, 0, false),
                UnlockEvent(1000 + 10 * 60000L, 0, true), // Session 1: 10m
                UnlockEvent(1000 + 10 * 60000L + 90000L, 0, false), // Gap 90s. Starts at 691000
                UnlockEvent(691000 + 15 * 60000L, 0, true) // Session 2: 15m. Ends at 1591000
            ),
            dateEpochDay = 0,
            currentTimeMillis = 30 * 60000L
        )
        // Max session is 15 minutes -> EXCELLENT
        assertEquals(15, metrics.maxContinuousSessionMinutes)
        assertEquals(EyeHealthStatus.EXCELLENT, metrics.status)
        assertEquals(0, metrics.dailyStrainMinutes)
    }

    private fun createUsage(totalMinutes: Int) = AppUsage(
        "pkg", "Name", com.earnergy.domain.model.AppCategory.OTHER, totalMinutes.minutes, com.earnergy.domain.model.AppRole.IGNORED
    )
}
