package com.freemusic.presentation.ui.mini

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * Mini Player - 底部迷你播放器
 * 显示当前播放歌曲和基本控制
 */
@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    progress: Float, // 0-1
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Column {
            // 进度条
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = PrimaryIndigo,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 封面
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = "封面",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 歌曲信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 控制按钮
                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "暂停" else "播放",
                        tint = PrimaryIndigo
                    )
                }
                
                IconButton(onClick = onNextClick) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "下一首",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 展开的 Mini Player
 * 在播放页面底部显示
 */
@Composable
fun ExpandedMiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    progress: Float,
    currentTime: String,
    totalTime: String,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onLikeClick: () -> Unit,
    isFavorite: Boolean,
    onDownloadClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (song == null) return
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        // 进度条
        Column {
            Slider(
                value = progress,
                onValueChange = { /* 可以添加 seek 功能 */ },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryIndigo,
                    activeTrackColor = PrimaryIndigo
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = totalTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "喜欢",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "上一首",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            FilledIconButton(
                onClick = onPlayPauseClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PrimaryIndigo
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            IconButton(onClick = onNextClick) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "下一首",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            IconButton(onClick = onDownloadClick) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "下载",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
