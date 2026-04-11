package com.freemusic.presentation.ui.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    onBackClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAddSongs: () -> Unit,
    onRemoveSong: (Song) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(0) } // 0=默认, 1=标题, 2=艺术家, 3=专辑, 4=时长
    var showSortMenu by remember { mutableStateOf(false) }
    
    val sortedSongs = remember(playlist.songs, sortOrder) {
        when (sortOrder) {
            1 -> playlist.songs.sortedBy { it.title.lowercase() }
            2 -> playlist.songs.sortedBy { it.artist.lowercase() }
            3 -> playlist.songs.sortedBy { it.album.lowercase() }
            4 -> playlist.songs.sortedBy { it.duration }
            else -> playlist.songs
        }
    }
    
    val filteredSongs = remember(sortedSongs, searchQuery) {
        if (searchQuery.isBlank()) sortedSongs
        else sortedSongs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true) ||
            it.album.contains(searchQuery, ignoreCase = true)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist.name) },
                actions = {
                    // 排序按钮
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "排序")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("默认顺序", fontWeight = if (sortOrder == 0) FontWeight.Bold else FontWeight.Normal) },
                                onClick = { sortOrder = 0; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("按标题", fontWeight = if (sortOrder == 1) FontWeight.Bold else FontWeight.Normal) },
                                onClick = { sortOrder = 1; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("按艺术家", fontWeight = if (sortOrder == 2) FontWeight.Bold else FontWeight.Normal) },
                                onClick = { sortOrder = 2; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("按专辑", fontWeight = if (sortOrder == 3) FontWeight.Bold else FontWeight.Normal) },
                                onClick = { sortOrder = 3; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("按时长", fontWeight = if (sortOrder == 4) FontWeight.Bold else FontWeight.Normal) },
                                onClick = { sortOrder = 4; showSortMenu = false }
                            )
                        }
                    }
                    IconButton(onClick = onAddSongs) {
                        Icon(Icons.Default.Add, contentDescription = "添加歌曲")
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
            // 搜索框
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索歌单中的歌曲...") },
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
            
            if (playlist.songs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "歌单还没有歌曲",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击右上角添加歌曲",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = onAddSongs) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("添加歌曲")
                        }
                    }
                }
            } else if (filteredSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "未找到匹配的歌曲",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        ListItem(
                            headlineContent = { Text("共 ${filteredSongs.size} 首") },
                            supportingContent = { Text("总时长: ${filteredSongs.sumOf { it.duration }.let { millis ->
                                val minutes = millis / 60000
                                val seconds = (millis % 60000) / 1000
                                "${minutes}分${seconds}秒"
                            }}") },
                            leadingContent = {
                                Icon(Icons.Default.QueueMusic, contentDescription = null, tint = PrimaryIndigo)
                            }
                        )
                        Divider()
                    }
                    items(filteredSongs) { song ->
                        PlaylistSongItem(
                            song = song,
                            onClick = { onSongClick(song) },
                            onDelete = if (playlist.id != "favorites") {
                                { onRemoveSong(song) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistSongItem(
    song: Song,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    var showMenu by remember { mutableStateOf(false) }
    
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = song.title,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
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
        },
        trailingContent = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("播放") },
                        onClick = {
                            showMenu = false
                            onClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                        }
                    )
                    
                    if (onDelete != null) {
                        DropdownMenuItem(
                            text = { Text("从歌单移除") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    )
}
