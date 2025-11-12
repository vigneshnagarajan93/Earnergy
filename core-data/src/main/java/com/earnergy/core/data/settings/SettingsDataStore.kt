package com.earnergy.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val hourlyRate: Flow<Double> = dataStore.data
        .map { preferences ->
            preferences[HOURLY_RATE_KEY] ?: DEFAULT_HOURLY_RATE
        }

    suspend fun setHourlyRate(value: Double) {
        dataStore.edit { prefs ->
            prefs[HOURLY_RATE_KEY] = value
        }
    }

    companion object {
        const val DATA_STORE_NAME = "earnergy_settings"
        private val HOURLY_RATE_KEY = doublePreferencesKey("hourly_rate")
        private const val DEFAULT_HOURLY_RATE = 25.0
    }
}
