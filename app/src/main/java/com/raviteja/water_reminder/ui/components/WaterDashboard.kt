package com.raviteja.water_reminder.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raviteja.water_reminder.logic.QuotesProvider
import com.raviteja.water_reminder.viewmodel.WaterViewModel
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun WaterDashboard(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val totalIntake by viewModel.totalIntake.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val dailyGoal = userProfile.dailyGoal
    val unit = userProfile.unit
    val progress = if (dailyGoal > 0) (totalIntake.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f
    
    var showCustomDialog by remember { mutableStateOf(false) }
    val randomQuote = remember { QuotesProvider.getRandomQuote() }
    val streak by viewModel.currentStreak.collectAsState()
    val hydrationStatus by viewModel.hydrationStatus.collectAsState()
    val hydrationDeficit by viewModel.hydrationDeficit.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current

    var party by remember { mutableStateOf<List<Party>>(emptyList()) }

    LaunchedEffect(progress) {
        if (progress >= 1f && totalIntake > 0) {
            party = listOf(
                Party(
                    speed = 0f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def).map { it.toInt() },
                    position = Position.Relative(0.5, 0.3),
                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100)
                )
            )
            kotlinx.coroutines.delay(3000)
            party = emptyList()
        }
    }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Hello"
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "WaterProgress"
    )

    if (showCustomDialog) {
        CustomIntakeDialog(
            unit = unit,
            onDismiss = { showCustomDialog = false },
            onConfirm = { amount ->
                viewModel.addWaterIntake(amount)
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Battery Optimization Warning
            val powerManager = remember { context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager }
            val isOptimizing = remember { !powerManager.isIgnoringBatteryOptimizations(context.packageName) }
            
            if (isOptimizing && userProfile.remindersEnabled) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = android.content.Intent().apply {
                                action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                data = android.net.Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        },
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.BatteryAlert, contentDescription = null, tint = Color(0xFFFFA000))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reminders may be delayed",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap to disable battery optimization",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$greeting, ${userProfile.name}".uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Stay hydrated and healthy.",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = {
                        if (userProfile.remindersEnabled) {
                            com.raviteja.water_reminder.worker.ReminderScheduler.cancelReminders(context)
                            viewModel.updateProfile(userProfile.copy(remindersEnabled = false))
                        } else {
                            viewModel.updateProfile(userProfile.copy(remindersEnabled = true))
                            com.raviteja.water_reminder.worker.ReminderScheduler.scheduleReminders(context, 45)
                        }
                    }
                ) {
                    Icon(
                        if (userProfile.remindersEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                        contentDescription = "Toggle Reminders",
                        modifier = Modifier.padding(12.dp),
                        tint = if (userProfile.remindersEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main Circular Progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round,
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = lerp(
                        Color(0xFF00C2FF), // Deep Blue/Cyan
                        Color(0xFFB2EBF2), // Light Cyan
                        animatedProgress
                    ),
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 64.sp
                        )
                    )
                    Text(
                        text = "OF DAILY GOAL",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${viewModel.formatAmount(totalIntake, unit)} / ${viewModel.formatAmount(dailyGoal, unit)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Status and Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val statusColor = when (hydrationStatus) {
                    com.raviteja.water_reminder.logic.HydrationAlgorithm.ReminderStatus.CRITICAL -> MaterialTheme.colorScheme.error
                    com.raviteja.water_reminder.logic.HydrationAlgorithm.ReminderStatus.NORMAL -> Color(0xFFFBC02D)
                    com.raviteja.water_reminder.logic.HydrationAlgorithm.ReminderStatus.PASSIVE -> Color(0xFF4CAF50)
                }

                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "STATUS",
                    value = hydrationStatus.name,
                    icon = Icons.Rounded.Info,
                    color = statusColor
                )

                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "STREAK",
                    value = if (streak == 1) "1 Day" else "$streak Days",
                    icon = Icons.Rounded.Whatshot,
                    color = Color(0xFFFF5722)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hydration Deficit Info
            if (hydrationDeficit > 0 && progress < 1f) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "You are ${viewModel.formatAmount(hydrationDeficit, unit)} behind schedule.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Quick Add
            Text(
                text = "QUICK ADD",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.Start).padding(start = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val amounts = if (unit == "ml") listOf(150, 250, 500) else listOf(5, 8, 17)
                val labels = listOf("Glass", "Bottle", "Large")
                val icons = listOf(Icons.Rounded.LocalDrink, Icons.Rounded.WaterDrop, Icons.Rounded.Coffee)

                amounts.forEachIndexed { index, amount ->
                    DrinkTypeItem(
                        icon = icons[index],
                        amount = if (unit == "ml") "${amount}ml" else "${amount}oz",
                        label = labels[index]
                    ) { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (unit == "ml") viewModel.addWaterIntake(amount) 
                        else viewModel.addWaterIntake((amount * 29.57).toInt()) 
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quote Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.FormatQuote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "\"$randomQuote\"",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        FloatingActionButton(
            onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showCustomDialog = true 
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Custom", modifier = Modifier.size(28.dp))
        }

        if (party.isNotEmpty()) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = party
            )
        }
    }
}

@Composable
fun StatusCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(icon, contentDescription = null, modifier = Modifier.padding(6.dp), tint = color)
                }
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun DrinkTypeItem(icon: ImageVector, amount: String, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .width(95.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(amount, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
