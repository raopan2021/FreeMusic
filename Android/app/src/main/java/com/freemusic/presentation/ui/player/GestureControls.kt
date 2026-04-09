package com.freemusic.presentation.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.freemusic.presentation.ui.theme.PrimaryIndigo
import kotlin.math.abs

/**
 * 手势控制播放器
 */
@Composable
fun GesturePlayerControl(
    currentVolume: Float,
    currentProgress: Float,
    onVolumeChange: (Float) -> Unit,
    onSeek: (Float) -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    var showGestureOverlay by remember { mutableStateOf(false) }
    var gestureType by remember { mutableStateOf(GestureType.NONE) }
    var gestureValue by remember { mutableFloatStateOf(0f) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        if (gestureType == GestureType.SEEK && gestureValue != currentProgress) {
                            onSeek(gestureValue)
                        }
                        showGestureOverlay = false
                        gestureType = GestureType.NONE
                        gestureValue = 0f
                    },
                    onDragCancel = {
                        showGestureOverlay = false
                        gestureType = GestureType.NONE
                        gestureValue = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val horizontal = abs(dragAmount.x) > abs(dragAmount.y)
                    
                    if (gestureType == GestureType.NONE) {
                        gestureType = if (horizontal) GestureType.SEEK else GestureType.VOLUME
                        gestureValue = if (horizontal) currentProgress else currentVolume
                        showGestureOverlay = true
                    }
                    
                    val delta = if (horizontal) dragAmount.x / size.width else -dragAmount.y / size.height
                    gestureValue = (gestureValue + delta).coerceIn(0f, 1f)
                    
                    if (gestureType == GestureType.VOLUME) {
                        onVolumeChange(gestureValue)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onPlayPause()
                    }
                )
            }
    ) {
        if (showGestureOverlay) {
            GestureOverlay(
                gestureType = gestureType,
                value = gestureValue,
                primaryColor = primaryColor
            )
        }
    }
}

enum class GestureType {
    NONE, VOLUME, SEEK
}

@Composable
private fun GestureOverlay(
    gestureType: GestureType,
    value: Float,
    primaryColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (gestureType) {
                    GestureType.VOLUME -> Icons.Default.VolumeUp
                    GestureType.SEEK -> Icons.Default.FastForward
                    GestureType.NONE -> Icons.Default.TouchApp
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when (gestureType) {
                    GestureType.VOLUME -> "音量"
                    GestureType.SEEK -> "进度"
                    GestureType.NONE -> ""
                },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                color = primaryColor
            )
        }
    }
}

/**
 * 滑块式音量控制
 */
@Composable
fun VolumeSlider(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when {
                volume == 0f -> Icons.Default.VolumeOff
                volume < 0.5f -> Icons.Default.VolumeDown
                else -> Icons.Default.VolumeUp
            },
            contentDescription = "音量",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor
            )
        )
        
        Text(
            text = "${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.width(40.dp)
        )
    }
}

/**
 * 播放速度控制
 */
@Composable
fun PlaybackSpeedControl(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = "播放速度",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        speeds.forEach { s ->
            val isSelected = abs(speed - s) < 0.01f
            Surface(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { onSpeedChange(s) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${s}x",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * 播放模式选择器
 */
@Composable
fun PlaybackModeSelector(
    repeatMode: com.freemusic.presentation.ui.player.controls.PlayRepeatMode,
    isShuffleEnabled: Boolean,
    onRepeatToggle: () -> Unit,
    onShuffleToggle: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShuffleToggle) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "随机播放",
                tint = if (isShuffleEnabled) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        
        IconButton(onClick = onRepeatToggle) {
            Icon(
                imageVector = when (repeatMode) {
                    com.freemusic.presentation.ui.player.controls.PlayRepeatMode.OFF -> Icons.Default.Repeat
                    com.freemusic.presentation.ui.player.controls.PlayRepeatMode.ALL -> Icons.Default.Repeat
                    com.freemusic.presentation.ui.player.controls.PlayRepeatMode.ONE -> Icons.Default.RepeatOne
                },
                contentDescription = "循环模式",
                tint = when (repeatMode) {
                    com.freemusic.presentation.ui.player.controls.PlayRepeatMode.OFF -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else -> primaryColor
                }
            )
        }
    }
}
