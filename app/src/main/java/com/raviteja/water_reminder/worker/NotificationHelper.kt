package com.raviteja.water_reminder.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.raviteja.water_reminder.MainActivity
import com.raviteja.water_reminder.R
import com.raviteja.water_reminder.util.Constants
import com.raviteja.water_reminder.data.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object NotificationHelper {
    private const val CHANNEL_ID = "hydra_reminder_channel"

    fun sendWelcomeNotification(context: Context) {
        val title = "Welcome to Hydra! 🌊"
        val content = "You're all set. We'll help you stay hydrated throughout the day."
        
        // Use a default sound or fetch once if absolutely needed, 
        // but for welcome we can just use default.
        sendNotification(context, title, content, NotificationCompat.PRIORITY_HIGH)
    }

    fun sendNotification(
        context: Context,
        title: String,
        content: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        soundName: String = "hydra_chime"
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val soundResId = when(soundName) {
            "hydra_save" -> R.raw.hydra_save
            "hydra_bell" -> R.raw.hydra_bell
            else -> R.raw.hydra_chime
        }
        
        val soundUri = Uri.parse("android.resource://${context.packageName}/$soundResId")
        val channelId = "hydra_reminder_channel_$soundName"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                "Hydra Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to stay hydrated"
                enableLights(true)
                setShowBadge(true)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val drink250Intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = Constants.ACTION_DRINK_250
        }
        val drink250PendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            drink250Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val drink500Intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = Constants.ACTION_DRINK_500
        }
        val drink500PendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            drink500Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = Constants.ACTION_SNOOZE
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .setColor(0xFF00C2FF.toInt())
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setLights(0xFF00C2FF.toInt(), 3000, 3000)
            .addAction(R.drawable.ic_launcher_foreground, "Drink 250ml", drink250PendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Drink 500ml", drink500PendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Snooze", snoozePendingIntent)
            .build()

        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
    }
}
