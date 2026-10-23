package com.raviteja.water_reminder.util

import com.raviteja.water_reminder.data.entity.WaterLog
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StatsUtilsTest {

    private val zoneId = ZoneId.of("UTC")

    @Test
    fun `calculateStreak should identify continuous days`() {
        val today = LocalDate.of(2023, 10, 10)
        val logs = listOf(
            WaterLog(amount = 2000, timestamp = today.atStartOfDay(zoneId).toInstant().toEpochMilli()),
            WaterLog(amount = 2000, timestamp = today.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()),
            WaterLog(amount = 2000, timestamp = today.minusDays(2).atStartOfDay(zoneId).toInstant().toEpochMilli())
        )

        assertEquals(3, StatsUtils.calculateStreak(logs, zoneId, today))
    }

    @Test
    fun `calculateStreak should return 0 if today and yesterday are missing`() {
        val today = LocalDate.of(2023, 10, 10)
        val logs = listOf(
            WaterLog(amount = 2000, timestamp = today.minusDays(2).atStartOfDay(zoneId).toInstant().toEpochMilli())
        )

        assertEquals(0, StatsUtils.calculateStreak(logs, zoneId, today))
    }

    @Test
    fun `calculateStreak should handle today missing but yesterday present`() {
        val today = LocalDate.of(2023, 10, 10)
        val logs = listOf(
            WaterLog(amount = 2000, timestamp = today.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()),
            WaterLog(amount = 2000, timestamp = today.minusDays(2).atStartOfDay(zoneId).toInstant().toEpochMilli())
        )

        assertEquals(2, StatsUtils.calculateStreak(logs, zoneId, today))
    }

    @Test
    fun `calculateWeeklyCompletionRate should work correctly`() {
        val today = LocalDate.of(2023, 10, 10)
        val dailyGoal = 2000
        val logs = listOf(
            WaterLog(amount = 2000, timestamp = today.atStartOfDay(zoneId).toInstant().toEpochMilli()),
            WaterLog(amount = 2000, timestamp = today.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()),
            WaterLog(amount = 1000, timestamp = today.minusDays(2).atStartOfDay(zoneId).toInstant().toEpochMilli())
        )

        // 2 out of 7 days met goal = 2/7 * 100 = 28.57...
        assertEquals(28.571428f, StatsUtils.calculateWeeklyCompletionRate(logs, dailyGoal, zoneId, today), 0.01f)
    }
}
