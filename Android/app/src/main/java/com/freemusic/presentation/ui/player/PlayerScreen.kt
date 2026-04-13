package com.freemusic.presentation.ui.player

import android.content.Intent
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.freemusic.data.preferences.CoverStyleType
import com.freemusic.domain.model.Song
import com.freemusic.domain.model.LyricLine
import com.freemusic.domain.model.Playlist
import com.freemusic.presentation.ui.player.controls.PlayRepeatMode
import com.freemusic.presentation.ui.player.lyrics.DvdLyricsView
import com.freemusic.presentation.ui.player.lyrics.PlayerDvdLyricsView
import com.freemusic.presentation.ui.player.lyrics.ScrollingLyricsView
import com.freemusic.presentation.viewmodel.PlayerViewModel
import com.freemusic.presentation.viewmodel.QueueItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerScreen(
    onBackClick: () -> Unit = {},
    onQueueClick: () -> Unit = {},
    viewModel: PlayerViewModel = hiltViewModel(),
    // Settings
    particlesEnabled: Boolean = true,
    particleIntensity: Float = 1f,
    coverStyleType: CoverStyleType = CoverStyleType.ROUND,
    visualizerEnabled: Boolean = false,
    equalizerPreset: Int = 0,
    onParticlesToggle: () -> Unit = {},
    onVisualizerToggle: () -> Unit = {},
    onEqualizerSelect: (Int) -> Unit = {},
    shakeToSkipEnabled: Boolean = false,
    lyricsFontSize: Int = 16,
    // Playlists for "Add to Playlist"
    playlists: List<Playlist> = emptyList(),
    onAddSongsToPlaylist: (List<Song>, Playlist) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showRepeatModeSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showEffectsLayer by remember { mutableStateOf(false) }
    var showLyricsSettingsLayer by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    // Pager状态:0=播放页,1=歌词页
    val pagerState = rememberPagerState(pageCount = { 2 })

    // 分享处理器
    val handleShare: () -> Unit = {
        uiState.currentSong?.let { song ->
            val shareText = "🎵 ${song.title} - ${song.artist}\n\n来自 FreeMusic"
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "分享歌曲"))
        }
    }

    // 解析歌词
    val parsedLyrics = remember(uiState.lyrics?.lrc) {
        parseLyrics(uiState.lyrics?.lrc ?: "")
    }

    // 找到当前歌词行
    val currentLyricIndex = remember(parsedLyrics, uiState.currentPosition) {
        parsedLyrics.indexOfLast { it.timeMs <= uiState.currentPosition }.coerceAtLeast(0)
    }

    // 计算进度
    val progress = if (uiState.duration > 0) {
        uiState.currentPosition.toFloat() / uiState.duration.toFloat()
    } else 0f

    // 格式化时间
    val currentTimeStr = formatTime(uiState.currentPosition)
    val totalTimeStr = formatTime(uiState.duration)

    // 背景色使用主题背景色
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // HorizontalPager 左右滑动切换
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> PlayerPage(
                    song = uiState.currentSong,
                    isPlaying = uiState.isPlaying,
                    progress = progress,
                    currentTime = currentTimeStr,
                    totalTime = totalTimeStr,
                    isFavorite = uiState.isFavorite,
                    primaryColor = primaryColor,
                    lyrics = parsedLyrics,
                    currentLyricIndex = currentLyricIndex,
                    sleepTimerRemainingSeconds = uiState.sleepTimerRemainingSeconds,
                    repeatMode = uiState.repeatMode,
                    particlesEnabled = particlesEnabled,
                    visualizerEnabled = visualizerEnabled,
                    equalizerPreset = equalizerPreset,
                    onParticlesToggle = onParticlesToggle,
                    onVisualizerToggle = onVisualizerToggle,
                    onEqualizerSelect = onEqualizerSelect,
                    onSeek = { progress -> viewModel.seekTo((progress * uiState.duration).toLong()) },
                    onPlayPause = viewModel::togglePlayPause,
                    onPrevious = viewModel::skipToPrevious,
                    onNext = viewModel::skipToNext,
                    onFavoriteToggle = viewModel::toggleFavorite,
                    onRepeatToggle = { showRepeatModeSheet = true },
                    onSleepTimerToggle = { showSleepTimerSheet = true },
                    onQueueToggle = { showQueueSheet = true },
                    onMoreToggle = { showMoreSheet = true },
                    onEffectsLayerToggle = { showEffectsLayer = true }
                )
                1 -> LyricsPage(
                    lyrics = parsedLyrics,
                    currentLyricIndex = currentLyricIndex,
                    primaryColor = primaryColor,
                    lyricsFontSize = lyricsFontSize,
                    backgroundColor = backgroundColor,
                    onLongPress = { showLyricsSettingsLayer = true }
                )
            }
        }

        // 页面指示器
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(2) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                        .background(
                            color = if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                )
            }
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

    // 循环模式选择Sheet
    if (showRepeatModeSheet) {
        RepeatModeSheet(
            currentMode = uiState.repeatMode,
            isShuffleEnabled = uiState.isShuffleEnabled,
            onModeSelect = { mode ->
                // 计算需要切换多少次才能到达目标模式
                fun needToggles(from: PlayRepeatMode, to: PlayRepeatMode): Int {
                    val states = listOf(PlayRepeatMode.OFF, PlayRepeatMode.ALL, PlayRepeatMode.ONE)
                    val fromIdx = states.indexOf(from)
                    val toIdx = states.indexOf(to)
                    return (toIdx - fromIdx + 3) % 3
                }
                val currentMode = uiState.repeatMode
                val needed = needToggles(currentMode, mode)
                repeat(needed) { viewModel.toggleRepeatMode() }
                if (mode == PlayRepeatMode.ALL && uiState.isShuffleEnabled) {
                    viewModel.toggleShuffle()
                }
            },
            onShuffleToggle = viewModel::toggleShuffle,
            onDismiss = { showRepeatModeSheet = false },
            accentColor = primaryColor
        )
    }

    // 睡眠定时器Sheet
    if (showSleepTimerSheet) {
        SleepTimerSheet(
            currentSeconds = uiState.sleepTimerRemainingSeconds,
            onSet = { minutes -> viewModel.setSleepTimer(minutes) },
            onDismiss = { showSleepTimerSheet = false }
        )
    }

    // 特效Layer（粒子、可视化、均衡器合并）
    if (showEffectsLayer) {
        EffectsLayer(
            particlesEnabled = particlesEnabled,
            visualizerEnabled = visualizerEnabled,
            equalizerPreset = equalizerPreset,
            onParticlesToggle = onParticlesToggle,
            onVisualizerToggle = onVisualizerToggle,
            onEqualizerSelect = onEqualizerSelect,
            onDismiss = { showEffectsLayer = false }
        )
    }

    // 歌词设置Layer（长按歌词弹出）
    if (showLyricsSettingsLayer) {
        LyricsSettingsLayer(
            onDismiss = { showLyricsSettingsLayer = false },
            onOpenSettings = { showLyricsSettingsLayer = false }
        )
    }

    // 播放队列Sheet
    if (showQueueSheet) {
        QueueSheet(
            queueItems = uiState.queueItems,
            currentIndex = uiState.currentIndex,
            onRemove = { index -> viewModel.removeFromQueue(index) },
            onMove = { from, to -> viewModel.moveQueueItem(from, to) },
            onPlay = { index -> viewModel.playFromQueue(index) },
            onClear = { viewModel.clearQueue() },
            onDismiss = { showQueueSheet = false }
        )
    }

    // 更多选项Sheet
    if (showMoreSheet) {
        MoreOptionsSheet(
            song = uiState.currentSong,
            isFavorite = uiState.isFavorite,
            onFavoriteToggle = viewModel::toggleFavorite,
            onAddToPlaylist = {
                showMoreSheet = false
                showPlaylistDialog = true
            },
            onShare = handleShare,
            onDismiss = { showMoreSheet = false }
        )
    }

    // 添加到歌单对话框
    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text("添加到歌单") },
            text = {
                Column {
                    if (playlists.isEmpty()) {
                        Text("暂无歌单,请先创建歌单")
                    } else {
                        playlists.forEach { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        uiState.currentSong?.let { song ->
                                            onAddSongsToPlaylist(listOf(song), playlist)
                                        }
                                        showPlaylistDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "${playlist.songs.size} 首歌曲",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlaylistDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 播放页面
 */
@Composable
private fun PlayerPage(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    currentTime: String,
    totalTime: String,
    isFavorite: Boolean,
    primaryColor: Color,
    lyrics: List<LyricLine>,
    currentLyricIndex: Int,
    sleepTimerRemainingSeconds: Long,
    repeatMode: PlayRepeatMode = PlayRepeatMode.OFF,
    // 设置参数
    particlesEnabled: Boolean = true,
    visualizerEnabled: Boolean = false,
    equalizerPreset: Int = 0,
    onParticlesToggle: () -> Unit = {},
    onVisualizerToggle: () -> Unit = {},
    onEqualizerSelect: (Int) -> Unit = {},
    // 回调
    onSeek: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onSleepTimerToggle: () -> Unit,
    onQueueToggle: () -> Unit,
    onMoreToggle: () -> Unit,
    onEffectsLayerToggle: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
        // 顶部:歌曲信息
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = song?.title ?: "未知歌曲",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song?.artist ?: "未知艺术家",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 中间:专辑封面 + 3行歌词
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 专辑封面
                AsyncImage(
                    model = song?.coverUrl,
                    contentDescription = "专辑封面",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(24.dp))

                // DVD风格歌词: 当前行靠左，下一行靠右
                PlayerDvdLyricsView(
                    lyrics = lyrics,
                    currentLineIndex = currentLyricIndex,
                    primaryColor = primaryColor
                )
            }
        }

        // 进度条
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = totalTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // 底部:控制按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 上一首
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "上一首",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(36.dp)
                )
            }

            // 播放/暂停
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor, primaryColor.copy(alpha = 0.8f))
                        ),
                        shape = CircleShape
                    )
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            // 下一首
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "下一首",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // 底部设置栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 循环模式
            IconButton(onClick = onRepeatToggle) {
                Icon(
                    imageVector = when (repeatMode) {
                        PlayRepeatMode.OFF -> Icons.Outlined.Repeat
                        PlayRepeatMode.ALL -> Icons.Default.Repeat
                        PlayRepeatMode.ONE -> Icons.Outlined.RepeatOne
                    },
                    contentDescription = "循环模式",
                    tint = when (repeatMode) {
                        PlayRepeatMode.OFF -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            // 睡眠定时
            IconButton(onClick = onSleepTimerToggle) {
                if (sleepTimerRemainingSeconds > 0) {
                    // 显示倒计时，格式：H:MM:SS 或 MM:SS
                    val totalSeconds = sleepTimerRemainingSeconds
                    val hours = totalSeconds / 3600
                    val minutes = (totalSeconds % 3600) / 60
                    val seconds = totalSeconds % 60
                    val timeText = if (hours > 0) {
                        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
                    }
                    Text(
                        text = timeText,
                        color = primaryColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "睡眠定时",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 特效按钮（合并了粒子、可视化、均衡器）
            IconButton(onClick = onEffectsLayerToggle) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "特效",
                    tint = if (particlesEnabled || visualizerEnabled || equalizerPreset > 0) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // 播放队列
            IconButton(onClick = onQueueToggle) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "播放队列",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 更多
            IconButton(onClick = onMoreToggle) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多",
                    tint = Color.White
                )
            }
        }
    }
    }
}

