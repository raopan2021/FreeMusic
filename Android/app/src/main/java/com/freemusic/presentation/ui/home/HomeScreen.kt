package com.freemusic.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.freemusic.presentation.ui.theme.PrimaryIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onLocalMusicClick: () -> Unit = {},
    onPlaylistClick: () -> Unit = {},
    onFolderBrowserClick: () -> Unit = {},
    onArtistBrowserClick: () -> Unit = {},
    onAlbumBrowserClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FreeMusic") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "欢迎使用 FreeMusic",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 快捷入口
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAccessCard(
                    icon = Icons.Default.MusicNote,
                    title = "本地音乐",
                    onClick = onLocalMusicClick,
                    modifier = Modifier.weight(1f)
                )
                QuickAccessCard(
                    icon = Icons.Default.Folder,
                    title = "文件夹",
                    onClick = onFolderBrowserClick,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAccessCard(
                    icon = Icons.Default.QueueMusic,
                    title = "歌单",
                    onClick = onPlaylistClick,
                    modifier = Modifier.weight(1f)
                )
                QuickAccessCard(
                    icon = Icons.Default.History,
                    title = "最近播放",
                    onClick = onHistoryClick,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAccessCard(
                    icon = Icons.Default.Album,
                    title = "专辑",
                    onClick = onAlbumBrowserClick,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onSearchClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("搜索在线歌曲")
            }
        }
    }
}

@Composable
private fun QuickAccessCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryIndigo,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
