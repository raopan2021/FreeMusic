package com.freemusic.presentation.ui.player.gesture

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.freemusic.presentation.ui.theme.PrimaryIndigo
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 手势播放控制
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GesturePlayerControl(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isGestureActive by remember { mutableStateOf(false) }
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isGestureActive) offsetX else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "offsetX"
    )
    
    val animatedOffsetY by animateFloatAsState(
        targetValue = if (isGestureActive) offsetY else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "offsetY"
    )
    
    Box(
        modifier = modifier
            .offset { IntOffset(animatedOffsetX.roundToInt(), animatedOffsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isGestureActive = true
                    },
                    onDragEnd = {
                        isGestureActive = false
                        val screenWidth = configuration.screenWidthDp.dp.toPx()
                        val screenHeight = configuration.screenHeightDp.dp.toPx()
                        
                        when {
                            abs(offsetX) > screenWidth * 0.3f -> {
                                if (offsetX > 0) onSwipeRight() else onSwipeLeft()
                            }
                            abs(offsetY) > screenHeight * 0.2f -> {
                                if (offsetY > 0) onSwipeDown() else onSwipeUp()
                            }
                        }
                        
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragCancel = {
                        isGestureActive = false
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { _, dragAmount ->
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() }
                )
            }
    ) {
        content()
    }
}

/**
 * 滑动手势指示器
 */
@Composable
fun SwipeIndicator(
    direction: SwipeDirection,
    progress: Float,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val alpha = (progress * 0.8f).coerceIn(0f, 0.8f)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alpha * 0.3f)),
        contentAlignment = when (direction) {
            SwipeDirection.LEFT -> Alignment.CenterEnd
            SwipeDirection.RIGHT -> Alignment.CenterStart
            SwipeDirection.UP -> Alignment.BottomCenter
            SwipeDirection.DOWN -> Alignment.TopCenter
        }
    ) {
        Icon(
            imageVector = when (direction) {
                SwipeDirection.LEFT -> Icons.Default.SkipNext
                SwipeDirection.RIGHT -> Icons.Default.SkipPrevious
                SwipeDirection.UP -> Icons.Default.KeyboardArrowUp
                SwipeDirection.DOWN -> Icons.Default.KeyboardArrowDown
            },
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier
                .size(72.dp)
                .padding(16.dp)
                .graphicsLayer {
                    val scale = 0.5f + progress * 0.5f
                    scaleX = scale
                    scaleY = scale
                    this.alpha = progress.coerceIn(0f, 1f)
                }
        )
    }
}

enum class SwipeDirection {
    LEFT, RIGHT, UP, DOWN
}

/**
 * 音量滑动手势区域
 */
@Composable
fun VolumeGestureArea(
    onVolumeChange: (Float) -> Unit,
    currentVolume: Float,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { _ -> },
                    onDragEnd = { },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val delta = -dragAmount / 200f
                        val newVolume = (currentVolume + delta).coerceIn(0f, 1f)
                        onVolumeChange(newVolume)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when {
                    currentVolume == 0f -> Icons.Default.VolumeOff
                    currentVolume < 0.5f -> Icons.Default.VolumeDown
                    else -> Icons.Default.VolumeUp
                },
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(currentVolume * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "上下滑动调节音量",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 亮度滑动手势区域
 */
@Composable
fun BrightnessGestureArea(
    onBrightnessChange: (Float) -> Unit,
    currentBrightness: Float,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { _ -> },
                    onDragEnd = { },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val delta = -dragAmount / 200f
                        val newBrightness = (currentBrightness + delta).coerceIn(0f, 1f)
                        onBrightnessChange(newBrightness)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = when {
                    currentBrightness < 0.3f -> Icons.Default.BrightnessLow
                    currentBrightness < 0.7f -> Icons.Default.BrightnessMedium
                    else -> Icons.Default.BrightnessHigh
                },
                contentDescription = null,
                tint = Color.Yellow,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(currentBrightness * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "上下滑动调节亮度",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 长按播放/暂停
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongPressPlayButton(
    onPlayPause: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .size(80.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.8f))
                ),
                shape = CircleShape
            )
            .combinedClickable(
                onClick = { onPlayPause() },
                onLongClick = {
                    isPressed = true
                    onPlayPause()
                },
                onLongClickLabel = if (isPlaying) "暂停" else "播放"
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "暂停" else "播放",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(200)
            isPressed = false
        }
    }
}

/**
 * 摇一摇控制
 */
@Composable
fun ShakeToControl(
    onShake: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // 简化实现，实际需要加速度传感器
    Box(modifier = modifier) {
        content()
    }
}
