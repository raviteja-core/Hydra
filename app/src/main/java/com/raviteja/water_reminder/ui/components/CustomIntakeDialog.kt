package com.raviteja.water_reminder.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CustomIntakeDialog(
    unit: String = "ml",
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { 
            Text(
                text = "Custom Intake",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    isError = false
                },
                label = { Text("Amount ($unit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = isError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val inputVal = amountText.toFloatOrNull()
                    if (inputVal != null && inputVal > 0) {
                        val amountInMl = if (unit == "oz") {
                            (inputVal * 29.5735f).toInt()
                        } else {
                            inputVal.toInt()
                        }
                        if (amountInMl > 0) {
                            onConfirm(amountInMl)
                            onDismiss()
                        } else {
                            isError = true
                        }
                    } else {
                        isError = true
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Entry", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
