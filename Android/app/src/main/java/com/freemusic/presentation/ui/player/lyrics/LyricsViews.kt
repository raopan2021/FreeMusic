package com.freemusic.presentation.ui.player.lyrics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freemusic.domain.model.LyricLine
import com.freemusic.domain.model.LyricWord

/**
 * 卡拉OK逐字高亮歌词组件
 */
@Composable
fun KaraokeLyricsView(
    lyrics: List<LyricLine>,
    currentLineIndex: Int,
    onLineClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1),
    textColor: Color = Color.White
) {
    val listState = rememberLazyListState()
    
    // 自动滚动到当前行
    LaunchedEffect(currentLineIndex) {
        if (lyrics.isNotEmpty() && currentLineIndex in lyrics.indices) {
            listState.animateScrollToItem(
                index = currentLineIndex.coerceIn(0, lyrics.size - 1),
                scrollOffset = -100
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )
    ) {
        if (lyrics.isEmpty()) {
            Text(
                text = "暂无歌词",
                modifier = Modifier.align(Alignment.Center),
                color = textColor.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(lyrics) { index, lyricLine ->
                    val isCurrentLine = index == currentLineIndex
                    val isPreviousLine = index == currentLineIndex - 1
                    val isNextLine = index == currentLineIndex + 1
                    
                    LyricLineItem(
                        lyricLine = lyricLine,
                        isCurrentLine = isCurrentLine,
                        isPreviousLine = isPreviousLine,
                        isNextLine = isNextLine,
                        onClick = { onLineClick(index) },
                        primaryColor = primaryColor,
                        textColor = textColor
                    )
                }
            }
        }
    }
}

/**
 * 单行歌词项（卡拉OK效果）
 */
@Composable
fun LyricLineItem(
    lyricLine: LyricLine,
    isCurrentLine: Boolean,
    isPreviousLine: Boolean,
    isNextLine: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    textColor: Color
) {
    // 动画
    val scale by animateFloatAsState(
        targetValue = when {
            isCurrentLine -> 1.1f
            isPreviousLine || isNextLine -> 1f
            else -> 0.95f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = when {
            isCurrentLine -> 1f
            isPreviousLine -> 0.5f
            isNextLine -> 0.7f
            else -> 0.4f
        },
        animationSpec = tween(300),
        label = "alpha"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 逐字高亮歌词
        if (lyricLine.words.isNotEmpty()) {
            KaraokeText(
                words = lyricLine.words.map { it.text },
                isActive = isCurrentLine,
                textColor = textColor,
                primaryColor = primaryColor
            )
        } else {
            Text(
                text = lyricLine.text,
                fontSize = if (isCurrentLine) 22.sp else 18.sp,
                fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentLine) primaryColor else textColor.copy(alpha = alpha),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            )
        }
    }
}

/**
 * 逐字高亮文本
 */
@Composable
fun KaraokeText(
    words: List<String>,
    isActive: Boolean,
    textColor: Color,
    primaryColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        words.forEachIndexed { index, word ->
            val isCompleted = false // 简化处理

            Text(
                text = word,
                fontSize = if (isActive) 24.sp else 18.sp,
                fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isCompleted -> primaryColor
                    isActive -> textColor
                    else -> textColor.copy(alpha = 0.7f)
                }
            )
            
            if (index < words.size - 1) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

/**
 * 简化的歌词行组件（用于迷你播放器）
 */
@Composable
fun SimpleLyricLine(
    text: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lyric_indicator")
    
    val dotCount by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_count"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isPlaying) {
            // 播放指示器
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(primaryColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = if (isPlaying) {
                val dots = ".".repeat(dotCount.toInt())
                text.take(20) + dots
            } else {
                text.take(30)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPlaying) primaryColor else Color.Gray,
            maxLines = 1
        )
    }
}

/**
 * 翻译歌词视图
 */
@Composable
fun TranslationLyricsView(
    lyrics: List<LyricLine>,
    currentLineIndex: Int,
    onLineClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    val listState = rememberLazyListState()
    
    LaunchedEffect(currentLineIndex) {
        if (lyrics.isNotEmpty() && currentLineIndex in lyrics.indices) {
            listState.animateScrollToItem(
                index = currentLineIndex.coerceIn(0, lyrics.size - 1),
                scrollOffset = -50
            )
        }
    }

    Column(modifier = modifier) {
        if (lyrics.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无翻译歌词",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 60.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(lyrics) { index, lyricLine ->
                    val isCurrentLine = index == currentLineIndex
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clickable { onLineClick(index) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 原文
                        Text(
                            text = lyricLine.text,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrentLine) primaryColor else Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * 逐行滚动歌词视图
 */
@Composable
fun ScrollingLyricsView(
    lyrics: List<LyricLine>,
    currentLineIndex: Int,
    onLineClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    val listState = rememberLazyListState()
    
    // 中心对齐当前行
    LaunchedEffect(currentLineIndex) {
        if (lyrics.isNotEmpty() && currentLineIndex in lyrics.indices) {
            listState.animateScrollToItem(
                index = currentLineIndex,
                scrollOffset = -200
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        val centerY = maxHeight / 2
        
        if (lyrics.isEmpty()) {
            Text(
                text = "暂无歌词",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { Spacer(modifier = Modifier.height(centerY)) }
                
                itemsIndexed(lyrics) { index, lyricLine ->
                    val isCurrentLine = index == currentLineIndex
                    val distanceFromCenter = kotlin.math.abs(index - currentLineIndex)
                    val scale = 1f - (distanceFromCenter * 0.1f).coerceAtMost(0.3f)
                    val alpha = 1f - (distanceFromCenter * 0.2f).coerceAtMost(0.7f)
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                            .clickable { onLineClick(index) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = lyricLine.text,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrentLine) primaryColor else Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(centerY)) }
            }
        }
    }
}

/**
 * 迷你歌词徽章（用于卡片上显示）
 */
@Composable
fun MiniLyricBadge(
    lyric: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF6366F1)
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = if (isPlaying) primaryColor.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.MusicNote else Icons.Default.Lyrics,
                contentDescription = null,
                tint = if (isPlaying) primaryColor else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = lyric.take(15) + if (lyric.length > 15) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = if (isPlaying) primaryColor else Color.Gray
            )
        }
    }
}
