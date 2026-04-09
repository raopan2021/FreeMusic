package com.freemusic.presentation.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 桌面Widget数据
 */
data class WidgetData(
    val songTitle: String,
    val artistName: String,
    val coverUrl: String?,
    val isPlaying: Boolean,
    val progress: Float,
    val primaryColor: Int
)

/**
 * Widget Action Types
 */
object WidgetAction {
    const val ACTION_PLAY_PAUSE = "com.freemusic.ACTION_PLAY_PAUSE"
    const val ACTION_NEXT = "com.freemusic.ACTION_NEXT"
    const val ACTION_PREVIOUS = "com.freemusic.ACTION_PREVIOUS"
    const val ACTION_OPEN_APP = "com.freemusic.ACTION_OPEN_APP"
}

/**
 * Widget 主题
 */
enum class WidgetTheme(val displayName: String) {
    LIGHT("浅色"),
    DARK("深色"),
    TRANSPARENT("透明"),
    ADAPTIVE("自适应")
}

/**
 * Widget 大小选项
 */
enum class WidgetSize(val displayName: String, val minWidth: Int, val minHeight: Int) {
    MINI("迷你", 2, 1),
    SMALL("小号", 2, 2),
    MEDIUM("中号", 4, 2),
    LARGE("大号", 4, 4)
}

/**
 * Widget 配置界面
 */
@Composable
fun WidgetConfigurationScreen(
    primaryColor: Color = PrimaryIndigo,
    onBackClick: () -> Unit
) {
    // Widget configuration UI placeholder
}

/**
 * Widget 主题选择器
 */
@Composable
fun WidgetThemeSelector(
    selectedTheme: WidgetTheme,
    onThemeSelected: (WidgetTheme) -> Unit,
    primaryColor: Color = PrimaryIndigo
) {
    // Widget theme selection UI placeholder
}

/**
 * Widget预览
 */
@Composable
fun WidgetPreview(
    size: WidgetSize,
    theme: WidgetTheme,
    data: WidgetData,
    primaryColor: Color = PrimaryIndigo
) {
    // Widget preview UI placeholder
}

/**
 * Widget Provider 状态
 */
data class WidgetState(
    val isEnabled: Boolean = false,
    val theme: WidgetTheme = WidgetTheme.LIGHT,
    val size: WidgetSize = WidgetSize.MEDIUM,
    val showCover: Boolean = true,
    val showProgress: Boolean = true,
    val updateInterval: Int = 30 // seconds
)

/**
 * Widget 更新回调
 */
interface WidgetUpdateCallback {
    fun onWidgetUpdate(data: WidgetData)
    fun onWidgetAction(action: String)
}
