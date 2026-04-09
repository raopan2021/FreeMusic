package com.freemusic.presentation.ui.player.gesture

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

/**
 * 手势控制器状态
 */
data class GestureState(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val isDragging: Boolean = false
)

/**
 * 播放控制手势检测器
 */
@Composable
fun rememberGestureState(): GestureState {
    return remember { GestureState() }
}

/**
 * 滑动控制音量
 */
@Composable
fun VolumeGestureControl(
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var volume by remember { mutableFloatStateOf(0.5f) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragOffset = 0f },
                    onDragEnd = { },
                    onDragCancel = { dragOffset = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                        
                        // 每 10dp 变化 1% 音量
                        val volumeDelta = -dragOffset / 1000f
                        volume = (volume + volumeDelta).coerceIn(0f, 1f)
                        onVolumeChange(volume)
                        
                        if (abs(dragOffset) > 50f) {
                            dragOffset = 0f // 重置以继续检测
                        }
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * 滑动控制进度
 */
@Composable
fun SeekGestureControl(
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var seekPosition by remember { mutableFloatStateOf(0f) }
    var isSeeking by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isSeeking = true
                        seekPosition = 0f
                    },
                    onDragEnd = {
                        isSeeking = false
                        onSeek(seekPosition)
                    },
                    onDragCancel = {
                        isSeeking = false
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        seekPosition += dragAmount / size.width
                        seekPosition = seekPosition.coerceIn(0f, 1f)
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * 双击手势检测
 */
@Composable
fun DoubleTapGesture(
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTap()
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * 长按手势检测
 */
@Composable
fun LongPressGesture(
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongPress()
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * 滑动删除/完成手势
 */
enum class SwipeDirection {
    LEFT,
    RIGHT,
    UP,
    DOWN,
    NONE
}

@Composable
fun SwipeableGesture(
    onSwipe: (SwipeDirection) -> Unit,
    swipeThreshold: Float = 200f,
    modifier: Modifier = Modifier,
    content: @Composable (offset: Offset, isSwiping: Boolean) -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isSwiping by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isSwiping = true
                    },
                    onDragEnd = {
                        val direction = when {
                            offset.x < -swipeThreshold -> SwipeDirection.LEFT
                            offset.x > swipeThreshold -> SwipeDirection.RIGHT
                            offset.y < -swipeThreshold -> SwipeDirection.UP
                            offset.y > swipeThreshold -> SwipeDirection.DOWN
                            else -> SwipeDirection.NONE
                        }
                        if (direction != SwipeDirection.NONE) {
                            onSwipe(direction)
                        }
                        offset = Offset.Zero
                        isSwiping = false
                    },
                    onDragCancel = {
                        offset = Offset.Zero
                        isSwiping = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += Offset(dragAmount.x, dragAmount.y)
                    }
                )
            }
    ) {
        content(offset, isSwiping)
    }
}

/**
 * 专辑封面手势控制（缩放 + 旋转）
 */
@Composable
fun TransformableCover(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotationChange ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    rotation += rotationChange
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // 双击重置
                        scale = 1f
                        rotation = 0f
                        offsetX = 0f
                        offsetY = 0f
                    }
                )
            }
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                rotationZ = animatedRotation
                translationX = offsetX
                translationY = offsetY
            }
    ) {
        content()
    }
}

/**
 * 滑动手势播放器
 * 支持上下滑动控制上一首/下一首
 * 左右滑动控制音量
 */
@Composable
fun GestureMusicPlayer(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var swipeDirection by remember { mutableStateOf(SwipeDirection.NONE) }
    var accumulatedY by remember { mutableFloatStateOf(0f) }
    var accumulatedX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        accumulatedY = 0f
                        accumulatedX = 0f
                    },
                    onDragEnd = {
                        when {
                            accumulatedY < -150f -> onNext()  // 向上滑 = 下一首
                            accumulatedY > 150f -> onPrevious() // 向下滑 = 上一首
                        }
                        accumulatedY = 0f
                        accumulatedX = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedY += dragAmount
                        
                        // 如果主要是上下滑动
                        if (abs(accumulatedY) > abs(accumulatedX)) {
                            // 可以添加视觉反馈
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        accumulatedX = 0f
                    },
                    onDragEnd = {
                        when {
                            accumulatedX < -150f -> onVolumeChange(1f)   // 向左滑 = 音量最大
                            accumulatedX > 150f -> onVolumeChange(0f)  // 向右滑 = 静音
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedX += dragAmount
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * 3D 翻转手势
 */
@Composable
fun FlipGesture(
    onFlipStart: () -> Unit,
    onFlipEnd: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (flipProgress: Float) -> Unit
) {
    var isFlipping by remember { mutableStateOf(false) }
    var flipProgress by remember { mutableFloatStateOf(0f) }

    val animatedFlip by animateFloatAsState(
        targetValue = flipProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "flip"
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isFlipping = true
                        onFlipStart()
                    },
                    onDragEnd = {
                        isFlipping = false
                        flipProgress = 0f
                        onFlipEnd()
                    },
                    onDragCancel = {
                        isFlipping = false
                        flipProgress = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // 水平拖动触发翻转
                        flipProgress = (dragAmount.x / size.width).coerceIn(-1f, 1f)
                    }
                )
            }
    ) {
        content(animatedFlip)
    }
}
