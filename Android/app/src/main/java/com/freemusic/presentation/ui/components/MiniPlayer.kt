package com.freemusic.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.freemusic.domain.model.Song

/**
 * 迷你播放器组件
 */
@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    progress: Float, // 0-1
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    if (song == null) return
    
    // 脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "mini_player")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // 进度条动画
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "progress"
    )
    
    // 封面缩放动画
    val coverScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cover_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .navigationBarsPadding()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column {
            // 进度条
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = primaryColor,
                trackColor = primaryColor.copy(alpha = 0.2f)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 封面（带动画）
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer {
                            scaleX = coverScale
                            scaleY = coverScale
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .shadow(if (isPlaying) 8.dp else 4.dp, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = song.coverUrl,
                        contentDescription = "专辑封面",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // 播放时的高光效果
                    if (isPlaying) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = pulseAlpha * 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 歌曲信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 控制按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 播放/暂停按钮
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = primaryColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    // 下一首按钮
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "下一首",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 歌词迷你播放器（带歌词片段）
 */
@Composable
fun LyricMiniPlayer(
    song: Song?,
    currentLyric: String?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    if (song == null) return
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "progress"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.1f)
        )
    ) {
        Column {
            // 渐变进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    primaryColor,
                                    primaryColor.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 封面
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = "专辑封面",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 歌词和信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // 当前歌词（如果有）
                    if (currentLyric != null) {
                        Text(
                            text = currentLyric,
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.graphicsLayer {
                                alpha = 0.8f
                            }
                        )
                    }
                }
                
                // 控制按钮
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        tint = primaryColor
                    )
                }
                
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "下一首",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 3D 悬浮效果的迷你播放器
 */
@Composable
fun FloatingMiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    if (song == null) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "progress"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = floatOffset
                shadowElevation = 16f
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column {
            // 彗星进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val progressWidth = size.width * animatedProgress
                    val gradient = Brush.horizontalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.1f),
                            primaryColor.copy(alpha = 0.4f),
                            primaryColor.copy(alpha = 0.8f),
                            primaryColor
                        )
                    )
                    // 绘制进度条背景
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.1f),
                                primaryColor.copy(alpha = 0.1f)
                            )
                        )
                    )
                    // 绘制进度
                    if (progressWidth > 0) {
                        drawRect(brush = gradient, size = Size(progressWidth, size.height))
                        // 彗星头部发光效果
                        drawCircle(
                            color = primaryColor,
                            radius = size.height * 2f,
                            center = Offset(progressWidth, size.height / 2)
                        )
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.5f),
                            radius = size.height * 4f,
                            center = Offset(progressWidth, size.height / 2)
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 封面（带3D倾斜效果）
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .graphicsLayer {
                            rotationY = if (isPlaying) {
                                kotlin.math.sin(System.currentTimeMillis() / 500.0).toFloat() * 5f
                            } else 0f
                            cameraDistance = 12f * density
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = song.coverUrl,
                        contentDescription = "专辑封面",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 歌曲信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 控制按钮组
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPlayPause) {
                        Surface(
                            shape = CircleShape,
                            color = primaryColor
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "暂停" else "播放",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(28.dp)
                            )
                        }
                    }
                    
                    IconButton(onClick = onNext) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "下一首",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
