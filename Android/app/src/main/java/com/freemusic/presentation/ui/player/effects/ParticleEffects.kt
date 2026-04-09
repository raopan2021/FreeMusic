package com.freemusic.presentation.ui.player.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 樱花飘落效果
 */
@Composable
fun CherryBlossomEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f // 0.0 - 1.0
) {
    val particles = remember {
        List((20 * intensity).toInt().coerceAtLeast(5)) {
            CherryBlossomParticle()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cherry_blossom")
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
            val x = size.width * particle.startX + sin(progress * 10) * 30
            val y = size.height * progress
            
            // 飘落时的左右摆动
            val swayX = sin(progress * 8 + particle.offset * 5) * 20
            
            // 旋转
            val rotation = progress * 360 * 2 + particle.offset * 360
            
            drawCircle(
                color = particle.color.copy(alpha = 0.7f),
                radius = particle.size,
                center = Offset(x + swayX, y)
            )
        }
    }
}

private data class CherryBlossomParticle(
    val startX: Float = Random.nextFloat(),
    val offset: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() * 6 + 3,
    val color: Color = listOf(
        Color(0xFFFFB7C5), // 樱花粉
        Color(0xFFFFC0CB), // 粉色
        Color(0xFFFFD1DC), // 浅粉
        Color(0xFFFFFFE0)  // 白色
    ).random(),
    val speed: Float = Random.nextFloat() * 0.5f + 0.5f
)

/**
 * 雨滴效果
 */
@Composable
fun RainEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f // 0.0 - 1.0 (小到暴雨)
) {
    val particles = remember {
        List((100 * intensity).toInt().coerceAtLeast(20)) {
            RainParticle(intensity)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((500 / intensity).toInt().coerceAtLeast(100), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val progress = (time + particle.offset) % 1f
            val x = size.width * particle.startX + sin(progress * 3) * 10
            val y = size.height * progress * particle.speed
            
            // 雨滴长度根据强度变化
            val dropLength = 15f * intensity + particle.length
            
            drawLine(
                color = particle.color.copy(alpha = 0.4f + 0.3f * intensity),
                start = Offset(x, y),
                end = Offset(x - 2, y + dropLength),
                strokeWidth = 1.5f * intensity
            )
        }
    }
}

private data class RainParticle(
    val startX: Float = Random.nextFloat(),
    val offset: Float = Random.nextFloat(),
    val speed: Float = Random.nextFloat() * 0.5f + 0.8f,
    val length: Float = Random.nextFloat() * 10,
    val color: Color = Color(0xFFB0C4DE) // 雨蓝色
)

/**
 * 玻璃水珠溅落效果
 */
@Composable
fun GlassWaterDropsEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    var drops by remember { mutableStateOf(listOf<WaterDrop>()) }
    var key by remember { mutableIntStateOf(0) }

    LaunchedEffect(intensity) {
        while (true) {
            delay((1000 / intensity).toLong().coerceAtLeast(200))
            if (drops.size < (20 * intensity).toInt()) {
                drops = drops + WaterDrop(
                    x = Random.nextFloat(),
                    y = 0f,
                    size = Random.nextFloat() * 8 + 4,
                    speed = Random.nextFloat() * 0.02f + 0.01f
                )
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glass_drops")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // 更新水滴位置
    LaunchedEffect(time) {
        drops = drops.mapNotNull { drop ->
            val newY = drop.y + drop.speed
            if (newY > 1f) null else drop.copy(y = newY)
        }
    }

    Canvas(modifier = modifier) {
        drops.forEach { drop ->
            val x = size.width * drop.x
            val y = size.height * drop.y
            
            // 主水珠
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = drop.size,
                center = Offset(x, y)
            )
            
            // 水珠高光
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = drop.size * 0.3f,
                center = Offset(x - drop.size * 0.2f, y - drop.size * 0.2f)
            )
        }
    }
}

private data class WaterDrop(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float
)

/**
 * 综合粒子效果系统
 * 根据歌词内容自动切换效果
 */
@Composable
fun LyricsParticleEffects(
    modifier: Modifier = Modifier,
    lyricsContent: String?,
    currentPosition: Long = 0
) {
    val lowerLyrics = lyricsContent?.lowercase() ?: ""
    
    // 检测歌词中的天气关键词
    val hasRain = lowerLyrics.contains("雨") || lowerLyrics.contains("rain")
    val hasHeavyRain = lowerLyrics.contains("大雨") || lowerLyrics.contains("暴雨") || lowerLyrics.contains("storm")
    val hasCherry = lowerLyrics.contains("樱花") || lowerLyrics.contains("cherry") || lowerLyrics.contains("春天")
    val hasSnow = lowerLyrics.contains("雪") || lowerLyrics.contains("snow")
    
    // 计算效果强度
    val rainIntensity = when {
        hasHeavyRain -> 2.0f
        hasRain -> 1.0f
        else -> 0f
    }
    
    val cherryIntensity = when {
        hasCherry -> 1.0f
        !hasRain -> 0.3f // 平时有轻微樱花飘落
        else -> 0f
    }
    
    val snowIntensity = if (hasSnow && !hasRain) 1.0f else 0f
    
    Box(modifier = modifier) {
        // 樱花飘落
        if (cherryIntensity > 0) {
            CherryBlossomEffect(
                modifier = Modifier.matchParentSize(),
                intensity = cherryIntensity
            )
        }
        
        // 雨效果
        if (rainIntensity > 0) {
            RainEffect(
                modifier = Modifier.matchParentSize(),
                intensity = rainIntensity
            )
        }
        
        // 雪效果
        if (snowIntensity > 0) {
            SnowEffect(
                modifier = Modifier.matchParentSize(),
                intensity = snowIntensity
            )
        }
        
        // 玻璃水珠
        if (rainIntensity > 0.5f) {
            GlassWaterDropsEffect(
                modifier = Modifier.matchParentSize(),
                intensity = rainIntensity
            )
        }
    }
}

/**
 * 雪花飘落效果
 */
@Composable
fun SnowEffect(
    modifier: Modifier = Modifier,
    intensity: Float = 1f
) {
    val particles = remember {
        List((30 * intensity).toInt().coerceAtLeast(10)) {
            SnowParticle()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "snow")
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
            val x = size.width * particle.startX + sin(progress * 5) * 30
            val y = size.height * progress
            
            // 雪花六角形简化表示
            drawCircle(
                color = particle.color.copy(alpha = 0.8f),
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
    val color: Color = Color.White
)
