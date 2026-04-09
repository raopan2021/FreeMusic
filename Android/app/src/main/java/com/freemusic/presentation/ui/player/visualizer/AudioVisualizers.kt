package com.freemusic.presentation.ui.player.visualizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

/**
 * 3D频谱可视化器
 */
@Composable
fun Spectrum3DVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF6366F1),
    barCount: Int = 32
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spectrum_3d")
    
    val animatedAmplitudes = remember {
        mutableStateListOf<Float>().apply {
            repeat(barCount) { add(0.1f) }
        }
    }
    
    LaunchedEffect(amplitudes) {
        if (amplitudes.isNotEmpty()) {
            amplitudes.forEachIndexed { index, amp ->
                if (index < animatedAmplitudes.size) {
                    animatedAmplitudes[index] = amp.coerceIn(0.1f, 1f)
                }
            }
        }
    }
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val barWidth = size.width / barCount
        val maxBarHeight = size.height * 0.8f
        
        animatedAmplitudes.forEachIndexed { index, amplitude ->
            val x = index * barWidth
            
            // 3D效果 - 多层叠加
            for (layer in 0..2) {
                val layerOffset = layer * 3f
                val alpha = 1f - (layer * 0.25f)
                val depth = 8f - (layer * 2f)
                
                // 计算高度
                val waveOffset = sin(phase + index * 0.3f) * 0.1f
                val height = (amplitude + waveOffset) * maxBarHeight
                
                // 绘制3D柱体
                val color = barColor.copy(alpha = alpha)
                
                // 正面
                drawRect(
                    color = color,
                    topLeft = Offset(x + layerOffset, size.height - height),
                    size = Size(barWidth * 0.7f, height)
                )
                
                // 顶面
                drawRect(
                    color = color.copy(alpha = alpha * 0.8f),
                    topLeft = Offset(x + layerOffset, size.height - height),
                    size = Size(barWidth * 0.7f, 4f)
                )
                
                // 侧面
                drawRect(
                    color = color.copy(alpha = alpha * 0.6f),
                    topLeft = Offset(x + layerOffset + barWidth * 0.7f, size.height - height),
                    size = Size(depth, height)
                )
            }
        }
    }
}

/**
 * 圆形玫瑰花瓣可视化器
 */
@Composable
fun CircularRoseVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1),
    secondaryColor: Color = Color(0xFFEC4899)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circular_rose")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = minOf(centerX, centerY) * 0.8f
        
        // 绘制多个花瓣层
        for (layer in 0..3) {
            val layerAlpha = 1f - (layer * 0.2f)
            val layerRadius = maxRadius * (1f - layer * 0.15f)
            val petalCount = 6 + layer * 2
            
            for (i in 0 until petalCount) {
                val angle = (i * 360f / petalCount) + rotation + (layer * 15f)
                val amplitude = if (amplitudes.isNotEmpty()) {
                    amplitudes[i % amplitudes.size]
                } else 0.3f
                
                val petalLength = layerRadius * (0.5f + amplitude * 0.5f)
                val petalWidth = 20f + amplitude * 30f
                
                val path = Path().apply {
                    val startAngle = Math.toRadians(angle - 90.0)
                    val midAngle = Math.toRadians(angle - 45.0)
                    val endAngle = Math.toRadians(angle.toDouble())
                    
                    val startX = centerX + cos(startAngle).toFloat() * layerRadius * 0.3f
                    val startY = centerY + sin(startAngle).toFloat() * layerRadius * 0.3f
                    
                    val tipX = centerX + cos(endAngle).toFloat() * petalLength
                    val tipY = centerY + sin(endAngle).toFloat() * petalLength
                    
                    val controlX = centerX + cos(midAngle).toFloat() * petalLength * 0.7f
                    val controlY = centerY + sin(midAngle).toFloat() * petalLength * 0.7f
                    
                    moveTo(startX, startY)
                    quadraticBezierTo(controlX, controlY, tipX, tipY)
                    quadraticBezierTo(
                        centerX + cos(Math.toRadians(angle + 45.0)).toFloat() * petalLength * 0.7f,
                        centerY + sin(Math.toRadians(angle + 45.0)).toFloat() * petalLength * 0.7f,
                        startX, startY
                    )
                    close()
                }
                
                val gradient = Brush.radialGradient(
                    colors = listOf(
                        if (layer % 2 == 0) primaryColor else secondaryColor,
                        (if (layer % 2 == 0) primaryColor else secondaryColor).copy(alpha = 0.3f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = petalLength
                )
                
                drawPath(
                    path = path,
                    brush = gradient,
                    alpha = layerAlpha * (0.5f + amplitude * 0.5f)
                )
            }
        }
        
        // 中心圆
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.8f),
                    primaryColor.copy(alpha = 0.2f)
                ),
                center = Offset(centerX, centerY),
                radius = maxRadius * 0.2f
            ),
            radius = maxRadius * 0.15f,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * 波浪线可视化器
 */
