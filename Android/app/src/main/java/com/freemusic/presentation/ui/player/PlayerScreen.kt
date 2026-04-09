package com.freemusic.presentation.ui.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.freemusic.domain.model.LyricLine
import com.freemusic.domain.model.Song
import com.freemusic.presentation.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 启动进度更新
    LaunchedEffect(Unit) {
        viewModel.startProgressUpdates()
    }

    // 获取当前播放歌曲的封面作为背景
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val gradientColors = remember(uiState.currentSong?.coverUrl) {
        listOf(surfaceColor, surfaceVariantColor)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(gradientColors)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 顶部导航栏
            TopAppBar(
                title = { Text("正在播放") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 专辑封面
            AlbumCover(
                coverUrl = uiState.currentSong?.coverUrl,
                isPlaying = uiState.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 歌曲信息
            SongInfo(
                song = uiState.currentSong,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 进度条
            ProgressBar(
                currentPosition = uiState.currentPosition,
                duration = uiState.duration,
                onSeek = viewModel::seekTo
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 控制按钮
            PlayerControls(
                isPlaying = uiState.isPlaying,
                onPlayPause = viewModel::togglePlayPause,
                onSkipToNext = viewModel::skipToNext,
                onSkipToPrevious = viewModel::skipToPrevious
            )

            Spacer(modifier = Modifier.weight(1f))

            // 歌词显示区域（简化版）
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
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun AlbumCover(
    coverUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = "专辑封面",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 播放时显示的圆环效果
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f))
            )
        }
    }
}

@Composable
private fun SongInfo(
    song: Song?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = song?.title ?: "未选择歌曲",
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = song?.artist ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
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
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                modifier = Modifier.size(40.dp)
            )
        }

        // 播放/暂停
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                modifier = Modifier.size(40.dp)
            )
        }

        // 下一首
        IconButton(onClick = onSkipToNext) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "下一首",
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
            .height(150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        if (lines.isEmpty()) {
            Text(
                text = "暂无歌词",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Text(
                        text = lines[currentLineIndex].text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun parseLyricLine(line: String): LyricLine? {
    // 解析 [mm:ss.xx] 格式的时间戳
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

    return LyricLine(timeMs = timeMs, text = text, endTimeMs = null)
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
