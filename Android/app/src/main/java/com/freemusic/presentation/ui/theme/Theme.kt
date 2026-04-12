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

/**
 * 完整的主题预设（包含深色和浅色两套配色）
 */
data class ThemeColorScheme(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color
)

data class ThemePreset(
    val id: String,
    val displayName: String,
    val lightColors: ThemeColorScheme,
    val darkColors: ThemeColorScheme
)

/**
 * 预设主题列表 - 小米SU7全系外观颜色
 */
object ThemePresets {
    // 默认紫色主题
    val Default = ThemePreset(
        id = "default",
        displayName = "默认紫",
        lightColors = ThemeColorScheme(
            primary = Color(0xFF6750A4),
            secondary = Color(0xFF625B71),
            tertiary = Color(0xFF7D5260),
            background = Color(0xFFF8F9FA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE7E0EC),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            onSurfaceVariant = Color(0xFF49454F)
        ),
        darkColors = ThemeColorScheme(
            primary = Color(0xFFD0BCFF),
            secondary = Color(0xFFCCC2DC),
            tertiary = Color(0xFFEFB8C8),
            background = Color(0xFF1C1B1F),
            surface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFF49454F),
            onPrimary = Color(0xFF381E72),
            onSecondary = Color(0xFF332D41),
            onBackground = Color(0xFFE6E1E5),
            onSurface = Color(0xFFE6E1E5),
            onSurfaceVariant = Color(0xFFCAC4D0)
        )
    )
    
    // 卡布里蓝
    val CabriBlue = ThemePreset(
        id = "cabri_blue",
        displayName = "卡布里蓝",
        lightColors = ThemeColorScheme(
            primary = Color(0xFF4A9EE5),
            secondary = Color(0xFF3A8ED5),
            tertiary = Color(0xFF6ABEF5),
            background = Color(0xFFF5F9FC),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE3F2FD),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF0D2A3D),
            onSurface = Color(0xFF0D2A3D),
            onSurfaceVariant = Color(0xFF1976D2)
        ),
        darkColors = ThemeColorScheme(
            primary = Color(0xFF4A9EE5),
            secondary = Color(0xFF6ABEF5),
            tertiary = Color(0xFF3A8ED5),
            background = Color(0xFF0D2A3D),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF1E3A4D),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFFE3F2FD),
            onSurface = Color(0xFFE3F2FD),
            onSurfaceVariant = Color(0xFF90CAF9)
        )
    )
    
    // 赤霞红
    val ChiXiaRed = ThemePreset(
        id = "chi_xia_red",
        displayName = "赤霞红",
        lightColors = ThemeColorScheme(
            primary = Color(0xFFC72C41),
            secondary = Color(0xFFB71C31),
            tertiary = Color(0xFFE73C51),
            background = Color(0xFFFFF5F5),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFFFEBEE),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF4A0A12),
            onSurface = Color(0xFF4A0A12),
            onSurfaceVariant = Color(0xFFD32F2F)
        ),
        darkColors = ThemeColorScheme(
            primary = Color(0xFFEF5350),
            secondary = Color(0xFFE73C51),
            tertiary = Color(0xFFB71C31),
            background = Color(0xFF1A0A0A),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2D1515),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFFFFEBEE),
            onSurface = Color(0xFFFFEBEE),
            onSurfaceVariant = Color(0xFFEF9A9A)
        )
    )
    
    // 靛石绿
    val DianStoneGreen = ThemePreset(
        id = "dian_stone_green",
        displayName = "靛石绿",
        lightColors = ThemeColorScheme(
            primary = Color(0xFF2A4F4D),
            secondary = Color(0xFF1A3F3D),
            tertiary = Color(0xFF3A6F6D),
            background = Color(0xFFF5F9F8),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE8F5F3),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF0A1A19),
            onSurface = Color(0xFF0A1A19),
            onSurfaceVariant = Color(0xFF1B8F7D)
        ),
        darkColors = ThemeColorScheme(
            primary = Color(0xFF4DB6AC),
            secondary = Color(0xFF3A6F6D),
            tertiary = Color(0xFF26A69A),
            background = Color(0xFF0A1A19),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF1A2A28),
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color(0xFFE8F5F3),
            onSurface = Color(0xFFE8F5F3),
            onSurfaceVariant = Color(0xFF80CBC4)
        )
    )
    
    // 雅灰
    val YaGray = ThemePreset(
        id = "ya_gray",
        displayName = "雅灰",
        lightColors = ThemeColorScheme(
            primary = Color(0xFF6B6B6B),
            secondary = Color(0xFF5A5A5A),
            tertiary = Color(0xFF8A8A8A),
            background = Color(0xFFF5F5F5),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEEEEEE),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
            onSurfaceVariant = Color(0xFF757575)
        ),
        darkColors = ThemeColorScheme(
            primary = Color(0xFF9E9E9E),
            secondary = Color(0xFFB0B0B0),
            tertiary = Color(0xFF757575),
            background = Color(0xFF1A1A1A),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2D2D2D),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color(0xFFE0E0E0),
            onSurface = Color(0xFFE0E0E0),
            onSurfaceVariant = Color(0xFFBDBDBD)
        )
    )
    
    // 流金粉
    val LiuJinPink = ThemePreset(
        id = "liu_jin_pink",
        displayName = "流金粉",
        lightColors = ThemeColorScheme(
            primary = Color(0xFFD4A0A0),
            secondary = Color(0xFFC49090),
            tertiary = Color(0xFFE4B0B0),
            background = Color(0xFFFCF8F8),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFFFF0F0),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF2A1A1A),
            onSurface = Color(0xFF2A1A1A),
            onSurfaceVariant = Color(0xFFB08080)
        ),
        darkColors = ThemeColorScheme(
            primary = Color(0xFFE6B8B8),
            secondary = Color(0xFFF6C8C8),
            tertiary = Color(0xFFD4A0A0),
            background = Color(0xFF1A1212),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2D1F1F),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color(0xFFFFF0F0),
            onSurface = Color(0xFFFFF0F0),
            onSurfaceVariant = Color(0xFFD4A0A0)
        )
    )
    
    // 霞光紫
    val XiaGuangPurple = ThemePreset(
        id = "xia_guang_purple",
        displayName = "霞光紫",
        lightColors = ThemeColorScheme(
            primary = Color(0xFF996699),
            secondary = Color(0xFF895689),
            tertiary = Color(0xFFA976A9),
            background = Color(0xFFFAF8FC),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF3EEF3),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF2A1A2A),
            onSurface = Color(0xFF2A1A2A),
            onSurfaceVariant = Color(0xFF7E57C2)
        ),
        darkColors = ThemeColorScheme(
            primary = Color(0xFFCE93D8),
            secondary = Color(0xFFBA68C8),
            tertiary = Color(0xFFAB47BC),
            background = Color(0xFF1A121A),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2D1F2D),
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color(0xFFF3EEF3),
            onSurface = Color(0xFFF3EEF3),
            onSurfaceVariant = Color(0xFFCE93D8)
        )
    )
    
    // 璀璨洋红
    val CuiCanMagenta = ThemePreset(
        id = "cui_can_magenta",
        displayName = "璀璨洋红",
        lightColors = ThemeColorScheme(
            primary = Color(0xFFD92F7C),
            secondary = Color(0xFFC91F6C),
            tertiary = Color(0xFFE93F8C),
            background = Color(0xFFFFF5FA),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFFFF0F5),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF4A0A2A),
            onSurface = Color(0xFF4A0A2A),
            onSurfaceVariant = Color(0xFFE91E63)
        ),
        darkColors = ThemeColorScheme(
            primary = Color(0xFFF06292),
            secondary = Color(0xFFE93F8C),
            tertiary = Color(0xFFEC407A),
            background = Color(0xFF1A0A12),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2D1520),
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color(0xFFFFF0F5),
            onSurface = Color(0xFFFFF0F5),
            onSurfaceVariant = Color(0xFFF48FB1)
        )
    )
    
    // 曜石黑
    val YaoShiBlack = ThemePreset(
        id = "yao_shi_black",
        displayName = "曜石黑",
        lightColors = ThemeColorScheme(
            primary = Color(0xFF4A4A4A),
            secondary = Color(0xFF3A3A3A),
            tertiary = Color(0xFF5A5A5A),
            background = Color(0xFFF5F5F5),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEEEEEE),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF1A1A1A),
            onSurface = Color(0xFF1A1A1A),
            onSurfaceVariant = Color(0xFF757575)
        ),
        darkColors = ThemeColorScheme(
            primary = Color(0xFF4A4A4A),
            secondary = Color(0xFF5A5A5A),
            tertiary = Color(0xFF3A3A3A),
            background = Color(0xFF1A1A1A),
            surface = Color(0xFF1E1E1E),
            surfaceVariant = Color(0xFF2D2D2D),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFFE0E0E0),
            onSurface = Color(0xFFE0E0E0),
            onSurfaceVariant = Color(0xFFB0BEC5)
        )
    )
    
    // 获取所有预设主题
    val allPresets = listOf(
        Default, CabriBlue, ChiXiaRed, DianStoneGreen, 
        YaGray, LiuJinPink, XiaGuangPurple, CuiCanMagenta, YaoShiBlack
    )
    
    // 根据ID获取预设
    fun getPresetById(id: String): ThemePreset? = allPresets.find { it.id == id }
}

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
    themePreset: ThemePreset? = null, // 完整主题预设
    content: @Composable () -> Unit
) {
    // 如果有预设主题，根据 darkTheme 参数选择配色
    if (themePreset != null) {
        val colors = if (darkTheme) themePreset.darkColors else themePreset.lightColors
        
        val colorScheme = if (darkTheme) {
            darkColorScheme(
                primary = colors.primary,
                secondary = colors.secondary,
                tertiary = colors.tertiary,
                background = colors.background,
                surface = colors.surface,
                surfaceVariant = colors.surfaceVariant,
                onPrimary = colors.onPrimary,
                onSecondary = colors.onSecondary,
                onBackground = colors.onBackground,
                onSurface = colors.onSurface,
                onSurfaceVariant = colors.onSurfaceVariant
            )
        } else {
            lightColorScheme(
                primary = colors.primary,
                secondary = colors.secondary,
                tertiary = colors.tertiary,
                background = colors.background,
                surface = colors.surface,
                surfaceVariant = colors.surfaceVariant,
                onPrimary = colors.onPrimary,
                onSecondary = colors.onSecondary,
                onBackground = colors.onBackground,
                onSurface = colors.onSurface,
                onSurfaceVariant = colors.onSurfaceVariant
            )
        }
        
        val view = LocalView.current
        if (!view.isInEditMode) {
            LaunchedEffect(colorScheme.background) {
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
        return
    }
    
    // 否则使用默认/动态/纯色配色
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
