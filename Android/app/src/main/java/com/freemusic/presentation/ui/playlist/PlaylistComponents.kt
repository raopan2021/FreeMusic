package com.freemusic.presentation.ui.playlist

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
import com.freemusic.domain.model.Playlist
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 播放列表详情头部
 */
@Composable
fun PlaylistDetailHeader(
    playlist: Playlist,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // 背景封面
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AsyncImage(
                model = playlist.coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // 渐变遮罩
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
        }
        
        // 顶部导航
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
            
            Row {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "分享",
                        tint = Color.White
                    )
                }
            }
        }
        
        // 底部信息
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${playlist.songs.size}首",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPlayAll,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("播放全部")
                }
                
                OutlinedButton(
                    onClick = onShuffle,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("随机播放")
                }
            }
        }
    }
}

/**
 * 歌曲列表项
 */
@Composable
fun SongListItem(
    song: Song,
    index: Int,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 索引
        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isPlaying) {
                MusicPlayingIndicator(primaryColor = primaryColor)
            } else {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
        
        // 封面
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                color = if (isPlaying) primaryColor else MaterialTheme.colorScheme.onSurface,
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
        
        // 更多按钮
        IconButton(onClick = onMoreClick) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "更多",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun MusicPlayingIndicator(primaryColor: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val height by animateFloatAsState(
                targetValue = if (index == 1) 16f else 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(300),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .background(primaryColor, RoundedCornerShape(2.dp))
            )
        }
    }
}

/**
 * 播放列表操作栏
 */
@Composable
fun PlaylistActionsBar(
    onSort: () -> Unit,
    onDownload: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ActionButton(
                icon = Icons.Default.Sort,
                label = "排序",
                onClick = onSort
            )
            ActionButton(
                icon = Icons.Default.Download,
                label = "下载",
                onClick = onDownload
            )
        }
        
        ActionButton(
            icon = Icons.Default.Checklist,
            label = "多选",
            onClick = onSelect
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * 相似歌单推荐
 */
@Composable
fun SimilarPlaylistsRow(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Column(modifier = modifier) {
        Text(
            text = "相关歌单",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(playlists) { playlist ->
                SimilarPlaylistCard(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist) },
                    primaryColor = primaryColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimilarPlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            AsyncImage(
                model = playlist.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${playlist.songs.size}首",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * 批量选择模式
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchSelectMode(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDelete: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = primaryColor.copy(alpha = 0.1f)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "已选择 $selectedCount/$totalCount",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Row {
                    TextButton(onClick = if (selectedCount == totalCount) onDeselectAll else onSelectAll) {
                        Text(if (selectedCount == totalCount) "取消全选" else "全选")
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = onAddToPlaylist,
                    enabled = selectedCount > 0
                ) {
                    Icon(Icons.Default.PlaylistAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加到歌单")
                }
                
                TextButton(
                    onClick = onDelete,
                    enabled = selectedCount > 0,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除")
                }
            }
        }
    }
}

/**
 * 排序选项对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortOptionsDialog(
    onDismiss: () -> Unit,
    onSortByName: () -> Unit,
    onSortByArtist: () -> Unit,
    onSortByDuration: () -> Unit,
    onSortByRecentlyAdded: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("排序方式") },
        text = {
            Column {
                SortOptionItem(
                    icon = Icons.Default.SortByAlpha,
                    text = "按名称",
                    onClick = onSortByName
                )
                SortOptionItem(
                    icon = Icons.Default.Person,
                    text = "按歌手",
                    onClick = onSortByArtist
                )
                SortOptionItem(
                    icon = Icons.Default.Timer,
                    text = "按时长",
                    onClick = onSortByDuration
                )
                SortOptionItem(
                    icon = Icons.Default.AccessTime,
                    text = "按添加时间",
                    onClick = onSortByRecentlyAdded
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun SortOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text)
    }
}
