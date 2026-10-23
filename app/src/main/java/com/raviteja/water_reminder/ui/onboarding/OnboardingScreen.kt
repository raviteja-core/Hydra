package com.raviteja.water_reminder.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raviteja.water_reminder.data.datastore.UserProfile

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import java.util.Locale

@Composable
fun OnboardingScreen(
    onComplete: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var age by remember { mutableStateOf("25") }
    var weight by remember { mutableStateOf("70") }
    var wakeTime by remember { mutableStateOf("08:00") }
    var sleepTime by remember { mutableStateOf("22:00") }
    var remindersEnabled by remember { mutableStateOf(true) }
    
    var activityLevel by remember { mutableStateOf("Medium") }
    var climate by remember { mutableStateOf("Normal") }
    var goalType by remember { mutableStateOf("Maintenance") }

    val formatTime = { hour: Int, minute: Int ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        val hourIn12 = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        String.format(Locale.getDefault(), "%02d:%02d %s", hourIn12, minute, amPm)
    }

    val showTimePicker = { isWake: Boolean ->
        val currentTime = if (isWake) wakeTime else sleepTime
        val parts = currentTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        TimePickerDialog(context, { _, h, m ->
            val formatted = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            if (isWake) wakeTime = formatted else sleepTime = formatted
        }, hour, minute, false).show()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        remindersEnabled = isGranted
        completeOnboarding(name, gender, age, weight, wakeTime, sleepTime, activityLevel, climate, goalType, remindersEnabled, onComplete)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "HYDRATION RITUAL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Welcome to Hydra",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Personalize your biological profile to calibrate optimal hydration intervals.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Name
        OnboardingTextField(
            value = name,
            onValueChange = { name = it },
            label = "Your Name",
            placeholder = "e.g. Alex"
        )

        // Gender Selection
        OnboardingSelector(
            title = "Gender",
            options = listOf("Male", "Female", "Other"),
            selectedOption = gender,
            onOptionSelected = { gender = it }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Age
            OnboardingTextField(
                value = age,
                onValueChange = { age = it },
                label = "Age",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )

            // Weight
            OnboardingTextField(
                value = weight,
                onValueChange = { weight = it },
                label = "Weight (kg)",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        // Times
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val wakeParts = wakeTime.split(":")
            OnboardingTextField(
                value = formatTime(wakeParts[0].toInt(), wakeParts[1].toInt()),
                onValueChange = { },
                label = "Wake Time",
                readOnly = true,
                modifier = Modifier.weight(1f),
                onClick = { showTimePicker(true) }
            )
            val sleepParts = sleepTime.split(":")
            OnboardingTextField(
                value = formatTime(sleepParts[0].toInt(), sleepParts[1].toInt()),
                onValueChange = { },
                label = "Sleep Time",
                readOnly = true,
                modifier = Modifier.weight(1f),
                onClick = { showTimePicker(false) }
            )
        }

        // Activity Level
        OnboardingSelector(
            title = "Activity Level",
            options = listOf("Low", "Medium", "High"),
            selectedOption = activityLevel,
            onOptionSelected = { activityLevel = it },
            description = when(activityLevel) {
                "Low" -> "Sedentary lifestyle, mostly desk work"
                "Medium" -> "Moderate daily exercise or walking"
                "High" -> "Intense training or active physical labor"
                else -> ""
            }
        )

        // Climate
        OnboardingSelector(
            title = "Climate",
            options = listOf("Cold", "Normal", "Hot"),
            selectedOption = climate,
            onOptionSelected = { climate = it },
            description = when(climate) {
                "Cold" -> "Low temperature, low sweat"
                "Normal" -> "Temperate conditions"
                "Hot" -> "Warm or humid, elevated perspiration"
                else -> ""
            }
        )

        // Goal Type
        OnboardingSelector(
            title = "Hydration Strategy",
            options = listOf("Maintenance", "Fitness", "Medical"),
            selectedOption = goalType,
            onOptionSelected = { goalType = it },
            description = when(goalType) {
                "Maintenance" -> "General daily vitality & focus"
                "Fitness" -> "Athletic performance (+10%)"
                "Medical" -> "Clinical hydration guidelines"
                else -> ""
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (remindersEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    completeOnboarding(name, gender, age, weight, wakeTime, sleepTime, activityLevel, climate, goalType, remindersEnabled, onComplete)
                }
            },
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Text("Begin Ritual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        
        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = readOnly,
                enabled = enabled,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            )
            if (onClick != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(onClick = onClick)
                )
            }
        }
    }
}

@Composable
fun OnboardingSelector(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    description: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        
        if (description.isNotBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clickable { onOptionSelected(option) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = option,
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

private fun completeOnboarding(
    name: String,
    gender: String,
    age: String,
    weight: String,
    wakeTime: String,
    sleepTime: String,
    activityLevel: String,
    climate: String,
    goalType: String,
    remindersEnabled: Boolean,
    onComplete: (UserProfile) -> Unit
) {
    val weightInt = (weight.toIntOrNull() ?: 70).coerceIn(20, 300)
    val ageInt = (age.toIntOrNull() ?: 25).coerceIn(10, 120)
    val tempProfile = UserProfile(
        name = if (name.isBlank()) "User" else name.trim(),
        gender = gender,
        age = ageInt,
        weight = weightInt,
        wakeTime = wakeTime,
        sleepTime = sleepTime,
        activityLevel = activityLevel,
        climate = climate,
        goalType = goalType,
        isFirstRun = false,
        remindersEnabled = remindersEnabled
    )
    val dailyGoal = com.raviteja.water_reminder.logic.HydrationAlgorithm.calculateDailyGoal(tempProfile)
    onComplete(tempProfile.copy(dailyGoal = dailyGoal))
}
