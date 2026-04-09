package com.freemusic.presentation.ui.theme

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * 从专辑封面提取主题颜色
 */
suspend fun extractColorsFromBitmap(bitmap: Bitmap): ThemeColors = withContext(Dispatchers.Default) {
    try {
        val palette = Palette.from(bitmap).generate()
        
        val dominant = palette.dominantSwatch
        val vibrant = palette.vibrantSwatch
        val darkVibrant = palette.darkVibrantSwatch
        val lightVibrant = palette.lightVibrantSwatch
        val muted = palette.mutedSwatch
        val darkMuted = palette.darkMutedSwatch
        val lightMuted = palette.lightMutedSwatch
        
        // 提取主要颜色
        val primaryColor = when {
            vibrant != null -> Color(vibrant.rgb)
            lightVibrant != null -> Color(lightVibrant.rgb)
            dominant != null -> Color(dominant.rgb)
            else -> Color(0xFF6366F1)
        }
        
        val secondaryColor = when {
            darkVibrant != null -> Color(darkVibrant.rgb)
            muted != null -> Color(muted.rgb)
            lightMuted != null -> Color(lightMuted.rgb)
            else -> Color(0xFF8B5CF6)
        }
        
        val backgroundColor = when {
            darkMuted != null -> Color(darkMuted.rgb)
            darkVibrant != null -> Color(darkVibrant.rgb)
            else -> Color(0xFF1E1B4B)
        }
        
        val surfaceColor = when {
            muted != null -> Color(muted.rgb).copy(alpha = 0.7f)
            else -> Color(0xFF312E81).copy(alpha = 0.7f)
        }
        
        val onPrimaryColor = if (vibrant?.bodyTextColor != null) {
            Color(vibrant.bodyTextColor)
        } else if (dominant?.bodyTextColor != null) {
            Color(dominant.bodyTextColor)
        } else {
            Color.White
        }
        
        // 计算对比色
        val isDark = isColorDark(primaryColor)
        val accentColor = if (isDark) {
            primaryColor.copy(alpha = 0.8f)
        } else {
            primaryColor.copy(alpha = 0.6f)
        }
        
        ThemeColors(
            primary = primaryColor,
            secondary = secondaryColor,
            background = backgroundColor,
            surface = surfaceColor,
            onPrimary = onPrimaryColor,
            accent = accentColor,
            isDark = isDark
        )
    } catch (e: Exception) {
        ThemeColors() // 返回默认颜色
    }
}

private fun isColorDark(color: Color): Boolean {
    val darkness = 1 - (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return darkness >= 0.5
}

/**
 * 主题颜色数据类
 */
data class ThemeColors(
    val primary: Color = Color(0xFF6366F1),
    val secondary: Color = Color(0xFF8B5CF6),
    val background: Color = Color(0xFF1E1B4B),
    val surface: Color = Color(0xFF312E81).copy(alpha = 0.7f),
    val onPrimary: Color = Color.White,
    val accent: Color = Color(0xFF6366F1).copy(alpha = 0.8f),
    val isDark: Boolean = true
)

/**
 * 颜色插值工具
 */
object ColorUtils {
    fun lerp(start: Color, end: Color, fraction: Float): Color {
        return Color(
            red = lerp(start.red, end.red, fraction),
            green = lerp(start.green, end.green, fraction),
            blue = lerp(start.blue, end.blue, fraction),
            alpha = lerp(start.alpha, end.alpha, fraction)
        )
    }
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction.coerceIn(0f, 1f)
    }
    
    /**
     * 调整颜色亮度
     */
    fun adjustBrightness(color: Color, factor: Float): Color {
        return Color(
            red = (color.red * factor).coerceIn(0f, 1f),
            green = (color.green * factor).coerceIn(0f, 1f),
            blue = (color.blue * factor).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }
    
    /**
     * 获取互补色
     */
    fun getComplementary(color: Color): Color {
        return Color(
            red = 1f - color.red,
            green = 1f - color.green,
            blue = 1f - color.blue,
            alpha = color.alpha
        )
    }
    
    /**
     * 获取近似互补色（120度旋转）
     */
    fun getTriadic(color: Color): Color {
        return Color(
            red = color.blue,
            green = color.red,
            blue = color.green,
            alpha = color.alpha
        )
    }
    
    /**
     * 创建一个颜色方案
     */
    fun createColorScheme(baseColor: Color): ColorScheme {
        return ColorScheme(
            primary = baseColor,
            primaryVariant = adjustBrightness(baseColor, 0.8f),
            secondary = getTriadic(baseColor),
            secondaryVariant = adjustBrightness(getTriadic(baseColor), 0.9f),
            background = adjustBrightness(baseColor, 0.15f),
            surface = adjustBrightness(baseColor, 0.25f),
            error = Color(0xFFEF4444),
            onPrimary = if (isColorDark(baseColor)) Color.White else Color.Black,
            onSecondary = if (isColorDark(getTriadic(baseColor))) Color.White else Color.Black,
            onBackground = if (isColorDark(adjustBrightness(baseColor, 0.15f))) Color.White else Color.Black,
            onSurface = if (isColorDark(adjustBrightness(baseColor, 0.25f))) Color.White else Color.Black
        )
    }
}

/**
 * 颜色方案
 */
data class ColorScheme(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val background: Color,
    val surface: Color,
    val error: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color
)
