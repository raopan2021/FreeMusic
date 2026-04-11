package com.freemusic.presentation.ui.player.controls

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 播放进度滑块
 */
@Composable
fun ProgressSlider(
    progress: Float,
    currentTime: String,
    totalTime: String,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = progress,
            onValueChange = onSeek,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = totalTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 播放器控制按钮
 */
@Composable
fun PlayerControls(
    isPlaying: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    repeatMode: PlayRepeatMode,
    isShuffleEnabled: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRepeatToggle: () -> Unit,
    onShuffleToggle: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 随机播放
        IconButton(onClick = onShuffleToggle) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "随机播放",
                tint = if (isShuffleEnabled) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        
        // 上一首
        IconButton(
            onClick = onPrevious,
            enabled = hasPrevious
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "上一首",
                modifier = Modifier.size(36.dp),
                tint = if (hasPrevious) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
        
        // 播放/暂停
        AnimatedPlayButton(
            isPlaying = isPlaying,
            onClick = onPlayPause,
            size = 72.dp,
            primaryColor = primaryColor
        )
        
        // 下一首
        IconButton(
            onClick = onNext,
            enabled = hasNext
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "下一首",
                modifier = Modifier.size(36.dp),
                tint = if (hasNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
        
        // 循环模式
        IconButton(onClick = onRepeatToggle) {
            Icon(
                imageVector = when (repeatMode) {
                    PlayRepeatMode.OFF -> Icons.Default.Repeat
                    PlayRepeatMode.ALL -> Icons.Default.Repeat
                    PlayRepeatMode.ONE -> Icons.Default.RepeatOne
                },
                contentDescription = "循环模式",
                tint = when (repeatMode) {
                    PlayRepeatMode.OFF -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else -> primaryColor
                }
            )
        }
    }
}

/**
 * 动画播放按钮
 */
@Composable
fun AnimatedPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    primaryColor: Color = PrimaryIndigo
) {
    val infiniteTransition = rememberInfiniteTransition(label = "play_button")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) pulseScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor,
                        primaryColor.copy(alpha = 0.8f)
                    )
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "暂停" else "播放",
            tint = Color.White,
            modifier = Modifier.size(size * 0.4f)
        )
    }
}

/**
 * 快捷操作栏
 */
@Composable
fun QuickActionsBar(
    isFavorite: Boolean,
    isBluetoothConnected: Boolean,
    onFavoriteToggle: () -> Unit,
    onShare: () -> Unit,
    onQueue: () -> Unit,
    onLyricsToggle: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 收藏
        IconButton(onClick = onFavoriteToggle) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "收藏",
                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
            )
        }
        
        // 歌词
        IconButton(onClick = onLyricsToggle) {
            Icon(
                imageVector = Icons.Default.Lyrics,
                contentDescription = "歌词",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // 蓝牙
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = "蓝牙",
                tint = if (isBluetoothConnected) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        
        // 队列
        IconButton(onClick = onQueue) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "播放队列",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 播放速度选择器
 */
@Composable
fun SpeedSelector(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = "播放速度",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        
        speeds.forEach { speed ->
            val isSelected = currentSpeed == speed
            
            Surface(
                onClick = { onSpeedChange(speed) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${speed}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * 音量控制条
 */
@Composable
fun VolumeControl(
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

enum class PlayRepeatMode {
    OFF, ALL, ONE
}
