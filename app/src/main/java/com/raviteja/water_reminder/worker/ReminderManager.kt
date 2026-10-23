package com.raviteja.water_reminder.worker

import android.content.Context
import android.util.Log
import com.raviteja.water_reminder.HydraApplication
import com.raviteja.water_reminder.data.datastore.UserPreferences
import com.raviteja.water_reminder.data.datastore.UserProfile
import com.raviteja.water_reminder.logic.HydrationAlgorithm
import com.raviteja.water_reminder.logic.QuotesProvider
import kotlinx.coroutines.flow.first
import java.util.Calendar

object ReminderManager {
    private const val TAG = "ReminderManager"

    suspend fun handleReminder(context: Context) {
        try {
            Log.d(TAG, "handleReminder: triggered")
            val userPreferences = UserPreferences(context)
            val userProfile = userPreferences.userProfileFlow.first()
            
            if (!userProfile.remindersEnabled) {
                Log.d(TAG, "Reminders disabled in settings, stopping chain.")
                return
            }

            if (isWithinActiveHours(userProfile)) {
                val app = context.applicationContext as? HydraApplication
                val repository = app?.repository ?: com.raviteja.water_reminder.data.repository.WaterRepository(
                    com.raviteja.water_reminder.data.local.WaterDatabase.getDatabase(context).waterLogDao()
                )
                
                val todayLogs = repository.getTodayLogs().first()
                val todayIntake = todayLogs.sumOf { it.amount }
                val expectedIntake = HydrationAlgorithm.calculateExpectedIntake(userProfile, java.time.LocalTime.now())
                val status = HydrationAlgorithm.getReminderStatus(expectedIntake, todayIntake)
                
                Log.d(TAG, "handleReminder: todayIntake=$todayIntake ml, expectedIntake=$expectedIntake ml, status=$status")

                val lastLogTime = todayLogs.maxByOrNull { it.timestamp }?.timestamp ?: 0L
                val lastNotificationTime = userProfile.lastNotificationTime
                
                var currentIgnoreCount = userProfile.ignoreCount
                if (lastNotificationTime > 0 && lastLogTime < lastNotificationTime) {
                    currentIgnoreCount++
                } else if (lastLogTime > lastNotificationTime) {
                    currentIgnoreCount = 0
                }

                NotificationHelper.sendNotification(
                    context = context,
                    title = when (status) {
                        HydrationAlgorithm.ReminderStatus.CRITICAL -> "🚨 Hydra Alert: Critical Deficit!"
                        HydrationAlgorithm.ReminderStatus.NORMAL -> "💧 Time for a sip, ${userProfile.name}"
                        HydrationAlgorithm.ReminderStatus.PASSIVE -> "🌿 Stay Hydrated"
                    },
                    content = QuotesProvider.getNotificationMessage(status),
                    priority = when (status) {
                        HydrationAlgorithm.ReminderStatus.CRITICAL -> 1 // NotificationCompat.PRIORITY_HIGH
                        HydrationAlgorithm.ReminderStatus.NORMAL -> 0 // NotificationCompat.PRIORITY_DEFAULT
                        HydrationAlgorithm.ReminderStatus.PASSIVE -> -1 // NotificationCompat.PRIORITY_LOW
                    },
                    soundName = userProfile.notificationSound
                )

                userPreferences.saveUserProfile(userProfile.copy(
                    lastNotificationTime = System.currentTimeMillis(),
                    ignoreCount = currentIgnoreCount
                ))

                val nextInterval = HydrationAlgorithm.calculateNextReminderInterval(status, currentIgnoreCount)
                Log.d(TAG, "Dispatched notification. Scheduling next reminder in $nextInterval minutes (ignoreCount=$currentIgnoreCount)")
                ReminderScheduler.scheduleReminders(context, nextInterval.toLong(), androidx.work.ExistingWorkPolicy.REPLACE)
            } else {
                Log.d(TAG, "Outside active hours, scheduling for morning.")
                scheduleForMorning(context, userProfile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in handleReminder", e)
            // Fallback: try again in an hour if something went wrong
            ReminderScheduler.scheduleReminders(context, 60, androidx.work.ExistingWorkPolicy.REPLACE)
        }
    }

    fun isWithinActiveHours(profile: UserProfile): Boolean {
        val currentTime = java.time.LocalTime.now()
        val wakeTime = try { java.time.LocalTime.parse(profile.wakeTime) } catch (e: Exception) { java.time.LocalTime.of(8, 0) }
        val sleepTime = try { java.time.LocalTime.parse(profile.sleepTime) } catch (e: Exception) { java.time.LocalTime.of(22, 0) }
        return HydrationAlgorithm.isWithinActiveHours(currentTime, wakeTime, sleepTime)
    }

    fun scheduleForMorning(context: Context, profile: UserProfile) {
        val calendar = Calendar.getInstance()
        val wakeParts = profile.wakeTime.split(":").map { it.toInt() }
        
        val nextWake = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, wakeParts[0])
            set(Calendar.MINUTE, wakeParts[1])
            set(Calendar.SECOND, 0)
            if (before(calendar) || calendar.get(Calendar.HOUR_OF_DAY) >= wakeParts[0]) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        val delayMinutes = (nextWake.timeInMillis - calendar.timeInMillis) / (1000 * 60)
        Log.d(TAG, "scheduleForMorning: next wake time is ${profile.wakeTime}, delayMinutes=$delayMinutes")
        ReminderScheduler.scheduleReminders(context, delayMinutes.coerceAtLeast(1), androidx.work.ExistingWorkPolicy.REPLACE)
    }
}
