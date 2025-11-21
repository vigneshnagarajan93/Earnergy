package com.earnergy.core.data.repository

import com.earnergy.core.data.local.AppUsageDao
import com.earnergy.core.data.local.AppUsageEntity
import com.earnergy.core.data.settings.SettingsDataStore
import com.earnergy.core.data.usagestats.UsageStatsDataSource
import com.earnergy.domain.model.AppUsage
import com.earnergy.domain.model.DaySummary
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

import com.earnergy.core.data.local.AppConfigDao
import com.earnergy.core.data.local.AppConfigEntity
import com.earnergy.domain.model.AppRole

@Singleton
class UsageRepository @Inject constructor(
    private val usageStatsDataSource: UsageStatsDataSource,
    private val appUsageDao: AppUsageDao,
    private val appConfigDao: AppConfigDao,
    private val settingsDataStore: SettingsDataStore,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    suspend fun refreshToday() {
        val todayEpochDay = LocalDate.now(clock).toEpochDay()
        val usages = usageStatsDataSource.queryUsageForDay(todayEpochDay)
        val entities = usages.map { usage ->
            AppUsageEntity(
                dateEpochDay = todayEpochDay,
                packageName = usage.packageName,
                displayName = usage.displayName,
                category = usage.category,
                totalSeconds = usage.totalForeground.inWholeSeconds,
                isSystemApp = usage.isSystemApp
            )
        }
        appUsageDao.replaceForDay(todayEpochDay, entities)
    }

    fun observeDaySummary(epochDay: Long): Flow<DaySummary> {
        return combine(
            appUsageDao.observeForDay(epochDay),
            appConfigDao.observeAll(),
            settingsDataStore.hourlyRate
        ) { usageEntities, configEntities, hourlyRate ->
            val configMap = configEntities.associate { it.packageName to it.role }
            DaySummary(
                dateEpochDay = epochDay,
                usages = usageEntities.map { it.toDomain(configMap[it.packageName]) },
                hourlyRate = hourlyRate,
                energyStart = null,
                energyEnd = null
            )
        }.distinctUntilChanged()
    }

    suspend fun loadDay(epochDay: Long): DaySummary? {
        val entities = appUsageDao.getForDay(epochDay)
        if (entities.isEmpty()) return null
        val hourlyRate = settingsDataStore.hourlyRate.first()
        
        val allConfigs = appConfigDao.observeAll().first()
        val configMap = allConfigs.associate { it.packageName to it.role }

        return DaySummary(
            dateEpochDay = epochDay,
            usages = entities.map { it.toDomain(configMap[it.packageName]) },
            hourlyRate = hourlyRate,
            energyStart = null,
            energyEnd = null
        )
    }

    suspend fun updateAppRole(packageName: String, role: AppRole) {
        appConfigDao.insert(AppConfigEntity(packageName, role))
    }

    private fun AppUsageEntity.toDomain(configRole: AppRole?): AppUsage {
        val effectiveRole = configRole ?: when {
            isSystemApp -> AppRole.IGNORED
            category == com.earnergy.domain.model.AppCategory.PRODUCTIVE -> AppRole.INVESTED
            category == com.earnergy.domain.model.AppCategory.SOCIAL || 
            category == com.earnergy.domain.model.AppCategory.ENTERTAINMENT -> AppRole.DRIFT
            else -> AppRole.IGNORED
        }
        
        return AppUsage(
            packageName = packageName,
            displayName = displayName,
            category = category,
            totalForeground = totalSeconds.seconds,
            role = effectiveRole,
            isSystemApp = isSystemApp
        )
    }
}
