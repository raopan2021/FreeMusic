package com.freemusic.presentation.ui.player

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    shakeToSkipEnabled: Boolean = false,
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
    
    val primaryColor = Color(0xFF5C6BC0) // PrimaryIndigo equivalent
    
    // Pager状态：0=播放页，1=歌词页
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.4f),
                        Color.Black,
                        Color.Black
                    )
                )
            )
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
                    sleepTimerRemaining = uiState.sleepTimerRemainingMinutes,
                    particlesEnabled = particlesEnabled,
                    visualizerEnabled = visualizerEnabled,
                    equalizerPreset = equalizerPreset,
                    onSeek = { progress -> viewModel.seekTo((progress * uiState.duration).toLong()) },
                    onPlayPause = viewModel::togglePlayPause,
                    onPrevious = viewModel::skipToPrevious,
                    onNext = viewModel::skipToNext,
                    onFavoriteToggle = viewModel::toggleFavorite,
                    onRepeatToggle = { showRepeatModeSheet = true },
                    onSleepTimerToggle = { showSleepTimerSheet = true },
                    onQueueToggle = { showQueueSheet = true },
                    onMoreToggle = { showMoreSheet = true }
                )
                1 -> LyricsPage(
                    lyrics = parsedLyrics,
                    currentLyricIndex = currentLyricIndex,
                    primaryColor = primaryColor
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
                when (mode) {
                    PlayRepeatMode.ALL -> {
                        if (uiState.isShuffleEnabled) viewModel.toggleShuffle()
                        if (uiState.repeatMode == PlayRepeatMode.ONE) viewModel.toggleRepeatMode()
                    }
                    PlayRepeatMode.ONE -> {
                        if (uiState.repeatMode != PlayRepeatMode.ONE) viewModel.toggleRepeatMode()
                    }
                    else -> {}
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
            onDismiss = { showSleepTimerSheet = false }
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
                        Text("暂无歌单，请先创建歌单")
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
    sleepTimerRemaining: Int,
    // 设置参数
    particlesEnabled: Boolean = true,
    visualizerEnabled: Boolean = false,
    equalizerPreset: Int = 0,
    onParticlesToggle: () -> Unit = {},
    onVisualizerToggle: () -> Unit = {},
    onEqualizerClick: () -> Unit = {},
    // 回调
    onSeek: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onSleepTimerToggle: () -> Unit,
    onQueueToggle: () -> Unit,
    onMoreToggle: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
        // 顶部：歌曲信息
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
        
        // 中间：专辑封面 + 3行歌词
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
                
                // 3行歌词预览
                LyricsPreview(
                    lyrics = lyrics,
                    currentIndex = currentLyricIndex,
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
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = totalTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
        
        // 底部：控制按钮
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
                    tint = Color.White,
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
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // 下一首
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "下一首",
                    tint = Color.White,
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
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "循环模式",
                    tint = Color.White
                )
            }
            
            // 睡眠定时
            IconButton(onClick = onSleepTimerToggle) {
                if (sleepTimerRemaining > 0) {
                    // 显示倒计时
                    val hours = sleepTimerRemaining / 60
                    val minutes = sleepTimerRemaining % 60
                    val timeText = if (hours > 0) {
                        String.format("%d:%02d", hours, minutes)
                    } else {
                        String.format("%02d", minutes)
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
                        tint = Color.White
                    )
                }
            }
            
            // 粒子效果
            IconButton(onClick = onParticlesToggle) {
                Icon(
                    imageVector = Icons.Default.BubbleChart,
                    contentDescription = "粒子效果",
                    tint = if (particlesEnabled) primaryColor else Color.White.copy(alpha = 0.5f)
                )
            }
            
            // 可视化
            IconButton(onClick = onVisualizerToggle) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = "可视化",
                    tint = if (visualizerEnabled) primaryColor else Color.White.copy(alpha = 0.5f)
                )
            }
            
            // 均衡器
            IconButton(onClick = onEqualizerClick) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "均衡器",
                    tint = if (equalizerPreset > 0) primaryColor else Color.White.copy(alpha = 0.5f)
                )
            }
            
            // 播放队列
            IconButton(onClick = onQueueToggle) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "播放队列",
                    tint = Color.White
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
 * 歌词页面
 */
@Composable
private fun LyricsPage(
    lyrics: List<LyricLine>,
    currentLyricIndex: Int,
    primaryColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        
        // 全屏歌词
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (lyrics.isEmpty()) {
                Text(
                    text = "暂无歌词",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.5f)
                )
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    lyrics.forEachIndexed { index, line ->
                        val isCurrentLine = index == currentLyricIndex
                        val isPastLine = index < currentLyricIndex
                        
                        Text(
                            text = line.text.ifEmpty { "　" },
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (isCurrentLine) 20.sp else 16.sp
                            ),
                            color = when {
                                isCurrentLine -> Color.White
                                isPastLine -> Color.White.copy(alpha = 0.4f)
                                else -> Color.White.copy(alpha = 0.6f)
                            },
                            fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(150.dp))
    }
}

