package com.freemusic.presentation.ui.player.queue

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 播放队列完整视图
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayQueueScreen(
    currentSong: Song?,
    queue: List<Song>,
    currentIndex: Int,
    onSongClick: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部栏
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "播放队列",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "${queue.size} 首歌曲",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    if (queue.isNotEmpty()) {
                        TextButton(onClick = onClear) {
                            Text("清空")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.KeyboardArrowDown, "关闭")
                    }
                }
            )
            
            // 当前播放
            currentSong?.let { song ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = primaryColor.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "正在播放",
                            style = MaterialTheme.typography.labelMedium,
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = null,
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
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 队列列表
            if (queue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "播放队列为空",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    itemsIndexed(
                        items = queue,
                        key = { index, song -> "${song.id}_$index" }
                    ) { index, song ->
                        QueueItem(
                            song = song,
                            isCurrentPlaying = index == currentIndex,
                            onClick = { onSongClick(index) },
                            onRemove = { onRemove(index) },
                            onMoveUp = { onMoveUp(index) },
                            onMoveDown = { onMoveDown(index) },
                            isFirst = index == 0,
                            isLast = index == queue.lastIndex,
                            primaryColor = primaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueItem(
    song: Song,
    isCurrentPlaying: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
    primaryColor: Color
) {
    var showActions by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 拖动手柄
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "拖动",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 封面
        Box {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            if (isCurrentPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrentPlaying) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentPlaying) primaryColor else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // 操作按钮
        if (showActions) {
            Row {
                IconButton(
                    onClick = onMoveUp,
                    enabled = !isFirst
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        "上移",
                        tint = if (!isFirst) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = !isLast
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        "下移",
                        tint = if (!isLast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        "移除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            IconButton(onClick = { showActions = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    "更多",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * 迷你播放队列
 */
@Composable
fun MiniQueueCard(
    queue: List<Song>,
    currentIndex: Int,
    onSongClick: (Int) -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "播放队列 (${queue.size})",
                    style = MaterialTheme.typography.titleSmall
                )
                Icon(
                    Icons.Default.ExpandLess,
                    "展开",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 预览队列
            Row(
                horizontalArrangement = Arrangement.spacedBy((-8).dp)
            ) {
                queue.take(5).forEachIndexed { index, song ->
                    val isCurrent = index == 0 && currentIndex < queue.size
                    
                    AsyncImage(
                        model = song.coverUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isCurrent) 2.dp else 0.dp,
                                color = if (isCurrent) primaryColor else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
                
                if (queue.size > 5) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${queue.size - 5}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
