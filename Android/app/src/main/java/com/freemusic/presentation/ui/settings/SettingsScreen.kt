package com.freemusic.presentation.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freemusic.presentation.ui.theme.PrimaryIndigo
import com.freemusic.presentation.ui.theme.ThemePreset
import com.freemusic.presentation.ui.theme.ThemePresets

/**
 * 设置屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    pureBlackEnabled: Boolean,
    onPureBlackToggle: (Boolean) -> Unit,
    customPrimaryColor: Int,
    onCustomPrimaryColorChange: (Int) -> Unit,
    themePresetId: String?,
    onThemePresetChange: (String?) -> Unit,
    particleEffect: String,
    onParticleEffectChange: (String) -> Unit,
    coverStyle: String,
    onCoverStyleChange: (String) -> Unit,
    coverSwitchInterval: Int,
    onCoverSwitchIntervalChange: (Int) -> Unit,
    visualizerStyle: String,
    onVisualizerStyleChange: (String) -> Unit,
    equalizerPreset: String,
    onEqualizerPresetChange: (String) -> Unit,
    autoPlayEnabled: Boolean,
    onAutoPlayToggle: (Boolean) -> Unit,
    playbackSpeed: Float,
    onPlaybackSpeedChange: (Float) -> Unit,
    lyricsFontSize: Int = 16,
    onLyricsFontSizeChange: (Int) -> Unit = {},
    skipSilenceEnabled: Boolean,
    onSkipSilenceToggle: (Boolean) -> Unit,
    shakeToSkipEnabled: Boolean = false,
    onShakeToSkipToggle: ((Boolean) -> Unit)? = null,
    autoCleanHistoryEnabled: Boolean = false,
    onAutoCleanHistoryToggle: ((Boolean) -> Unit)? = null,
    highQualityEnabled: Boolean,
    onHighQualityToggle: (Boolean) -> Unit,
    cacheSize: String = "0 MB",
    onClearCache: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    primaryColor: androidx.compose.ui.graphics.Color = PrimaryIndigo
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showThemePresetDialog by remember { mutableStateOf(false) }
    var showParticleDialog by remember { mutableStateOf(false) }
    var showCoverStyleDialog by remember { mutableStateOf(false) }
    var showVisualizerDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showCoverSwitchDialog by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }
    var showPlaybackSpeedDialog by remember { mutableStateOf(false) }
    var showLyricsFontSizeDialog by remember { mutableStateOf(false) }

    val particleEffects = listOf("无", "星星", "泡泡", "烟花")
    val visualizerStyles = listOf("无", "条形", "圆形", "波形")
    val equalizerPresets = listOf("平坦", "低音增强", "高音增强", "人声", "古典", "摇滚")
    val coverSwitchOptions = listOf(0, 1, 3, 5, 10, 15, 30)
    val playbackSpeedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 顶部导航
            item {
                TopAppBar(
                    title = { Text("设置") }
                )
            }

            // 外观设置
            item {
                SettingsSection(title = "外观") {
                    // 主题预设选择
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "主题预设",
                        subtitle = "选择完整配色方案",
                        onClick = { showThemePresetDialog = true }
                    )
                    
                    // 主题颜色（自定义颜色）
                    SettingsItem(
                        icon = Icons.Default.ColorLens,
                        title = "自定义主题色",
                        subtitle = if (customPrimaryColor == -1) "跟随主题预设" else "自定义颜色",
                        onClick = { showColorPickerDialog = true },
                        trailing = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = if (customPrimaryColor == -1)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            Color(customPrimaryColor),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    )
                    
                    // 深色模式开关
                    SettingsSwitchItem(
                        icon = Icons.Default.DarkMode,
                        title = "深色模式",
                        subtitle = "启用深色主题",
                        checked = currentTheme == "深色",
                        onCheckedChange = { onThemeChange(if (it) "深色" else "浅色") }
                    )

                    // 封面样式
                    SettingsItem(
                        icon = Icons.Default.Image,
                        title = "封面样式",
                        subtitle = coverStyleToDisplayName(coverStyle),
                        onClick = { showCoverStyleDialog = true }
                    )

                    // 封面自动切换
                    SettingsItem(
                        icon = Icons.Default.Timer,
                        title = "封面自动切换",
                        subtitle = if (coverSwitchInterval == 0) "关闭" else "${coverSwitchInterval}秒",
                        onClick = { showCoverSwitchDialog = true }
                    )

                    // 歌词字体大小
                    SettingsItem(
                        icon = Icons.Default.TextFields,
                        title = "歌词字体大小",
                        subtitle = "${lyricsFontSize}sp",
                        onClick = { showLyricsFontSizeDialog = true }
                    )
                }
            }

            // 视觉效果
            item {
                SettingsSection(title = "视觉效果") {
                    SettingsItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "粒子效果",
                        subtitle = particleEffect,
                        onClick = { showParticleDialog = true }
                    )

                    SettingsItem(
                        icon = Icons.Default.Equalizer,
                        title = "可视化器",
                        subtitle = visualizerStyle,
                        onClick = { showVisualizerDialog = true }
                    )

                    SettingsItem(
                        icon = Icons.Default.Tune,
                        title = "均衡器",
                        subtitle = equalizerPreset,
                        onClick = { showEqualizerDialog = true }
                    )
                }
            }

            // 播放设置
            item {
                SettingsSection(title = "播放设置") {
                    SettingsSwitchItem(
                        icon = Icons.Default.PlayCircle,
                        title = "自动播放",
                        subtitle = "启动时自动播放",
                        checked = autoPlayEnabled,
                        onCheckedChange = onAutoPlayToggle
                    )

                    SettingsItem(
                        icon = Icons.Default.Speed,
                        title = "播放速度",
                        subtitle = "${playbackSpeed}x",
                        onClick = { showPlaybackSpeedDialog = true }
                    )

                    SettingsSwitchItem(
                        icon = Icons.Default.SkipNext,
                        title = "跳过静音",
                        subtitle = "自动跳过音频中的静音片段",
                        checked = skipSilenceEnabled,
                        onCheckedChange = onSkipSilenceToggle
                    )

                    if (onShakeToSkipToggle != null) {
                        SettingsSwitchItem(
                            icon = Icons.Default.Vibration,
                            title = "摇一摇切歌",
                            subtitle = "摇晃手机切换到下一首",
                            checked = shakeToSkipEnabled,
                            onCheckedChange = { onShakeToSkipToggle.invoke(it) }
                        )
                    }

                    if (onAutoCleanHistoryToggle != null) {
                        SettingsSwitchItem(
                            icon = Icons.Default.AutoDelete,
                            title = "自动清理历史",
                            subtitle = "播放记录超过100条时自动清理",
                            checked = autoCleanHistoryEnabled,
                            onCheckedChange = { onAutoCleanHistoryToggle.invoke(it) }
                        )
                    }

                    SettingsSwitchItem(
                        icon = Icons.Default.HighQuality,
                        title = "高质量音频",
                        subtitle = "优先播放SQ/HQ音质",
                        checked = highQualityEnabled,
                        onCheckedChange = onHighQualityToggle
                    )
                }
            }

            // 存储
            item {
                SettingsSection(title = "存储") {
                    SettingsItem(
                        icon = Icons.Default.DeleteSweep,
                        title = "清理缓存",
                        subtitle = "当前缓存: $cacheSize",
                        onClick = onClearCache
                    )

                    SettingsItem(
                        icon = Icons.Default.Download,
                        title = "导入歌单",
                        subtitle = "从链接或文件导入",
                        onClick = onImportClick
                    )
                }
            }

            // 关于
            item {
                SettingsSection(title = "关于") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "关于 FreeMusic",
                        subtitle = "版本 0.1.0",
                        onClick = { showAboutDialog = true }
                    )

                    SettingsItem(
                        icon = Icons.Default.Code,
                        title = "开源许可",
                        subtitle = "查看项目依赖的许可",
                        onClick = { }
                    )
                }
            }
        }

        // 封面样式对话框
        if (showCoverStyleDialog) {
            AlertDialog(
                onDismissRequest = { showCoverStyleDialog = false },
                title = { Text("选择封面样式") },
                text = {
                    Column {
                        val coverStyleMap = mapOf(
                            "圆形" to "ROUND",
                            "圆角方形" to "SQUARE",
                            "方形" to "SQUARE_NO_ROUND",
                            "菱形" to "DIAMOND",
                            "带边框圆形" to "BORDER_ROUND",
                            "六边形" to "HEXAGON",
                            "平行四边形" to "PARALLELOGRAM"
                        )
                        val displayNames = coverStyleMap.keys.toList()
                        val enumNames = coverStyleMap.values.toList()

                        displayNames.forEachIndexed { index, displayName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCoverStyleChange(enumNames[index])
                                        showCoverStyleDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = coverStyle == enumNames[index],
                                    onClick = {
                                        onCoverStyleChange(enumNames[index])
                                        showCoverStyleDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = displayName)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCoverStyleDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // 封面自动切换对话框
        if (showCoverSwitchDialog) {
            AlertDialog(
                onDismissRequest = { showCoverSwitchDialog = false },
                title = { Text("选择切换间隔") },
                text = {
                    Column {
                        coverSwitchOptions.forEach { seconds ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCoverSwitchIntervalChange(seconds)
                                        showCoverSwitchDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = seconds == coverSwitchInterval,
                                    onClick = {
                                        onCoverSwitchIntervalChange(seconds)
                                        showCoverSwitchDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = if (seconds == 0) "关闭" else "${seconds}秒")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCoverSwitchDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // 主题颜色选择对话框
        if (showColorPickerDialog) {
            ColorPickerDialog(
                currentColor = customPrimaryColor,
                onColorSelected = { color ->
                    onCustomPrimaryColorChange(color)
                    showColorPickerDialog = false
                },
                onDismiss = { showColorPickerDialog = false }
            )
        }
        
        // 主题预设选择对话框
        if (showThemePresetDialog) {
            ThemePresetDialog(
                currentPresetId = themePresetId,
                onPresetSelected = { preset ->
                    onThemePresetChange(preset.id)
                    showThemePresetDialog = false
                },
                onDismiss = { showThemePresetDialog = false }
            )
        }

        // 播放速度对话框 - 使用滑块控制,精确到两位小数
        if (showPlaybackSpeedDialog) {
            var tempSpeed by remember { mutableStateOf(playbackSpeed) }
            AlertDialog(
                onDismissRequest = { showPlaybackSpeedDialog = false },
                title = { Text("播放速度") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%.2fx", tempSpeed),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Slider(
                            value = tempSpeed,
                            onValueChange = { tempSpeed = it },
                            valueRange = 0.5f..2.0f,
                            steps = 29, // 0.05  granularity (1.5 / 0.05 = 30 steps)
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0.5x", style = MaterialTheme.typography.bodySmall)
                            Text("1.0x", style = MaterialTheme.typography.bodySmall)
                            Text("2.0x", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onPlaybackSpeedChange(tempSpeed)
                        showPlaybackSpeedDialog = false
                    }) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPlaybackSpeedDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }


        // 歌词字体大小对话框
        if (showLyricsFontSizeDialog) {
            var sliderValue by remember { mutableFloatStateOf(lyricsFontSize.toFloat()) }
            AlertDialog(
                onDismissRequest = { showLyricsFontSizeDialog = false },
                title = { Text("歌词字体大小") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 实时显示当前值
                        Text(
                            text = "${sliderValue.toInt()}sp",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            valueRange = 12f..32f,
                            steps = 9
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("小", style = MaterialTheme.typography.bodySmall)
                            Text("中", style = MaterialTheme.typography.bodySmall)
                            Text("大", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // 预览 - 使用内边距来直观展示大小
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "歌词预览示例",
                                modifier = Modifier.padding(all = sliderValue.toInt().dp),
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = sliderValue.toInt().sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onLyricsFontSizeChange(sliderValue.toInt())
                        showLyricsFontSizeDialog = false
                    }) {
                        Text("完成")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLyricsFontSizeDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // 粒子效果对话框
        if (showParticleDialog) {
            AlertDialog(
                onDismissRequest = { showParticleDialog = false },
                title = { Text("选择粒子效果") },
                text = {
                    Column {
                        particleEffects.forEach { effect ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onParticleEffectChange(effect)
                                        showParticleDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = effect == particleEffect,
                                    onClick = {
                                        onParticleEffectChange(effect)
                                        showParticleDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = effect)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showParticleDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // 可视化器对话框
        if (showVisualizerDialog) {
            AlertDialog(
                onDismissRequest = { showVisualizerDialog = false },
                title = { Text("选择可视化器") },
                text = {
                    Column {
                        visualizerStyles.forEach { style ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onVisualizerStyleChange(style)
                                        showVisualizerDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = style == visualizerStyle,
                                    onClick = {
                                        onVisualizerStyleChange(style)
                                        showVisualizerDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = style)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showVisualizerDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // 均衡器对话框
        if (showEqualizerDialog) {
            AlertDialog(
                onDismissRequest = { showEqualizerDialog = false },
                title = { Text("选择均衡器预设") },
                text = {
                    Column {
                        equalizerPresets.forEach { preset ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEqualizerPresetChange(preset)
                                        showEqualizerDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = preset == equalizerPreset,
                                    onClick = {
                                        onEqualizerPresetChange(preset)
                                        showEqualizerDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = preset)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showEqualizerDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // 关于对话框
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FreeMusic")
                    }
                },
                text = {
                    Column {
                        Text("版本: 0.1.0")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("一个优雅的音乐播放器,支持多种音频格式和视觉效果。")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "功能特点:", fontWeight = FontWeight.Bold)
                        Text("• 多种粒子视觉效果")
                        Text("• 歌词同步显示")
                        Text("• 均衡器调节")
                        Text("• 歌单导入")
                        Text("• 深色/纯黑主题")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("确定")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsThemeSelector(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    // display name to theme value
    val themeOptions = listOf(
        "跟随系统" to "默认",
        "浅色模式" to "浅色",
        "深色模式" to "深色"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "深色模式",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            themeOptions.forEach { (displayName, themeValue) ->
                OutlinedButton(
                    onClick = { onThemeChange(themeValue) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (themeValue == currentTheme)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            Color.Transparent
                    ),
                    border = if (themeValue == currentTheme)
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else
                        ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

/**
 * 颜色选择器对话框
 */
