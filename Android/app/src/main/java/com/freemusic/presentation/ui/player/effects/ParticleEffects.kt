package com.freemusic.presentation.ui.player.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

// ============ 基础粒子效果 ============

/**
 * 樱花飘落效果
 */
@Composable
fun CherryBlossomEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val particles = remember {
        List((15 * intensity).toInt().coerceAtLeast(5)) {
            SakuraParticle()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cherry")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val progress = (time + particle.offset) % 1f
            val x = size.width * particle.startX + sin(progress * 12) * 40
            val y = size.height * progress
            val sway = sin(progress * 8 + particle.offset * 5) * 30
            val rotation = progress * 720 + particle.offset * 360

            rotate(rotation, Offset(x + sway, y)) {
                drawCircle(
                    color = particle.color.copy(alpha = 0.6f + particle.opacity * 0.3f),
                    radius = particle.size,
                    center = Offset(x + sway, y)
                )
            }
        }
    }
}

private data class SakuraParticle(
    val startX: Float = Random.nextFloat(),
    val offset: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 8 + 4,
    val color: Color = listOf(
        Color(0xFFFFB7C5),
        Color(0xFFFFC0CB),
        Color(0xFFFFD1DC),
        Color(0xFFFFE4E1),
        Color(0xFFFFFFFF)
    ).random(),
    val opacity: Float = Random.nextFloat()
)

/**
 * 雨滴效果
 */
@Composable
fun RainEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val particles = remember {
        List((80 * intensity).toInt().coerceAtLeast(20)) {
            RainParticle(intensity)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((400 / intensity).toInt().coerceAtLeast(50), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val progress = (time + particle.offset) % 1f
            val x = size.width * particle.startX + sin(progress * 5 + particle.offset * 3) * 8
            val y = size.height * progress * particle.speed
            val dropLength = 20f * intensity + particle.length

            drawLine(
                color = particle.color.copy(alpha = 0.3f + 0.4f * intensity),
                start = Offset(x, y),
                end = Offset(x - 3, y + dropLength),
                strokeWidth = 1.5f * intensity,
                cap = StrokeCap.Round
            )
            
            if (particle.hasSplash && progress > 0.9f) {
                val splashAlpha = (progress - 0.9f) * 10 * intensity
                drawCircle(
                    color = particle.color.copy(alpha = splashAlpha * 0.5f),
                    radius = particle.size * 3,
                    center = Offset(x, size.height * 0.95f)
                )
            }
        }
    }
}

private data class RainParticle(
    val startX: Float = Random.nextFloat(),
    val offset: Float = Random.nextFloat(),
    val speed: Float = Random.nextFloat() * 0.5f + 0.7f,
    val length: Float = Random.nextFloat() * 15,
    val size: Float = Random.nextFloat() * 3 + 2,
    val color: Color = Color(0xFFB0C4DE),
    val hasSplash: Boolean = Random.nextFloat() > 0.7f
)

/**
 * 雪效
 */
@Composable
fun SnowEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val particles = remember {
        List((40 * intensity).toInt().coerceAtLeast(10)) {
            SnowParticle()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "snow")
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
        particles.forEach { particle ->
            val progress = (time + particle.offset) % 1f
            val x = size.width * particle.startX + sin(progress * 6) * 50
            val y = size.height * progress

            drawCircle(
                color = particle.color.copy(alpha = particle.opacity),
                radius = particle.size,
                center = Offset(x, y)
            )
        }
    }
}

private data class SnowParticle(
    val startX: Float = Random.nextFloat(),
    val offset: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 4 + 2,
    val color: Color = Color.White,
    val opacity: Float = Random.nextFloat() * 0.5f + 0.3f
)

/**
 * 玻璃水珠
 */
