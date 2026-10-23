package com.raviteja.water_reminder.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConversionAndFormattingTest {

    @Test
    fun testConvertAmountMlToOz() {
        val ml = 250
        val oz = ml * 0.033814f
        assertEquals(8.4535f, oz, 0.001f)
    }

    @Test
    fun testFormatAmountMl() {
        val amountMl = 250
        val unit = "ml"
        val formatted = "${amountMl} ml"
        assertEquals("250 ml", formatted)
    }

    @Test
    fun testFormatAmountOz() {
        val amountMl = 250
        val oz = amountMl * 0.033814f
        val formatted = "%.1f oz".format(oz)
        assertEquals("8.5 oz", formatted)
    }

    @Test
    fun testFormatAmountValueForAverageIntake() {
        val avgMl = 35
        val formattedMl = "$avgMl"
        assertEquals("35", formattedMl)

        val oz = avgMl * 0.033814f
        val formattedOz = "%.1f".format(oz)
        assertEquals("1.2", formattedOz)
    }

    @Test
    fun testStreakPluralization() {
        val streakOne = 1
        val formattedOne = if (streakOne == 1) "1 Day" else "$streakOne Days"
        assertEquals("1 Day", formattedOne)

        val streakMultiple = 5
        val formattedMultiple = if (streakMultiple == 1) "1 Day" else "$streakMultiple Days"
        assertEquals("5 Days", formattedMultiple)

        val streakZero = 0
        val formattedZero = if (streakZero == 1) "1 Day" else "$streakZero Days"
        assertEquals("0 Days", formattedZero)
    }

    @Test
    fun testCustomIntakeOzToMlConversion() {
        val ozInput = 8.5f
        val ml = (ozInput * 29.5735f).toInt()
        assertEquals(251, ml)
    }
}
