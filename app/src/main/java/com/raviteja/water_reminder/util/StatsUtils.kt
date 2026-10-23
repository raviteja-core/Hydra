package com.raviteja.water_reminder.util

import com.raviteja.water_reminder.data.entity.WaterLog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object StatsUtils {

    fun calculateStreak(logs: List<WaterLog>, zoneId: ZoneId = ZoneId.systemDefault(), today: LocalDate = LocalDate.now()): Int {
        if (logs.isEmpty()) return 0
        
        val logDates = logs.map {
            Instant.ofEpochMilli(it.timestamp)
                .atZone(zoneId)
                .toLocalDate()
        }.distinct().sortedDescending()

        var streak = 0
        var currentCheckDate = today

        if (logDates.isEmpty()) return 0
        
        if (logDates[0] != currentCheckDate) {
            val yesterday = currentCheckDate.minusDays(1)
            if (logDates[0] == yesterday) {
                currentCheckDate = yesterday
            } else {
                // If today is not in logs and yesterday is not in logs, streak is broken
                return 0
            }
        }

        for (date in logDates) {
            if (date == currentCheckDate) {
                streak++
                currentCheckDate = currentCheckDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    fun calculateWeeklyCompletionRate(logs: List<WaterLog>, dailyGoal: Int, zoneId: ZoneId = ZoneId.systemDefault(), today: LocalDate = LocalDate.now()): Float {
        if (dailyGoal <= 0) return 0f

        var daysMetGoal = 0
        for (i in 0..6) {
            val checkDay = today.minusDays(i.toLong())
            val dayIntake = logs.filter { log ->
                Instant.ofEpochMilli(log.timestamp)
                    .atZone(zoneId)
                    .toLocalDate() == checkDay
            }.sumOf { it.amount }
            
            if (dayIntake >= dailyGoal) daysMetGoal++
        }
        return (daysMetGoal.toFloat() / 7f) * 100f
    }

    fun calculateWeeklyData(logs: List<WaterLog>, zoneId: ZoneId = ZoneId.systemDefault(), today: LocalDate = LocalDate.now()): List<Int> {
        val weeklyIntakeMl = MutableList(7) { 0 }
        for (i in 0..6) {
            val checkDay = today.minusDays(i.toLong())
            weeklyIntakeMl[6 - i] = logs.filter { log ->
                Instant.ofEpochMilli(log.timestamp)
                    .atZone(zoneId)
                    .toLocalDate() == checkDay
            }.sumOf { it.amount }
        }
        return weeklyIntakeMl
    }
}
