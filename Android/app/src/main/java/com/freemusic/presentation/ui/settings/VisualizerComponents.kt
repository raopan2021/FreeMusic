package com.freemusic.presentation.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * 简易频谱可视化
 * 显示不同频率的强度
 */
@Composable
fun EqualizerVisualizer(
    modifier: Modifier = Modifier,
    frequencies: List<Float> = listOf(0.3f, 0.5f, 0.7f, 0.4f, 0.6f, 0.8f, 0.5f, 0.3f),
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
) {
    Canvas(modifier = modifier) {
        val barWidth = size.width / (frequencies.size * 2)
        val maxHeight = size.height
        
        frequencies.forEachIndexed { index, frequency ->
            val barHeight = maxHeight * frequency.coerceIn(0f, 1f)
            val x = barWidth + index * barWidth * 2
            
            // 渐变效果
            val color = if (frequency > 0.7f) primaryColor else secondaryColor
            
            // 绘制圆角矩形条
            drawRoundRect(
                color = color,
                topLeft = Offset(x, maxHeight - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }
}

/**
 * 波形可视化
 * 显示音频波形
 */
@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    waveform: List<Float> = (0 until 32).map { sin(it * 0.3).toFloat() * 0.5f + 0.5f },
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        val centerY = size.height / 2
        val stepX = size.width / (waveform.size - 1)
        
        waveform.forEachIndexed { index, amplitude ->
            val x = index * stepX
            val barHeight = size.height * amplitude * 0.8f
            
            // 绘制中心对称的条形
            drawLine(
                color = primaryColor,
                start = Offset(x, centerY - barHeight / 2),
                end = Offset(x, centerY + barHeight / 2),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * 圆形可视化
 * 显示环形频谱
 */
@Composable
fun CircularVisualizer(
    modifier: Modifier = Modifier,
    frequencies: List<Float> = List(12) { (Math.random() * 0.5 + 0.3).toFloat() },
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val baseRadius = minOf(size.width, size.height) * 0.3f
        val maxBarHeight = minOf(size.width, size.height) * 0.15f
        
        frequencies.forEachIndexed { index, frequency ->
            val angle = (index.toFloat() / frequencies.size) * 2 * Math.PI - Math.PI / 2
            val barHeight = maxBarHeight * frequency
            
            val startX = centerX + baseRadius * kotlin.math.cos(angle).toFloat()
            val startY = centerY + baseRadius * kotlin.math.sin(angle).toFloat()
            val endX = centerX + (baseRadius + barHeight) * kotlin.math.cos(angle).toFloat()
            val endY = centerY + (baseRadius + barHeight) * kotlin.math.sin(angle).toFloat()
            
            val color = if (frequency > 0.6f) primaryColor else secondaryColor
            
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
