package com.freemusic.presentation.ui.player.cover

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * 音频可视化叠加层
 * 实际使用需要接入 Media3 的 AudioSession
 */
@Composable
fun AudioVisualizerOverlay(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF5C6BC0),
    intensity: Float = 0.5f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val bars = remember { List(12) { it } }

    Canvas(modifier = modifier) {
        val barWidth = size.width / bars.size
        val maxHeight = size.height * 0.3f

        bars.forEachIndexed { index, _ ->
            val offsetX = index * barWidth + barWidth / 2

            val phase = index * 0.4f + time * 10f
            val barHeight = maxHeight * (0.2f + 0.8f * ((sin(phase.toDouble()) + 1) / 2).toFloat() * intensity)

            // 使用主题色作为主色调，带渐变效果
            val hue = (index * 15f + time * 30f) % 360f
            val color = Color.hsv(hue, 0.5f, 0.7f, 0.6f)

            drawRoundRect(
                color = color,
                topLeft = Offset(offsetX - barWidth / 3f, size.height - barHeight),
                size = Size(barWidth / 1.5f, barHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}

/**
 * 环形脉冲动画效果
 */
@Composable
fun RingPulseOverlay(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF5C6BC0),
    isPlaying: Boolean = true
) {
    if (!isPlaying) return

    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")

    val rings = remember {
        List(3) { it }
    }

    Canvas(modifier = modifier) {
        rings.forEach { index ->
            val progress = (System.currentTimeMillis() / 1000f + index * 0.3f) % 1f
            val radius = size.minDimension / 2 * (0.7f + progress * 0.5f)
            val alpha = (1f - progress) * 0.4f

            drawCircle(
                color = primaryColor.copy(alpha = alpha),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}