package com.raviteja.water_reminder.ui.settings

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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raviteja.water_reminder.viewmodel.WaterViewModel

import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Contrast
import com.raviteja.water_reminder.worker.ReminderScheduler
import com.raviteja.water_reminder.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

import android.app.TimePickerDialog
import android.app.AlarmManager
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
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        // Name
        SettingsItem(title = "Account") {
            var nameText by remember(userProfile.name) { mutableStateOf(userProfile.name) }
            var isEditingName by remember { mutableStateOf(false) }
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditingName) {
                        OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
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
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Hydration Enthusiast", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { isEditingName = true }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit Name", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Schedule
        SettingsItem(title = "Schedule") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f).clickable { showTimePicker(true) }) {
                        Text("Wake Up", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(formatTime(userProfile.wakeTime), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    VerticalDivider(modifier = Modifier.height(40.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Column(modifier = Modifier.weight(1f).clickable { showTimePicker(false) }) {
                        Text("Sleep Time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(formatTime(userProfile.sleepTime), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Notifications
        SettingsItem(title = "Reminders") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("Smart Reminders", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
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
                            }
                        )
                    }

                    // Battery Optimization
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    
                    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
                    var hasExactAlarmPermission by remember { mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            alarmManager.canScheduleExactAlarms()
                        } else true
                    ) }

                    // Refresh permission state when screen is resumed
                    DisposableEffect(context) {
                        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    hasExactAlarmPermission = alarmManager.canScheduleExactAlarms()
                                }
                            }
                        }
                        // Note: This requires the context to be a LifecycleOwner, which is typical for Activities.
                        // For simplicity in this Compose setup, we'll assume it's an Activity context or handled by the parent.
                        // A more robust way would be using LocalLifecycleOwner.
                        (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle?.addObserver(observer)
                        onDispose {
                            (context as? androidx.lifecycle.LifecycleOwner)?.lifecycle?.removeObserver(observer)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasExactAlarmPermission) {
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
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Rounded.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Enable Exact Reminders", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text("Required for reliable notifications", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }

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
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ignore Battery Optimizations", fontWeight = FontWeight.SemiBold)
                            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                            val isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.packageName)
                            Text(
                                text = if (isIgnoring) "Optimization Disabled (Recommended)" else "Optimization Enabled (May delay reminders)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isIgnoring) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Sound Selection
                    if (userProfile.remindersEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Text("Notification Sound", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        val sounds = listOf(
                            "hydra_chime" to R.raw.hydra_chime,
                            "hydra_save" to R.raw.hydra_save,
                            "hydra_bell" to R.raw.hydra_bell
                        )

                        sounds.forEach { (name, resId) ->
                            val isSelected = userProfile.notificationSound == name
                            Surface(
                                onClick = { viewModel.updateProfile(userProfile.copy(notificationSound = name)) },
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.updateProfile(userProfile.copy(notificationSound = name)) }
                                    )
                                    Text(
                                        text = name.replace("hydra_", "").replaceFirstChar { it.uppercase() },
                                        modifier = Modifier.weight(1f),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    com.raviteja.water_reminder.worker.ReminderManager.handleReminder(context)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Send Test Reminder", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Units and Goal
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SettingsItem(title = "Units", modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ml", "oz").forEach { unit ->
                            val isSelected = userProfile.unit == unit
                            Surface(
                                modifier = Modifier.weight(1f).height(44.dp).clickable { viewModel.updateProfile(userProfile.copy(unit = unit)) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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

            SettingsItem(title = "Daily Goal", modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(24.dp),
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
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Personalization
        SettingsItem(title = "Personalization") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PersonalizationSelector(
                        title = "Activity Level",
                        options = listOf("Low", "Medium", "High"),
                        selected = userProfile.activityLevel,
                        description = when(userProfile.activityLevel) {
                            "Low" -> "Sedentary lifestyle."
                            "Medium" -> "Moderate daily activity."
                            "High" -> "Very active / Athlete."
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
                            "Cold" -> "Low temp, minimal sweat."
                            "Normal" -> "Temperate conditions."
                            "Hot" -> "High temp or humidity."
                            else -> ""
                        },
                        onSelected = { item ->
                            val updated = userProfile.copy(climate = item)
                            val newGoal = com.raviteja.water_reminder.logic.HydrationAlgorithm.calculateDailyGoal(updated)
                            viewModel.updateProfile(updated.copy(dailyGoal = newGoal))
                        }
                    )

                    PersonalizationSelector(
                        title = "Goal Type",
                        options = listOf("Maintenance", "Fitness", "Medical"),
                        selected = userProfile.goalType,
                        description = when(userProfile.goalType) {
                            "Maintenance" -> "Standard hydration."
                            "Fitness" -> "Performance focus (+10%)."
                            "Medical" -> "Consult your physician."
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
        SettingsItem(title = "Appearance") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val modes = listOf("System", "Light", "Dark")
                    modes.forEach { mode ->
                        val isSelected = userProfile.themeMode == mode
                        Surface(
                            onClick = { viewModel.updateProfile(userProfile.copy(themeMode = mode)) },
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 12.dp)
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
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset Data
        TextButton(
            onClick = { showResetDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Rounded.DeleteForever, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Reset All Data", fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(32.dp))
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
            title = { Text("Update Daily Goal") },
            text = {
                OutlinedTextField(
                    value = tempGoal,
                    onValueChange = { tempGoal = it },
                    label = { Text("Goal in ${userProfile.unit}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
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
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Data?") },
            text = { Text("This will permanently delete all your water logs and profile settings. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetData()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsItem(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(), 
            style = MaterialTheme.typography.labelMedium, 
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(Modifier.height(12.dp))
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
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            if (description.isNotBlank()) {
                Text(
                    text = " • $description",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
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
                    modifier = Modifier.weight(1f).height(40.dp).clickable { onSelected(option) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            option,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
