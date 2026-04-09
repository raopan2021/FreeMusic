package com.freemusic.presentation.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 预设主题色
 */
data class ThemeColor(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val icon: ImageVector
)

val PresetThemes = listOf(
    ThemeColor(
        name = "靛蓝",
        primary = Color(0xFF3F51B5),
        secondary = Color(0xFF5C6BC0),
        tertiary = Color(0xFF7986CB),
        icon = Icons.Default.BlurOn
    ),
    ThemeColor(
        name = "深紫",
        primary = Color(0xFF673AB7),
        secondary = Color(0xFF7E57C2),
        tertiary = Color(0xFF9575CD),
        icon = Icons.Default.Palette
    ),
    ThemeColor(
        name = "蓝绿",
        primary = Color(0xFF009688),
        secondary = Color(0xFF26A69A),
        tertiary = Color(0xFF4DB6AC),
        icon = Icons.Default.WaterDrop
    ),
    ThemeColor(
        name = "绿色",
        primary = Color(0xFF4CAF50),
        secondary = Color(0xFF66BB6A),
        tertiary = Color(0xFF81C784),
        icon = Icons.Default.Eco
    ),
    ThemeColor(
        name = "橙色",
        primary = Color(0xFFFF9800),
        secondary = Color(0xFFFFA726),
        tertiary = Color(0xFFFFB74D),
        icon = Icons.Default.WbSunny
    ),
    ThemeColor(
        name = "红色",
        primary = Color(0xFFF44336),
        secondary = Color(0xFFEF5350),
        tertiary = Color(0xFFE57373),
        icon = Icons.Default.Fireplace
    ),
    ThemeColor(
        name = "粉色",
        primary = Color(0xFFE91E63),
        secondary = Color(0xFFEC407A),
        tertiary = Color(0xFFF06292),
        icon = Icons.Default.Favorite
    ),
    ThemeColor(
        name = "青色",
        primary = Color(0xFF00BCD4),
        secondary = Color(0xFF26C6DA),
        tertiary = Color(0xFF4DD0E1),
        icon = Icons.Default.Air
    )
)

/**
 * 主题选择器
 */
@Composable
fun ThemeColorPicker(
    selectedTheme: ThemeColor,
    onThemeSelected: (ThemeColor) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "选择主题色",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(PresetThemes) { theme ->
                ThemeColorItem(
                    theme = theme,
                    isSelected = theme.name == selectedTheme.name,
                    onClick = { onThemeSelected(theme) }
                )
            }
        }
    }
}

@Composable
private fun ThemeColorItem(
    theme: ThemeColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(theme.primary, theme.tertiary)
                    )
                )
                .then(
                    if (isSelected) {
                        Modifier.background(
                            color = Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                    } else Modifier
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = theme.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) theme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * 自定义颜色选择器
 */
@Composable
fun CustomColorPicker(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "自定义颜色",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 当前颜色预览
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(currentColor)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            
            // 预设颜色网格
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val customColors = listOf(
                    Color(0xFFFF5252), Color(0xFFFF4081), Color(0xFFE040FB),
                    Color(0xFF7C4DFF), Color(0xFF536DFE), Color(0xFF448AFF),
                    Color(0xFF40C4FF), Color(0xFF18FFFF), Color(0xFF64FFDA),
                    Color(0xFF69F0AE), Color(0xFFB2FF59), Color(0xFFFFFF00),
                    Color(0xFFFFD740), Color(0xFFFFAB40), Color(0xFFFF6E40),
                    Color(0xFF8D6E63)
                )
                
                items(customColors) { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorSelected(color) }
                            .then(
                                if (color == currentColor) {
                                    Modifier.border(2.dp, Color.White, CircleShape)
                                } else Modifier
                            )
                    )
                }
            }
        }
    }
}

/**
 * 颜色滑块
 */
@Composable
fun ColorSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    colorProvider: (Float) -> Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${(value * 255).toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = colorProvider(value),
                activeTrackColor = colorProvider(value)
            )
        )
    }
}

/**
 * RGB颜色编辑器
 */
@Composable
fun RGBColorEditor(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val red = remember { mutableFloatStateOf(color.red) }
    val green = remember { mutableFloatStateOf(color.green) }
    val blue = remember { mutableFloatStateOf(color.blue) }
    
    LaunchedEffect(red.value, green.value, blue.value) {
        onColorChange(Color(red.value, green.value, blue.value))
    }
    
    Column(modifier = modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(red.value, green.value, blue.value))
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ColorSlider(
            label = "红",
            value = red.value,
            onValueChange = { red.value = it },
            colorProvider = { Color(it, 0f, 0f) }
        )
        
        ColorSlider(
            label = "绿",
            value = green.value,
            onValueChange = { green.value = it },
            colorProvider = { Color(0f, it, 0f) }
        )
        
        ColorSlider(
            label = "蓝",
            value = blue.value,
            onValueChange = { blue.value = it },
            colorProvider = { Color(0f, 0f, it) }
        )
    }
}

/**
 * 主题预览卡片
 */
@Composable
fun ThemePreviewCard(
    themeColor: ThemeColor,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
    val surfaceColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color.White
    
    Card(
        modifier = modifier.width(160.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // 顶部主题色条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(themeColor.primary)
            )
            
            Column(modifier = Modifier.padding(12.dp)) {
                // 预览元素
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(themeColor.primary)
                        .align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = themeColor.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor.primary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("预览", style = MaterialTheme.typography.labelSmall)
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("应用", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * 深色/浅色主题切换
 */
@Composable
fun ThemeModeToggle(
    isDarkMode: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LightMode,
            contentDescription = null,
            tint = if (!isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        Switch(
            checked = isDarkMode,
            onCheckedChange = onToggle,
            thumbContent = {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
        
        Icon(
            imageVector = Icons.Default.DarkMode,
            contentDescription = null,
            tint = if (isDarkMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

/**
 * 主题效果预览
 */
@Composable
fun ThemeEffectPreview(
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "效果预览",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 按钮预览
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("主要按钮")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "主要按钮",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                // 卡片预览
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = primaryColor.copy(alpha = 0.2f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(primaryColor.copy(alpha = 0.3f))
                    )
                }
                
                // 图标预览
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

/**
 * 跟随专辑颜色的动态主题
 */
@Composable
fun DynamicColorPreview(
    coverUrl: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (coverUrl != null) {
                coil.compose.AsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                
                // 渐变遮罩
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
            
            Text(
                text = "动态主题预览",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
