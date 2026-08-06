package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.HUDBackground
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.viewmodel.GestureOverlay

@Composable
fun GestureHUD(
    overlayState: GestureOverlay,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = overlayState !is GestureOverlay.None,
        enter = fadeIn() + scaleIn(initialScale = 0.85f),
        exit = fadeOut() + scaleOut(targetScale = 0.85f),
        modifier = modifier
    ) {
        Surface(
            color = HUDBackground,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 12.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.testTag("gesture_hud_container")
        ) {
            when (overlayState) {
                is GestureOverlay.Brightness -> {
                    val icon = when {
                        overlayState.percent < 35 -> Icons.Default.BrightnessLow
                        overlayState.percent < 70 -> Icons.Default.BrightnessMedium
                        else -> Icons.Default.Brightness7
                    }
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .testTag("brightness_hud"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Brightness",
                            tint = AccentCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(
                                modifier = Modifier.width(120.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Brightness",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${overlayState.percent}%",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { overlayState.percent / 100f },
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(6.dp),
                                color = AccentCyan,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
                is GestureOverlay.Volume -> {
                    val icon = when {
                        overlayState.percent == 0 -> Icons.Default.VolumeMute
                        overlayState.percent < 50 -> Icons.Default.VolumeDown
                        else -> Icons.Default.VolumeUp
                    }
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .testTag("volume_hud"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Volume",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(
                                modifier = Modifier.width(120.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Volume",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (overlayState.percent == 0) "Muted" else "${overlayState.percent}%",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { overlayState.percent / 100f },
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(6.dp),
                                color = PrimaryIndigo,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
                is GestureOverlay.Seek -> {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .testTag("seek_hud"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (overlayState.isForward) Icons.Default.FastForward else Icons.Default.FastRewind,
                            contentDescription = "Seek",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (overlayState.isForward) "+${overlayState.deltaSeconds}s" else "-${overlayState.deltaSeconds}s",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                GestureOverlay.None -> {}
            }
        }
    }
}
