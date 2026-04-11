package com.freemusic.presentation.ui.import

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.freemusic.domain.model.Song

/**
 * 批量导入结果
 */
sealed class ImportResult {
    data class Success(val songs: List<Song>, val playlistName: String?) : ImportResult()
    data class Error(val message: String) : ImportResult()
    data object Loading : ImportResult()
}

/**
 * 批量导入屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    onBackClick: () -> Unit,
    onImportPlaylist: (String) -> Unit, // 导入歌单链接
    onImportSongs: (List<String>) -> Unit, // 导入歌曲名称列表
    onCreatePlaylist: (String, List<Song>) -> Unit // 创建本地歌单
) {
    var inputText by remember { mutableStateOf("") }
    var importResults by remember { mutableStateOf<List<ImportResult>>(emptyList()) }
    var selectedSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var playlistName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("批量导入") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 输入区域
            Text(
                text = "导入方式",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 歌单链接输入
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("歌单/专辑链接") },
                placeholder = { Text("粘贴网易云音乐歌单链接...") },
                leadingIcon = {
                    Icon(Icons.Default.Link, contentDescription = null)
                },
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = { inputText = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            isLoading = true
                            onImportPlaylist(inputText)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = inputText.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.PlaylistAdd, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导入歌单")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // 评论解析模式
            Text(
                text = "评论解析",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "粘贴包含歌曲名称的评论文本，自动识别生成歌单",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            var commentText by remember { mutableStateOf("") }
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                label = { Text("评论内容") },
                placeholder = { Text("粘贴论坛/评论区的文本内容...") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (commentText.isNotBlank()) {
                        isLoading = true
                        // 从评论中提取歌曲名称
                        val songNames = parseSongNamesFromComments(commentText)
                        onImportSongs(songNames)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = commentText.isNotBlank() && !isLoading
            ) {
                Icon(Icons.Default.TextSnippet, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("解析歌曲")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // 解析结果
            if (selectedSongs.isNotEmpty()) {
                Text(
                    text = "已识别歌曲 (${selectedSongs.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 歌单名称输入
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("歌单名称（可选）") },
                    placeholder = { Text("输入歌单名称...") },
                    leadingIcon = {
                        Icon(Icons.Default.PlaylistPlay, contentDescription = null)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(selectedSongs) { song ->
                        SongImportItem(
                            song = song,
                            onRemove = {
                                selectedSongs = selectedSongs.filter { it.id != song.id }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (selectedSongs.isNotEmpty()) {
                            onCreatePlaylist(
                                playlistName.ifBlank { "导入歌单" },
                                selectedSongs
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存歌单")
                }
            } else if (importResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ContentPaste,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "粘贴内容开始导入",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SongImportItem(
    song: Song,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "移除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 从评论文本中解析歌曲名称
 * 支持多种格式：
 * - 歌名 - 歌手
 * - 歌名（歌手）
 * - "歌名" 歌手
 * - 单独的歌名（需要进一步匹配）
 */
fun parseSongNamesFromComments(text: String): List<String> {
    val results = mutableListOf<String>()
    
    // 分割成行
    val lines = text.lines()
    
    for (line in lines) {
        val cleaned = line.trim()
        if (cleaned.isBlank()) continue
        
        var matched = false
        
        // 尝试匹配格式1: 歌名 - 歌手
        val dashPattern = Regex("""^(.+?)\s*[-–—]\s*(.+)$""")
        dashPattern.find(cleaned)?.let { match ->
            val songName = match.groupValues[1].trim()
            if (songName.length >= 2) {
                results.add(songName)
                matched = true
            }
        }
        
        if (matched) continue
        
        // 尝试匹配格式2: 歌名（歌手）
        val parenPattern = Regex("""^(.+?)\s*[（(].+?[）)]$""")
        parenPattern.find(cleaned)?.let { match ->
            val songName = match.groupValues[1].trim()
            if (songName.length >= 2) {
                results.add(songName)
                matched = true
            }
        }
        
        if (matched) continue
        
        // 尝试匹配格式3: "歌名" 或『歌名』
        val quotePattern = Regex("""^["『『"](.+?)["』"']""")
        quotePattern.find(cleaned)?.let { match ->
            val songName = match.groupValues[1].trim()
            if (songName.length >= 2) {
                results.add(songName)
                matched = true
            }
        }
        
        if (matched) continue
        
        // 如果行比较短且不包含特殊字符，也可能是歌名
        if (cleaned.length in 2..20 && !cleaned.contains("http") && 
            !cleaned.contains("@") && !cleaned.contains("#")) {
            // 过滤掉一些明显不是歌名的内容
            val isLikelySong = cleaned.any { it in "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\u4e00-\u9fa5" }
            if (isLikelySong && !cleaned.all { it.isDigit() || it == '.' }) {
                results.add(cleaned)
            }
        }
    }
    
    return results.distinct()
}

/**
 * 检测是否为歌单/专辑链接
 */
fun detectPlaylistLink(text: String): Boolean {
    val patterns = listOf(
        Regex("""https?://music\.163\.com/\#/playlist\?id=\d+"""),
        Regex("""https?://y\.music\.qq\.com/\w+"""),
        Regex("""https?://c\.y\.qq\.com/\w+"""),
        Regex("""https?://music\.qq\.com/\#/playlist/\d+""")
    )
    
    return patterns.any { it.containsMatchIn(text) }
}

/**
 * 从链接中提取歌单ID
 */
fun extractPlaylistId(link: String): String? {
    val patterns = listOf(
        Regex("""id=(\d+)"""),
        Regex("""playlist/(\d+)"""),
        Regex("""/(\d+)$""")
    )
    
    for (pattern in patterns) {
        pattern.find(link)?.let { match ->
            return match.groupValues.getOrNull(1)
        }
    }
    
    return null
}
