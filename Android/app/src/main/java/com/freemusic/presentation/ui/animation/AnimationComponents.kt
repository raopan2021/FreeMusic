package com.freemusic.presentation.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.freemusic.presentation.ui.theme.PrimaryIndigo
import kotlin.math.cos
import kotlin.math.sin

/**
 * 封面旋转动画
 */
@Composable
fun CoverRotationAnimation(
    coverUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cover_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 外圈
        Box(
            modifier = Modifier
                .size(280.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.3f),
                            primaryColor.copy(alpha = 0.1f),
                            primaryColor.copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // 封面
        Box(
            modifier = Modifier
                .size(240.dp)
                .rotate(if (isPlaying) rotation else 0f)
                .clip(CircleShape)
                .shadow(8.dp, CircleShape)
        ) {
            if (coverUrl != null) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(primaryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }
    }
}

/**
 * 粒子效果
 */
@Composable
fun ParticleEffect(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 20,
    primaryColor: Color = PrimaryIndigo
) {
    val particles = remember {
        List(particleCount) {
            Particle(
                x = (Math.random() * 300).toFloat(),
                y = (Math.random() * 500).toFloat(),
                radius = (Math.random() * 4 + 2).toFloat(),
                speed = (Math.random() * 2 + 1).toFloat()
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    if (isPlaying) {
        Canvas(modifier = modifier.fillMaxSize()) {
            particles.forEach { particle ->
                val animatedY = particle.y - (animationProgress * 200 * particle.speed)
                val alpha = (1f - animationProgress).coerceIn(0f, 1f)
                
                drawCircle(
                    color = primaryColor.copy(alpha = alpha * 0.6f),
                    radius = particle.radius.dp.toPx(),
                    center = Offset(
                        x = particle.x.dp.toPx() + sin(animatedY * 0.05f) * 20.dp.toPx(),
                        y = animatedY.dp.toPx()
                    )
                )
            }
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float
)

/**
 * 音频可视化条
 */
@Composable
fun AudioVisualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    primaryColor: Color = PrimaryIndigo
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    
    val animations = (0 until barCount).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 8f,
            targetValue = (30 + Math.random() * 20).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (300 + index * 100).toInt(),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isPlaying) {
            animations.forEach { animState ->
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(animState.value.dp)
                        .background(
                            primaryColor,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        } else {
            repeat(barCount) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(8.dp)
                        .background(
                            primaryColor.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

/**
 * 波形动画
 */
@Composable
fun WaveformAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    Canvas(modifier = modifier.size(100.dp, 40.dp)) {
        val centerY = size.height / 2
        
        for (i in 0..20) {
            val x = (i * size.width / 20)
            val amplitude = if (isPlaying) {
                (sin((phase + i * 0.4).toDouble()) * 12).toFloat() + 12f
            } else {
                4f
            }
            
            drawLine(
                color = primaryColor.copy(alpha = if (isPlaying) 0.8f else 0.3f),
                start = Offset(x, centerY - amplitude),
                end = Offset(x, centerY + amplitude),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

/**
 * 渐变背景动画
 */
@Composable
fun AnimatedGradientBackground(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isPlaying) {
                        listOf(
                            primaryColor.copy(alpha = 0.3f + animatedOffset * 0.1f),
                            MaterialTheme.colorScheme.background,
                            primaryColor.copy(alpha = 0.2f + animatedOffset * 0.1f)
                        )
                    } else {
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    }
                )
            )
    )
}

/**
 * 播放按钮动画
 */
@Composable
fun AnimatedPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    primaryColor: Color = PrimaryIndigo
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .scale(scale)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor,
                        primaryColor.copy(alpha = 0.8f)
                    )
                ),
                shape = CircleShape
            )
            .shadow(8.dp, CircleShape)
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "暂停" else "播放",
            tint = Color.White,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}

/**
 * 心跳动画
 */
@Composable
fun HeartbeatAnimation(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale1"
    )
    
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, delayMillis = 150),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale2"
    )
    
    if (isActive) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier
                    .size((24 * scale1).dp)
                    .graphicsLayer {
                        alpha = (1f - (scale1 - 1f) / 0.3f).coerceIn(0.5f, 1f)
                    }
            )
            
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.7f),
                modifier = Modifier
                    .size((24 * scale2).dp)
                    .graphicsLayer {
                        alpha = (1f - (scale2 - 1f) / 0.3f).coerceIn(0.3f, 0.7f)
                    }
            )
        }
    } else {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = primaryColor.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 进度环动画
 */
@Composable
fun AnimatedProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp,
    primaryColor: Color = PrimaryIndigo
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "progress"
    )
    
    Canvas(modifier = modifier.size(size)) {
        val sweepAngle = animatedProgress * 360f
        
        // 背景环
        drawArc(
            color = primaryColor.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
        
        // 进度环
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

/**
 * 滑动手势指示器
 */
@Composable
fun SwipeHintIndicator(
    direction: SwipeDirection,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val offsetX by animateFloatAsState(
        targetValue = when (direction) {
            SwipeDirection.LEFT -> -20f
            SwipeDirection.RIGHT -> 20f
            SwipeDirection.UP, SwipeDirection.DOWN -> 0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )
    
    val offsetY by animateFloatAsState(
        targetValue = when (direction) {
            SwipeDirection.UP -> -20f
            SwipeDirection.DOWN -> 20f
            SwipeDirection.LEFT, SwipeDirection.RIGHT -> 0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    
    Box(
        modifier = modifier
            .offset(x = offsetX.dp, y = offsetY.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (direction) {
                SwipeDirection.LEFT -> Icons.Default.ChevronLeft
                SwipeDirection.RIGHT -> Icons.Default.ChevronRight
                SwipeDirection.UP -> Icons.Default.KeyboardArrowUp
                SwipeDirection.DOWN -> Icons.Default.KeyboardArrowDown
            },
            contentDescription = null,
            tint = primaryColor.copy(alpha = 0.5f),
            modifier = Modifier.size(32.dp)
        )
    }
}

enum class SwipeDirection {
    LEFT, RIGHT, UP, DOWN
}

/**
 * 淡入淡出过渡
 */
@Composable
fun FadeInOutContent(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "fade"
    )
    
    Box(
        modifier = Modifier.graphicsLayer { this.alpha = alpha }
    ) {
        content()
    }
}

/**
 * 缩放进入动画
 */
@Composable
fun ScaleInContent(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}
