package com.raviteja.water_reminder.ui.history

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raviteja.water_reminder.data.entity.WaterLog
import com.raviteja.water_reminder.ui.theme.EditorialSerif
import com.raviteja.water_reminder.viewmodel.WaterViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: WaterViewModel,
    modifier: Modifier = Modifier
) {
    val allLogs by viewModel.allLogs.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val completionRate by viewModel.weeklyCompletionRate.collectAsState()
    val weeklyData by viewModel.weeklyData.collectAsState()
    
    val weekData = remember(allLogs) { calculateCenterTodayWeekData(allLogs) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Timeline, 1: Logs

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        
        // Editorial Title
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(
                text = "HYDRATION METRICS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Hydration Journey",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = EditorialSerif
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Segmented Pill Control (Activity | Progress vibe from Reference)
        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Timeline", "Intake Logs").forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    val animBg by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                        label = "tabBg"
                    )
                    val animText by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "tabText"
                    )

                    Surface(
                        onClick = { selectedTab = index },
                        shape = RoundedCornerShape(20.dp),
                        color = animBg,
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = animText
                            )
                        }
                    }
                }
            }
        }

        // Discrete Dot-Matrix & Column Weekly Chart (Inspired by Reference Image 2)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "7-Day Rhythm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val today = remember { SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date()) }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = today,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 7 columns with dot matrix and amount indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weekData.forEach { dayInfo ->
                        val ratio = if (userProfile.dailyGoal > 0) {
                            (dayInfo.amount.toFloat() / userProfile.dailyGoal.toFloat()).coerceIn(0f, 1.2f)
                        } else 0f

                        val activeDots = when {
                            ratio >= 1.0f -> 4
                            ratio >= 0.75f -> 3
                            ratio >= 0.50f -> 2
                            ratio >= 0.25f -> 1
                            else -> 0
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            // 4 Discrete Matrix Dots
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                for (dotIndex in (3 downTo 0)) {
                                    val isFilled = dotIndex < activeDots
                                    val dotColor = when {
                                        isFilled && dayInfo.isToday -> MaterialTheme.colorScheme.primary
                                        isFilled -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = dotColor,
                                        modifier = Modifier.size(14.dp)
                                    ) {}
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Day Label with Today Highlight Indicator
                            if (dayInfo.isToday) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = dayInfo.dayLabel.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = dayInfo.dayLabel.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Stats Summary Grid (Inspired by Reference Image 2: Calories & Workout)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EditorialMetricCard(
                modifier = Modifier.weight(1f),
                title = "Daily Average",
                value = viewModel.formatAmountValue(weeklyData.average().toInt(), userProfile.unit),
                unit = userProfile.unit,
                icon = Icons.Rounded.LocalDrink,
                iconTint = MaterialTheme.colorScheme.primary
            )
            EditorialMetricCard(
                modifier = Modifier.weight(1f),
                title = "Completion",
                value = "${completionRate.toInt()}",
                unit = "%",
                icon = Icons.Rounded.PieChart,
                iconTint = MaterialTheme.colorScheme.secondary
            )
        }

        // Streak Card (Warm Coral Highlight Banner)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HYDRATION STREAK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (currentStreak == 1) "1 Day Active" else "$currentStreak Days Active",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "🔥",
                    fontSize = 24.sp
                )
            }
        }

        // Recent Logs List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT INTAKE LOGS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "${allLogs.size} Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (allLogs.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No hydration logs yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                allLogs.take(20).forEach { log ->
                    EditorialLogItem(log, viewModel, userProfile.unit)
                }
            }
        }
        
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun EditorialMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconTint: Color
) {
    Surface(
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.12f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = iconTint)
                    }
                }
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

@Composable
fun EditorialLogItem(log: WaterLog, viewModel: WaterViewModel, unit: String) {
    val dateString = try {
        val cal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
        val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        val hourIn12 = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute = cal.get(Calendar.MINUTE)
        String.format(Locale.getDefault(), "%02d:%02d %s", hourIn12, minute, amPm)
    } catch (e: Exception) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp))
    }
    
    val dayString = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(log.timestamp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = viewModel.formatAmount(log.amount, unit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$dayString • $dateString", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = { viewModel.deleteWaterLog(log) },
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

data class DayDrinkData(
    val dayLabel: String,
    val amount: Int,
    val isToday: Boolean
)

private fun calculateCenterTodayWeekData(logs: List<WaterLog>): List<DayDrinkData> {
    val result = mutableListOf<DayDrinkData>()
    
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val dateFormat = SimpleDateFormat("E", Locale.getDefault())

    for (i in -3..3) {
        val checkDay = Calendar.getInstance()
        checkDay.timeInMillis = today.timeInMillis
        checkDay.add(Calendar.DAY_OF_YEAR, i)
        
        val dayAmount = logs.filter {
            val logCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            logCal.set(Calendar.HOUR_OF_DAY, 0)
            logCal.set(Calendar.MINUTE, 0)
            logCal.set(Calendar.SECOND, 0)
            logCal.set(Calendar.MILLISECOND, 0)
            logCal.timeInMillis == checkDay.timeInMillis
        }.sumOf { (it.amount * it.hydrationFactor).toInt() }

        result.add(
            DayDrinkData(
                dayLabel = dateFormat.format(checkDay.time),
                amount = dayAmount,
                isToday = i == 0
            )
        )
    }
    
    return result
}
