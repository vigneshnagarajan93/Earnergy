package com.earnergy

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.earnergy.worker.UsageTrackingWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class EarnergyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // Create notification channels
        com.earnergy.util.NotificationHelper.createNotificationChannels(this)
        
        // Schedule workers
        scheduleUsageTracking()
        scheduleBreakReminders()
        scheduleSuggestions()
    }

    private fun scheduleUsageTracking() {
        val workRequest = PeriodicWorkRequestBuilder<UsageTrackingWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            UsageTrackingWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    private fun scheduleBreakReminders() {
        val workRequest = PeriodicWorkRequestBuilder<com.earnergy.worker.BreakReminderWorker>(
            20, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            com.earnergy.worker.BreakReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    private fun scheduleSuggestions() {
        val workRequest = PeriodicWorkRequestBuilder<com.earnergy.worker.SuggestionWorker>(
            30, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            com.earnergy.worker.SuggestionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
