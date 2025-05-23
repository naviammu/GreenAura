package com.example.greenaura

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val plantName = intent.getStringExtra("plantName") ?: "Unknown Plant"
        val task = intent.getStringExtra("task") ?: "Task"

        Log.d("ReminderReceiver", "⏰ Alarm received for plant: $plantName - Task: $task")

        // Play the default ringtone when the alarm triggers
        playRingtone(context)

        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "plant_reminder_channel"

            // Create notification channel for Android O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Plant Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for plant care reminders"
                    setSound(null, null) // Disable default sound at channel level
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Set custom sound from raw resource
            val soundUri = Uri.parse("android.resource://${context.packageName}/raw/alarm_sound")

            // Build the notification
            val builder = NotificationCompat.Builder(context, channelId)
                .setContentTitle("Reminder: $plantName")
                .setContentText("Time to $task your plant!")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(soundUri)  // Apply custom sound
                .setAutoCancel(true) // Automatically dismiss when clicked

            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, builder.build())

            Log.d("ReminderReceiver", "✅ Notification sent (ID: $notificationId)")

            // Dismiss the notification after 30 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                notificationManager.cancel(notificationId)
                Log.d("ReminderReceiver", "✅ Notification dismissed (ID: $notificationId) after 30 seconds")
            }, 10000)  // 10 seconds in milliseconds

        } catch (e: Exception) {
            Log.e("ReminderReceiver", "❌ Error showing notification: ${e.message}", e)
        }
    }

    // Function to play the ringtone
    private fun playRingtone(context: Context) {
        // Get the default ringtone URI
        val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // Create a Ringtone object
        val ringtone: Ringtone = RingtoneManager.getRingtone(context, ringtoneUri)

        // Play the ringtone
        ringtone.play()
    }
}
