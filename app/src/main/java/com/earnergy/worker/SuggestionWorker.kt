package com.earnergy.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.earnergy.core.data.repository.UsageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker that periodically generates smart suggestions based on user metrics.
 * Runs every 30 minutes to analyze patterns and provide actionable insights.
 */
@HiltWorker
class SuggestionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageRepository: UsageRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Generate and store suggestions
            usageRepository.generateSuggestions()
            Result.success()
        } catch (e: Exception) {
            // Don't retry on failure, just wait for next scheduled run
            Result.success()
        }
    }

    companion object {
        const val WORK_NAME = "suggestion_worker"
    }
}
