package com.freemusic.presentation.ui.player.lyrics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.freemusic.domain.model.LyricLine
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 卡拉OK歌词视图
 */
@Composable
fun KaraokeLyricsView(
    lyrics: List<LyricLine>,
    currentLineIndex: Int,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val lazyListState = rememberLazyListState()
    
    LaunchedEffect(currentLineIndex) {
        if (lyrics.isNotEmpty() && currentLineIndex in lyrics.indices) {
            lazyListState.animateScrollToItem(
                index = currentLineIndex.coerceIn(0, lyrics.size - 1),
                scrollOffset = -100
            )
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(lyrics) { index, line ->
                val isCurrentLine = index == currentLineIndex
                val isPastLine = index < currentLineIndex
                
                LyricLineText(
                    text = line.text,
                    isCurrentLine = isCurrentLine,
                    isPastLine = isPastLine,
                    primaryColor = primaryColor
                )
            }
        }
        
        // 顶部渐变
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Black.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // 底部渐变
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f),
                            Color.Black
                        )
                    )
                )
        )
    }
}

@Composable
private fun LyricLineText(
    text: String,
    isCurrentLine: Boolean,
    isPastLine: Boolean,
    primaryColor: Color
) {
    val textAlpha by animateFloatAsState(
        targetValue = when {
            isCurrentLine -> 1f
            isPastLine -> 0.4f
            else -> 0.7f
        },
        label = "text_alpha"
    )
    
    val textScale by animateFloatAsState(
        targetValue = if (isCurrentLine) 1.1f else 1f,
        label = "text_scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrentLine) primaryColor else Color.White.copy(alpha = textAlpha),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 滚动歌词视图
 */
@Composable
fun ScrollingLyricsView(
    lyrics: List<LyricLine>,
    currentLineIndex: Int,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val lazyListState = rememberLazyListState()
    
    LaunchedEffect(currentLineIndex) {
        if (lyrics.isNotEmpty() && currentLineIndex in lyrics.indices) {
            lazyListState.animateScrollToItem(
                index = currentLineIndex,
                scrollOffset = -50
            )
        }
    }
    
    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(lyrics) { index, line ->
            val isCurrentLine = index == currentLineIndex
            
            Text(
                text = line.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrentLine) primaryColor else Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}

/**
 * 翻译歌词视图
 */
@Composable
fun TranslationLyricsView(
    lyrics: List<LyricLine>,
    currentLineIndex: Int,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val currentLine = lyrics.getOrNull(currentLineIndex)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = currentLine != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Text(
                text = currentLine?.text ?: "",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 桌面歌词组件（简洁版）
 */
@Composable
fun DesktopLyricsWidget(
    lyrics: List<LyricLine>,
    currentLineIndex: Int,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    if (lyrics.isEmpty() || currentLineIndex !in lyrics.indices) {
        Box(
            modifier = modifier
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无歌词",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        return
    }
    
    val currentLine = lyrics[currentLineIndex]
    val nextLine = lyrics.getOrNull(currentLineIndex + 1)
    
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = currentLine.text,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                textAlign = TextAlign.Center
            )
            
            if (nextLine != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = nextLine.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
