package com.freemusic.presentation.ui.player

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.freemusic.data.preferences.CoverStyleType
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.player.cover.AnimatedAlbumCover
import com.freemusic.presentation.ui.player.cover.CoverStyle
import com.freemusic.presentation.viewmodel.PlayerViewModel
import com.freemusic.util.ShakeDetector
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit,
    onQueueClick: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
    // Settings
    particlesEnabled: Boolean = true,
    particleIntensity: Float = 1f,
    coverStyleType: CoverStyleType = CoverStyleType.ROUND,
    visualizerEnabled: Boolean = false,
    shakeToSkipEnabled: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // 摇一摇检测器
    val shakeDetector = remember { 
        ShakeDetector(context) { viewModel.skipToNext() } 
    }
    
    LaunchedEffect(shakeToSkipEnabled) {
        if (shakeToSkipEnabled) {
            shakeDetector.start()
        } else {
            shakeDetector.stop()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            shakeDetector.stop()
        }
    }

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
    
    // 分享处理器
    val handleShare: () -> Unit = {
        uiState.currentSong?.let { song ->
            val title = song.title
            val artist = song.artist
            val shareText = "🎵 $title - $artist\n\n来自 FreeMusic"
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "分享歌曲")
            context.startActivity(shareIntent)
        }
    }
    
    // 页面状态 - 用于 HorizontalPager（0=歌曲介绍, 1=完整歌词）
    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = 0)
    
    // 用于 Pager 动画的 CoroutineScope
    val pagerScope = rememberCoroutineScope()
    
    // 是否可以滑动到歌词页面（需要歌词加载完成）
    val canScrollToLyrics = remember {
        derivedStateOf {
            // 只有当歌词已加载且不为空时才允许滑到歌词页面
            pagerState.currentPage == 0 || (uiState.lyrics?.lrc != null && uiState.lyrics?.lrc!!.isNotBlank())
        }
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
                    .blur(30.dp),
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

            // 可滑动的2个页面：0=歌曲介绍(含3行歌词), 1=完整歌词
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    // 左侧页面：歌曲介绍 + 3行歌词
                    0 -> SongIntroductionPage(
                        song = uiState.currentSong,
                        isFavorite = uiState.isFavorite,
                        onFavoriteClick = viewModel::toggleFavorite,
                        coverUrl = uiState.currentSong?.coverUrl,
                        isPlaying = uiState.isPlaying,
                        coverStyle = coverStyle,
                        lyrics = uiState.lyrics?.lrc,
                        currentPosition = uiState.currentPosition
                    )
                    // 右侧页面：完整歌词
                    1 -> FullLyricsPage(
                        lyrics = uiState.lyrics?.lrc,
                        currentPosition = uiState.currentPosition,
                        canAccess = canScrollToLyrics.value,
                        onBackToSong = { 
                            pagerScope.launch { 
                                pagerState.animateScrollToPage(0) 
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 进度条
            ProgressBar(
                currentPosition = uiState.currentPosition,
                duration = uiState.duration,
                onSeek = viewModel::seekTo
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 控制按钮（添加底部导航栏padding防止被遮挡）
            PlayerControls(
                isPlaying = uiState.isPlaying,
                onPlayPause = viewModel::togglePlayPause,
                onSkipToNext = viewModel::skipToNext,
                onSkipToPrevious = viewModel::skipToPrevious,
                onShareClick = handleShare,
                modifier = Modifier.navigationBarsPadding()
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

/**
 * 左侧页面：歌曲介绍 + 3行歌词
 */
@Composable
private fun SongIntroductionPage(
    song: Song?,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    coverUrl: String?,
    isPlaying: Boolean,
    coverStyle: CoverStyle,
    lyrics: String?,
    currentPosition: Long
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 专辑封面（缩小一点）
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedAlbumCover(
                coverUrl = coverUrl,
                isPlaying = isPlaying,
                coverStyle = coverStyle,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 歌曲信息（压缩空间）
        SongInfo(
            song = song,
            isFavorite = isFavorite,
            onFavoriteClick = onFavoriteClick,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 3行歌词（占满剩余空间）
        MiniLyricsDisplay(
            lyrics = lyrics,
            currentPosition = currentPosition,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

/**
 * 中间页面：专辑封面 + 3行歌词
 */
@Composable
private fun AlbumWithMiniLyricsPage(
    song: Song?,
    coverUrl: String?,
    isPlaying: Boolean,
    coverStyle: CoverStyle,
    visualizerEnabled: Boolean,
    lyrics: String?,
    currentPosition: Long
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 专辑封面
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedAlbumCover(
                coverUrl = coverUrl,
                isPlaying = isPlaying,
                coverStyle = coverStyle,
                showVisualizer = visualizerEnabled,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 3行歌词
        MiniLyricsDisplay(
            lyrics = lyrics,
            currentPosition = currentPosition,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
    }
}

/**
 * 右侧页面：完整歌词
 */
@Composable
private fun FullLyricsPage(
    lyrics: String?,
    currentPosition: Long,
    canAccess: Boolean,
    onBackToSong: () -> Unit
) {
    if (!canAccess || lyrics.isNullOrBlank()) {
        // 歌词不可用，显示提示
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (canAccess) "暂无歌词" else "歌词加载中...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onBackToSong) {
                    Text("返回歌曲", color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    } else {
        LyricsSection(
            lyrics = lyrics,
            currentPosition = currentPosition,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 迷你歌词显示（3行）
 */
@Composable
private fun MiniLyricsDisplay(
    lyrics: String?,
    currentPosition: Long,
    modifier: Modifier = Modifier
) {
    val lines = remember(lyrics) {
        lyrics?.lines()
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { parseLyricLine(it) }
            ?: emptyList()
    }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
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
            val currentLineIndex = lines.indexOfLast { it.timeMs <= currentPosition }
            
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                if (currentLineIndex >= 0 && currentLineIndex < lines.size) {
                    if (currentLineIndex > 0) {
                        Text(
                            text = lines[currentLineIndex - 1].text,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        text = lines[currentLineIndex].text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (currentLineIndex < lines.size - 1) {
                        Text(
                            text = lines[currentLineIndex + 1].text,
                            style = MaterialTheme.typography.bodySmall,
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
                text = if (duration > 0) "${(currentPosition * 100 / duration).toInt()}%" else "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
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
    onSkipToPrevious: () -> Unit,
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 分享
        IconButton(onClick = onShareClick) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "分享",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(28.dp)
            )
        }
        
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
    currentPosition: Long,
    modifier: Modifier = Modifier
) {
    val lines = remember(lyrics) {
        lyrics?.lines()
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { parseLyricLine(it) }
            ?: emptyList()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
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
