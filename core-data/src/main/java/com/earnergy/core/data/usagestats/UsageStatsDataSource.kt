package com.earnergy.core.data.usagestats

import android.app.usage.UsageStatsManager
import android.content.pm.PackageManager
import com.earnergy.domain.model.AppCategory
import com.earnergy.domain.model.AppUsage
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class UsageStatsDataSource @Inject constructor(
    private val usageStatsManager: UsageStatsManager,
    private val packageManager: PackageManager,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    fun queryUsageForDay(epochDay: Long): List<AppUsage> {
        val zone = clock.zone
        val date = LocalDate.ofEpochDay(epochDay)
        val startInstant = date.atStartOfDay(zone).toInstant()
        val endInstant = minOf(Instant.now(clock), startInstant.plusSeconds(SECONDS_PER_DAY))

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startInstant.toEpochMilli(),
            endInstant.toEpochMilli()
        ) ?: emptyList()

        return stats
            .filter { it.totalTimeInForeground > 0 }
            .groupBy { it.packageName }
            .map { (packageName, entries) ->
                val totalSeconds = entries.sumOf { (it.totalTimeInForeground / 1000L).coerceAtLeast(0) }
                val (displayName, isSystem) = resolveLabelAndSystemStatus(packageName)
                val category = guessCategory(packageName, displayName)
                AppUsage(
                    packageName = packageName,
                    displayName = displayName,
                    category = category,
                    totalForeground = totalSeconds.seconds,
                    isSystemApp = isSystem
                )
            }
            .sortedByDescending { it.totalForeground }
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
