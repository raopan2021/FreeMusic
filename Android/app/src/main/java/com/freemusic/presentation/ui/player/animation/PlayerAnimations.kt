package com.freemusic.presentation.ui.player.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * 3D旋转相册效果
 */
@Composable
fun CoverCarousel3D(
    covers: List<String>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "carousel")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(size.width, size.height) * 0.35f
        
        if (covers.isEmpty()) return@Canvas
        
        // 绘制3D旋转的封面圆盘
        for (i in covers.indices) {
            val angle = (i * 360f / covers.size) + rotation
            val rad = Math.toRadians(angle.toDouble())
            
            // 计算3D位置
            val x = centerX + cos(rad).toFloat() * radius
            val y = centerY + sin(rad).toFloat() * radius * 0.3f // 椭圆轨道模拟3D
            
            // 计算透明度（后面的更透明）
            val normalizedAngle = ((angle % 360f) + 360f) % 360f
            val alpha = when {
                normalizedAngle < 90f || normalizedAngle > 270f -> 1f
                normalizedAngle < 180f -> 1f - (normalizedAngle - 90f) / 90f * 0.5f
                else -> 1f - (360f - normalizedAngle - 90f) / 90f * 0.5f
            }
            
            // 计算缩放
            val coverScale = when {
                normalizedAngle < 90f || normalizedAngle > 270f -> scale
                else -> scale * 0.7f
            }
            
            // 绘制封面占位符
            drawCircle(
                color = Color.Gray.copy(alpha = alpha * 0.5f),
                radius = 40f * coverScale,
                center = Offset(x, y)
            )
        }
        
        // 中心封面
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6366F1),
                    Color(0xFF8B5CF6)
                ),
                center = Offset(centerX, centerY),
                radius = 60f * scale
            ),
            radius = 60f * scale,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * 镜像反射效果
 */
@Composable
fun MirrorReflectionEffect(
    coverUrl: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mirror")
    
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_offset"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        // 原始封面
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6366F1),
                    Color(0xFF8B5CF6)
                ),
                center = Offset(centerX, centerY),
                radius = 80f
            ),
            radius = 80f,
            center = Offset(centerX, centerY)
        )
        
        // 反射效果
        for (i in 0..10) {
            val y = centerY + (i * 8f)
            val alpha = (1f - i / 10f) * 0.3f
            val offset = sin(waveOffset * 2 * PI.toFloat() + i * 0.5f) * 5f
            
            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(centerX - 60f + offset, y),
                end = Offset(centerX + 60f + offset, y),
                strokeWidth = 2f
            )
        }
    }
}

/**
 * 呼吸灯效果
 */
@Composable
fun BreathingLightEffect(
    color: Color = Color(0xFF6366F1),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        // 外层光晕
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = alpha * 0.5f),
                    color.copy(alpha = 0f)
                ),
                center = Offset(centerX, centerY),
                radius = size.minDimension * 0.4f * scale
            ),
            radius = size.minDimension * 0.4f * scale,
            center = Offset(centerX, centerY)
        )
        
        // 内层光晕
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = alpha),
                    color.copy(alpha = 0f)
                ),
                center = Offset(centerX, centerY),
                radius = size.minDimension * 0.2f * scale
            ),
            radius = size.minDimension * 0.2f * scale,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * 频谱能量条
 */
@Composable
fun SpectrumBars(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF6366F1)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spectrum")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val barCount = amplitudes.size.coerceAtLeast(16)
        val barWidth = size.width / barCount * 0.8f
        val spacing = size.width / barCount * 0.2f
        
        for (i in 0 until barCount) {
            val amplitude = if (amplitudes.isNotEmpty()) {
                amplitudes[i % amplitudes.size]
            } else {
                val wave = sin(phase + i * 0.5f)
                (wave + 1f) / 2f * 0.5f + 0.2f
            }
            
            val barHeight = size.height * amplitude
            val x = i * (barWidth + spacing)
            val y = size.height - barHeight
            
            // 渐变颜色
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    barColor,
                    barColor.copy(alpha = 0.5f),
                    barColor.copy(alpha = 0.1f)
                ),
                startY = y,
                endY = size.height
            )
            
            // 绘制矩形条
            drawRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

