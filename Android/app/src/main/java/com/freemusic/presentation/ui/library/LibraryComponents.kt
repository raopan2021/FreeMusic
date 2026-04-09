package com.freemusic.presentation.ui.library

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 我的收藏页面
 */
@Composable
fun MyLibraryScreen(
    favoriteSongs: List<Song>,
    recentPlayed: List<Song>,
    downloadedSongs: List<Song>,
    onSongClick: (Song) -> Unit,
    onFavoriteSongMore: (Song) -> Unit,
    onRecentMore: (Song) -> Unit,
    onDownloadMore: (Song) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            LibrarySection(
                title = "我喜欢的音乐",
                icon = Icons.Default.Favorite,
                iconColor = Color.Red,
                songs = favoriteSongs,
                onSongClick = onSongClick,
                onSongMore = onFavoriteSongMore,
                emptyMessage = "还没有收藏任何歌曲",
                primaryColor = primaryColor
            )
        }
        
        item {
            LibrarySection(
                title = "最近播放",
                icon = Icons.Default.History,
                iconColor = primaryColor,
                songs = recentPlayed,
                onSongClick = onSongClick,
                onSongMore = onRecentMore,
                emptyMessage = "还没有播放历史",
                primaryColor = primaryColor
            )
        }
        
        item {
            LibrarySection(
                title = "下载管理",
                icon = Icons.Default.Download,
                iconColor = Color(0xFF10B981),
                songs = downloadedSongs,
                onSongClick = onSongClick,
                onSongMore = onDownloadMore,
                emptyMessage = "还没有下载任何歌曲",
                primaryColor = primaryColor
            )
        }
    }
}

@Composable
private fun LibrarySection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onSongMore: (Song) -> Unit,
    emptyMessage: String,
    primaryColor: Color
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${songs.size})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            if (songs.isNotEmpty()) {
                TextButton(onClick = { }) {
                    Text("更多")
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            songs.take(5).forEachIndexed { index, song ->
                LibrarySongItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    onMoreClick = { onSongMore(song) },
                    primaryColor = primaryColor
                )
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
    }
}

@Composable
private fun LibrarySongItem(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    primaryColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = "封面",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
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
            
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * 播放历史页面
 */
@Composable
fun PlayHistoryScreen(
    historyByDate: Map<String, List<Song>>,
    onSongClick: (Song) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "播放历史",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClearHistory) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("清空")
            }
        }
        
        LazyColumn {
            historyByDate.forEach { (date, songs) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                items(songs) { song ->
                    LibrarySongItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onMoreClick = { },
                        primaryColor = primaryColor
                    )
                }
            }
        }
    }
}

/**
 * 下载管理页面
 */
@Composable
fun DownloadManagerScreen(
    downloading: List<Pair<Song, Float>>,
    downloaded: List<Song>,
    onSongClick: (Song) -> Unit,
    onCancelDownload: (Song) -> Unit,
    onDeleteDownload: (Song) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        if (downloading.isNotEmpty()) {
            item {
                Text(
                    text = "正在下载 (${downloading.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            downloading.forEach { (song, progress) ->
                item {
                    DownloadingItem(
                        song = song,
                        progress = progress,
                        onCancel = { onCancelDownload(song) },
                        primaryColor = primaryColor
                    )
                }
            }
            
            item { Divider(modifier = Modifier.padding(vertical = 16.dp)) }
        }
        
        item {
            Text(
                text = "已下载 (${downloaded.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        downloaded.forEach { song ->
            item {
                LibrarySongItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    onMoreClick = { onDeleteDownload(song) },
                    primaryColor = primaryColor
                )
            }
        }
    }
}

@Composable
private fun DownloadingItem(
    song: Song,
    progress: Float,
    onCancel: () -> Unit,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = "封面",
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = primaryColor
            )
            
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        IconButton(onClick = onCancel) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "取消",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
