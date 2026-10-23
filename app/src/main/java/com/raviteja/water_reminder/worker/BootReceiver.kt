package com.raviteja.water_reminder.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.raviteja.water_reminder.data.datastore.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
        private const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == ACTION_QUICKBOOT_POWERON) {
            
            Log.d(TAG, "Boot event received: $action. Ensuring reminders are scheduled.")
            val pendingResult = goAsync()
            val userPreferences = UserPreferences(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val profile = userPreferences.userProfileFlow.first()
                    if (profile.remindersEnabled) {
                        if (ReminderManager.isWithinActiveHours(profile)) {
                            Log.d(TAG, "Within active hours. Scheduling reminder in 15 minutes.")
                            ReminderScheduler.scheduleReminders(context, 15, androidx.work.ExistingWorkPolicy.REPLACE)
                        } else {
                            Log.d(TAG, "Outside active hours. Scheduling for morning wake time.")
                            ReminderManager.scheduleForMorning(context, profile)
                        }
                    } else {
                        Log.d(TAG, "Reminders are disabled by user. Doing nothing.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling reminders on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

