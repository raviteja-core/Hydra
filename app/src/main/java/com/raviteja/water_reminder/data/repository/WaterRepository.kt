package com.raviteja.water_reminder.data.repository

import com.raviteja.water_reminder.data.entity.WaterLog
import com.raviteja.water_reminder.data.local.WaterLogDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class WaterRepository(private val waterLogDao: WaterLogDao) {

    fun getTodayLogs(): Flow<List<WaterLog>> {
        val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return waterLogDao.getLogsForRange(startOfDay, endOfDay)
    }

    fun getLogsForDate(date: LocalDate): Flow<List<WaterLog>> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return waterLogDao.getLogsForRange(startOfDay, endOfDay)
    }

    fun getFullLogs(): Flow<List<WaterLog>> {
        return waterLogDao.getAllLogs()
    }

    suspend fun addLog(amount: Int, drinkType: String = "Water", hydrationFactor: Float = 1.0f, timestamp: Long = System.currentTimeMillis()) {
        if (amount <= 0) return
        waterLogDao.insertLog(WaterLog(
            amount = amount,
            drinkType = drinkType,
            hydrationFactor = hydrationFactor,
            timestamp = timestamp
        ))
    }

    suspend fun deleteLog(log: WaterLog) {
        waterLogDao.deleteLog(log)
    }

    suspend fun deleteAllLogs() {
        waterLogDao.deleteAll()
    }
}
