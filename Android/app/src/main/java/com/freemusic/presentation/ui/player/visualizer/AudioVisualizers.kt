package com.freemusic.presentation.ui.player.visualizer

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * 3D频谱可视化器
 */
@Composable
fun SpectrumBars3D(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF6366F1),
    barCount: Int = 32
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spectrum")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "time"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val barWidth = size.width / barCount
            val maxHeight = size.height * 0.8f
            
            for (index in 0 until barCount) {
                val amplitude = amplitudes.getOrElse(index) { 0.3f }
                val animatedAmp = amplitude * (0.7f + time * 0.3f * ((index % 3) + 1) / 3f)
                
                val barHeight = maxHeight * animatedAmp.coerceIn(0.1f, 1f)
                
                // 3D效果 - 背面
                drawRect(
                    color = barColor.copy(alpha = 0.3f),
                    topLeft = Offset(index * barWidth + 2, size.height - barHeight + 4),
                    size = Size(barWidth - 4, barHeight)
                )
                
                // 主条
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(barColor, barColor.copy(alpha = 0.5f)),
                        startY = size.height - barHeight,
                        endY = size.height
                    ),
                    topLeft = Offset(index * barWidth, size.height - barHeight),
                    size = Size(barWidth - 4, barHeight)
                )
            }
        }
    }
}

/**
 * 圆形玫瑰图可视化器
 */
@Composable
fun AudioRoseChart(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rose")
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
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(250.dp)
                .rotate(rotation)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val maxRadius = size.minDimension / 2 - 20
            
            for (index in 0 until 64) {
                val angle = (index * 360f / 64) * (PI / 180f)
                val amplitude = amplitudes.getOrElse(index) { 0.5f }
                val radius = maxRadius * amplitude.coerceIn(0.1f, 1f)
                
                val x = centerX + radius * cos(angle).toFloat()
                val y = centerY + radius * sin(angle).toFloat()
                
                val particleSize = 8f + amplitude * 12f
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.8f),
                            primaryColor.copy(alpha = 0.2f)
                        ),
                        center = Offset(centerX, centerY),
                        radius = maxRadius
                    ),
                    radius = particleSize,
                    center = Offset(x, y)
                )
            }
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.3f))
                ),
                radius = 30f,
                center = Offset(centerX, centerY)
            )
        }
    }
}

/**
 * 波形可视化器
 */
@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    waveColor: Color = Color(0xFF6366F1)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerY = size.height / 2
            val amplitude = size.height * 0.3f
            
            val path = Path()
            path.moveTo(0f, centerY)
            
            for (x in 0..size.width.toInt() step 2) {
                val normalizedX = x / size.width
                val wave1 = sin((normalizedX * 4 * PI + phase).toDouble()).toFloat() * amplitude * 0.5f
                val wave2 = sin((normalizedX * 8 * PI + phase * 1.5).toDouble()).toFloat() * amplitude * 0.3f
                
                val y = centerY + wave1 + wave2
                path.lineTo(x.toFloat(), y)
            }
            
            drawPath(
                path = path,
                color = waveColor,
                style = Stroke(width = 3f)
            )
        }
    }
}

/**
 * 粒子环可视化器
 */
@Composable
fun ParticleRingVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particle_ring")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(300.dp)) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val baseRadius = size.minDimension / 3
            
            for (ring in 0..2) {
                val ringRadius = baseRadius + ring * 30
                val particleCount = 32 + ring * 8
                
                for (index in 0 until particleCount) {
                    val angle = (index * 360f / particleCount + time * 360 * (if (ring % 2 == 0) 1 else -1)) * (PI / 180f)
                    val wobble = sin((time * 4 * PI + index * 0.5).toDouble()).toFloat() * 10f
                    val radius = ringRadius + wobble
                    
                    val x = centerX + radius * cos(angle).toFloat()
                    val y = centerY + radius * sin(angle).toFloat()
                    
                    val amplitude = amplitudes.getOrElse(index % amplitudes.size) { 0.5f }
                    val particleSize = 4f + amplitude * 8f
                    
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.8f - ring * 0.2f),
                                Color.Transparent
                            ),
                            center = Offset(x, y),
                            radius = particleSize * 2
                        ),
                        radius = particleSize,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

/**
 * 脉冲环可视化器
 */
@Composable
fun PulseRingVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        val avgAmplitude = amplitudes.take(8).average().toFloat()
        
        for (ringIndex in 0..2) {
            val ringPulse = (pulse + ringIndex * 0.33f) % 1f
            val alpha = 0.6f - ringIndex * 0.15f - ringPulse * 0.3f
            val scale = 0.3f + ringIndex * 0.2f + ringPulse * 0.5f + avgAmplitude * 0.2f
            
            Canvas(modifier = Modifier.size(250.dp)) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val maxRadius = size.minDimension / 2 * scale
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = alpha.coerceAtLeast(0.1f)),
                            primaryColor.copy(alpha = 0f)
                        )
                    ),
                    radius = maxRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}

/**
 * 音频波形圆形
 */
@Composable
fun AudioWaveCircle(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
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
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .rotate(rotation)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val baseRadius = size.minDimension / 3
            
            val barCount = amplitudes.size.coerceIn(16, 64)
            
            for (i in 0 until barCount) {
                val angle = (i * 360f / barCount) * (PI / 180f)
                val amplitude = amplitudes.getOrElse(i % amplitudes.size) { 0.5f }
                val barLength = baseRadius * amplitude.coerceIn(0.2f, 1f)
                
                val startX = centerX + baseRadius * cos(angle).toFloat()
                val startY = centerY + baseRadius * sin(angle).toFloat()
                val endX = centerX + (baseRadius + barLength) * cos(angle).toFloat()
                val endY = centerY + (baseRadius + barLength) * sin(angle).toFloat()
                
                drawLine(
                    color = primaryColor.copy(alpha = 0.5f + amplitude * 0.5f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 4f + amplitude * 4f
                )
            }
        }
    }
}