@Composable
fun GlassWaterDropsEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    var drops by remember { mutableStateOf(listOf<WaterDrop>()) }

    LaunchedEffect(intensity) {
        while (true) {
            delay((800 / intensity).toLong().coerceAtLeast(150))
            if (drops.size < (15 * intensity).toInt()) {
                drops = drops + WaterDrop(
                    x = Random.nextFloat(),
                    y = 0f,
                    size = Random.nextFloat() * 10 + 5,
                    speed = Random.nextFloat() * 0.015f + 0.008f,
                    hasTrail = Random.nextFloat() > 0.5f
                )
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glass")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    LaunchedEffect(time) {
        drops = drops.mapNotNull { drop ->
            val newY = drop.y + drop.speed
            if (newY > 1.1f) null else drop.copy(y = newY)
        }
    }

    Canvas(modifier = modifier) {
        drops.forEach { drop ->
            val x = size.width * drop.x
            val y = size.height * drop.y

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.2f)
                    ),
                    center = Offset(x - drop.size * 0.2f, y - drop.size * 0.2f),
                    radius = drop.size
                ),
                radius = drop.size,
                center = Offset(x, y)
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = drop.size * 0.25f,
                center = Offset(x - drop.size * 0.3f, y - drop.size * 0.3f)
            )

            if (drop.hasTrail) {
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = Offset(x, y - drop.size),
                    end = Offset(x, y + drop.size * 2),
                    strokeWidth = drop.size * 0.1f
                )
            }
        }
    }
}

private data class WaterDrop(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val hasTrail: Boolean = false
)

// ============ 更多特效 ============

/**
 * 萤火虫效果
 */
@Composable
fun FirefliesEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val particles = remember {
        List((12 * intensity).toInt().coerceAtLeast(5)) {
            FireflyParticle()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fireflies")
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
            
            val x = size.width * particle.startX + sin(progress * 6 + particle.offset * 4) * 60
            val y = size.height * (0.3f + 0.4f * sin(progress * 4 + particle.offset * 2))
            
            val glow = (sin(progress * 20 + particle.offset * 10) + 1) / 2
            val alpha = glow * particle.brightness

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        particle.color.copy(alpha = alpha * 0.8f),
                        particle.color.copy(alpha = alpha * 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(x, y),
                    radius = particle.size * 4
                ),
                radius = particle.size * 4,
                center = Offset(x, y)
            )

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = particle.size * 0.5f,
                center = Offset(x, y)
            )
        }
    }
}

private data class FireflyParticle(
    val startX: Float = Random.nextFloat(),
    val offset: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 4 + 2,
    val color: Color = listOf(
        Color(0xFFFFFF00),
        Color(0xFFADFF2F),
        Color(0xFF7FFF00),
        Color(0xFFFFE4B5)
    ).random(),
    val brightness: Float = Random.nextFloat() * 0.5f + 0.5f
)

/**
 * 落叶效果
 */
@Composable
fun FallingLeavesEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val particles = remember {
        List((10 * intensity).toInt().coerceAtLeast(4)) {
            LeafParticle()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "leaves")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val progress = (time + particle.offset) % 1f
            val x = size.width * particle.startX + sin(progress * 8) * 80
            val y = size.height * progress
            val sway = sin(progress * 6 + particle.offset * 4) * 40
            val rotation = progress * 1080 * particle.rotationSpeed

            rotate(rotation, Offset(x + sway, y)) {
                drawOval(
                    color = particle.color.copy(alpha = 0.7f),
                    topLeft = Offset(x + sway - particle.size / 2, y - particle.size / 4),
                    size = Size(particle.size, particle.size / 2)
                )
            }
        }
    }
}

private data class LeafParticle(
    val startX: Float = Random.nextFloat(),
    val offset: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 12 + 8,
    val color: Color = listOf(
        Color(0xFFFF8C00),
        Color(0xFFFF6347),
        Color(0xFFFF4500),
        Color(0xFF9ACD32),
        Color(0xFF8B4513),
        Color(0xFFD2691E)
    ).random(),
    val rotationSpeed: Float = Random.nextFloat() * 0.5f + 0.5f
)

/**
 * 泡泡效果
 */
@Composable
fun BubblesEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val particles = remember {
        List((15 * intensity).toInt().coerceAtLeast(6)) {
            BubbleParticle()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bubbles")
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
        particles.forEach { particle ->
            val progress = (time + particle.offset) % 1f
            val x = size.width * particle.startX + sin(progress * 4) * 30
            val y = size.height * (1f - progress)
            val wobble = sin(progress * 10) * 5

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        particle.color.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(x + wobble, y),
                    radius = particle.size
                ),
                radius = particle.size,
                center = Offset(x + wobble, y)
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = particle.size * 0.2f,
                center = Offset(x + wobble - particle.size * 0.3f, y - particle.size * 0.3f)
            )
        }
    }
}

