package com.freemusic.presentation.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.freemusic.domain.model.Playlist
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.theme.PrimaryIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayHistoryScreen(
    recentPlays: List<Song>,
    mostPlayed: List<Song>,
    onBackClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAddToPlaylist: (Song, Playlist) -> Unit,
    playlists: List<Playlist>,
    onClearHistory: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("最近播放", "播放最多")
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    
    // 添加到歌单对话框
    var showAddDialog by remember { mutableStateOf(false) }
    var songToAdd by remember { mutableStateOf<Song?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("播放历史") },
                actions = {
                    IconButton(onClick = { showClearConfirmDialog = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "清空历史")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // Content
            val songs = if (selectedTab == 0) recentPlays else mostPlayed
            
            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedTab == 0) "还没有播放历史" else "还没有播放记录",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "播放一些歌曲来看看这里的内容吧",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(songs) { song ->
                        HistorySongItem(
                            song = song,
                            onClick = { onSongClick(song) },
                            onMoreClick = {
                                songToAdd = song
                                showAddDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 添加到歌单对话框
    if (showAddDialog && songToAdd != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加到歌单") },
            text = {
                if (playlists.isEmpty()) {
                    Text("暂无歌单，请先创建歌单")
                } else {
                    Column {
                        playlists.forEach { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAddToPlaylist(songToAdd!!, playlist)
                                        showAddDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
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
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 清空历史确认对话框
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("清空播放历史") },
            text = { Text("确定要清空所有播放历史吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun HistorySongItem(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (song.coverUrl != null) {
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = PrimaryIndigo.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("加入歌单") },
                        onClick = {
                            showMenu = false
                            onMoreClick()
                        },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}
