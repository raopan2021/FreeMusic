package com.freemusic.presentation.ui.player.queue

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.freemusic.domain.model.Song

/**
 * 播放队列组件
 */
@Composable
fun PlayQueue(
    currentSong: Song?,
    queue: List<Song>,
    currentIndex: Int,
    onSongClick: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onClear: () -> Unit,
    onShuffle: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    Column(modifier = modifier.fillMaxSize()) {
        // 头部
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "播放队列",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${queue.size} 首歌曲",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onShuffle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "随机播放",
                        tint = primaryColor
                    )
                }
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "清空队列"
                    )
                }
            }
        }
        
        Divider()
        
        // 队列列表
        if (queue.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
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
                // 当前播放部分
                if (currentSong != null) {
                    item {
                        Text(
                            text = "正在播放",
                            style = MaterialTheme.typography.labelMedium,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    item {
                        QueueItem(
                            song = currentSong,
                            isPlaying = true,
                            index = currentIndex,
                            onClick = { onSongClick(currentIndex) },
                            onRemove = { },
                            primaryColor = primaryColor
                        )
                    }
                }
                
                // 待播部分
                item {
                    Text(
                        text = "待播歌曲",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                itemsIndexed(
                    items = queue.filterIndexed { index, _ -> index != currentIndex },
                    key = { _, song -> song.id }
                ) { _, song ->
                    val actualIndex = queue.indexOf(song)
                    QueueItem(
                        song = song,
                        isPlaying = false,
                        index = actualIndex,
                        onClick = { onSongClick(actualIndex) },
                        onRemove = { onRemove(actualIndex) },
                        primaryColor = primaryColor
                    )
                }
            }
        }
    }
}

/**
 * 队列项
 */
@Composable
fun QueueItem(
    song: Song,
    isPlaying: Boolean,
    index: Int,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    primaryColor: Color
) {
    val animatedBorderAlpha by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = tween(300),
        label = "border_alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) {
                primaryColor.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 封面
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = "专辑封面",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // 播放中边框
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, primaryColor, RoundedCornerShape(8.dp))
                    )
                    
                    // 播放指示器
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "正在播放",
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 歌曲信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                    color = if (isPlaying) primaryColor else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 操作按钮
            if (!isPlaying) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "移除",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                IconButton(onClick = { /* 移动 */ }) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "拖动排序",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * 迷你播放列表（底部弹出）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniQueueSheet(
    currentSong: Song?,
    queue: List<Song>,
    currentIndex: Int,
    onSongClick: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            // 把手
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 当前歌曲
            currentSong?.let { song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "正在播放",
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
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
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = "专辑封面",
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
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            
            // 队列标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "播放队列 (${queue.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { /* 清空 */ }) {
                    Text("清空")
                }
            }
            
            // 队列列表
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                itemsIndexed(
                    items = queue.filterIndexed { index, _ -> index != currentIndex },
                    key = { _, song -> song.id }
                ) { _, song ->
                    val actualIndex = queue.indexOf(song)
                    QueueItem(
                        song = song,
                        isPlaying = false,
                        index = actualIndex,
                        onClick = { onSongClick(actualIndex) },
                        onRemove = { /* 移除 */ },
                        primaryColor = primaryColor
                    )
                }
            }
        }
    }
}

/**
 * 可拖拽排序的播放列表
 */
@Composable
fun DraggablePlayQueue(
    queue: List<Song>,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = queue,
            key = { _, song -> song.id }
        ) { index, song ->
            val isDragging = draggedItemIndex == index
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isDragging) {
                            Modifier
                                .shadow(8.dp, RoundedCornerShape(8.dp))
                        } else Modifier
                    )
            ) {
                // 拖动手柄
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "拖动",
                    tint = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 8.dp)
                )
                
                QueueItem(
                    song = song,
                    isPlaying = false,
                    index = index,
                    onClick = { },
                    onRemove = { onRemove(index) },
                    primaryColor = primaryColor
                )
            }
        }
    }
}

/**
 * 历史记录列表
 */
@Composable
fun HistoryList(
    history: List<Song>,
    onSongClick: (Song) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "最近播放",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClear) {
                Text("清空")
            }
        }
        
        LazyColumn {
            itemsIndexed(
                items = history.take(20),
                key = { _, song -> song.id }
            ) { index, song ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = song.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        Text(
                            text = song.artist,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingContent = {
                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = "专辑封面",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    },
                    trailingContent = {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.clickable { onSongClick(song) }
                )
            }
        }
    }
}
