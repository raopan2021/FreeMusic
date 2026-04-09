package com.freemusic.presentation.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 亮色主题配色
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

// 暗色主题配色
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

/**
 * 主题状态管理器
 */
data class ThemeState(
    val isDarkTheme: Boolean = false,
    val isPureBlack: Boolean = false // 真正的黑色（AMOLED）
)

private val LocalThemeState = staticCompositionLocalOf { mutableStateOf(ThemeState()) }

/**
 * 丝滑主题切换的 Theme
 */
@Composable
fun FreeMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    content: @Composable () -> Unit
) {
    val themeState = remember { mutableStateOf(ThemeState(isDarkTheme = darkTheme, isPureBlack = pureBlack)) }
    
    // 监听外部主题变化
    LaunchedEffect(darkTheme, pureBlack) {
        themeState.value = ThemeState(isDarkTheme = darkTheme, isPureBlack = pureBlack)
    }

    // 丝滑的颜色过渡动画
    val animatedPrimary = animateColorAsState(
        targetValue = if (themeState.value.isDarkTheme) DarkColorScheme.primary else LightColorScheme.primary,
        animationSpec = tween(durationMillis = 300),
        label = "primary"
    )
    val animatedOnPrimary = animateColorAsState(
        targetValue = if (themeState.value.isDarkTheme) DarkColorScheme.onPrimary else LightColorScheme.onPrimary,
        animationSpec = tween(durationMillis = 300),
        label = "onPrimary"
    )
    val animatedPrimaryContainer = animateColorAsState(
        targetValue = if (themeState.value.isDarkTheme) DarkColorScheme.primaryContainer else LightColorScheme.primaryContainer,
        animationSpec = tween(durationMillis = 300),
        label = "primaryContainer"
    )
    val animatedBackground = animateColorAsState(
        targetValue = when {
            themeState.value.isDarkTheme && themeState.value.isPureBlack -> Color.Black
            themeState.value.isDarkTheme -> DarkColorScheme.background
            else -> LightColorScheme.background
        },
        animationSpec = tween(durationMillis = 300),
        label = "background"
    )
    val animatedSurface = animateColorAsState(
        targetValue = when {
            themeState.value.isDarkTheme && themeState.value.isPureBlack -> Color.Black
            themeState.value.isDarkTheme -> DarkColorScheme.surface
            else -> LightColorScheme.surface
        },
        animationSpec = tween(durationMillis = 300),
        label = "surface"
    )

    val colorScheme = when {
        themeState.value.isDarkTheme -> DarkColorScheme.copy(
            primary = animatedPrimary.value,
            onPrimary = animatedOnPrimary.value,
            primaryContainer = animatedPrimaryContainer.value,
            background = animatedBackground.value,
            surface = animatedSurface.value
        )
        else -> LightColorScheme.copy(
            primary = animatedPrimary.value,
            onPrimary = animatedOnPrimary.value,
            primaryContainer = animatedPrimaryContainer.value,
            background = animatedBackground.value,
            surface = animatedSurface.value
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        LaunchedEffect(colorScheme.primary) {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !themeState.value.isDarkTheme
        }
    }

    CompositionLocalProvider(
        LocalThemeState provides themeState
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * 获取当前主题状态
 */
@Composable
fun rememberThemeState(): ThemeState {
    return LocalThemeState.current.value
}

/**
 * 切换主题
 */
@Composable
fun toggleDarkTheme() {
    val themeState = LocalThemeState.current
    themeState.value = themeState.value.copy(isDarkTheme = !themeState.value.isDarkTheme)
}

/**
 * 切换纯黑模式
 */
@Composable
fun togglePureBlack() {
    val themeState = LocalThemeState.current
    themeState.value = themeState.value.copy(isPureBlack = !themeState.value.isPureBlack)
}
