package com.freemusic.presentation.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freemusic.data.preferences.CoverStyleType
import com.freemusic.data.preferences.EqualizerPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    // Theme
    isDarkTheme: Boolean = false,
    onDarkThemeToggle: (Boolean) -> Unit = {},
    isPureBlack: Boolean = false,
    onPureBlackToggle: (Boolean) -> Unit = {},
    // Effects
    particlesEnabled: Boolean = true,
    onParticlesToggle: (Boolean) -> Unit = {},
    particleIntensity: Float = 1f,
    onParticleIntensityChange: (Float) -> Unit = {},
    coverStyle: CoverStyleType = CoverStyleType.ROUND,
    onCoverStyleChange: (CoverStyleType) -> Unit = {},
    visualizerEnabled: Boolean = false,
    onVisualizerToggle: (Boolean) -> Unit = {},
    // Equalizer
    equalizerPreset: Int = 0,
    onEqualizerPresetChange: (Int) -> Unit = {},
    bassBoost: Int = 0,
    onBassBoostChange: (Int) -> Unit = {},
    virtualizer: Int = 0,
    onVirtualizerChange: (Int) -> Unit = {},
    // Playback
    autoPlay: Boolean = true,
    onAutoPlayToggle: (Boolean) -> Unit = {},
    crossFadeEnabled: Boolean = false,
    onCrossFadeToggle: (Boolean) -> Unit = {},
    crossFadeDuration: Int = 3000,
    onCrossFadeDurationChange: (Int) -> Unit = {},
    // Lyrics
    lyricsFontSize: Int = 16,
    onLyricsFontSizeChange: (Int) -> Unit = {},
    // Import
    onImportClick: () -> Unit = {}
) {
    var showCoverStyleDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ============ 外观设置 ============
            SettingsSection(title = "外观")

            // 暗黑模式
            SwitchSettingsItem(
                title = "暗黑模式",
                subtitle = if (isDarkTheme) "已开启" else "已关闭",
                icon = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                checked = isDarkTheme,
                onCheckedChange = onDarkThemeToggle
            )

            // 纯黑模式
            SwitchSettingsItem(
                title = "纯黑模式",
                subtitle = "使用真正的黑色（AMOLED 省电）",
                icon = Icons.Default.Contrast,
                checked = isPureBlack,
                onCheckedChange = onPureBlackToggle
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ============ 视觉效果设置 ============
            SettingsSection(title = "视觉效果")

            // 粒子特效
            SwitchSettingsItem(
                title = "粒子特效",
                subtitle = "歌词触发动态粒子效果",
                icon = Icons.Default.AutoAwesome,
                checked = particlesEnabled,
                onCheckedChange = onParticlesToggle
            )

            // 粒子强度
            if (particlesEnabled) {
                SliderSettingsItem(
                    title = "粒子强度",
                    value = particleIntensity,
                    onValueChange = onParticleIntensityChange,
                    valueRange = 0.1f..2f,
                    icon = Icons.Default.Speed
                )
            }

            // 封面样式
            ClickableSettingsItem(
                title = "封面样式",
                subtitle = when (coverStyle) {
                    CoverStyleType.ROUND -> "圆形"
                    CoverStyleType.SQUARE -> "圆角方形"
                    CoverStyleType.SQUARE_NO_ROUND -> "方形"
                    CoverStyleType.DIAMOND -> "菱形"
                    CoverStyleType.BORDER_ROUND -> "边框圆形"
                    CoverStyleType.HEXAGON -> "六边形"
                    CoverStyleType.PARALLELOGRAM -> "平行四边形"
                },
                icon = Icons.Default.CropPortrait,
                onClick = { showCoverStyleDialog = true }
            )

            // 音频可视化
            SwitchSettingsItem(
                title = "音频可视化",
                subtitle = "专辑封面叠加音频波形",
                icon = Icons.Default.Waves,
                checked = visualizerEnabled,
                onCheckedChange = onVisualizerToggle
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ============ 音效设置 ============
            SettingsSection(title = "音效")

            // 均衡器
            ClickableSettingsItem(
                title = "均衡器",
                subtitle = EqualizerPreset.entries[equalizerPreset.coerceIn(0, EqualizerPreset.entries.size - 1)].displayName,
                icon = Icons.Default.Equalizer,
                onClick = { showEqualizerDialog = true }
            )

            // 低音增强
            SliderSettingsItem(
                title = "低音增强",
                value = bassBoost / 1000f,
                onValueChange = { onBassBoostChange((it * 1000).toInt()) },
                valueRange = 0f..1f,
                icon = Icons.Default.GraphicEq
            )

            // 虚拟环绕
            SliderSettingsItem(
                title = "虚拟环绕",
                value = virtualizer / 1000f,
                onValueChange = { onVirtualizerChange((it * 1000).toInt()) },
                valueRange = 0f..1f,
                icon = Icons.Default.SurroundSound
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ============ 播放设置 ============
            SettingsSection(title = "播放")

            // 自动播放
            SwitchSettingsItem(
                title = "自动播放",
                subtitle = "搜索完成后自动开始播放",
                icon = Icons.Default.PlayCircle,
                checked = autoPlay,
                onCheckedChange = onAutoPlayToggle
            )

            // 交叉淡入淡出
            SwitchSettingsItem(
                title = "交叉淡入淡出",
                subtitle = "歌曲切换时平滑过渡",
                icon = Icons.Default.SwapHoriz,
                checked = crossFadeEnabled,
                onCheckedChange = onCrossFadeToggle
            )

            // 淡入淡出时长
            if (crossFadeEnabled) {
                SliderSettingsItem(
                    title = "淡入淡出时长",
                    value = crossFadeDuration / 1000f,
                    onValueChange = { onCrossFadeDurationChange((it * 1000).toInt()) },
                    valueRange = 0.5f..10f,
                    valueSuffix = "秒",
                    icon = Icons.Default.Timer
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ============ 歌词设置 ============
            SettingsSection(title = "歌词")

            // 歌词字体大小
            SliderSettingsItem(
                title = "歌词字体大小",
                value = lyricsFontSize.toFloat(),
                onValueChange = { onLyricsFontSizeChange(it.toInt()) },
                valueRange = 12f..32f,
                valueSuffix = "sp",
                icon = Icons.Default.FormatSize
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ============ 导入导出 ============
            SettingsSection(title = "导入导出")

            ClickableSettingsItem(
                title = "批量导入",
                subtitle = "从链接或评论导入歌单",
                icon = Icons.Default.ContentPaste,
                onClick = onImportClick
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ============ 关于 ============
            SettingsSection(title = "关于")

            ListItem(
                headlineContent = { Text("FreeMusic") },
                supportingContent = { Text("版本 1.0.0") },
                leadingContent = {
                    Icon(Icons.Default.Info, contentDescription = null)
                }
            )

            ListItem(
                headlineContent = { Text("音乐 API") },
                supportingContent = { Text("网易云音乐 / Meting / LRCLIB") },
                leadingContent = {
                    Icon(Icons.Default.MusicNote, contentDescription = null)
                }
            )

            ListItem(
                headlineContent = { Text("开源许可") },
                supportingContent = { Text("Apache 2.0 / GPL 3.0") },
                leadingContent = {
                    Icon(Icons.Default.Code, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 封面样式选择对话框
    if (showCoverStyleDialog) {
        AlertDialog(
            onDismissRequest = { showCoverStyleDialog = false },
            title = { Text("选择封面样式") },
            text = {
                Column {
                    CoverStyleType.entries.forEach { style ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCoverStyleChange(style)
                                    showCoverStyleDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = coverStyle == style,
                                onClick = {
                                    onCoverStyleChange(style)
                                    showCoverStyleDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (style) {
                                    CoverStyleType.ROUND -> "圆形"
                                    CoverStyleType.SQUARE -> "圆角方形"
                                    CoverStyleType.SQUARE_NO_ROUND -> "方形"
                                    CoverStyleType.DIAMOND -> "菱形"
                                    CoverStyleType.BORDER_ROUND -> "边框圆形"
                                    CoverStyleType.HEXAGON -> "六边形"
                                    CoverStyleType.PARALLELOGRAM -> "平行四边形"
                                }
                            )
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

    // 均衡器选择对话框
    if (showEqualizerDialog) {
        AlertDialog(
            onDismissRequest = { showEqualizerDialog = false },
            title = { Text("均衡器预设") },
            text = {
                Column {
                    EqualizerPreset.entries.forEachIndexed { index, preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onEqualizerPresetChange(index)
                                    showEqualizerDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = equalizerPreset == index,
                                onClick = {
                                    onEqualizerPresetChange(index)
                                    showEqualizerDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = preset.displayName)
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
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SwitchSettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

@Composable
private fun ClickableSettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SliderSettingsItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueSuffix: String = ""
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (valueSuffix.isNotEmpty()) {
                    "%.1f$valueSuffix".format(value)
                } else {
                    "%.0f".format(value)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.padding(start = 40.dp)
        )
    }
}
