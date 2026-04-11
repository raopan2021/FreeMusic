package com.freemusic.presentation.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.freemusic.domain.model.Song
import com.freemusic.presentation.viewmodel.QueueItem

/**
 * 播放队列列表（支持拖动排序）
 * 使用 sh.calvin.reorderable 库 + 极简 item 设计减少 recomposition 开销
 */
@Composable
fun QueueList(
    queueItems: List<QueueItem>,
    currentIndex: Int,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onPlay: (Int) -> Unit,
    sheetHeight: Dp,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = listState,
        onMove = { from, to ->
            onMove(from.index, to.index)
        }
    )

    // 固定颜色值，避免每次 recompose 时调用 MaterialTheme
    val highlightBg = Color(0x4D6200EE)
    val primaryColor = Color(0xFF6200EE)

    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(
                items = queueItems,
                key = { _, item -> item.song.id }
            ) { index, item ->
                val song = item.song
                val isCurrentSong = index == currentIndex

                ReorderableItem(
                    state = reorderableState,
                    key = item.song.id
                ) { isDragging ->
                    // 极简 item：无动画、无额外修饰符
                    // 背景只在当前歌曲且非拖动时显示
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isCurrentSong && !isDragging) Modifier.background(highlightBg)
                                else Modifier
                            )
                            .clickable(enabled = !isCurrentSong) { onPlay(index) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "拖动排序",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .padding(start = 4.dp, end = 8.dp)
                                    .draggableHandle()
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrentSong) primaryColor else Color.Unspecified,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artist ?: "未知艺术家",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { onRemove(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "删除",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
