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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freemusic.domain.model.LyricLine
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * DVD风格歌词视图
 * 当前歌词行靠左显示（高亮）
 * 下一行歌词靠右显示
 * 模拟经典DVD卡拉OK的双行布局
 */
@Composable
fun DvdLyricsView(
    lyrics: List<LyricLine>,
    currentLineIndex: Int,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo,
    fontSize: Int = 16
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(24.dp)
    ) {
        if (lyrics.isEmpty()) {
            Text(
                text = "暂无歌词",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            val currentLine = lyrics.getOrNull(currentLineIndex)
            val nextLine = lyrics.getOrNull(currentLineIndex + 1)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 第一行：当前歌词，靠左
            LyricLineText(
                text = currentLine?.text ?: "",
                isHighlighted = true,
                isLeft = true,
                primaryColor = primaryColor,
                fontSize = fontSize
            )

            // 第二行：下一行歌词，靠右
            LyricLineText(
                text = nextLine?.text ?: "",
                isHighlighted = false,
                isLeft = false,
                primaryColor = primaryColor,
                fontSize = fontSize
            )
            }
        }
    }
}

@Composable
private fun LyricLineText(
    text: String,
    isHighlighted: Boolean,
    isLeft: Boolean,
    primaryColor: Color,
    fontSize: Int
) {
    val textAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0.5f,
        label = "text_alpha"
    )

    val textScale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.05f else 1f,
        label = "text_scale"
    )

    // 跑马灯动画
    val infiniteTransition = rememberInfiniteTransition(label = "marquee")
    val marqueeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "marquee_offset"
    )

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text.ifEmpty { " " },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = (fontSize + 8).sp,
                lineHeight = (fontSize + 14).sp
            ),
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) primaryColor else Color.White.copy(alpha = textAlpha),
            textAlign = if (isLeft) TextAlign.Start else TextAlign.End,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    // 跑马灯效果：当文本超长时水平滚动
                    val maxScroll = 100f
                    translationX = -marqueeOffset * maxScroll
                }
                .align(if (isLeft) Alignment.TopStart else Alignment.BottomEnd)
        )
    }
}

/**
 * 滚动歌词视图 - 全屏居中滚动显示
 * 所有歌词行居中对齐，垂直滚动，当前行高亮
 */
@Composable
fun ScrollingLyricsView(
    lyrics: List<LyricLine>,
    currentLineIndex: Int,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo,
    fontSize: Int = 16
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(lyrics) { index, line ->
                val isCurrentLine = index == currentLineIndex

                Text(
                    text = line.text,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = (fontSize + 4).sp,
                        lineHeight = (fontSize + 10).sp
                    ),
                    fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrentLine) primaryColor else Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )
            }
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
