package com.freemusic.presentation.ui.player.equalizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

/**
 * 频谱均衡器可视化
 */
@Composable
fun SpectrumEqualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    bandCount: Int = 8,
    color: Color = Color(0xFF00FFFF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spectrum")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val bars = remember {
        List(bandCount) { it }
    }

    Canvas(modifier = modifier) {
        val barWidth = size.width / bars.size
        val maxHeight = size.height * 0.9f

        bars.forEachIndexed { index, _ ->
            val phase = index * 0.3f + time * 10f
            val barHeight = if (isPlaying) {
                maxHeight * (0.2f + 0.8f * ((sin(phase.toDouble()) + 1) / 2).toFloat() * 0.7f +
                       Random.nextFloat() * 0.3f)
            } else {
                maxHeight * 0.05f
            }

            val x = index * barWidth + barWidth * 0.1f
            val barActualWidth = barWidth * 0.8f

            // 渐变颜色
            val hue = (index * 30f + time * 60f) % 360f
            val barColor = Color.hsv(hue, 0.7f, 0.9f)

            // 绘制渐变条
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        barColor,
                        barColor.copy(alpha = 0.5f),
                        barColor.copy(alpha = 0.2f)
                    ),
                    startY = size.height - barHeight,
                    endY = size.height
                ),
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barActualWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // 绘制高光
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(x + barActualWidth * 0.2f, size.height - barHeight + 4f),
                size = Size(barActualWidth * 0.2f, barHeight * 0.3f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
        }
    }
}

/**
 * 圆形均衡器
 */
@Composable
fun CircularEqualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    segments: Int = 12,
    color: Color = Color(0xFFFF00FF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circular")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(size.width, size.height) / 2 * 0.7f

        for (i in 0 until segments) {
            val angle = (i * 360f / segments + time * 360f) * (Math.PI / 180f).toFloat()
            val barLength = if (isPlaying) {
                radius * (0.3f + 0.4f * ((sin(time * 10f + i * 0.5f) + 1) / 2).toFloat())
            } else {
                radius * 0.2f
            }

            val hue = (i * 30f + time * 180f) % 360f
            val segmentColor = Color.hsv(hue, 0.7f, 0.9f)

            val startX = centerX + kotlin.math.cos(angle) * radius * 0.3f
            val startY = centerY + kotlin.math.sin(angle) * radius * 0.3f
            val endX = centerX + kotlin.math.cos(angle) * (radius * 0.3f + barLength)
            val endY = centerY + kotlin.math.sin(angle) * (radius * 0.3f + barLength)

            drawLine(
                color = segmentColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }

        // 中心圆
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = 0.8f),
                    color.copy(alpha = 0.4f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = radius * 0.4f
            ),
            radius = radius * 0.4f,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * 波形均衡器
 */
@Composable
fun WaveformEqualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    color: Color = Color(0xFF00FF88)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        val path = Path()
        val waveHeight = size.height * 0.3f

        path.moveTo(0f, size.height / 2)

        for (x in 0..size.width.toInt() step 4) {
            val phase = x / size.width * 4 * Math.PI.toFloat() + time * 2 * Math.PI.toFloat()
            val y = if (isPlaying) {
                size.height / 2 + waveHeight * sin(phase.toDouble()).toFloat()
            } else {
                size.height / 2
            }
            path.lineTo(x.toFloat(), y)
        }

        // 绘制波形
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )

        // 绘制第二条波形（镜像）
        val path2 = Path()
        path2.moveTo(0f, size.height / 2)

        for (x in 0..size.width.toInt() step 4) {
            val phase = x / size.width * 4 * Math.PI.toFloat() + time * 2 * Math.PI.toFloat() + Math.PI.toFloat()
            val y = if (isPlaying) {
                size.height / 2 - waveHeight * 0.5f * sin(phase.toDouble()).toFloat()
            } else {
                size.height / 2
            }
            path2.lineTo(x.toFloat(), y)
        }

        drawPath(
            path = path2,
            color = color.copy(alpha = 0.5f),
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )
    }
}

/**
 * 酒吧均衡器（垂直条形）
 */