/**
 * 歌词预览（3行）
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
                text = text.ifEmpty { "　" },
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
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
            
            ListItem(
                headlineContent = { Text("列表循环") },
                leadingContent = { Icon(Icons.Default.Repeat, contentDescription = null) },
                trailingContent = {
                    if (currentMode == PlayRepeatMode.ALL && !isShuffleEnabled) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = accentColor)
                    }
                },
                modifier = Modifier.clickable { onModeSelect(PlayRepeatMode.ALL) }
            )
            
            ListItem(
                headlineContent = { Text("单曲循环") },
                leadingContent = { Icon(Icons.Default.RepeatOne, contentDescription = null) },
                trailingContent = {
                    if (currentMode == PlayRepeatMode.ONE) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = accentColor)
                    }
                },
                modifier = Modifier.clickable { onModeSelect(PlayRepeatMode.ONE) }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            ListItem(
                headlineContent = { Text("随机播放") },
                leadingContent = { Icon(Icons.Default.Shuffle, contentDescription = null) },
                trailingContent = {
                    if (isShuffleEnabled) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = accentColor)
                    }
                },
                modifier = Modifier.clickable { onShuffleToggle() }
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
    onDismiss: () -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(30f) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "睡眠定时",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "${sliderValue.toInt()} 分钟",
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
                listOf(15, 30, 45, 60, 90).forEach { mins ->
                    FilterChip(
                        selected = sliderValue.toInt() == mins,
                        onClick = { sliderValue = mins.toFloat() },
                        label = { Text("${mins}分") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onDismiss() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                Button(
                    onClick = { onDismiss() },
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
    // 计算高度：1-2首歌自适应，更多歌占屏幕70%
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val headerHeight = 120.dp // header + padding 估算
    
    val sheetHeight = when {
        queueItems.size <= 2 -> (queueItems.size * 72).dp + headerHeight
        else -> screenHeight * 0.7f
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = queueItems.size <= 2)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (queueItems.size > 2) {
                                Modifier.height(sheetHeight)
                            } else {
                                Modifier.heightIn(max = sheetHeight)
                            }
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 播放队列列表（支持拖动排序）
 */
@Composable
private fun QueueList(
    queueItems: List<QueueItem>,
    currentIndex: Int,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onPlay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Reorderable state
    val reorderableState = rememberReorderableLazyListState(
        lazyListState = listState,
        onMove = { from, to ->
            onMove(from.index, to.index)
        }
    )
    
    // 初始滚动到当前歌曲（居中）
    LaunchedEffect(currentIndex) {
        if (queueItems.isNotEmpty() && currentIndex in queueItems.indices) {
            val visibleItems = 5
            val targetIndex = (currentIndex - visibleItems / 2).coerceAtLeast(0)
            listState.animateScrollToItem(index = targetIndex)
        }
    }
    
    // 队列变化时（拖动排序后）滚动到当前歌曲
    LaunchedEffect(queueItems) {
        // 延迟等待拖动动画完成
        kotlinx.coroutines.delay(150)
        if (queueItems.isNotEmpty() && currentIndex in queueItems.indices) {
            val visibleItems = 5
            val targetIndex = (currentIndex - visibleItems / 2).coerceAtLeast(0)
            listState.animateScrollToItem(index = targetIndex)
        }
    }
    
    // Focus到当前歌曲（居中）
    val scrollToCenter: () -> Unit = {
        if (queueItems.isNotEmpty() && currentIndex in queueItems.indices) {
            val visibleItems = 5
            val targetIndex = (currentIndex - visibleItems / 2).coerceAtLeast(0)
            coroutineScope.launch {
                listState.animateScrollToItem(index = targetIndex)
            }
        }
    }
    
    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(
                items = queueItems,
                key = { _, item -> item.song.id }
            ) { index, item ->
                val song = item.song
                val isCurrentSong = index == currentIndex
                
                ReorderableItem(
                    state = reorderableState,
                    key = item.song.id
                ) { isDragging ->
                    val isCurrentSong = index == currentIndex
                    
                    // 拖动时不改变背景高亮（保持当前播放歌曲的高亮）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isCurrentSong && !isDragging) {
                                    Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                } else Modifier
                            )
                            .clickable {
                                // 如果不是当前播放的歌曲，则切换到这首歌播放
                                if (!isCurrentSong) {
                                    onPlay(index)
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 拖动把手 - 三个点（长按可拖动排序）
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "长按拖动排序",
                            tint = Color.Gray,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .draggableHandle()
                        )
                        
                        // 歌曲信息
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrentSong) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist ?: "未知艺术家",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // 删除按钮
                        IconButton(
                            onClick = { onRemove(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "删除",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    if (index < queueItems.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    }
                }
            }
        }
        
        // 右下角 Focus 按钮
        if (queueItems.isNotEmpty()) {
            FloatingActionButton(
                onClick = scrollToCenter,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CenterFocusWeak,
                    contentDescription = "定位当前播放",
                    modifier = Modifier.size(20.dp)
                )
            }
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "更多",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
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
                modifier = Modifier.clickable(onClick = onFavoriteToggle)
            )
            
            // 添加到歌单
            ListItem(
                headlineContent = { Text("添加到歌单") },
                leadingContent = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onAddToPlaylist)
            )
            
            // 分享
            ListItem(
                headlineContent = { Text("分享") },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onShare)
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
