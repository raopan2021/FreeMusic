package com.freemusic.presentation.ui.player.transitions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.freemusic.domain.model.Song

/**
 * 页面过渡动画工厂
 */
object PageTransitions {

    /**
     * 缩放过渡
     */
    @Composable
    fun ScaleTransition(
        currentSong: Song?,
        nextSong: Song?,
        progress: Float, // 0-1
        contentScale: ContentScale = ContentScale.Crop
    ) {
        val scaleOut = 1f + (0.2f * progress)
        val alphaOut = 1f - progress

        Box(modifier = Modifier.fillMaxSize()) {
            // 当前封面缩小
            currentSong?.let { song ->
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scaleOut
                            scaleY = scaleOut
                            alpha = alphaOut
                        },
                    contentScale = contentScale
                )
            }

            // 下一封面上放大
            nextSong?.let { song ->
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = 1.2f - (0.2f * progress)
                            scaleY = 1.2f - (0.2f * progress)
                            alpha = progress
                        },
                    contentScale = contentScale
                )
            }
        }
    }

    /**
     * 旋转过渡
     */
    @Composable
    fun RotateTransition(
        currentSong: Song?,
        nextSong: Song?,
        progress: Float,
        contentScale: ContentScale = ContentScale.Crop
    ) {
        val currentRotation = progress * 90f
        val nextRotation = -90f + (progress * 90f)

        Box(modifier = Modifier.fillMaxSize()) {
            currentSong?.let { song ->
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = currentRotation
                            cameraDistance = 12f * density
                            alpha = 1f - progress
                        },
                    contentScale = contentScale
                )
            }

            nextSong?.let { song ->
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = nextRotation
                            cameraDistance = 12f * density
                            alpha = progress
                        },
                    contentScale = contentScale
                )
            }
        }
    }

    /**
     * 滑动过渡
     */
    @Composable
    fun SlideTransition(
        currentSong: Song?,
        nextSong: Song?,
        progress: Float,
        contentScale: ContentScale = ContentScale.Crop
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            currentSong?.let { song ->
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = -size.width * progress
                            alpha = 1f - progress
                        },
                    contentScale = contentScale
                )
            }

            nextSong?.let { song ->
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = size.width * (1f - progress)
                            alpha = progress
                        },
                    contentScale = contentScale
                )
            }
        }
    }

    /**
     * 淡入淡出过渡
     */
    @Composable
    fun FadeTransition(
        currentSong: Song?,
        nextSong: Song?,
        progress: Float,
        contentScale: ContentScale = ContentScale.Crop
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            currentSong?.let { song ->
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = 1f - progress
                        },
                    contentScale = contentScale
                )
            }

            nextSong?.let { song ->
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = progress
                        },
                    contentScale = contentScale
                )
            }
        }
    }

    /**
     * 翻页过渡
     */
    @Composable
    fun PageFlipTransition(
        currentSong: Song?,
        nextSong: Song?,
        progress: Float,
        contentScale: ContentScale = ContentScale.Crop
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            currentSong?.let { song ->
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = progress * 180f
                            cameraDistance = 16f * density
                            alpha = if (progress < 0.5f) 1f else 0f
                        },
                    contentScale = contentScale
                )
            }

            nextSong?.let { song ->
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = -180f + (progress * 180f)
                            cameraDistance = 16f * density
                            alpha = if (progress > 0.5f) progress else 0f
                        },
                    contentScale = contentScale
                )
            }
        }
    }
}

/**
 * 歌曲切换动画状态
 */
enum class TransitionType {
    NONE,
    SCALE,
    ROTATE,
    SLIDE,
    FADE,
    PAGE_FLIP,
    CROSSFADE,
    ZOOM_SLIDE
}

/**
 * 动画化封面切换
 */
@Composable
fun AnimatedCoverSwitch(
    currentSong: Song?,
    nextSong: Song?,
    transitionType: TransitionType,
    progress: Float,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    when (transitionType) {
        TransitionType.NONE -> {
            currentSong?.let { song ->
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
        }
        TransitionType.SCALE -> {
            PageTransitions.ScaleTransition(currentSong, nextSong, progress, contentScale)
        }
        TransitionType.ROTATE -> {
            PageTransitions.RotateTransition(currentSong, nextSong, progress, contentScale)
        }
        TransitionType.SLIDE -> {
            PageTransitions.SlideTransition(currentSong, nextSong, progress, contentScale)
        }
        TransitionType.FADE -> {
            PageTransitions.FadeTransition(currentSong, nextSong, progress, contentScale)
        }
        TransitionType.PAGE_FLIP -> {
            PageTransitions.PageFlipTransition(currentSong, nextSong, progress, contentScale)
        }
        TransitionType.CROSSFADE -> {
            // 交叉淡化使用 FADE
            PageTransitions.FadeTransition(currentSong, nextSong, progress, contentScale)
        }
        TransitionType.ZOOM_SLIDE -> {
            // 缩放滑动
            val scale = 1f - (0.3f * progress)
            nextSong?.let { song ->
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = progress
                            translationX = size.width * progress * 0.5f
                        },
                    contentScale = contentScale
                )
            }
        }
    }
}
