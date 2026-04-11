package com.freemusic.presentation.ui.local

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.freemusic.domain.model.Playlist
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.theme.PrimaryIndigo
import com.freemusic.presentation.viewmodel.LocalMusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalMusicScreen(
    onBackClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    playlists: List<Playlist> = emptyList(),
    onAddSongsToPlaylist: (List<Song>, Playlist) -> Unit = { _, _ -> },
    viewModel: LocalMusicViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showFilterDialog by remember { mutableStateOf(false) }
    var showPlaylistSelectDialog by remember { mutableStateOf(false) }
    
    // 批量选择状态
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedSongs by remember { mutableStateOf(setOf<String>()) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    
    // 权限
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    // 权限状态
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // 排序状态
    var showSortDialog by remember { mutableStateOf(false) }
    var currentSortOrder by remember { mutableStateOf(0) }
    val sortOptions = listOf("名称", "艺术家", "时长")
    
    // 权限请求 launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.scanLocalMusic()
        }
    }
    
    // 首次加载时检查权限并扫描
    LaunchedEffect(Unit) {
        if (hasPermission) {
            viewModel.scanLocalMusic()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (isSelectionMode) {
                        Text("已选择 ${selectedSongs.size} 首")
                    } else {
                        Text("本地音乐")
                    }
                },
                actions = {
                    if (hasPermission) {
                        if (isSelectionMode) {
                            // 选择模式下的操作
                            if (selectedSongs.isNotEmpty()) {
                                IconButton(onClick = { showPlaylistSelectDialog = true }) {
                                    Icon(Icons.Default.PlaylistAdd, contentDescription = "添加到歌单")
                                }
                            }
                            IconButton(onClick = {
                                // 全选/取消全选
                                if (selectedSongs.size == uiState.songs.size) {
                                    selectedSongs = emptySet()
                                } else {
                                    selectedSongs = uiState.songs.map { it.id }.toSet()
                                }
                            }) {
                                Icon(
                                    if (selectedSongs.size == uiState.songs.size) Icons.Default.Deselect else Icons.Default.SelectAll,
                                    contentDescription = if (selectedSongs.size == uiState.songs.size) "取消全选" else "全选"
                                )
                            }
                        } else {
                            // 正常模式
                            IconButton(onClick = { showSortDialog = true }) {
                                Icon(Icons.Default.Sort, contentDescription = "排序")
                            }
                            IconButton(onClick = { showFilterDialog = true }) {
                                Icon(Icons.Default.FilterList, contentDescription = "筛选设置")
                            }
                            IconButton(onClick = { viewModel.scanLocalMusic(currentSortOrder) }) {
                                Icon(Icons.Default.Refresh, contentDescription = "刷新")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !hasPermission -> {
                    // 没有权限
                    PermissionRequest(
                        onRequestPermission = {
                            permissionLauncher.launch(permission)
                        }
                    )
                }
                
                uiState.isLoading -> {
                    // 加载中
                    LoadingState(scannedCount = uiState.scannedCount)
                }
                
                uiState.error != null -> {
                    // 错误状态
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.scanLocalMusic() }
                    )
                }
                
                uiState.songs.isEmpty() -> {
                    // 没有音乐
                    EmptyState(onRetry = { viewModel.scanLocalMusic() })
                }
                
                else -> {
                    // 显示音乐列表
                    var searchQuery by remember { mutableStateOf("") }
                    val filteredSongs = remember(uiState.songs, searchQuery, currentSortOrder) {
                        val baseList = if (searchQuery.isBlank()) {
                            uiState.songs
                        } else {
                            uiState.songs.filter { song ->
                                song.title.contains(searchQuery, ignoreCase = true) ||
                                song.artist.contains(searchQuery, ignoreCase = true) ||
                                song.album.contains(searchQuery, ignoreCase = true)
                            }
                        }
                        when (currentSortOrder) {
                            0 -> baseList.sortedBy { it.title.lowercase() }
                            1 -> baseList.sortedBy { it.artist.lowercase() }
                            2 -> baseList.sortedBy { it.duration }
                            else -> baseList
                        }
                    }
                    
                    Column {
                        // 搜索框
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("搜索本地音乐...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "清除")
                                    }
                                }
                            },
                            singleLine = true
                        )
                        
                        // 歌曲数量
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "找到 ${filteredSongs.size} 首歌曲",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        
                        SongList(
                            songs = filteredSongs,
                            totalDuration = uiState.totalDuration,
                            onSongClick = onSongClick,
                            isSelectionMode = isSelectionMode,
                            selectedSongs = selectedSongs,
                            onSelectionChange = { selectedSongs = it },
                            onEnterSelectionMode = { songId ->
                                isSelectionMode = true
                                selectedSongs = setOf(songId)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 筛选设置对话框
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("本地音乐筛选规则") },
            text = {
                Column {
                    Text(
                        text = "当前筛选条件：",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• 过滤时长少于 60 秒的音频")
                    Text("过滤录音文件（文件名包含'录音'）")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "这些音频文件已被过滤，不会显示在列表中",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("关闭")
                }
            }
        )
    }
    
    // 排序对话框
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("选择排序方式") },
            text = {
                Column {
                    sortOptions.forEachIndexed { index, option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentSortOrder = index
                                    viewModel.scanLocalMusic(index)
                                    showSortDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = index == currentSortOrder,
                                onClick = {
                                    currentSortOrder = index
                                    viewModel.scanLocalMusic(index)
                                    showSortDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 选择歌单对话框
    if (showPlaylistSelectDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistSelectDialog = false },
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
                                        val songsToAdd = uiState.songs.filter { selectedSongs.contains(it.id) }
                                        onAddSongsToPlaylist(songsToAdd, playlist)
                                        showPlaylistSelectDialog = false
                                        isSelectionMode = false
                                        selectedSongs = emptySet<String>()
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
                TextButton(onClick = { showPlaylistSelectDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun PermissionRequest(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "需要存储权限",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "请授予存储权限以扫描本地音乐",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("授予权限")
        }
    }
}

@Composable
private fun LoadingState(scannedCount: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = PrimaryIndigo)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "正在扫描本地音乐...",
            style = MaterialTheme.typography.bodyMedium
        )
        if (scannedCount > 0) {
            Text(
                text = "已扫描 $scannedCount 首",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "扫描失败",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("重试")
        }
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "没有找到本地音乐",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "请将音乐文件放入手机存储的Music文件夹",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("重新扫描")
        }
    }
}

@Composable
private fun SongList(
    songs: List<Song>,
    totalDuration: String,
    onSongClick: (Song) -> Unit,
    isSelectionMode: Boolean = false,
    selectedSongs: Set<String> = emptySet(),
    onSelectionChange: (Set<String>) -> Unit = {},
    onEnterSelectionMode: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            ListItem(
                headlineContent = { Text("共 ${songs.size} 首歌曲") },
                supportingContent = { Text("总时长: $totalDuration") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = PrimaryIndigo
                    )
                }
            )
            Divider()
        }
        
        items(songs) { song ->
            SongItem(
                song = song,
                isSelected = selectedSongs.contains(song.id),
                isSelectionMode = isSelectionMode,
                onClick = {
                    if (isSelectionMode) {
                        val newSelection: Set<String> = if (selectedSongs.contains(song.id)) {
                            (selectedSongs - song.id) as Set<String>
                        } else {
                            (selectedSongs + song.id) as Set<String>
                        }
                        onSelectionChange(newSelection)
                    } else {
                        onSongClick(song)
                    }
                },
                onLongClick = {
                    if (!isSelectionMode) {
                        onEnterSelectionMode(song.id)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongItem(
    song: Song,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() }
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
            
            Text(
                text = formatDuration(song.duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
