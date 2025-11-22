package com.earnergy.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.earnergy.core.data.repository.UsageRepository
import com.earnergy.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import kotlinx.coroutines.flow.first

/**
 * Worker that periodically checks health metrics and sends break reminders.
 * Runs every 20 minutes to align with the 20-20-20 rule.
 */
@HiltWorker
class BreakReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageRepository: UsageRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Get today's health metrics
            val today = LocalDate.now().toEpochDay()
            val healthMetrics = usageRepository.observeHealthMetrics(today).first()
            
            // Check if user needs a break
            // Send notification if continuous screen time >= 20 minutes
            if (healthMetrics.continuousScreenTimeMinutes >= BREAK_THRESHOLD_MINUTES) {
                NotificationHelper.sendBreakReminder(
                    context = context,
                    continuousMinutes = healthMetrics.continuousScreenTimeMinutes,
                    eyeStrainScore = healthMetrics.eyeStrainScore.toInt()
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            // Don't retry on failure, just wait for next scheduled run
            Result.success()
        }
    }

    companion object {
        const val WORK_NAME = "break_reminder_worker"
        private const val BREAK_THRESHOLD_MINUTES = 20
    }
}