private data class BubbleParticle(
    val startX: Float = Random.nextFloat(),
    val offset: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 15 + 8,
    val color: Color = listOf(
        Color(0xFF87CEEB),
        Color(0xFFADD8E6),
        Color(0xFFB0E0E6),
        Color(0xFFE0FFFF)
    ).random()
)

/**
 * 星空效果
 */
@Composable
fun StarsEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val particles = remember {
        List((30 * intensity).toInt().coerceAtLeast(10)) {
            StarParticle()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "stars")
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
            val twinkle = (sin(progress * 10 + particle.offset * 5) + 1) / 2
            val alpha = particle.baseAlpha * (0.3f + 0.7f * twinkle)

            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = particle.size * (0.8f + 0.4f * twinkle),
                center = Offset(
                    size.width * particle.x,
                    size.height * particle.y
                )
            )

            if (particle.hasGlow) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            particle.color.copy(alpha = alpha * 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * particle.x, size.height * particle.y),
                        radius = particle.size * 3
                    ),
                    radius = particle.size * 3,
                    center = Offset(size.width * particle.x, size.height * particle.y)
                )
            }
        }
    }
}

private data class StarParticle(
    val x: Float = Random.nextFloat(),
    val y: Float = Random.nextFloat(),
    val offset: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 2 + 1,
    val color: Color = listOf(
        Color.White,
        Color(0xFFFFFAF0),
        Color(0xFFFFFFF0),
        Color(0xFFF0F8FF)
    ).random(),
    val baseAlpha: Float = Random.nextFloat() * 0.5f + 0.5f,
    val hasGlow: Boolean = Random.nextFloat() > 0.7f
)

/**
 * 烟花效果
 */
@Composable
fun FireworksEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    trigger: Boolean = false
) {
    var fireworks by remember { mutableStateOf(listOf<Firework>()) }
    var explosions by remember { mutableStateOf(listOf<Explosion>()) }

    LaunchedEffect(trigger, intensity) {
        if (trigger || intensity > 1.5f) {
            repeat((2 * intensity).toInt().coerceAtLeast(1)) {
                fireworks = fireworks + Firework(
                    x = Random.nextFloat(),
                    y = 0f,
                    targetY = Random.nextFloat() * 0.5f + 0.2f,
                    color = listOf(
                        Color(0xFFFF0000),
                        Color(0xFFFF6600),
                        Color(0xFFFFFF00),
                        Color(0xFF00FF00),
                        Color(0xFF00FFFF),
                        Color(0xFF6600FF)
                    ).random()
                )
                delay(300)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fireworks")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    LaunchedEffect(time) {
        // 更新烟花位置
        fireworks = fireworks.mapNotNull { firework ->
            val newY = firework.y + 0.03f
            if (newY >= firework.targetY) {
                // 爆炸
                explosions = explosions + Explosion(
                    x = firework.x,
                    y = firework.targetY,
                    color = firework.color,
                    particles = List(20) { ExplosionParticle(firework.color) }
                )
                null
            } else {
                firework.copy(y = newY)
            }
        }

        // 更新爆炸效果
        explosions = explosions.mapNotNull { explosion ->
            val newProgress = explosion.progress + 0.05f
            if (newProgress >= 1f) null else explosion.copy(progress = newProgress)
        }
    }

    Canvas(modifier = modifier) {
        // 绘制上升的烟花
        fireworks.forEach { firework ->
            drawCircle(
                color = firework.color,
                radius = 5f,
                center = Offset(size.width * firework.x, size.height * (1f - firework.y))
            )
        }

        // 绘制爆炸效果
        explosions.forEach { explosion ->
            explosion.particles.forEach { particle ->
                val angle = particle.angle
                val distance = explosion.progress * particle.speed * size.minDimension
                val alpha = 1f - explosion.progress

                val px = size.width * explosion.x + cos(angle) * distance
                val py = size.height * (1f - explosion.y) + sin(angle) * distance

                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = particle.size * (1f - explosion.progress * 0.5f),
                    center = Offset(px, py)
                )
            }
        }
    }
}

private data class Firework(
    val x: Float,
    val y: Float,
    val targetY: Float,
    val color: Color
)

private data class Explosion(
    val x: Float,
    val y: Float,
    val color: Color,
    val particles: List<ExplosionParticle>,
    val progress: Float = 0f
)

private data class ExplosionParticle(val color: Color) {
    val angle: Float = Random.nextFloat() * 2 * PI.toFloat()
    val speed: Float = Random.nextFloat() * 0.5f + 0.3f
    val size: Float = Random.nextFloat() * 4 + 2
}

/**
 * 极光效果
 */
@Composable
fun AuroraEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        val colors = listOf(
            Color(0xFF00FF88).copy(alpha = 0.3f * intensity),
            Color(0xFF00FFFF).copy(alpha = 0.2f * intensity),
            Color(0xFF88FF00).copy(alpha = 0.25f * intensity),
            Color(0xFF0088FF).copy(alpha = 0.2f * intensity)
        )

        colors.forEachIndexed { index, color ->
            val phase = time * 2 * PI.toFloat() + index * PI.toFloat() / 2
            val y1 = size.height * (0.2f + 0.1f * sin(phase))
            val y2 = size.height * (0.4f + 0.1f * sin(phase + 1))

            val path = Path().apply {
                moveTo(0f, size.height)
                cubicTo(
                    size.width * 0.25f, y1,
                    size.width * 0.75f, y2,
                    size.width, size.height * 0.3f + size.height * 0.2f * sin(phase + 2)
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(color, Color.Transparent),
                    startY = size.height * 0.2f,
                    endY = size.height
                )
            )
        }
    }
}

