package com.raviteja.water_reminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.raviteja.water_reminder.data.entity.WaterLog

@Database(entities = [WaterLog::class], version = 2, exportSchema = false)
abstract class WaterDatabase : RoomDatabase() {
    abstract fun waterLogDao(): WaterLogDao

    companion object {
        @Volatile
        private var INSTANCE: WaterDatabase? = null

        fun getDatabase(context: Context): WaterDatabase {
            val databaseContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                context.createDeviceProtectedStorageContext()
            } else {
                context
            }
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    databaseContext.applicationContext,
                    WaterDatabase::class.java,
                    "water_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