@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    waveColor: Color = Color(0xFF6366F1),
    backgroundColor: Color = Color.Transparent
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
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
        val centerY = size.height / 2
        val maxAmplitude = size.height / 2 * 0.8f
        
        // 背景
        if (backgroundColor != Color.Transparent) {
            drawRect(backgroundColor)
        }
        
        // 绘制多层波浪
        for (layer in 0..2) {
            val layerPhase = phase + (layer * PI.toFloat() / 3f)
            val layerAlpha = 1f - (layer * 0.3f)
            val layerAmplitude = maxAmplitude * (1f - layer * 0.2f)
            
            val path = Path().apply {
                moveTo(0f, centerY)
                
                for (x in 0..size.width.toInt() step 2) {
                    val index = x.toFloat() / size.width
                    val amp = if (amplitudes.isNotEmpty()) {
                        amplitudes[(x / 2) % amplitudes.size]
                    } else 0.3f
                    
                    val wave = sin(index * 8 * PI.toFloat() + layerPhase) * amp
                    val y = centerY - (wave * layerAmplitude)
                    
                    if (x == 0) {
                        moveTo(x.toFloat(), y)
                    } else {
                        lineTo(x.toFloat(), y)
                    }
                }
            }
            
            drawPath(
                path = path,
                color = waveColor.copy(alpha = layerAlpha),
                style = Stroke(width = 3f - layer)
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
    primaryColor: Color = Color(0xFF6366F1),
    particleCount: Int = 60
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particle_ring")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = minOf(centerX, centerY) * 0.7f
        
        // 绘制多个圆环
        for (ring in 0..2) {
            val ringRadius = maxRadius * (0.6f + ring * 0.2f)
            val ringAlpha = 1f - (ring * 0.3f)
            
            // 绘制粒子点
            for (i in 0 until particleCount / 3) {
                val angle = (i * 360f / (particleCount / 3)) + rotation + (ring * 30f)
                val amplitude = if (amplitudes.isNotEmpty()) {
                    amplitudes[i % amplitudes.size]
                } else 0.5f
                
                val rad = Math.toRadians(angle.toDouble())
                val distance = ringRadius + (amplitude * 30f * pulse)
                
                val x = centerX + cos(rad).toFloat() * distance
                val y = centerY + sin(rad).toFloat() * distance
                
                val particleSize = 4f + amplitude * 8f
                
                // 绘制粒子光晕
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor,
                            primaryColor.copy(alpha = 0f)
                        ),
                        center = Offset(x, y),
                        radius = particleSize * 2
                    ),
                    radius = particleSize * 2,
                    center = Offset(x, y)
                )
                
                // 绘制粒子核心
                drawCircle(
                    color = Color.White,
                    radius = particleSize * 0.3f,
                    center = Offset(x, y)
                )
            }
            
            // 绘制连接线
            for (i in 0 until particleCount / 3) {
                val angle1 = (i * 360f / (particleCount / 3)) + rotation + (ring * 30f)
                val amplitude1 = if (amplitudes.isNotEmpty()) amplitudes[i % amplitudes.size] else 0.5f
                
                val rad1 = Math.toRadians(angle1.toDouble())
                val dist1 = ringRadius + (amplitude1 * 30f * pulse)
                
                val x1 = centerX + cos(rad1).toFloat() * dist1
                val y1 = centerY + sin(rad1).toFloat() * dist1
                
                // 连接到下一个点
                val nextI = (i + 1) % (particleCount / 3)
                val angle2 = ((nextI) * 360f / (particleCount / 3)) + rotation + (ring * 30f)
                val amplitude2 = if (amplitudes.isNotEmpty()) amplitudes[nextI % amplitudes.size] else 0.5f
                
                val rad2 = Math.toRadians(angle2.toDouble())
                val dist2 = ringRadius + (amplitude2 * 30f * pulse)
                
                val x2 = centerX + cos(rad2).toFloat() * dist2
                val y2 = centerY + sin(rad2).toFloat() * dist2
                
                drawLine(
                    color = primaryColor.copy(alpha = ringAlpha * 0.3f * amplitude1),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 1f
                )
            }
        }
        
        // 中心发光
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.5f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = maxRadius * 0.3f
            ),
            radius = maxRadius * 0.3f * pulse,
            center = Offset(centerX, centerY)
        )
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
    
    val globalPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "global_pulse"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = minOf(centerX, centerY) * 0.8f
        
        val avgAmplitude = if (amplitudes.isNotEmpty()) amplitudes.average().toFloat() else 0.5f
        
        // 绘制多个脉冲环
        for (ring in 5 downTo 0) {
            val ringDelay = ring * 0.15f
            val animatedScale = globalPulse - ringDelay
            val ringRadius = maxRadius * animatedScale
            
            val ringAlpha = ((1f - ringDelay) * 0.3f * avgAmplitude).coerceIn(0f, 0.5f)
            
            drawCircle(
                color = primaryColor.copy(alpha = ringAlpha),
                radius = ringRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 3f)
            )
        }
        
        // 频谱条
        val barCount = 8
        val barLength = maxRadius * 0.3f
        
        for (i in 0 until barCount) {
            val angle = (i * 360f / barCount) - 90f
            val amplitude = if (amplitudes.isNotEmpty()) {
                amplitudes[i % amplitudes.size]
            } else 0.5f
            
            val rad = Math.toRadians(angle.toDouble())
            val barHeight = barLength * amplitude
            
            val startX = centerX + cos(rad).toFloat() * maxRadius * 0.5f
            val startY = centerY + sin(rad).toFloat() * maxRadius * 0.5f
            val endX = centerX + cos(rad).toFloat() * (maxRadius * 0.5f + barHeight)
            val endY = centerY + sin(rad).toFloat() * (maxRadius * 0.5f + barHeight)
            
            drawLine(
                color = primaryColor,
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
                    primaryColor,
                    primaryColor.copy(alpha = 0.3f)
                )
            ),
            radius = maxRadius * 0.15f,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * 音频数据生成器（模拟）
 */