/**
 * 流星效果
 */
@Composable
fun MeteorEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val particles = remember {
        List((3 * intensity).toInt().coerceAtLeast(1)) {
            MeteorParticle()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "meteor")
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
            
            val startX = size.width * particle.startX
            val startY = size.height * particle.startY
            val endX = startX - size.width * 0.5f * progress
            val endY = startY + size.height * 0.8f * progress

            // 流星尾巴渐变
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX - 20, endY)
                lineTo(endX, endY)
                lineTo(endX - 20, endY + 5)
                close()
            }

            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        particle.color.copy(alpha = 0f),
                        particle.color.copy(alpha = 0.5f * (1f - progress)),
                        Color.White.copy(alpha = (1f - progress))
                    ),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY)
                )
            )

            // 流星头部
            drawCircle(
                color = Color.White,
                radius = 3f * (1f - progress * 0.5f),
                center = Offset(endX, endY)
            )
        }
    }
}

private data class MeteorParticle(
    val startX: Float = Random.nextFloat() * 0.5f + 0.5f,
    val startY: Float = Random.nextFloat() * 0.3f,
    val offset: Float = Random.nextFloat(),
    val color: Color = Color(0xFFFFD700)
)

/**
 * 荧光波浪效果
 */
@Composable
fun NeonWaveEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    color: Color = Color(0xFFFF00FF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
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
        val waveHeight = size.height * 0.15f * intensity

        repeat(3) { waveIndex ->
            val phase = time * 2f * PI.toFloat() + waveIndex * PI.toFloat() / 1.5f
            val alpha = (0.3f + 0.2f * sin(phase)) * intensity

            val path = Path().apply {
                moveTo(0f, size.height * 0.5f)

                for (x in 0..size.width.toInt() step 5) {
                    val y = size.height * 0.5f + 
                           waveHeight * sin((x.toFloat() / size.width * 4f * PI.toFloat() + phase).toDouble()).toFloat() *
                           (1f - waveIndex * 0.2f)
                    lineTo(x.toFloat(), y)
                }

                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = alpha),
                        color.copy(alpha = alpha * 0.3f),
                        Color.Transparent
                    ),
                    startY = size.height * 0.3f,
                    endY = size.height * 0.7f
                )
            )
        }
    }
}

/**
 * 综合歌词特效系统
 */
