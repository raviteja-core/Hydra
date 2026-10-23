package com.raviteja.water_reminder.data.local

import androidx.room.*
import com.raviteja.water_reminder.data.entity.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_logs WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
    fun getLogsForRange(start: Long, end: Long): Flow<List<WaterLog>>

    @Query("SELECT SUM(amount) FROM water_logs WHERE timestamp >= :start AND timestamp <= :end")
    fun getTotalIntakeForRange(start: Long, end: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WaterLog)

    @Delete
    suspend fun deleteLog(log: WaterLog)

    @Query("DELETE FROM water_logs")
    suspend fun deleteAll()
}
