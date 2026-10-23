package com.raviteja.water_reminder

import android.app.Application
import com.raviteja.water_reminder.data.local.WaterDatabase
import com.raviteja.water_reminder.data.repository.WaterRepository

class HydraApplication : Application() {
    val database by lazy { WaterDatabase.getDatabase(this) }
    val repository by lazy { WaterRepository(database.waterLogDao()) }
}
