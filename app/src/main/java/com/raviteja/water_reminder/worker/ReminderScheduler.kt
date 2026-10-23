package com.raviteja.water_reminder.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val REMINDER_WORK_NAME = "water_reminder_work"
    private const val TAG = "ReminderScheduler"
    private const val ALARM_REQUEST_CODE = 1002

    fun scheduleReminders(context: Context, intervalMinutes: Long = 120, policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE) {
        Log.d(TAG, "Scheduling reminder in $intervalMinutes minutes with policy $policy")
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        
        if (policy == ExistingWorkPolicy.KEEP) {
            val existing = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (existing != null) {
                Log.d(TAG, "Alarm already scheduled, keeping existing one.")
                return
            }
        }

        // Check if we have permission for exact alarms on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms. Falling back to inexact.")
                scheduleInexactAlarm(context, intervalMinutes)
                return
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(intervalMinutes)

        // For maximum reliability even after days of inactivity, 
        // we use setAlarmClock which is the highest priority alarm.
        val showIntent = Intent(context, com.raviteja.water_reminder.MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            0,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    private fun scheduleInexactAlarm(context: Context, intervalMinutes: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(intervalMinutes)
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun cancelReminders(context: Context) {
        Log.d(TAG, "Cancelling reminders")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
