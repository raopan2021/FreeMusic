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
    particleEffect: String,
    onParticleEffectChange: (String) -> Unit,
    coverStyle: String,
    onCoverStyleChange: (String) -> Unit,
    visualizerStyle: String,
    onVisualizerStyleChange: (String) -> Unit,
    equalizerPreset: String,
    onEqualizerPresetChange: (String) -> Unit,
    autoPlayEnabled: Boolean,
    onAutoPlayToggle: (Boolean) -> Unit,
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
    var showVisualizerDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val particleEffects = listOf("无", "星星", "泡泡", "烟花")
    val visualizerStyles = listOf("无", "条形", "圆形", "波形")
    val equalizerPresets = listOf("平坦", "低音增强", "高音增强", "人声", "古典", "摇滚")

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 顶部导航
            item {
                TopAppBar(
                    title = { Text("设置") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
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
                    
                    // 封面样式
                    SettingsItem(
                        icon = Icons.Default.Image,
                        title = "封面样式",
                        subtitle = coverStyle,
                        onClick = { /* TODO */ }
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
