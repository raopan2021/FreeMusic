package com.freemusic.presentation.ui.player.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

/**
 * 彩虹效果
 */
@Composable
fun RainbowEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        val colors = listOf(
            Color.Red,
            Color(0xFFFF7F00), // Orange
            Color.Yellow,
            Color.Green,
            Color.Blue,
            Color(0xFF4B0082), // Indigo
            Color(0xFF9400D3)  // Violet
        )

        val arcCount = 8
        val maxRadius = size.minDimension / 2

        colors.forEachIndexed { colorIndex, color ->
            val phase = time * 2 * PI.toFloat() + colorIndex * PI.toFloat() / 4
            val offset = sin(phase) * 20

            for (i in 0 until arcCount) {
                val progress = i.toFloat() / arcCount
                val radius = maxRadius * (0.3f + progress * 0.7f) + offset
                val alpha = (0.5f - progress * 0.4f) * intensity

                drawArc(
                    color = color.copy(alpha = alpha),
                    startAngle = -90f + progress * 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                )
            }
        }
    }
}

/**
 * 宇宙星空效果
 */
@Composable
fun CosmicEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val particles = remember {
        List((60 * intensity).toInt().coerceAtLeast(20)) {
            CosmicParticle()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cosmic")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val progress = (time + particle.offset) % 1f
            
            val x = size.width * particle.x
            val y = size.height * particle.y
            val z = particle.z + progress * 2f // 深度变化
            
            // 3D 到 2D 投影
            val scale = 1f / (1f + z * 0.5f)
            val projectedX = size.width / 2 + (x - size.width / 2) * scale
            val projectedY = size.height / 2 + (y - size.height / 2) * scale
            
            // 星等随深度变化
            val magnitude = (1f - z / 2f) * particle.brightness
            val twinkle = (sin(progress * 20 + particle.offset * 10) + 1) / 2

            // 星迹
            if (particle.hasTrail) {
                val trailLength = particle.size * 3 * scale
                val angle = particle.trajectoryAngle + progress * particle.trajectorySpeed
                
                drawLine(
                    color = particle.color.copy(alpha = magnitude * 0.5f * twinkle),
                    start = Offset(projectedX, projectedY),
                    end = Offset(
                        projectedX - cos(angle) * trailLength,
                        projectedY - sin(angle) * trailLength
                    ),
                    strokeWidth = particle.size * scale
                )
            }

            // 星星核心
            drawCircle(
                color = particle.color.copy(alpha = magnitude * (0.5f + 0.5f * twinkle)),
                radius = particle.size * scale * (0.5f + twinkle * 0.5f),
                center = Offset(projectedX, projectedY)
            )

            // 光晕
            if (particle.hasGlow) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            particle.color.copy(alpha = magnitude * 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(projectedX, projectedY),
                        radius = particle.size * 5 * scale
                    ),
                    radius = particle.size * 5 * scale,
                    center = Offset(projectedX, projectedY)
                )
            }
        }

        // 银河带效果
        val galaxyPhase = time * 2 * PI.toFloat()
        val galaxyPath = Path().apply {
            moveTo(0f, size.height * 0.5f)
            
            for (x in 0..size.width.toInt() step 10) {
                val y = size.height * 0.5f + 
                        sin(x / size.width * PI.toFloat() * 2 + galaxyPhase) * 50 +
                        sin(x / size.width * PI.toFloat() * 4) * 20
                lineTo(x.toFloat(), y)
            }
            
            for (x in size.width.toInt() downTo 0 step 10) {
                val y = size.height * 0.5f + 
                        sin(x / size.width * PI.toFloat() * 2 + galaxyPhase) * 50 +
                        sin(x / size.width * PI.toFloat() * 4) * 20 +
                        30
                lineTo(x.toFloat(), y)
            }
            close()
        }

        drawPath(
            path = galaxyPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A0033).copy(alpha = 0.3f),
                    Color(0xFF330066).copy(alpha = 0.2f),
                    Color(0xFF1A0033).copy(alpha = 0.3f)
                )
            )
        )
    }
}

