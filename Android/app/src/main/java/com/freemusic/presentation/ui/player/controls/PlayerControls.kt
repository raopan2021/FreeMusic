package com.freemusic.presentation.ui.player.controls

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 播放控制按钮组
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
    primaryColor: Color = Color(0xFF6366F1)
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 随机播放
        IconButton(
            onClick = onShuffleToggle,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "随机播放",
                tint = if (isShuffleEnabled) primaryColor else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 上一首
        IconButton(
            onClick = onPrevious,
            enabled = hasPrevious,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "上一首",
                tint = if (hasPrevious) Color.White else Color.Gray,
                modifier = Modifier.size(40.dp)
            )
        }
        
        // 播放/暂停按钮（放大版）
        PlayPauseButton(
            isPlaying = isPlaying,
            onClick = onPlayPause,
            primaryColor = primaryColor,
            modifier = Modifier.size(80.dp)
        )
        
        // 下一首
        IconButton(
            onClick = onNext,
            enabled = hasNext,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "下一首",
                tint = if (hasNext) Color.White else Color.Gray,
                modifier = Modifier.size(40.dp)
            )
        }
        
        // 循环模式
        IconButton(
            onClick = onRepeatToggle,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = when (repeatMode) {
                    PlayRepeatMode.ALL -> Icons.Default.Repeat
                    PlayRepeatMode.ONE -> Icons.Default.RepeatOne
                    PlayRepeatMode.OFF -> Icons.Default.Repeat
                },
                contentDescription = "循环模式",
                tint = if (repeatMode != PlayRepeatMode.OFF) primaryColor else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 播放/暂停按钮
 */
@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "play_pause")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
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
            .then(
                Modifier.drawBehind {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val centerX = canvasWidth / 2
                    val centerY = canvasHeight / 2
                    val radius = minOf(centerX, centerY) * scale
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor, primaryColor.copy(alpha = 0.8f)),
                            center = Offset(centerX, centerY),
                            radius = radius
                        ),
                        radius = radius,
                        center = Offset(centerX, centerY)
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "暂停" else "播放",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )
    }
}

/**
 * 进度条控制
 */
@Composable
fun ProgressSlider(
    progress: Float, // 0-1
    currentTime: String,
    totalTime: String,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    var sliderPosition by remember { mutableFloatStateOf(progress) }
    var isDragging by remember { mutableStateOf(false) }
    
    LaunchedEffect(progress) {
        if (!isDragging) {
            sliderPosition = progress
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                isDragging = true
                sliderPosition = newValue
            },
            onValueChangeFinished = {
                isDragging = false
                onSeek(sliderPosition)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor,
                inactiveTrackColor = primaryColor.copy(alpha = 0.3f)
            )
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = totalTime,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

/**
 * 音量控制
 */
@Composable
fun VolumeControl(
    volume: Float, // 0-1
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
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
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor,
                inactiveTrackColor = primaryColor.copy(alpha = 0.3f)
            )
        )
        
        Text(
            text = "${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.width(40.dp)
        )
    }
}

/**
 * 播放速度控制
 */
@Composable
fun SpeedControl(
    currentSpeed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    val speeds = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "播放速度",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        
        speeds.forEach { speed ->
            val isSelected = currentSpeed == speed
            Surface(
                onClick = { onSpeedChange(speed) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) primaryColor else Color.Gray.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${speed}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) Color.White else Color.Gray,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * 耳机/蓝牙设备信息
 */
@Composable
fun AudioOutputInfo(
    context: Context,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val audioDevice = remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        // 获取当前音频输出设备
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            audioDevice.value = devices.firstOrNull()?.productName?.toString()
        }
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when {
            audioDevice.value?.contains("Bluetooth", ignoreCase = true) == true -> Icons.Default.Bluetooth
            audioDevice.value?.contains("Headset", ignoreCase = true) == true -> Icons.Default.Headphones
            else -> Icons.Default.Speaker
        }
        
        Icon(
            imageVector = icon,
            contentDescription = "音频输出",
            tint = primaryColor,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = audioDevice.value ?: "手机扬声器",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

/**
 * 收藏按钮
 */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "favorite")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "favorite_scale"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isFavorite) pulseScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animated_scale"
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorite) "取消收藏" else "收藏",
            tint = if (isFavorite) Color.Red else Color.Gray,
            modifier = Modifier.size(28.dp * animatedScale)
        )
    }
}

/**
 * 更多操作按钮
 */
@Composable
fun MoreActionsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "更多",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
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
    primaryColor: Color = Color(0xFF6366F1)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 收藏
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onFavoriteToggle)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "收藏",
                tint = if (isFavorite) Color.Red else Color.Gray
            )
            Text(
                text = "喜欢",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        
        // 分享
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onShare)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "分享",
                tint = Color.Gray
            )
            Text(
                text = "分享",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        
        // 歌词
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onLyricsToggle)
        ) {
            Icon(
                imageVector = Icons.Default.Lyrics,
                contentDescription = "歌词",
                tint = primaryColor
            )
            Text(
                text = "歌词",
                style = MaterialTheme.typography.labelSmall,
                color = primaryColor
            )
        }
        
        // 播放队列
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onQueue)
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "播放队列",
                tint = Color.Gray
            )
            Text(
                text = "队列",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

enum class PlayRepeatMode {
    OFF, ALL, ONE
}
