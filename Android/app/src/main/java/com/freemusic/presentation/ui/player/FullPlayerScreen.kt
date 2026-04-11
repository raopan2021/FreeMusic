package com.freemusic.presentation.ui.player

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.player.animation.*
import com.freemusic.presentation.ui.player.controls.*
import com.freemusic.presentation.ui.player.lyrics.*
import com.freemusic.presentation.ui.player.visualizer.*
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 完整播放器屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    currentTime: String,
    totalTime: String,
    isFavorite: Boolean,
    repeatMode: PlayRepeatMode,
    isShuffleEnabled: Boolean,
    lyrics: List<com.freemusic.domain.model.LyricLine>,
    currentLyricIndex: Int,
    particleEffect: String,
    coverStyle: String,
    onBackClick: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onFavoriteToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onShuffleToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLyrics by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    
    val primaryColor = PrimaryIndigo
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.3f),
                        Color.Black,
                        Color.Black
                    )
                )
            )
    ) {
        // 粒子效果背景
        ParticleBackground(
            effect = particleEffect,
            isPlaying = isPlaying,
            modifier = Modifier.fillMaxSize()
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 顶部栏
            TopAppBar(
                title = { Text("正在播放", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showQueue = true }) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "播放队列",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { /* TODO: 分享功能 */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "分享",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            // 专辑封面
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                AlbumCoverView(
                    coverUrl = song?.coverUrl,
                    style = coverStyle,
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
            
            // 歌曲信息
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song?.title ?: "未知歌曲",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song?.artist ?: "未知艺术家",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "收藏",
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 进度条
            ProgressSlider(
                progress = progress,
                currentTime = currentTime,
                totalTime = totalTime,
                onSeek = onSeek,
                modifier = Modifier.padding(horizontal = 32.dp),
                primaryColor = primaryColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 控制按钮
            PlayerControls(
                isPlaying = isPlaying,
                hasPrevious = true,
                hasNext = true,
                repeatMode = repeatMode,
                isShuffleEnabled = isShuffleEnabled,
                onPlayPause = onPlayPause,
                onPrevious = onPrevious,
                onNext = onNext,
                onRepeatToggle = onRepeatToggle,
                onShuffleToggle = onShuffleToggle,
                modifier = Modifier.padding(horizontal = 16.dp),
                primaryColor = primaryColor
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 底部快捷操作
            QuickActionsBar(
                isFavorite = isFavorite,
                isBluetoothConnected = false,
                onFavoriteToggle = onFavoriteToggle,
                onShare = { },
                onQueue = { showQueue = true },
                onLyricsToggle = { showLyrics = !showLyrics },
                modifier = Modifier.padding(bottom = 32.dp),
                primaryColor = primaryColor
            )
        }
        
        // 歌词面板
        AnimatedVisibility(
            visible = showLyrics,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                color = Color.Black.copy(alpha = 0.9f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                KaraokeLyricsView(
                    lyrics = lyrics,
                    currentLineIndex = currentLyricIndex,
                    primaryColor = primaryColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // 播放队列
        if (showQueue) {
            ModalBottomSheet(
                onDismissRequest = { showQueue = false }
            ) {
                Text("播放队列", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

/**
 * 专辑封面视图
 */
@Composable
fun AlbumCoverView(
    coverUrl: String?,
    style: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cover")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 外层光晕
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PrimaryIndigo.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        // 封面图片
        AsyncImage(
            model = coverUrl,
            contentDescription = "专辑封面",
            modifier = Modifier
                .fillMaxSize(if (isPlaying) 0.9f else 1f)
                .graphicsLayer {
                    if (isPlaying && style == "ROUND") {
                        rotationZ = rotation
                    }
                }
                .clip(
                    when (style) {
                        "ROUND" -> CircleShape
                        "SQUARE" -> RoundedCornerShape(16.dp)
                        "DIAMOND" -> RoundedCornerShape(8.dp)
                        else -> CircleShape
                    }
                ),
            contentScale = ContentScale.Crop
        )
        
        // 播放中指示器
        if (isPlaying) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(8.dp)
            )
        }
    }
}

/**
 * 粒子效果背景
 */
@Composable
fun ParticleBackground(
    effect: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    // 粒子效果可在此扩展
    // 目前使用可视化器代替
}

/**
 * 音频可视化器叠加层
 */
@Composable
fun VisualizerOverlay(
    style: String,
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    when (style) {
        "SPECTRUM" -> SpectrumBars3D(
            amplitudes = amplitudes,
            modifier = modifier,
            barColor = primaryColor
        )
        "CIRCULAR" -> AudioWaveCircle(
            amplitudes = amplitudes,
            modifier = modifier,
            primaryColor = primaryColor
        )
        "PULSE" -> PulseRingVisualizer(
            amplitudes = amplitudes,
            modifier = modifier,
            primaryColor = primaryColor
        )
        else -> { /* 默认无可视化 */ }
    }
}