/**
 * 音频波形圈
 */
@Composable
fun AudioWaveCircle(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1),
    secondaryColor: Color = Color(0xFFEC4899)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_circle")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val baseRadius = minOf(centerX, centerY) * 0.5f
        
        val pointCount = 60
        
        // 外圈波形
        val outerPath = Path()
        for (i in 0..pointCount) {
            val angle = (i * 360f / pointCount) + rotation
            val rad = Math.toRadians(angle.toDouble())
            
            val amplitude = if (amplitudes.isNotEmpty()) {
                amplitudes[i % amplitudes.size]
            } else {
                sin(angle * 3 * PI.toFloat() / 180f) * 0.3f + 0.5f
            }
            
            val radius = baseRadius + (amplitude * 30f)
            val x = centerX + cos(rad).toFloat() * radius
            val y = centerY + sin(rad).toFloat() * radius
            
            if (i == 0) {
                outerPath.moveTo(x, y)
            } else {
                outerPath.lineTo(x, y)
            }
        }
        outerPath.close()
        
        drawPath(
            path = outerPath,
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.3f),
                    secondaryColor.copy(alpha = 0.1f)
                ),
                center = Offset(centerX, centerY),
                radius = baseRadius + 40f
            )
        )
        
        // 内圈
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor,
                    primaryColor.copy(alpha = 0.5f)
                ),
                center = Offset(centerX, centerY),
                radius = baseRadius * 0.4f
            ),
            radius = baseRadius * 0.4f,
            center = Offset(centerX, centerY)
        )
        
        // 中心点
        drawCircle(
            color = Color.White,
            radius = 8f,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * 音频跳动的圆点
 */
@Composable
fun JumpingDots(
    count: Int = 3,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF6366F1)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jumping_dots")
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(count) { index ->
            val delay = index * 200
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(12.dp * scale)
                    .background(color, shape = androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}

/**
 * 加载动画集合
 */
@Composable
fun LoadingAnimations(
    type: LoadingAnimationType,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF6366F1)
) {
    when (type) {
        LoadingAnimationType.JUMPING_DOTS -> JumpingDots(color = color, modifier = modifier)
        LoadingAnimationType.SPECTRUM_BARS -> SpectrumBars(
            amplitudes = List(16) { kotlin.random.Random.nextFloat() },
            modifier = modifier,
            barColor = color
        )
        LoadingAnimationType.WAVE_CIRCLE -> AudioWaveCircle(
            amplitudes = List(32) { kotlin.random.Random.nextFloat() },
            modifier = modifier,
            primaryColor = color
        )
        LoadingAnimationType.BREATHING -> BreathingLightEffect(
            color = color,
            modifier = modifier
        )
        LoadingAnimationType.ROTATING_DISC -> RotatingDiscAnimation(modifier = modifier)
    }
}

/**
 * 旋转唱片动画
 */
@Composable
fun RotatingDiscAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc"
    )

    Canvas(modifier = modifier) {
        rotate(rotation) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Gray,
                        Color.DarkGray,
                        Color.Black
                    )
                ),
                radius = size.minDimension / 2
            )
            // 唱片纹路
            for (i in 1..5) {
                drawCircle(
                    color = Color.DarkGray.copy(alpha = 0.5f),
                    radius = size.minDimension / 2 * (i / 6f),
                    style = Stroke(width = 1f)
                )
            }
            // 中心孔
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = center
            )
        }
    }
}

enum class LoadingAnimationType {
    JUMPING_DOTS,
    SPECTRUM_BARS,
    WAVE_CIRCLE,
    BREATHING,
    ROTATING_DISC
}
