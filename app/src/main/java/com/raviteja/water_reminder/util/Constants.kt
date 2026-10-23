package com.raviteja.water_reminder.util

object Constants {
    const val UNIT_ML = "ml"
    const val UNIT_OZ = "oz"
    
    const val ML_TO_OZ = 0.033814f
    const val OZ_TO_ML = 29.5735f

    const val DEFAULT_GOAL_ML = 2500
    const val MIN_GOAL_ML = 500
    const val MAX_GOAL_ML = 10000

    const val NOTIFICATION_ID = 1001
    const val ALARM_REQUEST_CODE = 1002
    
    const val ACTION_DRINK_250 = "com.raviteja.water_reminder.ACTION_DRINK_250"
    const val ACTION_DRINK_500 = "com.raviteja.water_reminder.ACTION_DRINK_500"
    const val ACTION_SNOOZE = "com.raviteja.water_reminder.ACTION_SNOOZE"
}
