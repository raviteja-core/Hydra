package com.raviteja.water_reminder.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserProfile(
    val name: String = "User",
    val gender: String = "Male",
    val age: Int = 25,
    val weight: Int = 70,
    val wakeTime: String = "08:00",
    val sleepTime: String = "22:00",
    val dailyGoal: Int = 2500,
    val isFirstRun: Boolean = true,
    val unit: String = "ml",
    val themeMode: String = "System", // System, Light, Dark
    val remindersEnabled: Boolean = true,
    val notificationSound: String = "hydra_chime",
    val activityLevel: String = "Medium", // Low, Medium, High
    val climate: String = "Normal", // Cold, Normal, Hot
    val goalType: String = "Maintenance", // Maintenance, Fitness, Medical
    val lastNotificationTime: Long = 0L,
    val ignoreCount: Int = 0
)

class UserPreferences(context: Context) {

    private val dataStoreContext = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        context.createDeviceProtectedStorageContext()
    } else {
        context
    }

    companion object {
        val NAME = stringPreferencesKey("name")
        val GENDER = stringPreferencesKey("gender")
        val AGE = intPreferencesKey("age")
        val WEIGHT = intPreferencesKey("weight")
        val WAKE_TIME = stringPreferencesKey("wake_time")
        val SLEEP_TIME = stringPreferencesKey("sleep_time")
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
        val UNIT = stringPreferencesKey("unit")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val NOTIFICATION_SOUND = stringPreferencesKey("notification_sound")
        val ACTIVITY_LEVEL = stringPreferencesKey("activity_level")
        val CLIMATE = stringPreferencesKey("climate")
        val GOAL_TYPE = stringPreferencesKey("goal_type")
        val LAST_NOTIFICATION_TIME = longPreferencesKey("last_notification_time")
        val IGNORE_COUNT = intPreferencesKey("ignore_count")
    }

    val userProfileFlow: Flow<UserProfile> = dataStoreContext.dataStore.data.map { preferences ->
        UserProfile(
            name = preferences[NAME] ?: "User",
            gender = preferences[GENDER] ?: "Male",
            age = preferences[AGE] ?: 25,
            weight = preferences[WEIGHT] ?: 70,
            wakeTime = preferences[WAKE_TIME] ?: "08:00",
            sleepTime = preferences[SLEEP_TIME] ?: "22:00",
            dailyGoal = preferences[DAILY_GOAL] ?: 2500,
            isFirstRun = preferences[IS_FIRST_RUN] ?: true,
            unit = preferences[UNIT] ?: "ml",
            themeMode = preferences[THEME_MODE] ?: "System",
            remindersEnabled = preferences[REMINDERS_ENABLED] ?: true,
            notificationSound = preferences[NOTIFICATION_SOUND] ?: "hydra_chime",
            activityLevel = preferences[ACTIVITY_LEVEL] ?: "Medium",
            climate = preferences[CLIMATE] ?: "Normal",
            goalType = preferences[GOAL_TYPE] ?: "Maintenance",
            lastNotificationTime = preferences[LAST_NOTIFICATION_TIME] ?: 0L,
            ignoreCount = preferences[IGNORE_COUNT] ?: 0
        )
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        dataStoreContext.dataStore.edit { preferences ->
            preferences[NAME] = profile.name
            preferences[GENDER] = profile.gender
            preferences[AGE] = profile.age
            preferences[WEIGHT] = profile.weight
            preferences[WAKE_TIME] = profile.wakeTime
            preferences[SLEEP_TIME] = profile.sleepTime
            preferences[DAILY_GOAL] = profile.dailyGoal
            preferences[IS_FIRST_RUN] = profile.isFirstRun
            preferences[UNIT] = profile.unit
            preferences[THEME_MODE] = profile.themeMode
            preferences[REMINDERS_ENABLED] = profile.remindersEnabled
            preferences[NOTIFICATION_SOUND] = profile.notificationSound
            preferences[ACTIVITY_LEVEL] = profile.activityLevel
            preferences[CLIMATE] = profile.climate
            preferences[GOAL_TYPE] = profile.goalType
            preferences[LAST_NOTIFICATION_TIME] = profile.lastNotificationTime
            preferences[IGNORE_COUNT] = profile.ignoreCount
        }
    }

    suspend fun clearData() {
        dataStoreContext.dataStore.edit { it.clear() }
    }
}
