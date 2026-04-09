package com.freemusic.presentation.ui.player.cover

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.math.cos
import kotlin.math.sin

/**
 * 专辑封面样式枚举
 */
enum class CoverStyle {
    ROUND,           // 圆形
    SQUARE,          // 圆角方形
    SQUARE_NO_ROUND, // 方形
    DIAMOND,         // 菱形
    BORDER_ROUND,    // 带边框的圆角
    HEXAGON,         // 六边形
    PARALLELOGRAM,   // 平行四边形
}

/**
 * 专辑封面组件（带多种动画效果）
 */
@Composable
fun AnimatedAlbumCover(
    coverUrl: String?,
    isPlaying: Boolean,
    coverStyle: CoverStyle = CoverStyle.ROUND,
    showVisualizer: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 旋转动画（播放时旋转）
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

    // 脉冲动画
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // 呼吸动画（缩放+透明度）
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // 阴影动画
    val shadowElevation by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadow"
    )

    val currentRotation = if (isPlaying) rotation else 0f
    val currentScale = if (isPlaying) pulseScale else 1f
    val currentBreathe = if (isPlaying) breatheScale else 1f

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = currentRotation
                scaleX = currentScale * currentBreathe
                scaleY = currentScale * currentBreathe
                cameraDistance = 8f * density
            },
        contentAlignment = Alignment.Center
    ) {
        when (coverStyle) {
            CoverStyle.ROUND -> {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "专辑封面",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .shadow(shadowElevation.dp, CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            CoverStyle.SQUARE -> {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "专辑封面",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .shadow(shadowElevation.dp, RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            CoverStyle.SQUARE_NO_ROUND -> {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "专辑封面",
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(shadowElevation.dp, RectangleShape),
                    contentScale = ContentScale.Crop
                )
            }

            CoverStyle.DIAMOND -> {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "专辑封面",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = 45f
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .shadow(shadowElevation.dp, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            CoverStyle.BORDER_ROUND -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(4.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .shadow(shadowElevation.dp, CircleShape)
                ) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = "专辑封面",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            CoverStyle.HEXAGON -> {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "专辑封面",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(HexagonShape)
                        .shadow(shadowElevation.dp, HexagonShape),
                    contentScale = ContentScale.Crop
                )
            }

            CoverStyle.PARALLELOGRAM -> {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = "专辑封面",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(ParallelogramShape)
                        .shadow(shadowElevation.dp, ParallelogramShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // 音频可视化叠加层
        if (showVisualizer && isPlaying) {
            AudioVisualizerOverlay(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * 六边形 Shape
 */
val HexagonShape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val w = size.width
            val h = size.height
            val side = minOf(w, h) / 2f
            val centerX = w / 2f
            val centerY = h / 2f

            val points = List(6) { i ->
                val angle = Math.toRadians((60.0 * i - 30).toDouble())
                Offset(
                    (centerX + side * cos(angle)).toFloat(),
                    (centerY + side * sin(angle)).toFloat()
                )
            }

            moveTo(points[0].x, points[0].y)
            points.forEachIndexed { index, point ->
                if (index > 0) {
                    lineTo(point.x, point.y)
                }
            }
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * 平行四边形 Shape
 */
val ParallelogramShape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val skew = size.width * 0.15f
        val path = Path().apply {
            moveTo(skew, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - skew, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * 音频可视化叠加层（模拟）
 * 实际使用需要接入 Media3 的 AudioSession
 */
@Composable
fun AudioVisualizerOverlay(
    modifier: Modifier = Modifier,
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
        val maxHeight = size.height * 0.25f

        bars.forEachIndexed { index, _ ->
            val offsetX = index * barWidth + barWidth / 2
            
            val phase = index * 0.4f + time * 10f
            val barHeight = maxHeight * (0.2f + 0.8f * ((sin(phase.toDouble()) + 1) / 2).toFloat() * intensity)
            
            val hue = (index * 30f + time * 60f) % 360f
            val color = Color.hsv(hue, 0.6f, 0.8f, 0.5f)
            
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
 * 环形脉冲动画
 */
@Composable
fun RingPulseEffect(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    color: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    
    val rings = remember {
        List(3) { it }
    }

    Canvas(modifier = modifier) {
        if (!isPlaying) return@Canvas

        rings.forEach { index ->
            val progress = (System.currentTimeMillis() / 1000f + index * 0.3f) % 1f
            val radius = size.minDimension / 2 * (0.8f + progress * 0.4f)
            val alpha = (1f - progress) * 0.3f

            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}

/**
 * 专辑封面切换过渡动画
 */
@Composable
fun CoverTransition(
    currentCoverUrl: String?,
    nextCoverUrl: String?,
    progress: Float, // 0 = 当前, 1 = 下一首
    coverStyle: CoverStyle = CoverStyle.ROUND
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "cover_transition"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 当前封面淡出 + 缩小
        if (animatedProgress < 1f) {
            AsyncImage(
                model = currentCoverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 1f - animatedProgress
                        scaleX = 1f - animatedProgress * 0.2f
                        scaleY = 1f - animatedProgress * 0.2f
                    }
                    .then(getCoverModifier(coverStyle)),
                contentScale = ContentScale.Crop
            )
        }

        // 下一封面上淡入 + 放大
        if (animatedProgress > 0f) {
            AsyncImage(
                model = nextCoverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = animatedProgress
                        scaleX = 0.8f + animatedProgress * 0.2f
                        scaleY = 0.8f + animatedProgress * 0.2f
                    }
                    .then(getCoverModifier(coverStyle)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

private fun getCoverModifier(coverStyle: CoverStyle): Modifier {
    return when (coverStyle) {
        CoverStyle.ROUND -> Modifier.clip(CircleShape)
        CoverStyle.SQUARE -> Modifier.clip(RoundedCornerShape(16.dp))
        CoverStyle.SQUARE_NO_ROUND -> Modifier
        CoverStyle.DIAMOND -> Modifier.graphicsLayer { rotationZ = 45f }
        CoverStyle.BORDER_ROUND -> Modifier.border(3.dp, Color.White, CircleShape)
        CoverStyle.HEXAGON -> Modifier.clip(HexagonShape)
        CoverStyle.PARALLELOGRAM -> Modifier.clip(ParallelogramShape)
    }
}

/**
 * 毛玻璃封面效果
 */
@Composable
fun BlurredAlbumCover(
    coverUrl: String?,
    modifier: Modifier = Modifier,
    blurRadius: Float = 30f,
    isPlaying: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blur_pulse")
    
    val pulseBlur by infiniteTransition.animateFloat(
        initialValue = blurRadius,
        targetValue = blurRadius * 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blur_pulse"
    )

    AsyncImage(
        model = coverUrl,
        contentDescription = null,
        modifier = modifier.blur((if (isPlaying) pulseBlur else blurRadius).dp),
        contentScale = ContentScale.Crop
    )
}

/**
 * 黑胶唱片机效果
 */
@Composable
fun VinylRecordEffect(
    coverUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isPlaying) 3000 else 100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 唱片外圈
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF2D2D2D),
                            Color(0xFF1A1A1A)
                        )
                    ),
                    shape = CircleShape
                )
        ) {
            // 唱片纹路
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (i in 0..20) {
                    val radius = size.minDimension / 2 * (0.3f + i * 0.03f)
                    drawCircle(
                        color = Color(0xFF333333),
                        radius = radius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.5f.dp.toPx())
                    )
                }
            }
        }

        // 中心封面
        AsyncImage(
            model = coverUrl,
            contentDescription = "专辑封面",
            modifier = Modifier
                .fillMaxSize(0.45f)
                .clip(CircleShape)
                .rotate(rotation)
        )

        // 中心圆孔
        Box(
            modifier = Modifier
                .fillMaxSize(0.08f)
                .background(Color(0xFF1A1A1A), CircleShape)
        )
    }
}
