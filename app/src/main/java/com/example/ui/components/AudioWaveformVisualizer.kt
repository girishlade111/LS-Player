package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryIndigo

@Composable
fun AudioWaveformVisualizer(
    isPlaying: Boolean,
    isBatterySaverActive: Boolean = false,
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    barWidth: Dp = 3.dp,
    barGap: Dp = 4.dp,
    maxHeight: Dp = 22.dp,
    color: Color = PrimaryIndigo
) {
    val shouldAnimate = isPlaying && !isBatterySaverActive
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase2"
    )

    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase3"
    )

    val basePattern = listOf(
        0.3f, 0.6f, 0.9f, 0.5f, 0.8f, 1.0f, 0.7f, 0.4f,
        0.8f, 0.5f, 0.9f, 0.6f, 0.3f, 0.7f, 1.0f, 0.5f,
        0.8f, 0.4f, 0.6f, 0.9f, 0.7f, 0.5f, 0.3f, 0.6f,
        0.8f, 0.5f, 0.9f, 0.4f
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight)
            .testTag("audio_waveform_visualizer")
    ) {
        val totalBarSpace = barWidth.toPx() + barGap.toPx()
        val totalWidthNeeded = barCount * totalBarSpace - barGap.toPx()
        val startX = (size.width - totalWidthNeeded) / 2f
        val centerY = size.height / 2f

        for (i in 0 until barCount) {
            val baseVal = basePattern[i % basePattern.size]
            val phaseFactor = when (i % 3) {
                0 -> phase1
                1 -> phase2
                else -> phase3
            }

            val amplitude = if (shouldAnimate) (baseVal * phaseFactor).coerceIn(0.15f, 1f) else 0.15f
            val currentBarHeight = size.height * amplitude
            val left = startX + i * totalBarSpace
            val top = centerY - (currentBarHeight / 2f)

            drawRoundRect(
                color = color.copy(alpha = if (shouldAnimate) 0.85f else 0.25f),
                topLeft = Offset(left, top),
                size = Size(barWidth.toPx(), currentBarHeight),
                cornerRadius = CornerRadius(barWidth.toPx() / 2f, barWidth.toPx() / 2f)
            )
        }
    }
}
