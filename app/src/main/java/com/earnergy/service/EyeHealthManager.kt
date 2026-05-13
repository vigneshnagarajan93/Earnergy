package com.earnergy.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.earnergy.core.data.local.UnlockEventEntity
import com.earnergy.core.data.local.UnlockEventDao
import com.earnergy.core.data.repository.UsageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class EyeHealthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageRepository: UsageRepository,
    private val unlockEventDao: UnlockEventDao
) : SensorEventListener {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastLockTime: Long = 0
    private var lastMovementTime: Long = System.currentTimeMillis()
    private var isIdle = false
    private var idleJob: Job? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val now = System.currentTimeMillis()
            val today = LocalDate.now().toEpochDay()

            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    lastLockTime = now
                    scope.launch(Dispatchers.IO) {
                        unlockEventDao.insert(
                            UnlockEventEntity(
                                timestamp = now,
                                dateEpochDay = today,
                                isLockEvent = true
                            )
                        )
                    }
                    stopIdleDetection()
                }
                Intent.ACTION_SCREEN_ON -> {
                    val lockDuration = now - lastLockTime

                    // 20-second Micro-Rest rule
                    if (lockDuration >= 20000L) {
                        scope.launch(Dispatchers.IO) {
                            usageRepository.logBreak(
                                durationSeconds = (lockDuration / 1000).toInt(),
                                wasManual = false
                            )
                        }
                    }

                    scope.launch(Dispatchers.IO) {
                        unlockEventDao.insert(
                            UnlockEventEntity(
                                timestamp = now,
                                dateEpochDay = today,
                                isLockEvent = false
                            )
                        )
                    }
                    startIdleDetection()
                }
            }
        }
    }

    fun start() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        context.registerReceiver(screenReceiver, filter)
        startIdleDetection()
    }

    fun stop() {
        context.unregisterReceiver(screenReceiver)
        stopIdleDetection()
    }

    private fun startIdleDetection() {
        lastMovementTime = System.currentTimeMillis()
        isIdle = false
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        idleJob?.cancel()
        idleJob = scope.launch {
            while (true) {
                delay(60000L) // Check every minute
                val now = System.currentTimeMillis()
                if (!isIdle && now - lastMovementTime >= 5 * 60000L) {
                    isIdle = true
                    // Log a lock event to "pause" the session timer
                    val today = LocalDate.now().toEpochDay()
                    scope.launch(Dispatchers.IO) {
                        unlockEventDao.insert(
                            UnlockEventEntity(
                                timestamp = now,
                                dateEpochDay = today,
                                isLockEvent = true
                            )
                        )
                    }
                }
            }
        }
    }

    private fun stopIdleDetection() {
        sensorManager.unregisterListener(this)
        idleJob?.cancel()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Simple movement threshold
            if (abs(x) > 0.5 || abs(y) > 0.5 || abs(z) > 10.5) { // 10.5 to account for gravity ~9.8
                lastMovementTime = System.currentTimeMillis()
                if (isIdle) {
                    isIdle = false
                    // Log an unlock event to "resume" the session timer
                    val today = LocalDate.now().toEpochDay()
                    scope.launch(Dispatchers.IO) {
                        unlockEventDao.insert(
                            UnlockEventEntity(
                                timestamp = System.currentTimeMillis(),
                                dateEpochDay = today,
                                isLockEvent = false
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