@Composable
fun BarEqualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    barCount: Int = 16,
    colors: List<Color> = listOf(
        Color(0xFFFF0000),
        Color(0xFFFF6600),
        Color(0xFFFFFF00),
        Color(0xFF00FF00),
        Color(0xFF00FFFF),
        Color(0xFF0000FF)
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bar")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        val barWidth = size.width / barCount
        val maxHeight = size.height * 0.85f

        for (i in 0 until barCount) {
            val phase = i * 0.4f + time * 8f
            val barHeight = if (isPlaying) {
                val base = ((sin(phase.toDouble()) + 1) / 2).toFloat()
                val random = Random.nextFloat() * 0.2f
                maxHeight * (base * 0.8f + random)
            } else {
                maxHeight * 0.05f
            }

            val colorIndex = (i * colors.size / barCount) % colors.size
            val color = colors[colorIndex]

            val x = i * barWidth + barWidth * 0.1f
            val barActualWidth = barWidth * 0.8f

            // 渐变
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        color,
                        color.copy(alpha = 0.6f)
                    ),
                    startY = size.height - barHeight,
                    endY = size.height
                ),
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barActualWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barActualWidth / 2, barActualWidth / 2)
            )
        }
    }
}

/**
 * 3D 频谱柱状图
 */
@Composable
fun Spectrum3DEqualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    columns: Int = 12,
    rows: Int = 6,
    color: Color = Color(0xFF00FFFF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spectrum3d")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        val columnWidth = size.width / columns
        val rowHeight = size.height / rows

        for (col in 0 until columns) {
            val columnPhase = col * 0.3f + time * 12f
            val columnHeight = if (isPlaying) {
                val base = ((sin(columnPhase.toDouble()) + 1) / 2).toFloat()
                rows * base
            } else {
                1f
            }

            for (row in 0 until columnHeight.toInt().coerceAtMost(rows)) {
                val hue = (col * 30f + row * 15f) % 360f
                val cellColor = Color.hsv(hue, 0.6f, 0.8f - row * 0.1f)

                val x = col * columnWidth + 2f
                val y = size.height - (row + 1) * rowHeight

                // 3D 效果
                drawRect(
                    color = cellColor,
                    topLeft = Offset(x, y),
                    size = Size(columnWidth - 4f, rowHeight - 2f)
                )

                // 高光
                drawRect(
                    color = Color.White.copy(alpha = 0.3f),
                    topLeft = Offset(x, y),
                    size = Size(columnWidth - 4f, 3f)
                )
            }
        }
    }
}

/**
 * 脉冲环均衡器
 */
@Composable
fun PulseRingEqualizer(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    ringCount: Int = 4,
    color: Color = Color(0xFFFF00FF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = minOf(size.width, size.height) / 2 * 0.9f

        for (i in 0 until ringCount) {
            val ringProgress = (time + i * 1f / ringCount) % 1f
            val radius = maxRadius * ringProgress
            val alpha = if (isPlaying) (1f - ringProgress) * 0.6f else 0.2f

            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 4f)
            )

            // 绘制圆点
            if (ringProgress < 0.1f && isPlaying) {
                val dotAngle = i * 60f + time * 360f
                val dotX = centerX + kotlin.math.cos(Math.toRadians(dotAngle.toDouble())).toFloat() * radius
                val dotY = centerY + kotlin.math.sin(Math.toRadians(dotAngle.toDouble())).toFloat() * radius

                drawCircle(
                    color = color,
                    radius = 6f,
                    center = Offset(dotX, dotY)
                )
            }
        }

        // 中心发光
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = 0.5f),
                    color.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = maxRadius * 0.3f
            ),
            radius = maxRadius * 0.3f,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * 综合均衡器组件（可切换样式）
 */
enum class EqualizerStyle {
    SPECTRUM,
    CIRCULAR,
    WAVEFORM,
    BAR,
    SPECTRUM_3D,
    PULSE_RING
}

@Composable
fun AudioEqualizer(
    modifier: Modifier = Modifier,
    style: EqualizerStyle = EqualizerStyle.SPECTRUM,
    isPlaying: Boolean = true
) {
    when (style) {
        EqualizerStyle.SPECTRUM -> SpectrumEqualizer(
            modifier = modifier,
            isPlaying = isPlaying
        )
        EqualizerStyle.CIRCULAR -> CircularEqualizer(
            modifier = modifier,
            isPlaying = isPlaying
        )
        EqualizerStyle.WAVEFORM -> WaveformEqualizer(
            modifier = modifier,
            isPlaying = isPlaying
        )
        EqualizerStyle.BAR -> BarEqualizer(
            modifier = modifier,
            isPlaying = isPlaying
        )
        EqualizerStyle.SPECTRUM_3D -> Spectrum3DEqualizer(
            modifier = modifier,
            isPlaying = isPlaying
        )
        EqualizerStyle.PULSE_RING -> PulseRingEqualizer(
            modifier = modifier,
            isPlaying = isPlaying
        )
    }
}
