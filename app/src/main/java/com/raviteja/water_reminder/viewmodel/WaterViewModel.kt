package com.raviteja.water_reminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.raviteja.water_reminder.data.entity.WaterLog
import com.raviteja.water_reminder.data.repository.WaterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

import com.raviteja.water_reminder.data.datastore.UserProfile
import com.raviteja.water_reminder.data.datastore.UserPreferences
import com.raviteja.water_reminder.logic.HydrationAlgorithm
import com.raviteja.water_reminder.util.StatsUtils

class WaterViewModel(
    private val repository: WaterRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val userProfile: StateFlow<UserProfile> = userPreferences.userProfileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    val todayLogs: StateFlow<List<WaterLog>> = repository.getTodayLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLogs: StateFlow<List<WaterLog>> = repository.getFullLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalIntake: StateFlow<Int> = todayLogs.map { logs ->
        logs.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Statistics States
    val currentStreak: StateFlow<Int> = allLogs.map { calculateStreak(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val hydrationDeficit: StateFlow<Int> = combine(userProfile, totalIntake) { profile, intake ->
        val expected = com.raviteja.water_reminder.logic.HydrationAlgorithm.calculateExpectedIntake(profile, java.time.LocalTime.now())
        (expected - intake).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val hydrationStatus: StateFlow<com.raviteja.water_reminder.logic.HydrationAlgorithm.ReminderStatus> = combine(userProfile, totalIntake) { profile, intake ->
        val expected = com.raviteja.water_reminder.logic.HydrationAlgorithm.calculateExpectedIntake(profile, java.time.LocalTime.now())
        com.raviteja.water_reminder.logic.HydrationAlgorithm.getReminderStatus(expected, intake)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.raviteja.water_reminder.logic.HydrationAlgorithm.ReminderStatus.PASSIVE)

    val weeklyCompletionRate: StateFlow<Float> = combine(allLogs, userProfile) { logs, profile ->
        calculateWeeklyCompletionRate(logs, profile.dailyGoal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val weeklyData: StateFlow<List<Int>> = allLogs.map { calculateWeeklyData(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(7) { 0 })

    // Growth Logic
    val growthLevel: StateFlow<Int> = totalIntake.map { intake ->
        (intake / 500).coerceIn(0, 5) // Grow every 500ml up to level 5
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addWaterIntake(amount: Int) {
        viewModelScope.launch {
            repository.addLog(amount, "Water", 1.0f)
        }
    }

    fun deleteWaterLog(log: WaterLog) {
        viewModelScope.launch {
            repository.deleteLog(log)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            userPreferences.saveUserProfile(profile)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.deleteAllLogs()
            userPreferences.clearData()
        }
    }

    fun convertAmount(amountMl: Int, toUnit: String): Float {
        return if (toUnit == "oz") {
            amountMl * 0.033814f
        } else {
            amountMl.toFloat()
        }
    }

    fun formatAmount(amountMl: Int, unit: String): String {
        val converted = convertAmount(amountMl, unit)
        return if (unit == "oz") {
            "%.1f oz".format(converted)
        } else {
            "${amountMl} ml"
        }
    }

    fun formatAmountValue(amountMl: Int, unit: String): String {
        val converted = convertAmount(amountMl, unit)
        return if (unit == "oz") {
            "%.1f".format(converted)
        } else {
            "$amountMl"
        }
    }

    private fun calculateStreak(logs: List<WaterLog>): Int {
        return StatsUtils.calculateStreak(logs)
    }

    private fun calculateWeeklyCompletionRate(logs: List<WaterLog>, dailyGoal: Int): Float {
        return StatsUtils.calculateWeeklyCompletionRate(logs, dailyGoal)
    }

    private fun calculateWeeklyData(logs: List<WaterLog>): List<Int> {
        return StatsUtils.calculateWeeklyData(logs)
    }

    class Factory(
        private val repository: WaterRepository,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WaterViewModel(repository, userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
