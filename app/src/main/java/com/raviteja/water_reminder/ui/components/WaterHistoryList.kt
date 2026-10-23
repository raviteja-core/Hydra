package com.raviteja.water_reminder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raviteja.water_reminder.data.entity.WaterLog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterHistoryList(
    logs: List<WaterLog>,
    onDelete: (WaterLog) -> Unit,
    modifier: Modifier = Modifier,
    unit: String = "ml"
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Today's History",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        if (logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No logs yet today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        items(
            items = logs,
            key = { it.id }
        ) { log ->
            val swipeState = rememberSwipeToDismissBoxState()

            if (swipeState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                LaunchedEffect(log) {
                    onDelete(log)
                    swipeState.snapTo(SwipeToDismissBoxValue.Settled)
                }
            }

            SwipeToDismissBox(
                state = swipeState,
                enableDismissFromStartToEnd = false,
                backgroundContent = {
                    val color = when (swipeState.dismissDirection) {
                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color, MaterialTheme.shapes.medium)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            ) {
                WaterLogItem(log = log, unit = unit)
            }
        }
    }
}

@Composable
fun WaterLogItem(log: WaterLog, unit: String = "ml") {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeString = timeFormat.format(Date(log.timestamp))

    val amountString = remember(log.amount, unit) {
        if (unit == "oz") {
            "%.1f oz".format(log.amount * 0.033814f)
        } else {
            "${log.amount} ml"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = amountString,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Water Intake",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = timeString,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
