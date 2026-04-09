package com.freemusic.presentation.ui.import

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.freemusic.domain.model.Playlist
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 歌单链接导入
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistLinkImport(
    onImport: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    var link by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = primaryColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "链接导入",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("粘贴歌单链接...")
                },
                singleLine = true,
                enabled = !isLoading,
                trailingIcon = {
                    if (link.isNotEmpty()) {
                        IconButton(onClick = { link = "" }) {
                            Icon(Icons.Default.Clear, "清除")
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { onImport(link) },
                modifier = Modifier.fillMaxWidth(),
                enabled = link.isNotEmpty() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导入中...")
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导入歌单")
                }
            }
        }
    }
}

/**
 * 评论解析导入
 */
@Composable
fun CommentParserImport(
    parsedSongs: List<Song>,
    onParse: (String) -> Unit,
    onSongSelected: (Song, Boolean) -> Unit,
    onImportSelected: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    var commentText by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = null,
                    tint = primaryColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "评论解析",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "复制歌曲评论内容，提取其中的歌曲信息",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = {
                    Text("粘贴评论内容...")
                },
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onParse(commentText) },
                    modifier = Modifier.weight(1f),
                    enabled = commentText.isNotEmpty()
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("解析")
                }
                
                Button(
                    onClick = onImportSelected,
                    modifier = Modifier.weight(1f),
                    enabled = parsedSongs.isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("导入")
                }
            }
            
            // 解析结果
            if (parsedSongs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "解析到 ${parsedSongs.size} 首歌曲",
                    style = MaterialTheme.typography.labelMedium,
                    color = primaryColor
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                parsedSongs.take(5).forEach { song ->
                    ParsedSongItem(
                        song = song,
                        onSelectedChange = { selected -> onSongSelected(song, selected) }
                    )
                }
                
                if (parsedSongs.size > 5) {
                    Text(
                        text = "还有 ${parsedSongs.size - 5} 首...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ParsedSongItem(
    song: Song,
    onSelectedChange: (Boolean) -> Unit
) {
    var selected by remember { mutableStateOf(true) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                selected = !selected
                onSelectedChange(selected)
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = {
                selected = it
                onSelectedChange(it)
            }
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}

/**
 * 本地歌单创建
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalPlaylistCreator(
    onCreate: (name: String, description: String) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = null,
                    tint = primaryColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "创建本地歌单",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("歌单名称") },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                label = { Text("描述（可选）") },
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onCreate(name, description) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("创建歌单")
            }
        }
    }
}

/**
 * 导入结果视图
 */
@Composable
fun ImportResultView(
    successCount: Int,
    failCount: Int,
    failedSongs: List<String>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (failCount > 0) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else 
                primaryColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (failCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (failCount > 0) MaterialTheme.colorScheme.error else primaryColor,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = if (failCount > 0) "部分导入成功" else "导入成功",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "成功: $successCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryColor
                )
                if (failCount > 0) {
                    Text(
                        text = "失败: $failCount",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (failedSongs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "导入失败的歌曲:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                failedSongs.take(3).forEach { songName ->
                    Text(
                        text = "• $songName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
                
                if (failedSongs.size > 3) {
                    Text(
                        text = "...还有 ${failedSongs.size - 3} 首",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onDismiss) {
                Text("完成")
            }
        }
    }
}

/**
 * 批量导入进度
 */
@Composable
fun BatchImportProgress(
    current: Int,
    total: Int,
    currentSong: String,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = current.toFloat() / total,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = primaryColor,
            trackColor = primaryColor.copy(alpha = 0.2f)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "正在导入 ($current/$total)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = currentSong,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
