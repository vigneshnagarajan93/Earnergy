package com.earnergy.core.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Currency
import java.util.Locale
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

    val currencyCode: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[CURRENCY_CODE_KEY] ?: try {
                Currency.getInstance(Locale.getDefault()).currencyCode
            } catch (e: Exception) {
                "USD"
            }
        }

    val healthFeaturesEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[HEALTH_FEATURES_ENABLED_KEY] ?: true
        }

    val brightnessWarningEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[BRIGHTNESS_WARNING_ENABLED_KEY] ?: true
        }

    suspend fun setHourlyRate(value: Double) {
        dataStore.edit { prefs ->
            prefs[HOURLY_RATE_KEY] = value
        }
    }

    suspend fun setCurrencyCode(code: String) {
        dataStore.edit { prefs ->
            prefs[CURRENCY_CODE_KEY] = code
        }
    }

    suspend fun setHealthFeaturesEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[HEALTH_FEATURES_ENABLED_KEY] = enabled
        }
    }

    suspend fun setBrightnessWarningEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[BRIGHTNESS_WARNING_ENABLED_KEY] = enabled
        }
    }

    companion object {
        const val DATA_STORE_NAME = "earnergy_settings"
        private val HOURLY_RATE_KEY = doublePreferencesKey("hourly_rate")
        private val CURRENCY_CODE_KEY = stringPreferencesKey("currency_code")
        private val HEALTH_FEATURES_ENABLED_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("health_features_enabled")
        private val BRIGHTNESS_WARNING_ENABLED_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("brightness_warning_enabled")
        private const val DEFAULT_HOURLY_RATE = 25.0
    }
}
