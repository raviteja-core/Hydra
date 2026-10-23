package com.raviteja.water_reminder.ui.history

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raviteja.water_reminder.data.entity.WaterLog
import com.raviteja.water_reminder.viewmodel.WaterViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.graphics.Brush

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

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(
                text = "Track your",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = "Hydration History",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Stats Summary Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatSmallCard(
                modifier = Modifier.weight(1f),
                title = "Avg. Intake",
                value = viewModel.formatAmountValue(weeklyData.average().toInt(), userProfile.unit),
                unit = userProfile.unit,
                icon = Icons.Rounded.LocalDrink,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
            StatSmallCard(
                modifier = Modifier.weight(1f),
                title = "Completion",
                value = "${completionRate.toInt()}",
                unit = "%",
                icon = Icons.Rounded.Insights,
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        }

        // Weekly Drinking Display Graph (Center Today)
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "WEEKLY PROGRESS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weekData.forEach { dayInfo ->
                        val progress = if (userProfile.dailyGoal > 0) {
                            (dayInfo.amount.toFloat() / userProfile.dailyGoal.toFloat()).coerceIn(0.1f, 1f)
                        } else 0.1f
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(180.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(progress)
                                        .background(
                                            brush = if (dayInfo.isToday) {
                                                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))
                                            } else {
                                                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)))
                                            },
                                            RoundedCornerShape(20.dp)
                                        )
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = dayInfo.dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (dayInfo.isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (dayInfo.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Streak Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF007AFF), Color(0xFF00C2FF))
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .clip(RoundedCornerShape(32.dp))
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ElectricBolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("CURRENT STREAK", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Text(
                    text = if (currentStreak == 1) "1 Day" else "$currentStreak Days",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Recent Logs List
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "RECENT LOGS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            if (allLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No logs yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                allLogs.take(20).forEach { log ->
                    LogItem(log, viewModel, userProfile.unit)
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun StatSmallCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(28.dp),
        color = containerColor
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(4.dp))
                Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
    }
}

@Composable
fun LogItem(log: WaterLog, viewModel: WaterViewModel, unit: String) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateString = try {
        val cal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
        val amPm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        val hourIn12 = cal.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute = cal.get(Calendar.MINUTE)
        String.format(Locale.getDefault(), "%02d:%02d %s", hourIn12, minute, amPm)
    } catch (e: Exception) {
        sdf.format(Date(log.timestamp))
    }
    
    val dayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dayString = dayFormat.format(Date(log.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    androidx.compose.material.icons.Icons.Rounded.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = viewModel.formatAmount(log.amount, unit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$dayString • $dateString", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            
            IconButton(
                onClick = { viewModel.deleteWaterLog(log) },
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f))
            ) {
                Icon(
                    androidx.compose.material.icons.Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
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
