package com.freemusic.presentation.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 首页
 * - 顶部无重复标题
 * - 快捷入口：本的，音乐/文件夹/专辑/歌手/我喜欢/历史
 * - 歌单列表（内联显示）
 * - 右下角：添加歌单 + 设置
 * - 底部覆盖系统导航栏
 */
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onLocalMusicClick: () -> Unit = {},
    onPlaylistClick: (Long) -> Unit = {},
    onFolderBrowserClick: () -> Unit = {},
    onArtistBrowserClick: () -> Unit = {},
    onAlbumBrowserClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    playlists: List<PlaylistUiModel> = emptyList(),
    onCreatePlaylist: (String) -> Unit = {}
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 顶部间距
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // 搜索栏
            item {
                SearchBar(
                    onClick = onSearchClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 快捷入口
            item {
                Text(
                    text = "快捷入口",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // 快捷入口网格
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAccessItem(
                        icon = Icons.Outlined.MusicNote,
                        title = "本地音乐",
                        onClick = onLocalMusicClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessItem(
                        icon = Icons.Outlined.Folder,
                        title = "文件夹",
                        onClick = onFolderBrowserClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessItem(
                        icon = Icons.Outlined.Album,
                        title = "专辑",
                        onClick = onAlbumBrowserClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickAccessItem(
                        icon = Icons.Outlined.Person,
                        title = "歌手",
                        onClick = onArtistBrowserClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessItem(
                        icon = Icons.Outlined.FavoriteBorder,
                        title = "我喜欢",
                        onClick = onFavoritesClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessItem(
                        icon = Icons.Outlined.History,
                        title = "历史播放",
                        onClick = onHistoryClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 歌单
            if (playlists.isNotEmpty()) {
                item {
                    Text(
                        text = "我的歌单",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }

                items(playlists) { playlist ->
                    PlaylistRow(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist.id) }
                    )
                }
            }

            // 底部间距（给 FAB 留位置）
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // 右下角浮动按钮
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "创建歌单",
                    modifier = Modifier.size(20.dp)
                )
            }

            FloatingActionButton(
                onClick = onSettingsClick,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // 创建歌单对话框
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showCreateDialog = false
                    newPlaylistName = ""
                },
                title = { Text("创建歌单") },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("歌单名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                onCreatePlaylist(newPlaylistName)
                                newPlaylistName = ""
                                showCreateDialog = false
                            }
                        }
                    ) {
                        Text("创建")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showCreateDialog = false
                        newPlaylistName = ""
                    }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
private fun SearchBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "搜索在线歌曲",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickAccessItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: PlaylistUiModel,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${playlist.songCount} 首歌曲",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

data class PlaylistUiModel(
    val id: Long,
    val name: String,
    val songCount: Int
)
