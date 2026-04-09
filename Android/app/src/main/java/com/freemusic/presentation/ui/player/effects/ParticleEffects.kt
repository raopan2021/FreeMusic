package com.freemusic.presentation.ui.player.effects

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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * 樱花飘落效果
 */
@Composable
fun SakuraEffect(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    petalCount: Int = 30,
    primaryColor: Color = Color(0xFFFFB7C5)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sakura")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    if (!isPlaying) return
    
    Box(modifier = modifier.fillMaxSize()) {
        repeat(petalCount) { index ->
            val offsetX = (sin(index * 137.5f) * 0.5f + 0.5f)
            val offsetY = ((time + index * 0.033f) % 1f)
            val size = 8f + (index % 5) * 2f
            val rotation = time * 360f * (if (index % 2 == 0) 1 else -1)
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = (offsetX * 400).dp, y = (offsetY * 800).dp)
                        .size(size.dp)
                        .rotate(rotation)
                        .background(
                            color = primaryColor.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

/**
 * 星空闪烁效果
 */
@Composable
fun StarfieldEffect(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    starCount: Int = 100,
    primaryColor: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "starfield")
    
    if (!isPlaying) return
    
    Box(modifier = modifier.fillMaxSize()) {
        repeat(starCount) { index ->
            val infiniteTransition2 = rememberInfiniteTransition(label = "star_$index")
            val alpha by infiniteTransition2.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1000 + (index % 2000),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha_$index"
            )
            
            val x = (sin(index * 137.5f) * 0.5f + 0.5f) * 400
            val y = (cos(index * 137.5f) * 0.5f + 0.5f) * 800
            val size = 1f + (index % 3)
            
            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size(size.dp)
                    .background(
                        color = primaryColor.copy(alpha = alpha),
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * 雨滴效果
 */
@Composable
fun RainEffect(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    dropCount: Int = 50,
    primaryColor: Color = Color(0xFF6B9ACA)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    if (!isPlaying) return
    
    Box(modifier = modifier.fillMaxSize()) {
        repeat(dropCount) { index ->
            val offsetX = (sin(index * 137.5f) * 0.5f + 0.5f)
            val speed = 0.5f + (index % 3) * 0.25f
            val offsetY = ((time * speed + index * 0.02f) % 1f)
            
            Box(
                modifier = Modifier
                    .offset(x = (offsetX * 400).dp, y = (offsetY * 800).dp)
                    .size(2.dp, 15.dp)
                    .background(
                        color = primaryColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}

/**
 * 萤火虫效果
 */
@Composable
fun FireflyEffect(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    flyCount: Int = 20,
    primaryColor: Color = Color(0xFFFFD93D)
) {
    if (!isPlaying) return
    
    Box(modifier = modifier.fillMaxSize()) {
        repeat(flyCount) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "firefly_$index")
            val time by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3000 + (index * 500) % 4000,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "time_$index"
            )
            
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha_$index"
            )
            
            val x = (sin(time * 2 * PI + index) * 0.3f + 0.5f) * 400
            val y = (cos(time * 3 * PI + index) * 0.3f + 0.5f) * 800
            
            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size(4.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = alpha),
                                primaryColor.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * 极光效果
 */
@Composable
fun AuroraEffect(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF00FF88)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    if (!isPlaying) return
    
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.3f * (sin(time * 2 * PI).toFloat() * 0.5f + 0.5f)),
                            Color.Transparent
                        )
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00FFFF).copy(alpha = 0.2f * (cos(time * 3 * PI).toFloat() * 0.5f + 0.5f)),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

/**
 * 彩虹效果
 */
@Composable
fun RainbowEffect(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFFFF0000)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "time"
    )
    
    if (!isPlaying) return
    
    Box(modifier = modifier.fillMaxSize()) {
        val colors = listOf(
            Color.Red,
            Color(0xFFFF7F00),
            Color.Yellow,
            Color.Green,
            Color.Blue,
            Color(0xFF8B00FF)
        )
        
        colors.forEachIndexed { index, color ->
            val offset = (index - 2.5f) * 20 * (time * 0.5f + 0.5f)
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = offset.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

/**
 * 泡泡效果
 */
@Composable
fun BubbleEffect(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    bubbleCount: Int = 25,
    primaryColor: Color = Color(0xFF6DD5ED)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    if (!isPlaying) return
    
    Box(modifier = modifier.fillMaxSize()) {
        repeat(bubbleCount) { index ->
            val x = (sin(index * 137.5f) * 0.5f + 0.5f)
            val baseY = (cos(index * 137.5f) * 0.5f + 0.5f)
            val y = ((baseY - time + 1f) % 1f)
            val size = 5f + (index % 10) * 3f
            
            Box(
                modifier = Modifier
                    .offset(x = (x * 400).dp, y = (y * 800).dp)
                    .size(size.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.3f),
                                primaryColor.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * 烟雾效果
 */
@Composable
fun SmokeEffect(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color.Gray
) {
    val infiniteTransition = rememberInfiniteTransition(label = "smoke")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    if (!isPlaying) return
    
    Box(modifier = modifier.fillMaxSize()) {
        repeat(5) { index ->
            val x = 200f + sin(time * 2 * PI + index) * 100
            val y = 400f + cos(time * 3 * PI + index) * 200
            
            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size((50 + index * 20).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * 火焰效果
 */
@Composable
fun FlameEffect(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFFFF6B6B)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "time"
    )
    
    if (!isPlaying) return
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        primaryColor.copy(alpha = 0.1f * time),
                        primaryColor.copy(alpha = 0.2f * time)
                    )
                )
            )
    ) {
        // 火焰粒子
        repeat(20) { index ->
            val infiniteTransition2 = rememberInfiniteTransition(label = "flame_$index")
            val phase by infiniteTransition2.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000 + index * 100),
                    repeatMode = RepeatMode.Restart
                ),
                label = "phase_$index"
            )
            
            val x = 200f + sin(index * 0.5f) * 50
            val y = 600f - phase * 400
            
            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size((10 + index % 10).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Yellow.copy(alpha = 0.8f),
                                primaryColor.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * 波浪效果
 */
@Composable
fun WaveEffect(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF2193B0)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    if (!isPlaying) return
    
    Box(modifier = modifier.fillMaxSize()) {
        repeat(3) { waveIndex ->
            val offset = waveIndex * 0.3f
            val alpha = 0.3f - waveIndex * 0.1f
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (sin((time + offset) * 2 * PI) * 20).dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = alpha),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
