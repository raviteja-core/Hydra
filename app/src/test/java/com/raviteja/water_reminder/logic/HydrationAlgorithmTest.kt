package com.raviteja.water_reminder.logic

import com.raviteja.water_reminder.data.datastore.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class HydrationAlgorithmTest {

    @Test
    fun `calculateDailyGoal should adjust for climate and activity`() {
        val baseProfile = UserProfile(weight = 70, climate = "Normal", activityLevel = "Medium", goalType = "Maintenance")
        // Base = 70 * 35 = 2450. Medium activity = +500. Total = 2950
        assertEquals(2950, HydrationAlgorithm.calculateDailyGoal(baseProfile))

        val hotProfile = baseProfile.copy(climate = "Hot")
        // 2950 + 750 = 3700
        assertEquals(3700, HydrationAlgorithm.calculateDailyGoal(hotProfile))

        val fitnessProfile = baseProfile.copy(goalType = "Fitness")
        // 2950 * 1.1 = 3245
        assertEquals(3245, HydrationAlgorithm.calculateDailyGoal(fitnessProfile))
    }

    @Test
    fun `isWithinActiveHours should handle normal schedule`() {
        val wake = LocalTime.of(8, 0)
        val sleep = LocalTime.of(22, 0)
        
        assertTrue(HydrationAlgorithm.isWithinActiveHours(LocalTime.of(8, 0), wake, sleep))
        assertTrue(HydrationAlgorithm.isWithinActiveHours(LocalTime.of(15, 0), wake, sleep))
        assertTrue(HydrationAlgorithm.isWithinActiveHours(LocalTime.of(22, 0), wake, sleep))
        assertFalse(HydrationAlgorithm.isWithinActiveHours(LocalTime.of(7, 59), wake, sleep))
        assertFalse(HydrationAlgorithm.isWithinActiveHours(LocalTime.of(22, 1), wake, sleep))
    }

    @Test
    fun `isWithinActiveHours should handle overnight schedule`() {
        val wake = LocalTime.of(22, 0)
        val sleep = LocalTime.of(6, 0)

        assertTrue(HydrationAlgorithm.isWithinActiveHours(LocalTime.of(23, 0), wake, sleep))
        assertTrue(HydrationAlgorithm.isWithinActiveHours(LocalTime.of(1, 0), wake, sleep))
        assertTrue(HydrationAlgorithm.isWithinActiveHours(LocalTime.of(6, 0), wake, sleep))
        assertTrue(HydrationAlgorithm.isWithinActiveHours(LocalTime.of(22, 0), wake, sleep))
        assertFalse(HydrationAlgorithm.isWithinActiveHours(LocalTime.of(21, 59), wake, sleep))
        assertFalse(HydrationAlgorithm.isWithinActiveHours(LocalTime.of(6, 1), wake, sleep))
    }

    @Test
    fun `calculateExpectedIntake should return 0 before wake time`() {
        val profile = UserProfile(dailyGoal = 2000, wakeTime = "08:00", sleepTime = "22:00")
        assertEquals(0, HydrationAlgorithm.calculateExpectedIntake(profile, LocalTime.of(7, 0)))
    }

    @Test
    fun `calculateExpectedIntake should return full goal after sleep time`() {
        val profile = UserProfile(dailyGoal = 2000, wakeTime = "08:00", sleepTime = "22:00")
        assertEquals(2000, HydrationAlgorithm.calculateExpectedIntake(profile, LocalTime.of(23, 0)))
    }

    @Test
    fun `calculateNextReminderInterval should respect ignore count`() {
        val status = HydrationAlgorithm.ReminderStatus.NORMAL // Base 45
        assertEquals(45, HydrationAlgorithm.calculateNextReminderInterval(status, 0))
        assertEquals(55, HydrationAlgorithm.calculateNextReminderInterval(status, 1))
        assertEquals(75, HydrationAlgorithm.calculateNextReminderInterval(status, 3))
        assertEquals(75, HydrationAlgorithm.calculateNextReminderInterval(status, 10)) // Max adjustment +30 = 75
    }
}
