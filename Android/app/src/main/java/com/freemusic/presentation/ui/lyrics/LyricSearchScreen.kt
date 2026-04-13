package com.freemusic.presentation.ui.lyrics

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import coil.compose.AsyncImage
import com.freemusic.data.remote.api.LrclibLyric
import com.freemusic.domain.model.Lyrics
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 歌词搜索页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricSearchScreen(
    song: Song,
    lrclibResults: List<LrclibLyric>,
    isLoading: Boolean,
    onSearch: () -> Unit,
    onBackClick: () -> Unit,
    onLyricSelect: (Lyrics) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    // 预览选中的歌词
    var previewLyric by remember { mutableStateOf<LrclibLyric?>(null) }
    
    // 自动搜索
    LaunchedEffect(song) {
        focusRequester.requestFocus()
        onSearch()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // 顶部栏
        TopAppBar(
            title = { Text("搜索歌词") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        // 歌曲信息
        ListItem(
            headlineContent = { Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            supportingContent = { Text(song.artist, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingContent = {
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            },
            trailingContent = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        )
        
        HorizontalDivider()
        
        // 歌词结果列表
        if (lrclibResults.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "未找到歌词",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "试试其他来源或手动输入",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lrclibResults) { lyric ->
                    LyricResultItem(
                        lyric = lyric,
                        onClick = { previewLyric = lyric }
                    )
                }
            }
        }
    }
    
    // 歌词预览对话框
    previewLyric?.let { lyric ->
        LyricPreviewDialog(
            lyric = lyric,
            onConfirm = {
                // 转换为 Lyrics 对象
                val lyrics = parseLrclibLyric(lyric)
                onLyricSelect(lyrics)
                previewLyric = null
            },
            onDismiss = { previewLyric = null }
        )
    }
}

@Composable
private fun LyricResultItem(
    lyric: LrclibLyric,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lyric.trackName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${lyric.artistName} - ${lyric.albumName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (lyric.instrumental) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "纯音乐",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // 歌词预览
            val previewText = lyric.syncedLyrics?.lines()?.take(3)?.joinToString("\n") 
                ?: lyric.plainLyrics?.lines()?.take(3)?.joinToString("\n")
                ?: ""
            if (previewText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricPreviewDialog(
    lyric: LrclibLyric,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "歌词预览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${lyric.trackName} - ${lyric.artistName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 显示完整歌词
            val fullLyrics = lyric.syncedLyrics ?: lyric.plainLyrics ?: ""
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                LazyColumn {
                    items(fullLyrics.lines().size) { index ->
                        Text(
                            text = fullLyrics.lines().getOrElse(index) { "" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("使用此歌词")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 将 LrclibLyric 转换为 domain Lyrics
 */
private fun parseLrclibLyric(lrclibLyric: LrclibLyric): Lyrics {
    return Lyrics(
        songId = "",
        lrc = lrclibLyric.syncedLyrics ?: lrclibLyric.plainLyrics ?: "",
        yrc = null,
        translation = null,
        ttml = null
    )
}
