package com.earnergy.core.data.repository

import com.earnergy.core.data.settings.SettingsDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    val hourlyRate: Flow<Double> = settingsDataStore.hourlyRate

    suspend fun setHourlyRate(value: Double) {
        settingsDataStore.setHourlyRate(value)
    }
}
