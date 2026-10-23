package com.raviteja.water_reminder.util

import java.util.Locale

object UnitConverter {
    fun mlToOz(ml: Int): Float {
        return ml * Constants.ML_TO_OZ
    }

    fun ozToMl(oz: Float): Int {
        return (oz * Constants.OZ_TO_ML).toInt()
    }

    fun formatAmount(amountMl: Int, unit: String): String {
        return if (unit == Constants.UNIT_OZ) {
            "%.1f oz".format(Locale.US, mlToOz(amountMl))
        } else {
            "$amountMl ml"
        }
    }
    
    fun getQuickAddAmounts(unit: String): List<Int> {
        return if (unit == Constants.UNIT_OZ) {
            listOf(5, 8, 17) // approx 150ml, 250ml, 500ml
        } else {
            listOf(150, 250, 500)
        }
    }
}