@Composable
private fun ColorPickerDialog(
    currentColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // 小米 SU7 颜色主题
    val presetColors = listOf(
        // 海湾蓝
        Color(0xFF36B5E5) to "海湾蓝",
        // 熔岩橙
        Color(0xFFFF6532) to "熔岩橙",
        // 霞光紫
        Color(0xFF9B6EE8) to "霞光紫",
        // 曜石黑
        Color(0xFF1A1A1A) to "曜石黑",
        // 极地白
        Color(0xFFF5F5F5) to "极地白",
        // 钻石灰
        Color(0xFF8E8E93) to "钻石灰",
        // 橄榄绿
        Color(0xFF6B8E23) to "橄榄绿",
        // 烈焰红
        Color(0xFFC41E3A) to "烈焰红"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择主题颜色") },
        text = {
            Column {
                // 默认颜色选项
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onColorSelected(-1) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color(0xFF6750A4),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("默认紫色")
                    if (currentColor == -1) {
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已选择",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    text = "小米 SU7 主题色",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // 颜色网格 (2列)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetColors.chunked(2).forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowColors.forEach { (color, name) ->
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onColorSelected(color.hashCode()) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                color = color,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (currentColor == color.hashCode()) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "已选择",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            // 如果这行不满2个，填充空白
                            if (rowColors.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 主题预设选择对话框
 */
@Composable
private fun ThemePresetDialog(
    currentPresetId: String?,
    onPresetSelected: (ThemePreset) -> Unit,
    onDismiss: () -> Unit
) {
    val presets = ThemePresets.allPresets
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择主题预设") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    val isSelected = preset.id == currentPresetId
                    
                    val backgroundColor = if (preset.isDark) {
                        Color(0xFF1E1E1E)
                    } else {
                        Color(0xFFF5F5F5)
                    }
                    
                    val surfaceColor = if (preset.isDark) {
                        Color(0xFF2D2D2D)
                    } else {
                        Color.White
                    }
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPresetSelected(preset) },
                        shape = RoundedCornerShape(12.dp),
                        color = surfaceColor,
                        tonalElevation = if (isSelected) 4.dp else 2.dp,
                        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 颜色预览
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // 主题色圆点
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = preset.primary,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // 深色模式指示
                                Box(
                                    modifier = Modifier
                                        .size(40.dp, 8.dp)
                                        .background(
                                            color = backgroundColor,
                                            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                                        )
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (preset.isDark) "深色模式" else "浅色模式",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // 颜色条预览
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                listOf(
                                    preset.primary,
                                    preset.secondary,
                                    preset.tertiary
                                ).forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(
                                                color = color,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 将封面样式枚举名称转换为显示名称
 */
private fun coverStyleToDisplayName(style: String): String {
    return when (style) {
        "ROUND" -> "圆形"
        "SQUARE" -> "圆角方形"
        "SQUARE_NO_ROUND" -> "方形"
        "DIAMOND" -> "菱形"
        "BORDER_ROUND" -> "带边框圆形"
        "HEXAGON" -> "六边形"
        "PARALLELOGRAM" -> "平行四边形"
        else -> style
    }
}