@Composable
fun LyricsParticleEffects(
    modifier: Modifier = Modifier,
    lyricsContent: String?,
    currentPosition: Long = 0,
    enabled: Boolean = true
) {
    if (!enabled) return

    val lowerLyrics = lyricsContent?.lowercase() ?: ""

    // 检测关键词
    val hasRain = lowerLyrics.contains("雨") || lowerLyrics.contains("rain")
    val hasHeavyRain = lowerLyrics.contains("大雨") || lowerLyrics.contains("暴雨") || lowerLyrics.contains("storm")
    val hasSakura = lowerLyrics.contains("樱花") || lowerLyrics.contains("cherry") || lowerLyrics.contains("春天")
    val hasSnow = lowerLyrics.contains("雪") || lowerLyrics.contains("snow")
    val hasNight = lowerLyrics.contains("星空") || lowerLyrics.contains("night") || lowerLyrics.contains("星星")
    val hasAutumn = lowerLyrics.contains("秋") || lowerLyrics.contains("落叶") || lowerLyrics.contains("autumn")
    val hasOcean = lowerLyrics.contains("海") || lowerLyrics.contains("ocean") || lowerLyrics.contains("泡泡")
    val hasDream = lowerLyrics.contains("梦") || lowerLyrics.contains("dream") || lowerLyrics.contains("星空")
    val hasFirefly = lowerLyrics.contains("萤火虫") || lowerLyrics.contains("firefly") || lowerLyrics.contains("glow")
    val hasAurora = lowerLyrics.contains("极光") || lowerLyrics.contains("aurora") || lowerLyrics.contains("光")
    val hasMeteor = lowerLyrics.contains("流星") || lowerLyrics.contains("meteor") || lowerLyrics.contains("shooting star")

    // 计算强度
    val rainIntensity = when {
        hasHeavyRain -> 2.0f
        hasRain -> 1.0f
        else -> 0f
    }

    val sakuraIntensity = when {
        hasSakura -> 1.0f
        !hasRain && !hasSnow -> 0.2f
        else -> 0f
    }

    val snowIntensity = if (hasSnow && !hasRain) 1.0f else 0f
    val starsIntensity = if (hasNight || hasDream) 1.0f else 0f
    val leavesIntensity = if (hasAutumn && !hasRain) 1.0f else 0f
    val bubblesIntensity = if (hasOcean) 1.0f else 0f
    val firefliesIntensity = if (hasFirefly || hasNight) 0.5f else 0f
    val auroraIntensity = if (hasAurora) 1.0f else 0f
    val meteorIntensity = if (hasMeteor) 1.0f else 0f

    Box(modifier = modifier) {
        // 樱花
        if (sakuraIntensity > 0) {
            CherryBlossomEffect(
                modifier = Modifier.matchParentSize(),
                intensity = sakuraIntensity
            )
        }

        // 雨
        if (rainIntensity > 0) {
            RainEffect(
                modifier = Modifier.matchParentSize(),
                intensity = rainIntensity
            )
        }

        // 雪
        if (snowIntensity > 0) {
            SnowEffect(
                modifier = Modifier.matchParentSize(),
                intensity = snowIntensity
            )
        }

        // 星空
        if (starsIntensity > 0) {
            StarsEffect(
                modifier = Modifier.matchParentSize(),
                intensity = starsIntensity
            )
        }

        // 落叶
        if (leavesIntensity > 0) {
            FallingLeavesEffect(
                modifier = Modifier.matchParentSize(),
                intensity = leavesIntensity
            )
        }

        // 泡泡
        if (bubblesIntensity > 0) {
            BubblesEffect(
                modifier = Modifier.matchParentSize(),
                intensity = bubblesIntensity
            )
        }

        // 萤火虫
        if (firefliesIntensity > 0) {
            FirefliesEffect(
                modifier = Modifier.matchParentSize(),
                intensity = firefliesIntensity
            )
        }

        // 玻璃水珠
        if (rainIntensity > 0.5f) {
            GlassWaterDropsEffect(
                modifier = Modifier.matchParentSize(),
                intensity = rainIntensity
            )
        }

        // 极光
        if (auroraIntensity > 0) {
            AuroraEffect(
                modifier = Modifier.matchParentSize(),
                intensity = auroraIntensity
            )
        }

        // 流星
        if (meteorIntensity > 0) {
            MeteorEffect(
                modifier = Modifier.matchParentSize(),
                intensity = meteorIntensity
            )
        }

        // 烟花
        if (lowerLyrics.contains("烟花") || lowerLyrics.contains("firework")) {
            FireworksEffect(
                modifier = Modifier.matchParentSize(),
                trigger = true
            )
        }

        // 霓虹波浪（夜晚主题）
        if (hasNight && rainIntensity == 0f) {
            NeonWaveEffect(
                modifier = Modifier.matchParentSize(),
                color = Color(0xFFFF00FF)
            )
        }
    }
}
