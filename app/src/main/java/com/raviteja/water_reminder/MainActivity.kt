package com.raviteja.water_reminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (userProfile?.isFirstRun == false) {
                            EditorialBottomDock(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it }
                            )
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

@Composable
fun EditorialBottomDock(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple(0, Icons.Rounded.Home, "Home"),
                Triple(1, Icons.Rounded.History, "History"),
                Triple(2, Icons.Rounded.Settings, "Settings")
            )

            tabs.forEach { (index, icon, label) ->
                val isSelected = selectedTab == index
                val animatedBg by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "tabIndicator"
                )
                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "iconTint"
                )

                Surface(
                    onClick = { onTabSelected(index) },
                    shape = RoundedCornerShape(22.dp),
                    color = animatedBg,
                    modifier = Modifier.height(44.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = if (isSelected) 18.dp else 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                        if (isSelected) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = iconTint
                            )
                        }
                    }
                }
            }
        }
    }
}

