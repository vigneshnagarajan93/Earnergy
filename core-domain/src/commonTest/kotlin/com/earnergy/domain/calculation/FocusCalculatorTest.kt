package com.earnergy.domain.calculation

import com.earnergy.domain.model.AppRole
import com.earnergy.domain.model.AppUsage
import com.earnergy.domain.model.UnlockEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class FocusCalculatorTest {

    @Test
    fun testCompulsiveUnlockCount() {
        val usages = listOf(
            AppUsage("pkg1", "App 1", com.earnergy.domain.model.AppCategory.PRODUCTIVE, 10.minutes, AppRole.INVESTED)
        )

        val now = 1000000L
        val unlockEvents = listOf(
            UnlockEvent(now, 0, false),           // 1. Initial unlock
            UnlockEvent(now + 1000, 0, true),      // 2. Lock after 1s
            UnlockEvent(now + 30000, 0, false),    // 3. Unlock after 30s (Compulsive flow: 1 to 3 is 30s < 60s)
            UnlockEvent(now + 40000, 0, true),     // 4. Lock
            UnlockEvent(now + 100000, 0, false)    // 5. Unlock (Flow: 3 to 5 is 70s > 60s, Not compulsive)
        )

        val metrics = FocusCalculator.computeFocusMetrics(
            usages = usages,
            appSwitchEvents = emptyList(),
            unlockEvents = unlockEvents,
            notificationEvents = emptyList(),
            dateEpochDay = 0
        )

        assertEquals(3, metrics.unlockCount)
        assertEquals(1, metrics.compulsiveUnlockCount)
    }

    @Test
    fun testTotalNotificationCount() {
        val usages = listOf(
            AppUsage("pkg1", "App 1", com.earnergy.domain.model.AppCategory.PRODUCTIVE, 10.minutes, AppRole.INVESTED)
        )
        val notificationEvents = listOf(
            com.earnergy.domain.model.NotificationEvent(100L, "pkg1", 0),
            com.earnergy.domain.model.NotificationEvent(200L, "pkg2", 0)
        )

        val metrics = FocusCalculator.computeFocusMetrics(
            usages = usages,
            appSwitchEvents = emptyList(),
            unlockEvents = emptyList(),
            notificationEvents = notificationEvents,
            dateEpochDay = 0
        )

        assertEquals(2, metrics.totalNotificationCount)
        assertEquals(1, metrics.notificationCounts["pkg1"])
        assertEquals(1, metrics.notificationCounts["pkg2"])
    }
}
