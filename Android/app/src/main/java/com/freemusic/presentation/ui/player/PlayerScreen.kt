package com.freemusic.presentation.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.freemusic.data.preferences.CoverStyleType
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.player.cover.AnimatedAlbumCover
import com.freemusic.presentation.ui.player.cover.CoverStyle
import com.freemusic.presentation.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
    // Settings
    particlesEnabled: Boolean = true,
    particleIntensity: Float = 1f,
    coverStyleType: CoverStyleType = CoverStyleType.ROUND,
    visualizerEnabled: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    // 启动进度更新
    LaunchedEffect(Unit) {
        viewModel.startProgressUpdates()
    }

    // 平滑背景颜色过渡
    val animatedBgColor by animateColorAsState(
        targetValue = if (uiState.currentSong != null) Color(0xFF1A1A2E) else Color(0xFF121212),
        animationSpec = tween(500),
        label = "bg_color"
    )

    // 封面样式映射
    val coverStyle = when (coverStyleType) {
        CoverStyleType.ROUND -> CoverStyle.ROUND
        CoverStyleType.SQUARE -> CoverStyle.SQUARE
        CoverStyleType.SQUARE_NO_ROUND -> CoverStyle.SQUARE_NO_ROUND
        CoverStyleType.DIAMOND -> CoverStyle.DIAMOND
        CoverStyleType.BORDER_ROUND -> CoverStyle.BORDER_ROUND
        CoverStyleType.HEXAGON -> CoverStyle.HEXAGON
        CoverStyleType.PARALLELOGRAM -> CoverStyle.PARALLELOGRAM
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedBgColor)
    ) {
        // 模糊的背景封面
        if (uiState.currentSong?.coverUrl != null) {
            AsyncImage(
                model = uiState.currentSong?.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(80.dp),
                contentScale = ContentScale.Crop
            )

            // 渐变遮罩
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 顶部导航栏
            TopAppBar(
                title = { Text("正在播放", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onQueueClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "播放队列",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 专辑封面（带动画）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedAlbumCover(
                    coverUrl = uiState.currentSong?.coverUrl,
                    isPlaying = uiState.isPlaying,
                    coverStyle = coverStyle,
                    showVisualizer = visualizerEnabled,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 歌曲信息
            SongInfo(
                song = uiState.currentSong,
                isFavorite = uiState.isFavorite,
                onFavoriteClick = viewModel::toggleFavorite,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 进度条
            ProgressBar(
                currentPosition = uiState.currentPosition,
                duration = uiState.duration,
                onSeek = viewModel::seekTo
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 控制按钮
            PlayerControls(
                isPlaying = uiState.isPlaying,
                onPlayPause = viewModel::togglePlayPause,
                onSkipToNext = viewModel::skipToNext,
                onSkipToPrevious = viewModel::skipToPrevious
            )

            Spacer(modifier = Modifier.weight(1f))

            // 歌词显示区域
            LyricsSection(
                lyrics = uiState.lyrics?.lrc,
                currentPosition = uiState.currentPosition
            )
        }

        // 加载指示器
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
private fun SongInfo(
    song: Song?,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = song?.title ?: "未选择歌曲",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = song?.artist ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        // 收藏按钮
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavorite) "取消收藏" else "收藏",
                tint = if (isFavorite) Color.Red else Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ProgressBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(currentPosition, isDragging) {
        if (!isDragging && duration > 0) {
            sliderPosition = currentPosition.toFloat() / duration.toFloat()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = sliderPosition,
            onValueChange = { value ->
                isDragging = true
                sliderPosition = value
            },
            onValueChangeFinished = {
                isDragging = false
                onSeek((sliderPosition * duration).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSkipToNext: () -> Unit,
    onSkipToPrevious: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 上一首
        IconButton(onClick = onSkipToPrevious) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "上一首",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        // 播放/暂停
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
        }

        // 下一首
        IconButton(onClick = onSkipToNext) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "下一首",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun LyricsSection(
    lyrics: String?,
    currentPosition: Long
) {
    val lines = remember(lyrics) {
        lyrics?.lines()
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { parseLyricLine(it) }
            ?: emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        if (lines.isEmpty()) {
            Text(
                text = "暂无歌词",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // 找到当前行
            val currentLineIndex = lines.indexOfLast { it.timeMs <= currentPosition }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                if (currentLineIndex >= 0 && currentLineIndex < lines.size) {
                    // 上一行（淡出）
                    if (currentLineIndex > 0) {
                        Text(
                            text = lines[currentLineIndex - 1].text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 当前行（高亮）
                    Text(
                        text = lines[currentLineIndex].text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 下一行（淡出）
                    if (currentLineIndex < lines.size - 1) {
                        Text(
                            text = lines[currentLineIndex + 1].text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

private fun parseLyricLine(line: String): com.freemusic.domain.model.LyricLine? {
    val regex = Regex("""\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?\]""")
    val match = regex.find(line) ?: return null

    val minutes = match.groupValues[1].toIntOrNull() ?: return null
    val seconds = match.groupValues[2].toIntOrNull() ?: return null
    val millis = when (match.groupValues[3].length) {
        2 -> (match.groupValues[3].toIntOrNull() ?: 0) * 10
        3 -> match.groupValues[3].toIntOrNull() ?: 0
        else -> 0
    }

    val timeMs = (minutes * 60 + seconds) * 1000L + millis
    val text = line.substringAfter("]").trim()

    return com.freemusic.domain.model.LyricLine(timeMs = timeMs, text = text, endTimeMs = null)
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
