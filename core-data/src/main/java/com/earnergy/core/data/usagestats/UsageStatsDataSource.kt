package com.earnergy.core.data.usagestats

import android.app.usage.UsageStatsManager
import android.content.pm.PackageManager
import com.earnergy.domain.model.AppCategory
import android.app.usage.UsageEvents
import com.earnergy.domain.model.AppUsage
import com.earnergy.core.data.local.BreakEventEntity
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

data class UsageResult(
    val usages: List<AppUsage>,
    val automaticBreaks: List<BreakEventEntity>,
    val unlockEvents: List<com.earnergy.core.data.local.UnlockEventEntity> = emptyList()
)

@Singleton
class UsageStatsDataSource @Inject constructor(
    private val usageStatsManager: UsageStatsManager,
    private val packageManager: PackageManager,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    fun queryUsageForDay(epochDay: Long): UsageResult {
        val zone = clock.zone
        val date = LocalDate.ofEpochDay(epochDay)
        val startInstant = date.atStartOfDay(zone).toInstant()
        val endInstant = minOf(Instant.now(clock), startInstant.plusSeconds(SECONDS_PER_DAY))

        val startTime = startInstant.toEpochMilli()
        val endTime = endInstant.toEpochMilli()

        val events = usageStatsManager.queryEvents(startTime, endTime)

        val appUsageTimes = mutableMapOf<String, Long>()
        var lastResumedPackage: String? = null
        var lastResumedTime: Long? = null

        val automaticBreaks = mutableListOf<BreakEventEntity>()
        var lastPauseTime: Long = startTime

        val unlockEvents = mutableListOf<com.earnergy.core.data.local.UnlockEventEntity>()
        var lastNotificationTime: Long = 0
        var lastNotificationPackage: String? = null

        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            val timestamp = event.timeStamp
            val packageName = event.packageName

            when (event.eventType) {
                28 -> { // UsageEvents.Event.KEYGUARD_HIDDEN (API 28)
                    // Device unlocked. Check if it was preceded by a notification.
                    val wasNotificationLed = (timestamp - lastNotificationTime) < 5000 // 5 seconds threshold
                    unlockEvents.add(
                        com.earnergy.core.data.local.UnlockEventEntity(
                            timestamp = timestamp,
                            dateEpochDay = epochDay,
                            wasNotificationLed = wasNotificationLed,
                            triggeringPackage = if (wasNotificationLed) lastNotificationPackage else null
                        )
                    )
                }
                12 -> { // UsageEvents.Event.NOTIFICATION_INTERRUPTION (API 12/28)
                    lastNotificationTime = timestamp
                    lastNotificationPackage = packageName
                }
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    // Calculate if there was a break before this resume
                    val timeSinceLastPause = timestamp - lastPauseTime
                    if (timeSinceLastPause >= 30 * 60 * 1000L) { // 30+ minutes
                        automaticBreaks.add(
                            BreakEventEntity(
                                timestamp = lastPauseTime,
                                dateEpochDay = epochDay,
                                durationSeconds = (timeSinceLastPause / 1000L).toInt(),
                                wasManual = false
                            )
                        )
                    }

                    if (lastResumedPackage != null && lastResumedTime != null) {
                        val duration = timestamp - lastResumedTime!!
                        if (duration > 0) {
                            appUsageTimes[lastResumedPackage!!] = (appUsageTimes[lastResumedPackage!!] ?: 0L) + duration
                        }
                    }
                    lastResumedPackage = packageName
                    lastResumedTime = timestamp
                }
                UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> {
                    if (packageName == lastResumedPackage && lastResumedTime != null) {
                        val duration = timestamp - lastResumedTime!!
                        if (duration > 0) {
                            appUsageTimes[lastResumedPackage!!] = (appUsageTimes[lastResumedPackage!!] ?: 0L) + duration
                        }
                        lastResumedPackage = null
                        lastResumedTime = null
                    }
                    lastPauseTime = timestamp
                }
            }
        }

        // Handle case where an app is still running at the end of the queried period
        if (lastResumedPackage != null && lastResumedTime != null) {
            val duration = endTime - lastResumedTime!!
            if (duration > 0) {
                appUsageTimes[lastResumedPackage!!] = (appUsageTimes[lastResumedPackage!!] ?: 0L) + duration
            }
        } else {
            // Check for break up to current end time if no app is resumed
            val timeSinceLastPause = endTime - lastPauseTime
            if (timeSinceLastPause >= 30 * 60 * 1000L) {
                automaticBreaks.add(
                    BreakEventEntity(
                        timestamp = lastPauseTime,
                        dateEpochDay = epochDay,
                        durationSeconds = (timeSinceLastPause / 1000L).toInt(),
                        wasManual = false
                    )
                )
            }
        }

        val appUsages = appUsageTimes.mapNotNull { (packageName, totalMillis) ->
            if (totalMillis <= 0) return@mapNotNull null

            val totalSeconds = totalMillis / 1000L
            val (displayName, isSystem) = resolveLabelAndSystemStatus(packageName)
            val category = guessCategory(packageName, displayName)

            AppUsage(
                packageName = packageName,
                displayName = displayName,
                category = category,
                totalForeground = totalSeconds.seconds,
                isSystemApp = isSystem
            )
        }.sortedByDescending { it.totalForeground }

        return UsageResult(
            usages = appUsages,
            automaticBreaks = automaticBreaks,
            unlockEvents = unlockEvents
        )
    }

    private fun resolveLabelAndSystemStatus(packageName: String): Pair<String, Boolean> {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val label = packageManager.getApplicationLabel(applicationInfo)?.toString() ?: packageName
            val isSystem = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 ||
                    (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            label to isSystem
        } catch (_: PackageManager.NameNotFoundException) {
            packageName to false
        }
    }

    private fun guessCategory(packageName: String, displayName: String): AppCategory {
        val normalized = "$packageName ${displayName.lowercase()}"
        return when {
            normalized.contains("youtube") || normalized.contains("netflix") || normalized.contains("prime video") -> AppCategory.ENTERTAINMENT
            normalized.contains("instagram") || normalized.contains("facebook") || normalized.contains("twitter") || normalized.contains("x ") || normalized.contains("tiktok") -> AppCategory.SOCIAL
            normalized.contains("docs") || normalized.contains("slides") || normalized.contains("sheets") || normalized.contains("notion") || normalized.contains("todo") || normalized.contains("calendar") -> AppCategory.PRODUCTIVE
            else -> AppCategory.OTHER
        }
    }

    companion object {
        private const val SECONDS_PER_DAY = 24 * 60 * 60L
    }
}
