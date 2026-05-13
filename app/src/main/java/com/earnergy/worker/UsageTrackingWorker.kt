package com.earnergy.worker

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.earnergy.core.data.local.AppSwitchEventDao
import com.earnergy.core.data.local.AppSwitchEventEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Background worker that tracks app switches for focus score calculation.
 * Runs periodically (every 15 minutes by default) to detect when user switches between apps.
 */
@HiltWorker
class UsageTrackingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val appSwitchEventDao: AppSwitchEventDao,
    private val unlockEventDao: com.earnergy.core.data.local.UnlockEventDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "usage_tracking_worker"
        private const val LAST_CHECK_KEY = "last_check_timestamp"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val usageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) 
                as? UsageStatsManager ?: return@withContext Result.failure()

            // Get last check timestamp (or default to 15 minutes ago)
            val lastCheckTime = inputData.getLong(LAST_CHECK_KEY, 
                System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(15))
            val currentTime = System.currentTimeMillis()

            // Query usage events since last check
            val events = usageStatsManager.queryEvents(lastCheckTime, currentTime)
            val appSwitchEvents = mutableListOf<AppSwitchEventEntity>()
            val unlockEvents = mutableListOf<com.earnergy.core.data.local.UnlockEventEntity>()
            
            var previousPackage: String? = null
            var lastUnlockTime: Long = 0
            var lastLockTime: Long = 0

            // Process events
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                
                val currentPackage = event.packageName
                val currentTimestamp = event.timeStamp

                when (event.eventType) {
                    15, 18 -> { // SCREEN_INTERACTIVE, KEYGUARD_HIDDEN
                        if (currentTimestamp - lastUnlockTime > 500) {
                            val dateEpochDay = java.time.Instant.ofEpochMilli(currentTimestamp).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                            unlockEvents.add(
                                com.earnergy.core.data.local.UnlockEventEntity(
                                    timestamp = currentTimestamp,
                                    dateEpochDay = dateEpochDay,
                                    isLockEvent = false
                                )
                            )
                            lastUnlockTime = currentTimestamp
                        }
                    }
                    16, 17 -> { // SCREEN_NON_INTERACTIVE, KEYGUARD_SHOWN
                        if (currentTimestamp - lastLockTime > 500) {
                            val dateEpochDay = java.time.Instant.ofEpochMilli(currentTimestamp).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                            unlockEvents.add(
                                com.earnergy.core.data.local.UnlockEventEntity(
                                    timestamp = currentTimestamp,
                                    dateEpochDay = dateEpochDay,
                                    isLockEvent = true
                                )
                            )
                            lastLockTime = currentTimestamp
                        }
                    }
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        // If we have a previous app and it's different from current, record a switch
                        if (previousPackage != null && previousPackage != currentPackage) {
                            val dateEpochDay = java.time.Instant.ofEpochMilli(currentTimestamp).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()

                            appSwitchEvents.add(
                                AppSwitchEventEntity(
                                    timestamp = currentTimestamp,
                                    fromPackage = previousPackage,
                                    toPackage = currentPackage,
                                    dateEpochDay = dateEpochDay
                                )
                            )
                        }

                        previousPackage = currentPackage
                    }
                }
            }

            // Save all detected events to database
            if (appSwitchEvents.isNotEmpty()) {
                appSwitchEventDao.insertAll(appSwitchEvents)
            }
            if (unlockEvents.isNotEmpty()) {
                // Since this worker might re-process some events if lastCheckTime overlaps,
                // but Room handles REPLACE for the same ID. However, these don't have stable IDs.
                // In refreshToday we deleteForDay first. Here we might want to be careful.
                // For simplicity, we just insert.
                unlockEventDao.insertAll(unlockEvents)
            }

            // Clean up old data (older than 30 days)
            val thirtyDaysAgo = LocalDate.now().minusDays(30).toEpochDay()
            appSwitchEventDao.deleteOlderThan(thirtyDaysAgo)
            unlockEventDao.deleteOlderThan(thirtyDaysAgo)

            Result.success()
        } catch (e: Exception) {
            // Log error but don't fail - we'll try again next time
            e.printStackTrace()
            Result.retry()
        }
    }
}
