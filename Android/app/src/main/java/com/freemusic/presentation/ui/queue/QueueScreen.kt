package com.freemusic.presentation.ui.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.freemusic.domain.model.Song
import com.freemusic.presentation.viewmodel.PlayerViewModel
import com.freemusic.presentation.viewmodel.QueueViewModel
import com.freemusic.presentation.viewmodel.RepeatMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    onBackClick: () -> Unit,
    onSongClick: (Int) -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    viewModel: QueueViewModel = hiltViewModel()
) {
    val playerState by playerViewModel.uiState.collectAsState()
    val queueState by viewModel.uiState.collectAsState()
    
    // 使用 PlayerViewModel 的播放列表作为队列
    val currentQueue = playerState.playlist
    val currentIndex = playerState.currentIndex
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<Int>()) }
    
    val totalDuration = currentQueue.sumOf { it.duration }.let { millis ->
        val hours = millis / 3600000
        val minutes = (millis % 3600000) / 60000
        val seconds = (millis % 60000) / 1000
        if (hours > 0) "${hours}小时${minutes}分钟" else "${minutes}分${seconds}秒"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (isSelectionMode) {
                        Text("已选择 ${selectedItems.size} 项")
                    } else {
                        Text("播放队列")
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = {
                            // 移除选中的项目
                            selectedItems.sortedDescending().forEach { index ->
                                viewModel.removeFromQueue(index)
                            }
                            selectedItems = emptySet()
                            isSelectionMode = false
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除所选")
                        }
                    } else {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "清空队列"
                            )
                        }
                        IconButton(onClick = { isSelectionMode = true }) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "多选"
                            )
                        }
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
            // 队列控制栏
            QueueControls(
                isShuffleEnabled = queueState.isShuffleEnabled,
                repeatMode = queueState.repeatMode,
                totalCount = currentQueue.size,
                totalDuration = totalDuration,
                currentIndex = currentIndex,
                onShuffleClick = viewModel::toggleShuffle,
                onRepeatClick = viewModel::cycleRepeatMode
            )

            Divider()

            if (currentQueue.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "播放队列为空",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    itemsIndexed(
                        items = currentQueue,
                        key = { index, song -> "${song.id}_$index" }
                    ) { index, song ->
                        QueueItem(
                            song = song,
                            isPlaying = index == currentIndex,
                            isSelected = index in selectedItems,
                            isSelectionMode = isSelectionMode,
                            onClick = { 
                                if (isSelectionMode) {
                                    selectedItems = if (index in selectedItems) {
                                        selectedItems - index
                                    } else {
                                        selectedItems + index
                                    }
                                } else {
                                    onSongClick(index)
                                }
                            },
                            onRemove = { viewModel.removeFromQueue(index) },
                            onMoveUp = { viewModel.moveItem(index, index - 1) },
                            onMoveDown = { viewModel.moveItem(index, index + 1) },
                            canMoveUp = index > 0,
                            canMoveDown = index < currentQueue.size - 1,
                            onSelectionChange = { selected ->
                                selectedItems = if (selected) selectedItems + index else selectedItems - index
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 清空队列确认对话框
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("清空队列") },
            text = { Text("确定要清空播放队列吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearQueue()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("确定")
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
private fun QueueControls(
    isShuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    totalCount: Int,
    totalDuration: String,
    currentIndex: Int,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "${totalCount} 首歌曲",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "总时长: $totalDuration",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 随机播放
            IconButton(onClick = onShuffleClick) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "随机播放",
                    tint = if (isShuffleEnabled) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 循环模式
            IconButton(onClick = onRepeatClick) {
                Icon(
                    imageVector = when (repeatMode) {
                        RepeatMode.OFF -> Icons.Default.Repeat
                        RepeatMode.ALL -> Icons.Default.Repeat
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                    },
                    contentDescription = "循环模式: ${repeatMode.name}",
                    tint = if (repeatMode != RepeatMode.OFF) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QueueItem(
    song: Song,
    isPlaying: Boolean,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    onSelectionChange: ((Boolean) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    isPlaying -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 选择模式下的复选框
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChange
            )
        }
        
        // 播放指示器
        if (isPlaying) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "正在播放",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 封面
        AsyncImage(
            model = song.coverUrl,
            contentDescription = "封面",
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 歌曲信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isPlaying) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 时长
        Text(
            text = formatDuration(song.duration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 上移按钮
        if (canMoveUp) {
            IconButton(onClick = onMoveUp) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "上移",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 下移按钮
        if (canMoveDown) {
            IconButton(onClick = onMoveDown) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "下移",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 删除按钮
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "从队列移除",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
