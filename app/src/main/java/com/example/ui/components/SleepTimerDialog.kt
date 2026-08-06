package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryIndigo

@Composable
fun SleepTimerDialog(
    currentMinutes: Int?,
    onSelectMinutes: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        null to "Off",
        15 to "15 Minutes",
        30 to "30 Minutes",
        45 to "45 Minutes",
        60 to "60 Minutes"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("sleep_timer_dialog"),
        title = {
            Text(
                text = "Sleep Timer",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                options.forEach { (mins, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectMinutes(mins)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentMinutes == mins),
                            onClick = {
                                onSelectMinutes(mins)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryIndigo)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (currentMinutes == mins) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PrimaryIndigo)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}
