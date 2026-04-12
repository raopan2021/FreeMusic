package com.freemusic.presentation.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 品牌色彩
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// 自定义主题色
val PrimaryIndigo = Color(0xFF6366F1)
val SecondaryViolet = Color(0xFF8B5CF6)
val TertiaryPink = Color(0xFFEC4899)

// 渐变色
val GradientIndigo = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
val GradientSunset = listOf(Color(0xFFFF6B6B), Color(0xFFFFD93D))
val GradientOcean = listOf(Color(0xFF2193B0), Color(0xFF6DD5ED))
val GradientForest = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
val GradientPurple = listOf(Color(0xFF7F00FF), Color(0xFFE100FF))
val GradientFire = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))

// 深色主题色
val DarkBackground = Color(0xFF0F0F0F)
val DarkSurface = Color(0xFF1A1A1A)
val DarkSurfaceVariant = Color(0xFF2D2D2D)
val PureBlack = Color(0xFF000000)

// 亮色主题色  
val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)

// 小米 SU7 颜色主题
object SU7Colors {
    // 海湾蓝
    val OceanBlue = Color(0xFF36B5E5)
    // 熔岩橙
    val LavaOrange = Color(0xFFFF6532)
    // 霞光紫
    val AuroraPurple = Color(0xFF9B6EE8)
    // 曜石黑
    val ObsidianBlack = Color(0xFF1A1A1A)
    // 极地白
    val ArcticWhite = Color(0xFFF5F5F5)
    // 钻石灰
    val DiamondGray = Color(0xFF8E8E93)
    // 橄榄绿
    val OliveGreen = Color(0xFF6B8E23)
    // 烈焰红
    val FlameRed = Color(0xFFC41E3A)
    
    val all = listOf(
        "海湾蓝" to OceanBlue,
        "熔岩橙" to LavaOrange,
        "霞光紫" to AuroraPurple,
        "曜石黑" to ObsidianBlack,
        "极地白" to ArcticWhite,
        "钻石灰" to DiamondGray,
        "橄榄绿" to OliveGreen,
        "烈焰红" to FlameRed
    )
}

/**
 * 动态主题颜色提取器
 */
object DynamicThemeColors {
    
    /**
     * 从专辑封面提取主色调
     */
    fun extractPrimaryColor(coverColors: List<Color>): Color {
        if (coverColors.isEmpty()) return PrimaryIndigo
        return coverColors.firstOrNull() ?: PrimaryIndigo
    }
    
    /**
     * 创建和谐配色方案
     */
    fun createHarmoniousPalette(primary: Color): ThemePalette {
        return ThemePalette(
            primary = primary,
            secondary = primary.copy(alpha = 0.8f),
            tertiary = primary.copy(alpha = 0.9f),
            surface = primary.copy(alpha = 0.1f),
            background = primary.copy(alpha = 0.05f)
        )
    }
    
    /**
     * 获取渐变色方案
     */
    fun getGradientForMood(mood: MusicMood): List<Color> {
        return when (mood) {
            MusicMood.HAPPY -> GradientSunset
            MusicMood.SAD -> GradientOcean
            MusicMood.ENERGETIC -> GradientFire
            MusicMood.CALM -> GradientForest
            MusicMood.ROMANTIC -> GradientPurple
            MusicMood.DEFAULT -> GradientIndigo
        }
    }
}

data class ThemePalette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val surface: Color,
    val background: Color
)

enum class MusicMood {
    HAPPY, SAD, ENERGETIC, CALM, ROMANTIC, DEFAULT
}

private val Typography = androidx.compose.material3.Typography()

/**
 * 应用主题透明度
 */
fun Color.withAlpha(alpha: Float): Color = copy(alpha = alpha)

/**
 * 暗色主题
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    secondary = SecondaryViolet,
    tertiary = TertiaryPink,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

/**
 * 纯黑主题
 */
private val PureBlackColorScheme = darkColorScheme(
    primary = PrimaryIndigo,
    secondary = SecondaryViolet,
    tertiary = TertiaryPink,
    background = PureBlack,
    surface = PureBlack,
    surfaceVariant = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

/**
 * 亮色主题
 */
private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    secondary = SecondaryViolet,
    tertiary = TertiaryPink,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

/**
 * FreeMusic 应用主题
 */
@Composable
fun FreeMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme && pureBlack -> PureBlackColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        remember {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