private data class CosmicParticle(
    val x: Float = Random.nextFloat() * 1f,
    val y: Float = Random.nextFloat() * 1f,
    val z: Float = Random.nextFloat(),
    val offset: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 3 + 1,
    val color: Color = listOf(
        Color.White,
        Color(0xFFADD8E6),
        Color(0xFF87CEEB),
        Color(0xFFFFB6C1),
        Color(0xFFE6E6FA)
    ).random(),
    val brightness: Float = Random.nextFloat() * 0.5f + 0.5f,
    val hasGlow: Boolean = Random.nextFloat() > 0.5f,
    val hasTrail: Boolean = Random.nextFloat() > 0.7f,
    val trajectoryAngle: Float = Random.nextFloat() * 2 * PI.toFloat(),
    val trajectorySpeed: Float = Random.nextFloat() * 0.5f
)

/**
 * 魔法粒子效果
 */
@Composable
fun MagicEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val particles = remember {
        List((25 * intensity).toInt().coerceAtLeast(10)) {
            MagicParticle()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "magic")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val progress = (time + particle.offset) % 1f
            
            // 螺旋上升轨迹
            val angle = progress * particle.rotationSpeed * 2 * PI.toFloat()
            val radius = particle.radius * (1f - progress * 0.5f)
            val x = size.width * particle.startX + cos(angle) * radius
            val y = size.height * (1f - progress) // 从下往上
            
            val alpha = (1f - progress) * particle.brightness
            
            // 魔法光点
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        particle.color.copy(alpha = alpha),
                        particle.color.copy(alpha = alpha * 0.5f),
                        Color.Transparent
                    ),
                    center = Offset(x, y),
                    radius = particle.size * 3
                ),
                radius = particle.size * 3,
                center = Offset(x, y)
            )

            // 核心
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = particle.size * 0.5f,
                center = Offset(x, y)
            )

            // 魔法轨迹
            if (progress < 0.8f) {
                val trailAngle = angle - PI.toFloat() / 8
                for (i in 0..4) {
                    val trailProgress = i / 4f
                    val trailX = x - cos(trailAngle) * particle.size * 2 * trailProgress
                    val trailY = y - sin(trailAngle) * particle.size * 2 * trailProgress
                    val trailAlpha = alpha * (1f - trailProgress)

                    drawCircle(
                        color = particle.color.copy(alpha = trailAlpha * 0.3f),
                        radius = particle.size * (1f - trailProgress * 0.5f),
                        center = Offset(trailX, trailY)
                    )
                }
            }
        }
    }
}

private data class MagicParticle(
    val startX: Float = Random.nextFloat(),
    val offset: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 6 + 3,
    val color: Color = listOf(
        Color(0xFFFF00FF),
        Color(0xFF00FFFF),
        Color(0xFFFFD700),
        Color(0xFF00FF00),
        Color(0xFFFFFFFF)
    ).random(),
    val brightness: Float = Random.nextFloat() * 0.5f + 0.5f,
    val radius: Float = Random.nextFloat() * 50 + 20,
    val rotationSpeed: Float = Random.nextFloat() * 2 + 1
)

/**
 * 能量波纹效果
 */
@Composable
fun EnergyWaveEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    color: Color = Color(0xFF00FFFF)
) {
    val waves = remember {
        List(3) { it }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "energy")
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
        waves.forEach { waveIndex ->
            val waveProgress = (time + waveIndex * 0.33f) % 1f
            val radius = size.minDimension / 2 * waveProgress
            val alpha = (1f - waveProgress) * 0.5f * intensity

            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f * (1f - waveProgress * 0.5f))
            )

            // 能量点
            val dotCount = 12
            for (i in 0 until dotCount) {
                val dotAngle = (i * 360f / dotCount + waveProgress * 360f) * (PI / 180f).toFloat()
                val dotX = center.x + cos(dotAngle) * radius
                val dotY = center.y + sin(dotAngle) * radius

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = 0f)
                        ),
                        center = Offset(dotX, dotY),
                        radius = 8f
                    ),
                    radius = 8f,
                    center = Offset(dotX, dotY)
                )
            }
        }
    }
}

/**
 * 火焰效果
 */
