package com.raviteja.water_reminder.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raviteja.water_reminder.logic.HydrationAlgorithm
import com.raviteja.water_reminder.logic.QuotesProvider
import com.raviteja.water_reminder.ui.theme.EditorialSerif
import com.raviteja.water_reminder.ui.theme.SageGreen
import com.raviteja.water_reminder.ui.theme.WarmCoral
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
    val context = LocalContext.current

    var party by remember { mutableStateOf<List<Party>>(emptyList()) }

    LaunchedEffect(progress) {
        if (progress >= 1f && totalIntake > 0) {
            party = listOf(
                Party(
                    speed = 0f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    colors = listOf(0xFFFFA07A.toInt(), 0xFFFF6955.toInt(), 0xFF86A391.toInt(), 0xFFF5F2EB.toInt()),
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Battery Optimization Warning Banner (Subtle & Elegant)
            val powerManager = remember { context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager }
            val isOptimizing = remember { !powerManager.isIgnoringBatteryOptimizations(context.packageName) }
            
            if (isOptimizing && userProfile.remindersEnabled) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = android.content.Intent().apply {
                                action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                data = android.net.Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Rounded.BatteryAlert,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reminders may be delayed",
                                style = MaterialTheme.typography.labelMedium,
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

            // Top Header: User Profile Badge + Notification Bell
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Avatar Initial
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(46.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = userProfile.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "$greeting,".uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile.name,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = EditorialSerif
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Bell Quick Toggle
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (userProfile.remindersEnabled) Icons.Rounded.Notifications else Icons.Rounded.NotificationsOff,
                            contentDescription = "Toggle Reminders",
                            modifier = Modifier.size(20.dp),
                            tint = if (userProfile.remindersEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Editorial Mindful Hydration Banner (Inspired by Image 1 & 3)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Rhythm",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Stay in tune with your hydration.",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = EditorialSerif,
                                    fontSize = 22.sp,
                                    lineHeight = 28.sp
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.WaterDrop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "\"$randomQuote\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                    )

                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val quickAmount = if (unit == "ml") 250 else 8
                            if (unit == "ml") viewModel.addWaterIntake(quickAmount)
                            else viewModel.addWaterIntake((quickAmount * 29.57).toInt())
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            contentColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (unit == "ml") "Quick Log +250ml" else "Quick Log +8oz",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Hero Circular Progress Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 28.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(240.dp)
                    ) {
                        // Background Track
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 14.dp,
                            strokeCap = StrokeCap.Round
                        )
                        // Active Progress Arc with Warm Coral
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 14.dp,
                            strokeCap = StrokeCap.Round
                        )

                        // Centered Data
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 54.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "OF DAILY GOAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.2.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pill Badge Ratio
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = viewModel.formatAmount(totalIntake, unit),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " / ${viewModel.formatAmount(dailyGoal, unit)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status and Streak Cards (Side by Side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val statusText = hydrationStatus.name
                val statusColor = when (hydrationStatus) {
                    HydrationAlgorithm.ReminderStatus.CRITICAL -> MaterialTheme.colorScheme.primary
                    HydrationAlgorithm.ReminderStatus.NORMAL -> Color(0xFFE5A93C)
                    HydrationAlgorithm.ReminderStatus.PASSIVE -> MaterialTheme.colorScheme.secondary
                }

                EditorialStatCard(
                    modifier = Modifier.weight(1f),
                    subtitle = "RHYTHM",
                    value = statusText,
                    icon = Icons.Rounded.BubbleChart,
                    accentColor = statusColor
                )

                EditorialStatCard(
                    modifier = Modifier.weight(1f),
                    subtitle = "STREAK",
                    value = if (streak == 1) "1 Day" else "$streak Days",
                    icon = Icons.Rounded.LocalFireDepartment,
                    accentColor = MaterialTheme.colorScheme.primary
                )
            }

            // Behind schedule warning if any
            if (hydrationDeficit > 0 && progress < 1f) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "You are ${viewModel.formatAmount(hydrationDeficit, unit)} behind your expected intake schedule.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Quick Add Section (Categories Inspired by Reference)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUICK ADD",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    onClick = { showCustomDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Custom",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Quick Add Category Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val amounts = if (unit == "ml") listOf(150, 250, 500) else listOf(5, 8, 17)
                val labels = listOf("Glass", "Bottle", "Flask")
                val icons = listOf(Icons.Rounded.LocalDrink, Icons.Rounded.WaterDrop, Icons.Rounded.Coffee)

                amounts.forEachIndexed { index, amount ->
                    EditorialDrinkTile(
                        modifier = Modifier.weight(1f),
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

            Spacer(modifier = Modifier.height(100.dp))
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
fun EditorialStatCard(
    modifier: Modifier,
    subtitle: String,
    value: String,
    icon: ImageVector,
    accentColor: Color
) {
    Surface(
        modifier = modifier.height(98.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.14f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = accentColor)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun EditorialDrinkTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    amount: String,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
