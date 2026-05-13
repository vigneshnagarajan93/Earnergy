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
    val currencyCode: Flow<String> = settingsDataStore.currencyCode
    val healthFeaturesEnabled: Flow<Boolean> = settingsDataStore.healthFeaturesEnabled
    val brightnessWarningEnabled: Flow<Boolean> = settingsDataStore.brightnessWarningEnabled

    suspend fun setHourlyRate(value: Double) {
        settingsDataStore.setHourlyRate(value)
    }

    suspend fun setCurrencyCode(code: String) {
        settingsDataStore.setCurrencyCode(code)
    }

    suspend fun setHealthFeaturesEnabled(enabled: Boolean) {
        settingsDataStore.setHealthFeaturesEnabled(enabled)
    }

    suspend fun setBrightnessWarningEnabled(enabled: Boolean) {
        settingsDataStore.setBrightnessWarningEnabled(enabled)
    }
}
