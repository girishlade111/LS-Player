package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubtitleTrack
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.TextMuted
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubtitleOptionsBottomSheet(
    subtitles: List<SubtitleTrack>,
    selectedSubtitle: SubtitleTrack?,
    subtitleOffsetMs: Long = 0L,
    onAdjustSubtitleOffset: (Long) -> Unit = {},
    onSetSubtitleOffset: (Long) -> Unit = {},
    onSelectSubtitle: (SubtitleTrack?) -> Unit,
    onExternalSubtitlePicked: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onExternalSubtitlePicked(uri)
        }
    }

    val presetOffsets = listOf(-1000L, -500L, -250L, 0L, 250L, 500L, 1000L)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("subtitle_options_bottom_sheet"),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Title Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Subtitles,
                    contentDescription = null,
                    tint = PrimaryIndigo
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Subtitle Options",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add External Subtitle Button
            Button(
                onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_external_sub_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open External Subtitle (.srt, .vtt)")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SUBTITLE TIMING OFFSET SECTION
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subtitle_offset_section"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Subtitle Timing Offset",
                            tint = AccentCyan
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SUBTITLE TIMING SYNC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Text(
                                text = "Adjust offset in +/- milliseconds",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Current Offset Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (subtitleOffsetMs == 0L) PrimaryIndigo.copy(alpha = 0.15f) else AccentCyan.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = when {
                                    subtitleOffsetMs == 0L -> "0 ms (Synced)"
                                    subtitleOffsetMs > 0L -> "+${subtitleOffsetMs} ms"
                                    else -> "${subtitleOffsetMs} ms"
                                },
                                color = if (subtitleOffsetMs == 0L) PrimaryIndigo else AccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("subtitle_offset_text")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stepper Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onAdjustSubtitleOffset(-100L) },
                            modifier = Modifier.testTag("subtitle_offset_minus_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveCircleOutline,
                                contentDescription = "Decrease offset 100ms",
                                tint = PrimaryIndigo
                            )
                        }

                        OutlinedButton(
                            onClick = { onSetSubtitleOffset(0L) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("subtitle_offset_reset_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset offset",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Reset", fontWeight = FontWeight.Medium)
                        }

                        IconButton(
                            onClick = { onAdjustSubtitleOffset(100L) },
                            modifier = Modifier.testTag("subtitle_offset_plus_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = "Increase offset 100ms",
                                tint = PrimaryIndigo
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fine adjustment Slider (-5000ms to +5000ms)
                    Slider(
                        value = subtitleOffsetMs.coerceIn(-5000L, 5000L).toFloat(),
                        onValueChange = { valRounded ->
                            val roundedMs = ((valRounded / 50f).roundToLong()) * 50L
                            onSetSubtitleOffset(roundedMs)
                        },
                        valueRange = -5000f..5000f,
                        steps = 199, // 50ms steps
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCyan,
                            activeTrackColor = AccentCyan,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subtitle_offset_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetOffsets.forEach { preset ->
                            val isSelected = (subtitleOffsetMs == preset)
                            val label = when {
                                preset == 0L -> "0ms"
                                preset > 0L -> "+${preset}ms"
                                else -> "${preset}ms"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable { onSetSubtitleOffset(preset) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("subtitle_offset_chip_$preset")
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AVAILABLE TRACKS SECTION
            Text(
                text = "AVAILABLE TRACKS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.height(180.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSubtitle(null) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedSubtitle == null),
                            onClick = { onSelectSubtitle(null) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryIndigo)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Disable Subtitles",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                items(subtitles) { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSubtitle(track) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedSubtitle?.id == track.id),
                            onClick = { onSelectSubtitle(track) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryIndigo)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = track.label + if (track.isExternal) " (External)" else "",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (selectedSubtitle?.id == track.id) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