object AudioDataGenerator {
    
    fun generateSineWave(sampleCount: Int, frequency: Float = 0.1f): List<Float> {
        return (0 until sampleCount).map { i ->
            val t = i.toFloat() / sampleCount
            (sin(t * frequency * 2 * PI.toFloat()) + 1f) / 2f
        }
    }
    
    fun generateMusicWave(sampleCount: Int): List<Float> {
        return (0 until sampleCount).map { i ->
            val base = Random.nextFloat() * 0.3f
            val wave1 = sin(i.toFloat() * 0.2f) * 0.3f
            val wave2 = sin(i.toFloat() * 0.5f) * 0.2f
            val noise = Random.nextFloat() * 0.2f
            (base + wave1 + wave2 + noise).coerceIn(0.1f, 1f)
        }
    }
    
    fun generateBeatWave(sampleCount: Int, bpm: Float = 120f): List<Float> {
        val beatInterval = 60f / bpm
        return (0 until sampleCount).map { i ->
            val t = i.toFloat() / sampleCount
            val beatPhase = (t % beatInterval) / beatInterval
            val beat = if (beatPhase < 0.1f) 1f else 0.2f
            (beat + Random.nextFloat() * 0.2f).coerceIn(0.1f, 1f)
        }
    }
    
    fun generateFrequencyBars(barCount: Int, intensity: Float = 1f): List<Float> {
        return (0 until barCount).map { i ->
            val base = Random.nextFloat()
            val frequency = sin(i.toFloat() / barCount * PI.toFloat())
            (base * 0.3f + frequency * 0.5f + 0.2f) * intensity
        }
    }
}
