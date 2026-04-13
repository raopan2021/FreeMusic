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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
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
 * 当前歌词行固定在屏幕中间，上下滚动切换
 */
@Composable
fun ScrollingLyricsView(
    lyrics: List<LyricLine>,
    currentLineIndex: Int,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo,
    fontSize: Int = 16,
    backgroundColor: Color = Color.Black.copy(alpha = 0.9f)
) {
    val lazyListState = rememberLazyListState()
    
    LaunchedEffect(currentLineIndex) {
        if (lyrics.isNotEmpty() && currentLineIndex in lyrics.indices) {
            // 计算需要滚动到的位置，使当前行居中
            // 使用 smoothScrollTo 让动画更平滑
            lazyListState.animateScrollToItem(
                index = maxOf(0, currentLineIndex - 1),
                scrollOffset = 0
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 100.dp,
                    bottom = 80.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(vertical = 100.dp)
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
                        .padding(vertical = 8.dp, horizontal = 24.dp)
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

/**
 * 播放器内使用的紧凑DVD风格歌词组件
 * 当前歌词靠左高亮显示，下一行歌词靠右半透明
 * 歌词对显示: (1,2) -> (3,2) -> (3,4) -> (5,4) -> (5,6) ...
 * 带颜色过渡动画
 */
@Composable
fun PlayerDvdLyricsView(
    lyrics: List<LyricLine>,
    currentLineIndex: Int,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo,
    fontSize: Int = 16
) {
    // DVD歌词逻辑:
    // - 偶数行(0,2,4...): (idx, idx+1), 第1行高亮
    // - 奇数行(1,3,5...): (idx+1, idx), 第2行高亮
    val isSecondLineHighlighted = currentLineIndex % 2 == 1
    
    val line1Index = if (isSecondLineHighlighted) currentLineIndex + 1 else currentLineIndex
    val line2Index = if (isSecondLineHighlighted) currentLineIndex else currentLineIndex + 1
    
    val line1 = lyrics.getOrNull(line1Index)
    val line2 = lyrics.getOrNull(line2Index)
    
    // 动画颜色过渡
    val line1Color by animateColorAsState(
        targetValue = when {
            line1 == null -> Color.White.copy(alpha = 0.3f)
            !isSecondLineHighlighted -> primaryColor
            else -> Color.White.copy(alpha = 0.5f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "line1Color"
    )
    
    val line2Color by animateColorAsState(
        targetValue = when {
            line2 == null -> Color.White.copy(alpha = 0.5f)
            isSecondLineHighlighted -> primaryColor
            else -> Color.White.copy(alpha = 0.5f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "line2Color"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow)),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 第1行歌词 - 靠左显示
        Text(
            text = line1?.text ?: "暂无歌词",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = (fontSize + 4).sp
            ),
            color = line1Color,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 第2行歌词 - 靠右显示
        Text(
            text = line2?.text ?: " ",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (fontSize - 2).sp,
                fontWeight = FontWeight.Normal,
                lineHeight = (fontSize).sp
            ),
            color = line2Color,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
