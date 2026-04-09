package com.freemusic.presentation.ui.lyrics

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 歌词行数据
 */
data class LyricsLine(
    val time: Long, // 毫秒
    val text: String,
    val translation: String? = null,
    val isRomaJi: Boolean = false
)

/**
 * 同步歌词组件
 */
@Composable
fun SyncedLyricsView(
    lyrics: List<LyricsLine>,
    currentPosition: Long,
    onLyricClick: (LyricsLine) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val currentIndex = remember(lyrics, currentPosition) {
        lyrics.indexOfLast { it.time <= currentPosition }.coerceAtLeast(0)
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentPadding = PaddingValues(vertical = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(lyrics) { index, line ->
            LyricLineItem(
                line = line,
                isActive = index == currentIndex,
                isPast = index < currentIndex,
                onClick = { onLyricClick(line) },
                primaryColor = primaryColor
            )
        }
    }
}

@Composable
private fun LyricLineItem(
    line: LyricsLine,
    isActive: Boolean,
    isPast: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    val alpha = when {
        isActive -> 1f
        isPast -> 0.4f
        else -> 0.6f
    }
    
    val scale = if (isActive) 1.1f else 1f
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = line.text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = if (isActive) 20.sp else 16.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            textAlign = TextAlign.Center,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        )
        
        if (line.translation != null) {
            Text(
                text = line.translation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        if (line.isRomaJi) {
            Text(
                text = line.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 普通歌词组件
 */
@Composable
fun PlainLyricsView(
    lyrics: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = lyrics,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        modifier = modifier.padding(16.dp)
    )
}

/**
 * 歌词翻译视图
 */
@Composable
fun TranslatedLyricsView(
    lyrics: List<LyricsLine>,
    showOriginal: Boolean,
    showTranslation: Boolean,
    currentPosition: Long,
    onLyricClick: (LyricsLine) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val currentIndex = remember(lyrics, currentPosition) {
        lyrics.indexOfLast { it.time <= currentPosition }.coerceAtLeast(0)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        lyrics.forEachIndexed { index, line ->
            if ((index >= currentIndex - 2) && (index <= currentIndex + 2)) {
                LyricTranslationItem(
                    line = line,
                    isActive = index == currentIndex,
                    showOriginal = showOriginal,
                    showTranslation = showTranslation,
                    onClick = { onLyricClick(line) },
                    primaryColor = primaryColor
                )
            }
        }
    }
}

@Composable
private fun LyricTranslationItem(
    line: LyricsLine,
    isActive: Boolean,
    showOriginal: Boolean,
    showTranslation: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showOriginal && !showTranslation) {
            Text(
                text = line.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        } else if (showTranslation && line.translation != null) {
            Text(
                text = line.translation,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = line.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isActive) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
            
            if (line.translation != null) {
                Text(
                    text = line.translation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * 桌面歌词组件
 */
@Composable
fun DesktopLyricsView(
    lyrics: String,
    isLocked: Boolean,
    onLockToggle: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Surface(
        modifier = modifier
            .width(300.dp)
            .height(60.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = lyrics,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onLockToggle) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = if (isLocked) "解锁" else "锁定",
                    tint = if (isLocked) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 锁屏歌词
 */
@Composable
fun LockScreenLyricsView(
    songTitle: String,
    artistName: String,
    lyrics: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.8f),
                        Color.Black.copy(alpha = 0.6f)
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = songTitle,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = artistName,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = lyrics,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 歌词编辑对话框
 */
@Composable
fun LyricsEditDialog(
    originalLyrics: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editedLyrics by remember { mutableStateOf(originalLyrics) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text("编辑歌词") },
        text = {
            OutlinedTextField(
                value = editedLyrics,
                onValueChange = { editedLyrics = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                placeholder = { Text("输入歌词（每行一句）...") }
            )
        },
        confirmButton = {
            Button(onClick = { onSave(editedLyrics) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 歌词来源选择
 */
@Composable
fun LyricsSourceSelector(
    sources: List<LyricsSource>,
    selectedSource: LyricsSource?,
    onSourceSelected: (LyricsSource) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Column(modifier = modifier) {
        Text(
            text = "选择歌词来源",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        sources.forEach { source ->
            LyricsSourceItem(
                source = source,
                isSelected = source == selectedSource,
                onClick = { onSourceSelected(source) },
                primaryColor = primaryColor
            )
        }
    }
}

data class LyricsSource(
    val name: String,
    val description: String,
    val isPremium: Boolean = false
)

@Composable
private fun LyricsSourceItem(
    source: LyricsSource,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = source.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                if (source.isPremium) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = primaryColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Premium",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Text(
                text = source.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 歌词未找到状态
 */
@Composable
fun LyricsNotFoundState(
    onRetry: () -> Unit,
    onManualInput: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "未找到歌词",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "暂时没有这首歌的歌词",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("重试")
            }
            
            Button(onClick = onManualInput) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("手动输入")
            }
        }
    }
}

/**
 * 歌词同步微调滑块
 */
@Composable
fun LyricsOffsetSlider(
    offset: Long, // 毫秒，正数表示歌词提前，负数表示延后
    onOffsetChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Column(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "歌词同步微调",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "${if (offset >= 0) "+" else ""}${offset}ms",
                style = MaterialTheme.typography.bodyMedium,
                color = primaryColor
            )
        }
        
        Slider(
            value = offset.toFloat(),
            onValueChange = { onOffsetChange(it.toLong()) },
            valueRange = -3000f..3000f,
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor
            )
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "-3s",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = "0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = "+3s",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