/**
 * 歌词页面 - 全屏滚动歌词
 */
@Composable
private fun LyricsPage(
    lyrics: List<LyricLine>,
    currentLyricIndex: Int,
    primaryColor: Color,
    lyricsFontSize: Int = 16,
    backgroundColor: Color = Color.Black,
    onLongPress: () -> Unit = {}
) {
    // 全屏滚动歌词视图
    ScrollingLyricsView(
        lyrics = lyrics,
        currentLineIndex = currentLyricIndex,
        primaryColor = primaryColor,
        fontSize = lyricsFontSize,
        backgroundColor = backgroundColor,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() }
                )
            }
    )
}

/**
 * 歌词预览(3行)
 */
@Composable
private fun LyricsPreview(
    lyrics: List<LyricLine>,
    currentIndex: Int,
    primaryColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) { offset ->
            val index = currentIndex - 1 + offset
            val isCurrentLine = offset == 1
            val text = if (index in lyrics.indices) lyrics[index].text else ""

            Text(
                text = text.ifEmpty { " " },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = if (isCurrentLine) 16.sp else 14.sp
                ),
                color = if (isCurrentLine) Color.White else Color.White.copy(alpha = 0.5f),
                fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 循环模式选择Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatModeSheet(
    currentMode: PlayRepeatMode,
    isShuffleEnabled: Boolean,
    onModeSelect: (PlayRepeatMode) -> Unit,
    onShuffleToggle: () -> Unit,
    onDismiss: () -> Unit,
    accentColor: Color = Color(0xFF5C6BC0)
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "循环模式",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 列表循环
            ListItem(
                headlineContent = { Text("列表循环") },
                leadingContent = { Icon(Icons.Default.Repeat, contentDescription = null) },
                trailingContent = {
                    if (currentMode == PlayRepeatMode.ALL && !isShuffleEnabled) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = accentColor)
                    }
                },
                modifier = Modifier.clickable {
                    onModeSelect(PlayRepeatMode.ALL)
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                }
            )

            // 单曲循环
            ListItem(
                headlineContent = { Text("单曲循环") },
                leadingContent = { Icon(Icons.Default.RepeatOne, contentDescription = null) },
                trailingContent = {
                    if (currentMode == PlayRepeatMode.ONE) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = accentColor)
                    }
                },
                modifier = Modifier.clickable {
                    onModeSelect(PlayRepeatMode.ONE)
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                }
            )

            // 随机播放
            ListItem(
                headlineContent = { Text("随机播放") },
                leadingContent = { Icon(Icons.Default.Shuffle, contentDescription = null) },
                trailingContent = {
                    if (isShuffleEnabled) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = accentColor)
                    }
                },
                modifier = Modifier.clickable {
                    onShuffleToggle()
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 睡眠定时Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepTimerSheet(
    currentSeconds: Long,
    onSet: (Int) -> Unit,  // 仍以分钟为单位
    onDismiss: () -> Unit
) {
    // 将当前秒数转换为分钟（用于 slider）
    val currentMinutes = if (currentSeconds > 0) (currentSeconds / 60).toInt() else 0
    var sliderValue by remember { mutableFloatStateOf(if (currentMinutes > 0) currentMinutes.toFloat() else 30f) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // 显示当前剩余时间
    val displayTime = if (currentSeconds > 0) {
        val h = currentSeconds / 3600
        val m = (currentSeconds % 3600) / 60
        val s = currentSeconds % 60
        if (h > 0) String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
        else String.format(Locale.getDefault(), "%d:%02d", m, s)
    } else null

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "睡眠定时",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 剩余时间显示
            if (displayTime != null) {
                Text(
                    text = "剩余 $displayTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = if (sliderValue.toInt() == 0) "关闭定时" else "${sliderValue.toInt()} 分钟",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 0f..120f,
                steps = 23,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0, 15, 30, 45, 60, 90).forEach { mins ->
                    FilterChip(
                        selected = sliderValue.toInt() == mins,
                        onClick = { sliderValue = mins.toFloat() },
                        label = { Text(if (mins == 0) "关闭" else "${mins}分") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                Button(
                    onClick = {
                        onSet(sliderValue.toInt())
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("确定")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 特效Layer（合并了粒子效果、可视化、均衡器）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EffectsLayer(
    particlesEnabled: Boolean,
    visualizerEnabled: Boolean,
    equalizerPreset: Int,
    onParticlesToggle: () -> Unit,
    onVisualizerToggle: () -> Unit,
    onEqualizerSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // 均衡器预设选项
    val equalizerPresets = listOf(
        "平坦" to 0,
        "低音增强" to 1,
        "高音增强" to 2,
        "人声" to 3,
        "摇滚" to 4,
        "流行" to 5,
        "爵士" to 6,
        "古典" to 7,
        "电子" to 8
    )

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ========== 粒子效果 ==========
            Text(
                text = "粒子效果",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "背景特效动画",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ListItem(
                headlineContent = { Text("粒子效果") },
                leadingContent = { Icon(Icons.Default.BubbleChart, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = particlesEnabled,
                        onCheckedChange = { onParticlesToggle() }
                    )
                },
                modifier = Modifier.clickable { onParticlesToggle() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // ========== 可视化 ==========
            Text(
                text = "可视化",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "专辑封面随音频动态效果",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ListItem(
                headlineContent = { Text("可视化") },
                leadingContent = { Icon(Icons.Default.Equalizer, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = visualizerEnabled,
                        onCheckedChange = { onVisualizerToggle() }
                    )
                },
                modifier = Modifier.clickable { onVisualizerToggle() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // ========== 均衡器 ==========
            Text(
                text = "均衡器",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "调整音频频率",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 均衡器预设选择
            equalizerPresets.chunked(3).forEach { rowPresets ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPresets.forEach { (name, index) ->
                        val isSelected = equalizerPreset == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { onEqualizerSelect(index) },
                            label = { Text(name, style = MaterialTheme.typography.bodySmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // 填充空白
                    repeat(3 - rowPresets.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 歌词设置Layer（长按歌词弹出）
 * 展示"设置-外观"和"设置-播放设置"两个组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsSettingsLayer(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "设置",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 外观设置
            Text(
                text = "外观",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ListItem(
                headlineContent = { Text("深色模式") },
                leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                modifier = Modifier.clickable { onDismiss() }
            )
            ListItem(
                headlineContent = { Text("主题颜色") },
                leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.clickable { onOpenSettings(); onDismiss() }
            )
            ListItem(
                headlineContent = { Text("歌词字体大小") },
                leadingContent = { Icon(Icons.Default.TextFields, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.clickable { onOpenSettings(); onDismiss() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // 播放设置
            Text(
                text = "播放设置",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ListItem(
                headlineContent = { Text("播放速度") },
                leadingContent = { Icon(Icons.Default.Speed, contentDescription = null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.clickable { onOpenSettings(); onDismiss() }
            )
            ListItem(
                headlineContent = { Text("跳过静音") },
                leadingContent = { Icon(Icons.Default.SkipNext, contentDescription = null) },
                modifier = Modifier.clickable { onDismiss() }
            )
            ListItem(
                headlineContent = { Text("高质量音频") },
                leadingContent = { Icon(Icons.Default.HighQuality, contentDescription = null) },
                modifier = Modifier.clickable { onDismiss() }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 播放队列Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueSheet(
    queueItems: List<QueueItem>,
    currentIndex: Int,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onPlay: (Int) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    // 计算高度:1-2首歌自适应,更多歌占屏幕70%
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val headerHeight = 120.dp // header + padding 估算

    val sheetHeight = when {
        queueItems.size <= 2 -> (queueItems.size * 72).dp + headerHeight
        else -> screenHeight * 0.7f
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 展开后滚动到当前歌曲（居中）
    LaunchedEffect(sheetState) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "播放队列 (${queueItems.size} 首)",
                    style = MaterialTheme.typography.titleMedium
                )
                if (queueItems.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text("清空")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (queueItems.isEmpty()) {
                Text(
                    text = "暂无播放队列",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                QueueList(
                    queueItems = queueItems,
                    currentIndex = currentIndex,
                    onRemove = onRemove,
                    onMove = onMove,
                    onPlay = onPlay,
                    sheetHeight = sheetHeight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (queueItems.size > 2) {
                                Modifier.height(sheetHeight)
                            } else {
                                Modifier.heightIn(max = sheetHeight)
                            }
                        ),
                    scrollToCurrentOnLaunch = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 更多选项Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreOptionsSheet(
    song: Song?,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 歌曲信息
            if (song != null) {
                ListItem(
                    headlineContent = { Text(song.title, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(song.artist) },
                    leadingContent = {
                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 收藏
            ListItem(
                headlineContent = { Text(if (isFavorite) "取消收藏" else "收藏") },
                leadingContent = {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                },
                modifier = Modifier.clickable { onFavoriteToggle() }
            )

            // 添加到歌单
            ListItem(
                headlineContent = { Text("添加到歌单") },
                leadingContent = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) },
                modifier = Modifier.clickable { onAddToPlaylist() }
            )

            // 分享
            ListItem(
                headlineContent = { Text("分享") },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                modifier = Modifier.clickable { onShare() }
            )

            // 歌曲详情
            if (song != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ListItem(
                    headlineContent = { Text("歌曲信息") },
                    supportingContent = {
                        Text(
                            "时长: ${song.duration / 60000}:${String.format("%02d", (song.duration % 60000) / 1000)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 解析LRC歌词
 */
private fun parseLyrics(lrc: String): List<LyricLine> {
    if (lrc.isBlank()) return emptyList()

    val lyrics = mutableListOf<LyricLine>()
    val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")

    lrc.lines().forEach { line ->
        regex.find(line)?.let { match ->
            val (minutes, seconds, millis, text) = match.destructured
            val timeMs = minutes.toLong() * 60000 + seconds.toLong() * 1000 + millis.padEnd(3, '0').toLong()
            if (text.isNotBlank()) {
                lyrics.add(LyricLine(timeMs = timeMs, text = text.trim()))
            }
        }
    }

    return lyrics.sortedBy { it.timeMs }
}

/**
 * 格式化时间
 */
private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
