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
import com.earnergy.core.data.local.AppSwitchEventDao
import com.earnergy.core.data.local.BreakEventDao
import com.earnergy.core.data.local.BreakEventEntity
import com.earnergy.core.data.local.SuggestionDao
import com.earnergy.core.data.local.SuggestionEntity
import com.earnergy.domain.model.AppRole
import com.earnergy.domain.model.FocusTrend
import com.earnergy.domain.model.AppSwitchEvent
import com.earnergy.domain.model.BreakEvent
import com.earnergy.domain.model.HealthMetrics
import com.earnergy.domain.calculation.FocusCalculator
import com.earnergy.domain.calculation.HealthCalculator
import kotlinx.coroutines.flow.map

@Singleton
class UsageRepository @Inject constructor(
    private val usageStatsDataSource: UsageStatsDataSource,
    private val appUsageDao: AppUsageDao,
    private val appConfigDao: AppConfigDao,
    private val appSwitchEventDao: AppSwitchEventDao,
    private val breakEventDao: BreakEventDao,
    private val unlockEventDao: com.earnergy.core.data.local.UnlockEventDao,
    private val suggestionDao: SuggestionDao,
    private val settingsDataStore: SettingsDataStore,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    suspend fun refreshToday() {
        val todayEpochDay = LocalDate.now(clock).toEpochDay()
        val usageResult = usageStatsDataSource.queryUsageForDay(todayEpochDay)
        val entities = usageResult.usages.map { usage ->
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

        breakEventDao.deleteAutomaticBreaksForDay(todayEpochDay)
        breakEventDao.insertAll(usageResult.automaticBreaks)

        unlockEventDao.deleteForDay(todayEpochDay)
        unlockEventDao.insertAll(usageResult.unlockEvents)
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

    /**
     * Observe all apps (installed and those with usage) for a specific day.
     */
    fun observeAllApps(epochDay: Long): Flow<List<AppUsage>> {
        val installedApps = usageStatsDataSource.getInstalledApps()
        val installedMap = installedApps.associateBy { it.packageName }

        return combine(
            appUsageDao.observeForDay(epochDay),
            appConfigDao.observeAll()
        ) { usageEntities, configEntities ->
            val usageMap = usageEntities.associateBy { it.packageName }
            val configMap = configEntities.associate { it.packageName to it.role }

            val allPackages = (installedMap.keys + usageMap.keys).distinct()

            allPackages.map { packageName ->
                val installed = installedMap[packageName]
                val usage = usageMap[packageName]
                val configRole = configMap[packageName]

                val displayName = usage?.displayName ?: installed?.displayName ?: packageName
                val category = usage?.category ?: installed?.category ?: com.earnergy.domain.model.AppCategory.OTHER
                val isSystemApp = usage?.isSystemApp ?: installed?.isSystemApp ?: false
                val totalSeconds = usage?.totalSeconds ?: 0L

                val role = resolveRole(isSystemApp, category, configRole)

                AppUsage(
                    packageName = packageName,
                    displayName = displayName,
                    category = category,
                    totalForeground = totalSeconds.seconds,
                    role = role,
                    isSystemApp = isSystemApp
                )
            }.sortedWith(
                compareByDescending<AppUsage> { it.totalForeground }
                    .thenBy { it.displayName }
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

    /**
     * Load focus trends for a date range (typically a week).
     * Returns a list of FocusTrend objects, one per day.
     */
    suspend fun loadWeeklyFocusTrends(startEpochDay: Long, endEpochDay: Long): List<FocusTrend> {
        val trends = mutableListOf<FocusTrend>()
        
        for (day in startEpochDay..endEpochDay) {
            val daySummary = loadDay(day)
            val switchEvents = appSwitchEventDao.getForDay(day)
            val unlockEntities = unlockEventDao.getForDay(day)
            
            if (daySummary != null) {
                val switches = switchEvents.map { it.toDomain() }
                val unlocks = unlockEntities.map { it.toDomain() }
                val focusMetrics = FocusCalculator.computeFocusMetrics(
                    usages = daySummary.usages,
                    appSwitchEvents = switches,
                    unlockEvents = unlocks,
                    dateEpochDay = day
                )
                
                trends.add(
                    FocusTrend(
                        dateEpochDay = day,
                        focusScore = focusMetrics.focusScore,
                        appSwitchCount = focusMetrics.appSwitchCount,
                        deepWorkMinutes = focusMetrics.totalDeepWorkMinutes
                    )
                )
            }
        }
        
        return trends
    }

    suspend fun updateAppRole(packageName: String, role: AppRole) {
        appConfigDao.insert(AppConfigEntity(packageName, role))
    }

    private fun resolveRole(
        isSystemApp: Boolean,
        category: com.earnergy.domain.model.AppCategory,
        configRole: AppRole?
    ): AppRole {
        return configRole ?: when {
            isSystemApp -> AppRole.IGNORED
            category == com.earnergy.domain.model.AppCategory.PRODUCTIVE -> AppRole.INVESTED
            category == com.earnergy.domain.model.AppCategory.SOCIAL ||
            category == com.earnergy.domain.model.AppCategory.ENTERTAINMENT -> AppRole.DRIFT
            else -> AppRole.IGNORED
        }
    }

    private fun AppUsageEntity.toDomain(configRole: AppRole?): AppUsage {
        return AppUsage(
            packageName = packageName,
            displayName = displayName,
            category = category,
            totalForeground = totalSeconds.seconds,
            role = resolveRole(isSystemApp, category, configRole),
            isSystemApp = isSystemApp
        )
    }
    
    private fun com.earnergy.core.data.local.AppSwitchEventEntity.toDomain() = AppSwitchEvent(
        timestamp = timestamp,
        fromPackage = fromPackage,
        toPackage = toPackage,
        dateEpochDay = dateEpochDay
    )

    private fun com.earnergy.core.data.local.UnlockEventEntity.toDomain() = com.earnergy.domain.model.UnlockEvent(
        timestamp = timestamp,
        dateEpochDay = dateEpochDay,
        wasNotificationLed = wasNotificationLed,
        triggeringPackage = triggeringPackage
    )
    
    /**
     * Observe health metrics for a specific day.
     */
    fun observeHealthMetrics(epochDay: Long): Flow<HealthMetrics> {
        return combine(
            appUsageDao.observeForDay(epochDay),
            appConfigDao.observeAll(),
            breakEventDao.observeForDay(epochDay)
        ) { usageEntities, configEntities, breakEntities ->
            val configMap = configEntities.associate { it.packageName to it.role }
            val usages = usageEntities.map { it.toDomain(configMap[it.packageName]) }
            val breaks = breakEntities.map { it.toDomain() }
            
            HealthCalculator.computeHealthMetrics(
                usages = usages,
                breakEvents = breaks,
                dateEpochDay = epochDay,
                currentTimeMillis = System.currentTimeMillis()
            )
        }.distinctUntilChanged()
    }
    
    /**
     * Log a break event.
     */
    suspend fun logBreak(durationSeconds: Int, wasManual: Boolean = true) {
        val now = System.currentTimeMillis()
        val today = LocalDate.now(clock).toEpochDay()
        
        breakEventDao.insert(
            BreakEventEntity(
                timestamp = now,
                dateEpochDay = today,
                durationSeconds = durationSeconds,
                wasManual = wasManual
            )
        )
    }
    
    private fun BreakEventEntity.toDomain() = BreakEvent(
        timestamp = timestamp,
        dateEpochDay = dateEpochDay,
        durationSeconds = durationSeconds,
        wasManual = wasManual
    )
    
    /**
     * Observe active suggestions.
     */
    fun observeActiveSuggestions(): Flow<List<com.earnergy.domain.model.Suggestion>> {
        return suggestionDao.observeActiveSuggestions().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Dismiss a suggestion.
     */
    suspend fun dismissSuggestion(id: String) {
        suggestionDao.dismiss(id)
    }
    
    /**
     * Mark a suggestion as completed.
     */
    suspend fun completeSuggestion(id: String) {
        suggestionDao.markCompleted(id)
    }
    
    /**
     * Generate and store suggestions based on current metrics.
     */
    suspend fun generateSuggestions() {
        val today = LocalDate.now(clock).toEpochDay()
        
        // Get current metrics
        val daySummary = loadDay(today)
        val healthMetrics = observeHealthMetrics(today).first()
        val switchEvents = appSwitchEventDao.getForDay(today)
        val unlockEntities = unlockEventDao.getForDay(today)
        
        if (daySummary == null) return
        
        val switches = switchEvents.map { it.toDomain() }
        val unlocks = unlockEntities.map { it.toDomain() }
        val focusMetrics = com.earnergy.domain.calculation.FocusCalculator.computeFocusMetrics(
            usages = daySummary.usages,
            appSwitchEvents = switches,
            unlockEvents = unlocks,
            dateEpochDay = today
        )
        
        val impact = com.earnergy.domain.calculation.EarningCalculator.computeImpact(daySummary)
        val investedMinutes = (impact.productiveSeconds / 60).toInt()
        val driftMinutes = (impact.passiveSeconds / 60).toInt()
        
        // Generate suggestions
        val suggestions = com.earnergy.domain.calculation.SuggestionGenerator.generateSuggestions(
            healthMetrics = healthMetrics,
            focusMetrics = focusMetrics,
            investedMinutes = investedMinutes,
            driftMinutes = driftMinutes
        )
        
        // Store in database
        val entities = suggestions.map { it.toEntity() }
        suggestionDao.insertAll(entities)
        
        // Clean up old suggestions (older than 7 days)
        val cutoff = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        suggestionDao.deleteOlderThan(cutoff)
    }
    
    private fun SuggestionEntity.toDomain() = com.earnergy.domain.model.Suggestion(
        id = id,
        type = com.earnergy.domain.model.SuggestionType.valueOf(type),
        title = title,
        description = description,
        priority = com.earnergy.domain.model.Priority.valueOf(priority),
        manualSteps = if (manualStepsJson.isEmpty()) emptyList() else manualStepsJson.split("|"),
        autoActionAvailable = false,
        timestamp = timestamp
    )
    
    private fun com.earnergy.domain.model.Suggestion.toEntity() = SuggestionEntity(
        id = id,
        type = type.name,
        title = title,
        description = description,
        priority = priority.name,
        manualStepsJson = manualSteps.joinToString("|"),
        timestamp = timestamp,
        dismissed = false,
        completed = false
    )
}