@Composable
fun FlameEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val particles = remember {
        List((30 * intensity).toInt().coerceAtLeast(15)) {
            FlameParticle()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "flame")
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
        particles.forEach { particle ->
            val progress = (time + particle.offset) % 1f
            
            // 火焰向上飘动
            val x = size.width * particle.startX + sin(progress * 10) * particle.wobble
            val y = size.height * (1f - progress) // 从下往上
            val flicker = (sin(progress * 30 + particle.offset * 20) + 1) / 2
            
            val alpha = (1f - progress * 0.7f) * particle.intensity
            val flameHeight = particle.size * (1f + flicker * 0.5f)

            // 火焰渐变
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha),
                        Color(0xFFFFFF00).copy(alpha = alpha * 0.8f),
                        Color(0xFFFF6600).copy(alpha = alpha * 0.6f),
                        Color(0xFFFF0000).copy(alpha = alpha * 0.3f),
                        Color.Transparent
                    ),
                    startY = y - flameHeight,
                    endY = y + flameHeight * 0.5f
                ),
                radius = flameHeight * 0.6f,
                center = Offset(x, y)
            )

            // 火星
            if (particle.isEmber && progress > 0.7f) {
                val emberX = x + (Random.nextFloat() - 0.5f) * 20
                val emberY = y - flameHeight * progress * 2
                val emberAlpha = (progress - 0.7f) * 3 * alpha

                drawCircle(
                    color = Color(0xFFFFAA00).copy(alpha = emberAlpha),
                    radius = particle.size * 0.2f,
                    center = Offset(emberX, emberY)
                )
            }
        }
    }
}

private data class FlameParticle(
    val startX: Float = Random.nextFloat(),
    val offset: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 20 + 10,
    val intensity: Float = Random.nextFloat() * 0.5f + 0.5f,
    val wobble: Float = Random.nextFloat() * 20,
    val isEmber: Boolean = Random.nextFloat() > 0.6f
)

/**
 * 水波纹效果
 */
@Composable
fun WaterRippleEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    color: Color = Color(0xFF4169E1)
) {
    var ripples by remember { mutableStateOf(listOf<WaterRipple>()) }

    LaunchedEffect(intensity) {
        while (true) {
            delay((500 / intensity).toLong().coerceAtLeast(100))
            if (ripples.size < 10) {
                ripples = ripples + WaterRipple(
                    x = Random.nextFloat(),
                    y = Random.nextFloat()
                )
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "water")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    LaunchedEffect(time) {
        ripples = ripples.mapNotNull { ripple ->
            val newProgress = ripple.progress + 0.02f
            if (newProgress >= 1f) null else ripple.copy(progress = newProgress)
        }
    }

    Canvas(modifier = modifier) {
        ripples.forEach { ripple ->
            val centerX = size.width * ripple.x
            val centerY = size.height * ripple.y
            val maxRadius = minOf(size.width, size.height) * 0.3f
            val radius = maxRadius * ripple.progress
            val alpha = (1f - ripple.progress) * 0.5f * intensity

            // 水波纹
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // 内圈
            drawCircle(
                color = color.copy(alpha = alpha * 0.5f),
                radius = radius * 0.6f,
                center = Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
        }
    }
}

private data class WaterRipple(
    val x: Float,
    val y: Float,
    val progress: Float = 0f
)

/**
 * 综合特效组合
 */
@Composable
fun CombinedEffects(
    modifier: Modifier = Modifier,
    currentSongHasRain: Boolean = false,
    currentSongHasSnow: Boolean = false,
    currentSongHasNight: Boolean = false,
    currentSongHasFire: Boolean = false,
    currentSongHasOcean: Boolean = false,
    currentSongHasMagic: Boolean = false,
    currentSongHasCosmic: Boolean = false,
    intensity: Float = 1f
) {
    Box(modifier = modifier) {
        // 基础粒子效果
        if (currentSongHasRain) {
            RainEffect(
                modifier = Modifier.matchParentSize(),
                intensity = intensity
            )
        }

        if (currentSongHasSnow) {
            SnowEffect(
                modifier = Modifier.matchParentSize(),
                intensity = intensity
            )
        }

        if (currentSongHasNight || currentSongHasCosmic) {
            CosmicEffect(
                modifier = Modifier.matchParentSize(),
                intensity = if (currentSongHasCosmic) intensity * 1.5f else intensity * 0.5f
            )
        }

        if (currentSongHasFire) {
            FlameEffect(
                modifier = Modifier.matchParentSize(),
                intensity = intensity
            )
        }

        if (currentSongHasOcean) {
            WaterRippleEffect(
                modifier = Modifier.matchParentSize(),
                intensity = intensity,
                color = Color(0xFF4169E1)
            )
        }

        if (currentSongHasMagic) {
            MagicEffect(
                modifier = Modifier.matchParentSize(),
                intensity = intensity
            )
        }
    }
}
