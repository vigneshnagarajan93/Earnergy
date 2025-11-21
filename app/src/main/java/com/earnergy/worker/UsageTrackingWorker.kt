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
    private val appSwitchEventDao: AppSwitchEventDao
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
            
            var previousPackage: String? = null
            var previousTimestamp: Long? = null

            // Process events to detect app switches
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                
                // We only care about ACTIVITY_RESUMED events (app coming to foreground)
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    val currentPackage = event.packageName
                    val currentTimestamp = event.timeStamp
                    
                    // If we have a previous app and it's different from current, record a switch
                    if (previousPackage != null && previousPackage != currentPackage) {
                        val dateEpochDay = LocalDate.ofInstant(
                            java.time.Instant.ofEpochMilli(currentTimestamp),
                            ZoneId.systemDefault()
                        ).toEpochDay()
                        
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
                    previousTimestamp = currentTimestamp
                }
            }

            // Save all detected switches to database
            if (appSwitchEvents.isNotEmpty()) {
                appSwitchEventDao.insertAll(appSwitchEvents)
            }

            // Clean up old data (older than 30 days)
            val thirtyDaysAgo = LocalDate.now().minusDays(30).toEpochDay()
            appSwitchEventDao.deleteOlderThan(thirtyDaysAgo)

            Result.success()
        } catch (e: Exception) {
            // Log error but don't fail - we'll try again next time
            e.printStackTrace()
            Result.retry()
        }
    }
}
