package com.raviteja.water_reminder.logic

import com.raviteja.water_reminder.data.datastore.UserProfile
import java.time.LocalTime
import java.time.Duration
import kotlin.math.roundToInt

object HydrationAlgorithm {

    /**
     * Calculate base daily goal based on weight and other factors.
     * base_goal_ml = weight * 35
     */
    fun calculateDailyGoal(profile: UserProfile): Int {
        var goal = profile.weight * 35

        // Adjust for climate
        goal += when (profile.climate.lowercase()) {
            "hot" -> 750
            "cold" -> -200
            else -> 0
        }

        // Adjust for activity level
        goal += when (profile.activityLevel.lowercase()) {
            "high" -> 1000
            "medium" -> 500
            else -> 0
        }

        // Adjust for goal type
        goal = when (profile.goalType.lowercase()) {
            "fitness" -> (goal * 1.1).toInt()
            "medical" -> goal
            else -> goal
        }

        // Apply sanity limits
        return goal.coerceIn(500, 10000)
    }

    /**
     * Calculate the expected intake up to the current time based on biological rhythm weighting.
     */
    fun calculateExpectedIntake(profile: UserProfile, currentTime: LocalTime): Int {
        val wakeTime = try { LocalTime.parse(profile.wakeTime) } catch (e: Exception) { LocalTime.of(8, 0) }
        val sleepTime = try { LocalTime.parse(profile.sleepTime) } catch (e: Exception) { LocalTime.of(22, 0) }

        if (!isWithinActiveHours(currentTime, wakeTime, sleepTime)) {
            // If before wake time, expected is 0. If after sleep time, expected is full goal.
            // Note: This logic might need refinement for overnight schedules.
            return if (currentTime.isBefore(wakeTime) && wakeTime.isBefore(sleepTime)) 0 
                   else if (currentTime.isAfter(sleepTime) && wakeTime.isBefore(sleepTime)) profile.dailyGoal
                   else 0 // Default to 0 if unsure or middle of night in overnight schedule
        }

        val activeDurationMinutes = getActiveDurationMinutes(wakeTime, sleepTime)
        if (activeDurationMinutes <= 0) return 0

        val minutesSinceWake = getMinutesSinceWake(wakeTime, currentTime)

        // Weights: Morning (wake–3h): 1.2, Midday: 1.0, Afternoon slump: 0.9, Evening: 0.7
        val morningEndMinutes = 180
        val middayEndMinutes = (activeDurationMinutes * 0.6).toInt()
        val afternoonEndMinutes = (activeDurationMinutes * 0.8).toInt()

        val morningWeight = 1.2
        val middayWeight = 1.0
        val afternoonWeight = 0.9
        val eveningWeight = 0.7

        val totalWeight = (morningEndMinutes.coerceAtMost(activeDurationMinutes) * morningWeight) + 
                         ((middayEndMinutes - morningEndMinutes).coerceAtLeast(0) * middayWeight) + 
                         ((afternoonEndMinutes - middayEndMinutes).coerceAtLeast(0) * afternoonWeight) + 
                         ((activeDurationMinutes - afternoonEndMinutes).coerceAtLeast(0) * eveningWeight)

        var accumulatedWeight = 0.0
        
        if (minutesSinceWake > 0) {
            val morningMinutes = minutesSinceWake.coerceAtMost(morningEndMinutes)
            accumulatedWeight += morningMinutes * morningWeight
        }
        if (minutesSinceWake > morningEndMinutes) {
            val middayMinutes = (minutesSinceWake - morningEndMinutes).coerceAtMost(middayEndMinutes - morningEndMinutes)
            accumulatedWeight += middayMinutes * middayWeight
        }
        if (minutesSinceWake > middayEndMinutes) {
            val afternoonMinutes = (minutesSinceWake - middayEndMinutes).coerceAtMost(afternoonEndMinutes - middayEndMinutes)
            accumulatedWeight += afternoonMinutes * afternoonWeight
        }
        if (minutesSinceWake > afternoonEndMinutes) {
            val eveningMinutes = (minutesSinceWake - afternoonEndMinutes).coerceAtMost(activeDurationMinutes - afternoonEndMinutes)
            accumulatedWeight += eveningMinutes * eveningWeight
        }

        return (profile.dailyGoal * (accumulatedWeight / totalWeight)).roundToInt()
    }

    fun isWithinActiveHours(currentTime: LocalTime, wakeTime: LocalTime, sleepTime: LocalTime): Boolean {
        return if (sleepTime.isBefore(wakeTime)) {
            // Overnights schedule (e.g., 22:00 to 06:00)
            !currentTime.isBefore(wakeTime) || !currentTime.isAfter(sleepTime)
        } else {
            // Normal schedule (e.g., 08:00 to 22:00)
            !currentTime.isBefore(wakeTime) && !currentTime.isAfter(sleepTime)
        }
    }

    fun getActiveDurationMinutes(wakeTime: LocalTime, sleepTime: LocalTime): Int {
        return if (sleepTime.isBefore(wakeTime)) {
            val toMidnight = Duration.between(wakeTime, LocalTime.MAX).toMinutes().toInt() + 1
            val fromMidnight = Duration.between(LocalTime.MIN, sleepTime).toMinutes().toInt()
            toMidnight + fromMidnight
        } else {
            Duration.between(wakeTime, sleepTime).toMinutes().toInt()
        }
    }

    private fun getMinutesSinceWake(wakeTime: LocalTime, currentTime: LocalTime): Int {
        return if (currentTime.isBefore(wakeTime)) {
            // Only happens in overnight schedule when currentTime is after midnight but before sleepTime
            val toMidnight = Duration.between(wakeTime, LocalTime.MAX).toMinutes().toInt() + 1
            val fromMidnight = Duration.between(LocalTime.MIN, currentTime).toMinutes().toInt()
            toMidnight + fromMidnight
        } else {
            Duration.between(wakeTime, currentTime).toMinutes().toInt()
        }
    }

    /**
     * Determines the urgency and message for a reminder based on the hydration deficit.
     */
    fun getReminderStatus(expectedIntake: Int, actualIntake: Int): ReminderStatus {
        val deficit = expectedIntake - actualIntake
        if (deficit <= 0) return ReminderStatus.PASSIVE
        
        val deficitPercentage = if (expectedIntake > 0) (deficit.toFloat() / expectedIntake) else 0f

        return when {
            deficitPercentage >= 0.4 -> ReminderStatus.CRITICAL
            deficitPercentage > 0.1 -> ReminderStatus.NORMAL
            else -> ReminderStatus.PASSIVE
        }
    }

    /**
     * Determines the dynamic interval for the next reminder based on compliance.
     * Range: 20–90 minutes.
     */
    fun calculateNextReminderInterval(status: ReminderStatus, ignoreCount: Int): Int {
        val baseInterval = when (status) {
            ReminderStatus.CRITICAL -> 20
            ReminderStatus.NORMAL -> 45
            ReminderStatus.PASSIVE -> 90
        }

        val adjustment = (ignoreCount * 10).coerceAtMost(30)
        
        return (baseInterval + adjustment).coerceAtMost(90)
    }

    enum class ReminderStatus {
        CRITICAL, NORMAL, PASSIVE
    }
}
