package com.earnergy.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.earnergy.MainActivity
import com.earnergy.R

object NotificationHelper {
    
    private const val CHANNEL_ID_BREAK_REMINDERS = "break_reminders"
    private const val NOTIFICATION_ID_BREAK_REMINDER = 1001
    
    /**
     * Create notification channels for the app.
     * Should be called on app startup.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val breakReminderChannel = NotificationChannel(
                CHANNEL_ID_BREAK_REMINDERS,
                "Break Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications to remind you to take breaks for eye health"
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(breakReminderChannel)
        }
    }
    
    /**
     * Send a break reminder notification.
     */
    fun sendBreakReminder(
        context: Context,
        continuousMinutes: Int,
        eyeStrainScore: Int
    ) {
        // Check if we have notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return // Don't send notification if permission not granted
            }
        }
        
        // Create intent to open the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_BREAK_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Create a better icon
            .setContentTitle("👁️ Time for a break!")
            .setContentText("You've been using your phone for $continuousMinutes minutes. Take a 20-second break.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You've been using your phone for $continuousMinutes minutes. Follow the 20-20-20 rule: look at something 20 feet away for 20 seconds.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BREAK_REMINDER, notification)
    }
    
    /**
     * Check if notification permission is granted (Android 13+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required on older versions
        }
    }
}
