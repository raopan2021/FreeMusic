package com.freemusic.presentation.ui.player.effects

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.*

/**
 * 视觉效果类型
 */
enum class VisualEffectType {
    OFF,
    SAKURA,      // 樱花飘落
    STARFIELD,   // 星空
    RAIN,        // 雨滴
    FIREFLY,     // 萤火虫
    AURORA,      // 极光
    RAINBOW,     // 彩虹
    BUBBLE,      // 气泡
    SMOKE,       // 烟雾
    FLAME,       // 火焰
    WAVE         // 波浪
}

/**
 * 视觉效果组件
 */
@Composable
fun VisualEffects(
    effectType: VisualEffectType,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF5C6BC0)
) {
    if (effectType == VisualEffectType.OFF || !isPlaying) {
        return
    }
    
    when (effectType) {
        VisualEffectType.SAKURA -> SakuraEffect(isPlaying = true, modifier = modifier, primaryColor = primaryColor)
        VisualEffectType.STARFIELD -> StarfieldEffect(isPlaying = true, modifier = modifier, primaryColor = primaryColor)
        VisualEffectType.RAIN -> RainEffect(isPlaying = true, modifier = modifier, primaryColor = primaryColor)
        VisualEffectType.FIREFLY -> FireflyEffect(isPlaying = true, modifier = modifier, primaryColor = primaryColor)
        VisualEffectType.AURORA -> AuroraEffect(isPlaying = true, modifier = modifier, primaryColor = primaryColor)
        VisualEffectType.RAINBOW -> RainbowEffect(isPlaying = true, modifier = modifier, primaryColor = primaryColor)
        VisualEffectType.BUBBLE -> BubbleEffect(isPlaying = true, modifier = modifier, primaryColor = primaryColor)
        VisualEffectType.SMOKE -> SmokeEffect(isPlaying = true, modifier = modifier, primaryColor = primaryColor)
        VisualEffectType.FLAME -> FlameEffect(isPlaying = true, modifier = modifier, primaryColor = primaryColor)
        VisualEffectType.WAVE -> WaveEffect(isPlaying = true, modifier = modifier, primaryColor = primaryColor)
        else -> {}
    }
}

/**
 * 获取效果的显示名称
 */
fun VisualEffectType.getDisplayName(): String = when (this) {
    VisualEffectType.OFF -> "关闭"
    VisualEffectType.SAKURA -> "樱花飘落"
    VisualEffectType.STARFIELD -> "星空闪烁"
    VisualEffectType.RAIN -> "雨滴"
    VisualEffectType.FIREFLY -> "萤火虫"
    VisualEffectType.AURORA -> "极光"
    VisualEffectType.RAINBOW -> "彩虹"
    VisualEffectType.BUBBLE -> "气泡"
    VisualEffectType.SMOKE -> "烟雾"
    VisualEffectType.FLAME -> "火焰"
    VisualEffectType.WAVE -> "波浪"
}

/**
 * 获取所有可用效果列表
 */
fun getAllVisualEffects(): List<VisualEffectType> = VisualEffectType.entries
