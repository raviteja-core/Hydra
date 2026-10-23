package com.raviteja.water_reminder.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.raviteja.water_reminder.HydraApplication
import com.raviteja.water_reminder.data.local.WaterDatabase
import com.raviteja.water_reminder.data.repository.WaterRepository
import com.raviteja.water_reminder.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "NotificationActionReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "Notification action received: $action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? HydraApplication
                val repository = app?.repository ?: WaterRepository(WaterDatabase.getDatabase(context).waterLogDao())

                when (action) {
                    Constants.ACTION_DRINK_250 -> {
                        Log.d(TAG, "Logging 250ml water intake from notification action")
                        repository.addLog(250)
                        ReminderScheduler.scheduleReminders(context, 120) // Reset to standard interval
                    }
                    Constants.ACTION_DRINK_500 -> {
                        Log.d(TAG, "Logging 500ml water intake from notification action")
                        repository.addLog(500)
                        ReminderScheduler.scheduleReminders(context, 120)
                    }
                    Constants.ACTION_SNOOZE -> {
                        Log.d(TAG, "Snoozing reminder for 20 minutes")
                        ReminderScheduler.scheduleReminders(context, 20) // Snooze for 20 mins
                    }
                }
                // Dismiss notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.cancel(Constants.NOTIFICATION_ID)
            } catch (e: Exception) {
                Log.e(TAG, "Error handling notification action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
