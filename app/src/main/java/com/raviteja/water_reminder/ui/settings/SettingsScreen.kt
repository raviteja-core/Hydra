package com.raviteja.water_reminder.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raviteja.water_reminder.R
import com.raviteja.water_reminder.viewmodel.WaterViewModel
import com.raviteja.water_reminder.worker.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import android.Manifest
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showEditGoalDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val formatTime = { timeStr: String ->
        try {
            val parts = timeStr.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
            val hourIn12 = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
            String.format(Locale.getDefault(), "%02d:%02d %s", hourIn12, minute, amPm)
        } catch (e: Exception) {
            timeStr
        }
    }

    val showTimePicker = { isWake: Boolean ->
        val currentTime = if (isWake) userProfile.wakeTime else userProfile.sleepTime
        val parts = currentTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        TimePickerDialog(context, { _, h, m ->
            val formatted = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            if (isWake) {
                viewModel.updateProfile(userProfile.copy(wakeTime = formatted))
            } else {
                viewModel.updateProfile(userProfile.copy(sleepTime = formatted))
            }
        }, hour, minute, false).show()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.updateProfile(userProfile.copy(remindersEnabled = true))
            ReminderScheduler.scheduleReminders(context, 45)
        } else {
            viewModel.updateProfile(userProfile.copy(remindersEnabled = false))
            ReminderScheduler.cancelReminders(context)
        }
    }

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    val playSound = { soundResource: Int ->
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, soundResource)
        mediaPlayer?.start()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        
        // Editorial Header
        Column {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Personalize your rhythm, sound & goals",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Account / Name
        SettingsSection(title = "Account") {
            var nameText by remember(userProfile.name) { mutableStateOf(userProfile.name) }
            var isEditingName by remember { mutableStateOf(false) }
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditingName) {
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        )
                        IconButton(onClick = { 
                            viewModel.updateProfile(userProfile.copy(name = nameText))
                            isEditingName = false 
                        }) {
                            Icon(Icons.Rounded.Check, contentDescription = "Save Name", tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Hydration Enthusiast",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { isEditingName = true }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit Name", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }

        // Schedule
        SettingsSection(title = "Daily Window") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTimePicker(true) }
                    ) {
                        Text(
                            "WAKE TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatTime(userProfile.wakeTime),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier.height(36.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTimePicker(false) }
                    ) {
                        Text(
                            "SLEEP TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            formatTime(userProfile.sleepTime),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Reminders
        SettingsSection(title = "Reminders & Alerts") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Smart Reminders", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                            Text("Adaptive nudges between wake & sleep", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = userProfile.remindersEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        when (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                                            PackageManager.PERMISSION_GRANTED -> {
                                                viewModel.updateProfile(userProfile.copy(remindersEnabled = true))
                                                ReminderScheduler.scheduleReminders(context, 45)
                                            }
                                            else -> {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        }
                                    } else {
                                        viewModel.updateProfile(userProfile.copy(remindersEnabled = true))
                                        ReminderScheduler.scheduleReminders(context, 45)
                                    }
                                } else {
                                    viewModel.updateProfile(userProfile.copy(remindersEnabled = false))
                                    ReminderScheduler.cancelReminders(context)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }

                    // Exact Alarm Check
                    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
                    var hasExactAlarmPermission by remember { mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            alarmManager.canScheduleExactAlarms()
                        } else true
                    ) }

                    DisposableEffect(context) {
                        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    hasExactAlarmPermission = alarmManager.canScheduleExactAlarms()
                                }
                            }
                        }
                        (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle?.addObserver(observer)
                        onDispose {
                            (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle?.removeObserver(observer)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasExactAlarmPermission) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val intent = Intent().apply {
                                    action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Enable Exact Reminders", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                                Text("Required for punctual hydration nudges", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                            }
                            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    // Battery Optimization
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val intent = Intent().apply {
                                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Background Reliability", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                            val isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.packageName)
                            Text(
                                text = if (isIgnoring) "Optimized for continuous delivery" else "May be delayed by OS battery saver",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isIgnoring) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Sound Selection
                    if (userProfile.remindersEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Text(
                            "CHIME SOUND",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        val sounds = listOf(
                            "hydra_chime" to R.raw.hydra_chime,
                            "hydra_save" to R.raw.hydra_save,
                            "hydra_bell" to R.raw.hydra_bell
                        )

                        sounds.forEach { (name, resId) ->
                            val isSelected = userProfile.notificationSound == name
                            Surface(
                                onClick = { viewModel.updateProfile(userProfile.copy(notificationSound = name)) },
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp),
                                border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 12.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.updateProfile(userProfile.copy(notificationSound = name)) },
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                    Text(
                                        text = name.replace("hydra_", "").replaceFirstChar { it.uppercase() },
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    IconButton(onClick = { playSound(resId) }) {
                                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Play sound", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = {
                                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                    com.raviteja.water_reminder.worker.ReminderManager.handleReminder(context)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Send Test Reminder", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Units and Goal (Row)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SettingsSection(title = "Units", modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth().height(76.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("ml", "oz").forEach { unit ->
                            val isSelected = userProfile.unit == unit
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { viewModel.updateProfile(userProfile.copy(unit = unit)) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        unit.uppercase(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SettingsSection(title = "Daily Goal", modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth().height(76.dp),
                    onClick = { showEditGoalDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = viewModel.formatAmount(userProfile.dailyGoal, userProfile.unit),
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    }
                }
            }
        }

        // Personalization
        SettingsSection(title = "Metabolic Personalization") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PersonalizationSelector(
                        title = "Activity Level",
                        options = listOf("Low", "Medium", "High"),
                        selected = userProfile.activityLevel,
                        description = when(userProfile.activityLevel) {
                            "Low" -> "Sedentary lifestyle"
                            "Medium" -> "Moderate daily motion"
                            "High" -> "Active athlete"
                            else -> ""
                        },
                        onSelected = { level ->
                            val updated = userProfile.copy(activityLevel = level)
                            val newGoal = com.raviteja.water_reminder.logic.HydrationAlgorithm.calculateDailyGoal(updated)
                            viewModel.updateProfile(updated.copy(dailyGoal = newGoal))
                        }
                    )
                    
                    PersonalizationSelector(
                        title = "Climate",
                        options = listOf("Cold", "Normal", "Hot"),
                        selected = userProfile.climate,
                        description = when(userProfile.climate) {
                            "Cold" -> "Low temp, minimal sweat"
                            "Normal" -> "Temperate conditions"
                            "Hot" -> "Elevated transpiration"
                            else -> ""
                        },
                        onSelected = { item ->
                            val updated = userProfile.copy(climate = item)
                            val newGoal = com.raviteja.water_reminder.logic.HydrationAlgorithm.calculateDailyGoal(updated)
                            viewModel.updateProfile(updated.copy(dailyGoal = newGoal))
                        }
                    )

                    PersonalizationSelector(
                        title = "Hydration Strategy",
                        options = listOf("Maintenance", "Fitness", "Medical"),
                        selected = userProfile.goalType,
                        description = when(userProfile.goalType) {
                            "Maintenance" -> "Balanced daily wellness"
                            "Fitness" -> "Performance boost (+10%)"
                            "Medical" -> "Consult clinical guidance"
                            else -> ""
                        },
                        onSelected = { item ->
                            val updated = userProfile.copy(goalType = item)
                            val newGoal = com.raviteja.water_reminder.logic.HydrationAlgorithm.calculateDailyGoal(updated)
                            viewModel.updateProfile(updated.copy(dailyGoal = newGoal))
                        }
                    )
                }
            }
        }

        // Appearance
        SettingsSection(title = "Appearance & Theme") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val modes = listOf("System", "Light", "Dark")
                    modes.forEach { mode ->
                        val isSelected = userProfile.themeMode == mode
                        Surface(
                            onClick = { viewModel.updateProfile(userProfile.copy(themeMode = mode)) },
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 14.dp)
                            ) {
                                Icon(
                                    imageVector = when(mode) {
                                        "Light" -> Icons.Rounded.LightMode
                                        "Dark" -> Icons.Rounded.DarkMode
                                        else -> Icons.Rounded.Contrast
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    text = mode,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isSelected) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Danger Zone: Reset Data
        Surface(
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth().clickable { showResetDialog = true }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Reset All Data & Logs", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
            }
        }
        
        Spacer(Modifier.height(48.dp))
    }

    if (showEditGoalDialog) {
        val initialGoal = if (userProfile.unit == "oz") {
            String.format(Locale.US, "%.1f", userProfile.dailyGoal * 0.033814f)
        } else {
            userProfile.dailyGoal.toString()
        }
        var tempGoal by remember(userProfile.dailyGoal, userProfile.unit) { mutableStateOf(initialGoal) }
        AlertDialog(
            onDismissRequest = { showEditGoalDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(28.dp),
            title = {
                Column {
                    Text(
                        text = "Target Volume",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Update Daily Goal",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                OutlinedTextField(
                    value = tempGoal,
                    onValueChange = { tempGoal = it },
                    label = { Text("Goal in ${userProfile.unit.uppercase()}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val inputVal = tempGoal.toFloatOrNull()
                        if (inputVal != null && inputVal > 0) {
                            val goalInMl = if (userProfile.unit == "oz") {
                                (inputVal * 29.5735f).toInt()
                            } else {
                                inputVal.toInt()
                            }
                            if (goalInMl > 0) {
                                viewModel.updateProfile(userProfile.copy(dailyGoal = goalInMl))
                            }
                        }
                        showEditGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditGoalDialog = false },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Reset All Data?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all water intake logs, streaks, and personalization settings. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Reset Everything", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(), 
            style = MaterialTheme.typography.labelSmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
fun PersonalizationSelector(
    title: String,
    options: List<String>,
    selected: String,
    description: String = "",
    onSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            if (description.isNotBlank()) {
                Text(
                    text = " • $description",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clickable { onSelected(option) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            option,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
