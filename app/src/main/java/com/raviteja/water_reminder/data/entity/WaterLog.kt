package com.raviteja.water_reminder.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val drinkType: String = "Water",
    val hydrationFactor: Float = 1.0f
)
