package com.freemusic.presentation.ui.player.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 3D旋转木马动画
 */
@Composable
fun CarouselAnimation(
    songs: List<Song>,
    currentIndex: Int,
    isPlaying: Boolean,
    onSongClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val infiniteTransition = rememberInfiniteTransition(label = "carousel")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                }
        ) {
            songs.forEachIndexed { index, song ->
                val angle = (index * 360f / songs.size)
                val isCurrent = index == currentIndex
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = angle
                            translationX = if (isCurrent) 0f else 50f
                            alpha = if (isCurrent) 1f else 0.5f
                        }
                        .offset(x = if (isCurrent) 0.dp else 30.dp)
                        .clickable { onSongClick(index) }
                ) {
                    AsyncImage(
                        model = song.coverUrl,
                        contentDescription = "封面",
                        modifier = Modifier
                            .size(if (isCurrent) 200.dp else 150.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

/**
 * 镜像反射动画
 */
@Composable
fun MirrorReflectionAnimation(
    song: Song?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mirror")
    val reflectionAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reflection"
    )
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 主封面
        AsyncImage(
            model = song?.coverUrl,
            contentDescription = "封面",
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        // 反射
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(y = 260.dp)
                .graphicsLayer {
                    scaleX = 1f
                    scaleY = -0.3f
                    alpha = if (isPlaying) reflectionAlpha else 0.1f
                }
                .clip(CircleShape)
        ) {
            AsyncImage(
                model = song?.coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        // 反射遮罩
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .offset(y = 200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

/**
 * 呼吸灯动画
 */
@Composable
fun BreathingLightAnimation(
    song: Song?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        // 外层光晕
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .size((280 * scale).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = glowAlpha),
                                primaryColor.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        // 主封面
        AsyncImage(
            model = song?.coverUrl,
            contentDescription = "封面",
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .graphicsLayer {
                    scaleX = if (isPlaying) scale else 1f
                    scaleY = if (isPlaying) scale else 1f
                },
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * 频谱柱动画
 */
@Composable
fun SpectrumBarsAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo,
    barCount: Int = 32
) {
    if (!isPlaying) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "spectrum")
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(200.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(barCount) { index ->
                val animDelay = index * 50
                
                val height by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = (0.3f + Math.random().toFloat() * 0.7f),
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 300 + animDelay,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bar_$index"
                )
                
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height((height * 200).dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor,
                                    primaryColor.copy(alpha = 0.5f)
                                )
                            ),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
            }
        }
    }
}

/**
 * 旋转唱片动画
 */
@Composable
fun RotatingDiscAnimation(
    song: Song?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val infiniteTransition = rememberInfiniteTransition(label = "disc")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 唱片底
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.DarkGray,
                            Color.Black
                        )
                    ),
                    shape = CircleShape
                )
        ) {
            // 外圈
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2D2D2D),
                                Color(0xFF1A1A1A)
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                // 中心圆
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Center)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.8f),
                                    primaryColor.copy(alpha = 0.4f)
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    // 封面
                    AsyncImage(
                        model = song?.coverUrl,
                        contentDescription = "封面",
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        
        // 旋转的封面
        if (isPlaying) {
            AsyncImage(
                model = song?.coverUrl,
                contentDescription = "封面",
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .graphicsLayer {
                        rotationZ = rotation
                    },
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * 脉冲圆环动画
 */
@Composable
fun PulseRingAnimation(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    if (!isPlaying) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f + index * 0.2f,
                targetValue = 1.5f + index * 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "scale_$index"
            )
            
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.5f - index * 0.15f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "alpha_$index"
            )
            
            Box(
                modifier = Modifier
                    .size((150 * scale).dp)
                    .background(
                        color = primaryColor.copy(alpha = alpha),
                        shape = CircleShape
                    )
            )
        }
    }
}
