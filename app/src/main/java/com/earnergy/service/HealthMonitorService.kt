package com.earnergy.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.earnergy.R
import com.earnergy.core.data.repository.SettingsRepository
import com.earnergy.core.data.repository.UsageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.LocalDate

@AndroidEntryPoint
class HealthMonitorService : Service(), SensorEventListener {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var usageRepository: UsageRepository

    @Inject
    lateinit var eyeHealthManager: EyeHealthManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var eyeBreakOverlay: EyeBreakOverlay? = null
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private var lastAmbientLight: Float = -1f
    private var lastBrightnessWarningTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        registerLightSensor()
        observeHealthMetrics()
        eyeHealthManager.start()
    }

    private fun registerLightSensor() {
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun observeHealthMetrics() {
        serviceScope.launch {
            if (!settingsRepository.healthFeaturesEnabled.first()) return@launch

            val today = LocalDate.now().toEpochDay()
            usageRepository.observeHealthMetrics(today).collect { metrics ->
                // If continuous screen time is exactly a multiple of 20, show overlay
                // We use a small window to avoid double triggers but ensure it hits
                if (metrics.continuousScreenTimeMinutes >= 20 && metrics.continuousScreenTimeMinutes % 20 == 0) {
                    showEyeBreakOverlay()
                }
            }
        }
    }

    private fun showEyeBreakOverlay() {
        if (!Settings.canDrawOverlays(this)) return

        if (eyeBreakOverlay == null) {
            eyeBreakOverlay = EyeBreakOverlay(this)
        }
        eyeBreakOverlay?.show(onDismiss = {
            serviceScope.launch {
                usageRepository.logBreak(durationSeconds = 20, wasManual = false)
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            lastAmbientLight = event.values[0]
            checkBrightnessCondition()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun checkBrightnessCondition() {
        serviceScope.launch {
            if (!settingsRepository.brightnessWarningEnabled.first()) return@launch

            val currentBrightness = try {
                Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (e: Exception) {
                -1
            }

            if (currentBrightness == -1) return@launch

            // If ambient light is low (< 10 lux) and brightness is high (> 150/255)
            if (lastAmbientLight < 10f && currentBrightness > 150) {
                val now = System.currentTimeMillis()
                if (now - lastBrightnessWarningTime > WARNING_COOLDOWN_MS) {
                    showBrightnessWarning()
                    lastBrightnessWarningTime = now
                }
            }
        }
    }

    private fun showBrightnessWarning() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_eye)
            .setContentTitle("🌙 Screen too bright?")
            .setContentText("It's dark around you. Lowering screen brightness can reduce eye strain.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(BRIGHTNESS_WARNING_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Health Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service for health features"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_eye)
            .setContentTitle("Health Monitor Active")
            .setContentText("Monitoring for eye health features")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        eyeHealthManager.stop()
        serviceScope.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "health_monitor_service"
        private const val NOTIFICATION_ID = 2001
        private const val BRIGHTNESS_WARNING_ID = 2002
        private const val WARNING_COOLDOWN_MS = 30 * 60 * 1000L // 30 minutes
    }
}
