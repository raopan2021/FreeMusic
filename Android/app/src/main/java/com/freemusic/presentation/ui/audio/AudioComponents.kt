package com.freemusic.presentation.ui.audio

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 均衡器预设
 */
data class EqualizerPreset(
    val name: String,
    val bands: List<Float>, // 频段增益值，通常是5或10个频段
    val icon: String = "🎵"
)

val EqualizerPresets = listOf(
    EqualizerPreset("普通", listOf(0f, 0f, 0f, 0f, 0f), "🎵"),
    EqualizerPreset("流行", listOf(-1f, 2f, 4f, 2f, -1f), "🎤"),
    EqualizerPreset("摇滚", listOf(4f, 2f, -1f, 2f, 4f), "🎸"),
    EqualizerPreset("爵士", listOf(3f, 1f, -2f, 1f, 3f), "🎷"),
    EqualizerPreset("古典", listOf(4f, 2f, -1f, 2f, 4f), "🎻"),
    EqualizerPreset("电子", listOf(4f, 1f, -2f, 1f, 4f), "🎹"),
    EqualizerPreset("乡村", listOf(2f, 1f, 0f, 1f, 2f), "🤠"),
    EqualizerPreset("布鲁斯", listOf(3f, 1f, -1f, 2f, 3f), "🎺"),
    EqualizerPreset("低沉", listOf(4f, 2f, 0f, -2f, -4f), "🔈"),
    EqualizerPreset("清晰", listOf(-2f, 0f, 2f, 4f, 2f), "🔊")
)

/**
 * 均衡器主界面
 */
@Composable
fun EqualizerScreen(
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    currentPreset: EqualizerPreset,
    onPresetSelected: (EqualizerPreset) -> Unit,
    customBands: List<Float>,
    onBandsChanged: (List<Float>) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 开关
        EqualizerSwitch(
            isEnabled = isEnabled,
            onEnabledChange = onEnabledChange,
            primaryColor = primaryColor
        )
        
        if (isEnabled) {
            // 预设选择
            EqualizerPresetSelector(
                presets = EqualizerPresets,
                currentPreset = currentPreset,
                onPresetSelected = onPresetSelected,
                primaryColor = primaryColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 频段调节
            EqualizerBands(
                bands = customBands,
                onBandsChanged = onBandsChanged,
                primaryColor = primaryColor
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 重置按钮
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("重置为默认")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EqualizerSwitch(
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    primaryColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = if (isEnabled) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "均衡器",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isEnabled) "已开启" else "已关闭",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = onEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = primaryColor
                )
            )
        }
    }
}

@Composable
private fun EqualizerPresetSelector(
    presets: List<EqualizerPreset>,
    currentPreset: EqualizerPreset,
    onPresetSelected: (EqualizerPreset) -> Unit,
    primaryColor: Color
) {
    Column {
        Text(
            text = "预设",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { preset ->
                PresetChip(
                    preset = preset,
                    isSelected = preset.name == currentPreset.name,
                    onClick = { onPresetSelected(preset) },
                    primaryColor = primaryColor
                )
            }
        }
    }
}

@Composable
private fun PresetChip(
    preset: EqualizerPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceVariant,
        border = if (!isSelected) null else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = preset.icon, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = preset.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 频段调节滑块
 */
@Composable
private fun EqualizerBands(
    bands: List<Float>,
    onBandsChanged: (List<Float>) -> Unit,
    primaryColor: Color
) {
    val bandLabels = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "自定义调节",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bands.forEachIndexed { index, value ->
                    BandSlider(
                        label = bandLabels.getOrElse(index) { "" },
                        value = value,
                        onValueChange = { newValue ->
                            val newBands = bands.toMutableList()
                            newBands[index] = newValue
                            onBandsChanged(newBands)
                        },
                        primaryColor = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun BandSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    primaryColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${if (value >= 0) "+" else ""}${value.toInt()}dB",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -12f..12f,
            modifier = Modifier
                .height(150.dp)
                .width(36.dp),
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor,
                inactiveTrackColor = primaryColor.copy(alpha = 0.3f)
            )
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

/**
 * 环绕声/音效选项
 */
@Composable
fun SoundEffectsSection(
    enabledEffects: Set<SoundEffect>,
    onEffectToggle: (SoundEffect) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "音效",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SoundEffect.entries.forEach { effect ->
                SoundEffectItem(
                    effect = effect,
                    isEnabled = enabledEffects.contains(effect),
                    onToggle = { onEffectToggle(effect) },
                    primaryColor = primaryColor
                )
            }
        }
    }
}

enum class SoundEffect(
    val displayName: String,
    val description: String,
    val icon: String
) {
    BASS_BOOST("重低音", "增强低频效果", "🔈"),
    VIRTUALIZER("虚拟环绕", "模拟环绕声效果", "🔊"),
    REVERB("混响", "添加房间混响效果", "🏠"),
    LOUDNESS("响度增强", "提升整体音量和清晰度", "📢")
}

@Composable
private fun SoundEffectItem(
    effect: SoundEffect,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = effect.icon, style = MaterialTheme.typography.titleLarge)
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = effect.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = effect.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primaryColor
            )
        )
    }
}

/**
 * 音量控制器
 */
@Composable
fun VolumeControl(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMuteToggle) {
                Icon(
                    imageVector = when {
                        isMuted || volume == 0f -> Icons.Default.VolumeOff
                        volume < 0.5f -> Icons.Default.VolumeDown
                        else -> Icons.Default.VolumeUp
                    },
                    contentDescription = "音量",
                    tint = if (isMuted) MaterialTheme.colorScheme.error else primaryColor
                )
            }
            
            Slider(
                value = if (isMuted) 0f else volume,
                onValueChange = onVolumeChange,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = primaryColor,
                    activeTrackColor = primaryColor
                )
            )
            
            Text(
                text = "${((if (isMuted) 0f else volume) * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(40.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

/**
 * 音量增强滑块
 */
@Composable
fun VolumeBoostSlider(
    boost: Float,
    onBoostChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "音量增强",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "+${(boost * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = primaryColor
            )
        }
        
        Slider(
            value = boost,
            onValueChange = onBoostChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = primaryColor,
                activeTrackColor = primaryColor
            )
        )
    }
}

/**
 * 播放速度控制
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSpeedControl(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = primaryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "播放速度",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "${String.format("%.1f", speed)}x",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryColor
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { preset ->
                    FilterChip(
                        selected = speed == preset,
                        onClick = { onSpeedChange(preset) },
                        label = { Text("${preset}x") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
