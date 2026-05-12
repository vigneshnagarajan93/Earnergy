package com.earnergy

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.earnergy.core.ui.theme.EarnergyTheme
import com.earnergy.service.HealthMonitorService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        startHealthMonitorService()

        setContent {
            EarnergyTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    EarnergyApp()
                }
            }
        }
    }

    private fun startHealthMonitorService() {
        val intent = Intent(this, HealthMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
