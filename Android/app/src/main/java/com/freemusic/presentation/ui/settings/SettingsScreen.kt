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
                    // 主题选择（3个Radio Button）
                    SettingsThemeSelector(
                        currentTheme = currentTheme,
                        onThemeChange = { theme ->
                            onThemeChange(theme)
                            // 根据主题设置纯黑模式
                            onPureBlackToggle(theme == "纯黑")
                        }
                    )
                    
                    // 主题颜色
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "主题颜色",
                        subtitle = if (customPrimaryColor == -1) "默认紫色" else "自定义",
                        onClick = { showColorPickerDialog = true },
                        trailing = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = if (customPrimaryColor == -1) 
                                            Color(0xFF6750A4) 
                                        else 
                                            Color(customPrimaryColor),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
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
                    
                    SettingsItem(
                        icon = Icons.Default.TextFields,
                        title = "歌词字体大小",
                        subtitle = "${lyricsFontSize}sp",
                        onClick = { showLyricsFontSizeDialog = true }
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

        // 播放速度对话框 - 使用滑块控制，精确到两位小数
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
                        // 预览
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "歌词预览示例",
                                modifier = Modifier.padding(sliderValue.toInt().dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = sliderValue.toInt().sp
                                )
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
                        Text("一个优雅的音乐播放器，支持多种音频格式和视觉效果。")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "功能特点：", fontWeight = FontWeight.Bold)
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
    val themes = listOf("默认", "暗色", "纯黑")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "主题",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            themes.forEach { theme ->
                OutlinedButton(
                    onClick = { onThemeChange(theme) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (theme == currentTheme) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                        else 
                            Color.Transparent
                    ),
                    border = if (theme == currentTheme) 
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else
                        ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(
                        text = theme,
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
    // 预设颜色列表
    val presetColors = listOf(
        Color(0xFF6750A4), // 紫色（默认）
        Color(0xFF2196F3), // 蓝色
        Color(0xFF4CAF50), // 绿色
        Color(0xFFFF9800), // 橙色
        Color(0xFFFF5722), // 深橙
        Color(0xFFF44336), // 红色
        Color(0xFFE91E63), // 粉色
        Color(0xFF9C27B0), // 深紫
        Color(0xFF3F51B5), // 靼蓝
        Color(0xFF009688), // 青色
        Color(0xFF8BC34A), // 浅绿
        Color(0xFFFFEB3B), // 黄色
        Color(0xFF795548), // 棕色
        Color(0xFF607D8B), // 蓝灰
        Color(0xFF000000), // 黑色
        Color(0xFFFFFFFF), // 白色
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
                    Text("默认颜色")
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
                    text = "预设颜色",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // 颜色网格
                Column {
                    presetColors.chunked(4).forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowColors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp)
                                        .background(
                                            color = color,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onColorSelected(color.hashCode()) }
                                ) {
                                    if (currentColor == color.hashCode()) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "已选择",
                                            tint = if (color == Color.White || color == Color(0xFFFFFF00)) 
                                                Color.Black else Color.White,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                            // 如果这行不满4个，填充空白
                            repeat(4 - rowColors.size) {
                                Spacer(modifier = Modifier.size(56.dp))
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
