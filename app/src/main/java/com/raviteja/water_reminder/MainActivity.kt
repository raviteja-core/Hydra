package com.raviteja.water_reminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raviteja.water_reminder.data.local.WaterDatabase
import com.raviteja.water_reminder.data.repository.WaterRepository
import com.raviteja.water_reminder.ui.components.WaterDashboard
import com.raviteja.water_reminder.ui.theme.Water_reminderTheme
import com.raviteja.water_reminder.viewmodel.WaterViewModel
import com.raviteja.water_reminder.data.datastore.UserPreferences
import com.raviteja.water_reminder.ui.onboarding.OnboardingScreen
import com.raviteja.water_reminder.ui.history.HistoryScreen
import com.raviteja.water_reminder.ui.settings.SettingsScreen
import com.raviteja.water_reminder.worker.ReminderScheduler
import com.raviteja.water_reminder.worker.NotificationHelper
import androidx.work.ExistingWorkPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = WaterDatabase.getDatabase(this)
        val repository = WaterRepository(database.waterLogDao())
        val userPreferences = UserPreferences(this)
        
        val viewModel: WaterViewModel by viewModels {
            WaterViewModel.Factory(repository, userPreferences)
        }

        enableEdgeToEdge()
        setContent {
            val userProfile by userPreferences.userProfileFlow.collectAsState(initial = null)
            val systemDark = isSystemInDarkTheme()
            val isDark = when(userProfile?.themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> systemDark
            }
            
            Water_reminderTheme(darkTheme = isDark) {
                var selectedTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (userProfile?.isFirstRun == false) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                tonalElevation = 0.dp
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
                                    label = { Text("HOME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.Rounded.History, contentDescription = "History") },
                                    label = { Text("HISTORY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                                    label = { Text("SETTINGS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    when {
                        userProfile == null -> {
                            // Loading state
                        }
                        userProfile?.isFirstRun == true -> {
                            OnboardingScreen(
                                onComplete = { profile ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        userPreferences.saveUserProfile(profile)
                                        if (profile.remindersEnabled) {
                                            NotificationHelper.sendWelcomeNotification(this@MainActivity)
                                            ReminderScheduler.scheduleReminders(this@MainActivity, 30) // First reminder in 30 mins
                                        }
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        else -> {
                            // Ensure reminders are active.
                            // We use KEEP here so that opening the app doesn't reset 
                            // a timer that is already ticking down.
                            LaunchedEffect(userProfile?.remindersEnabled) {
                                if (userProfile?.remindersEnabled == true) {
                                    ReminderScheduler.scheduleReminders(this@MainActivity, 60, ExistingWorkPolicy.KEEP)
                                }
                            }

                            when (selectedTab) {
                                0 -> WaterDashboard(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                                1 -> HistoryScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                                2 -> SettingsScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
