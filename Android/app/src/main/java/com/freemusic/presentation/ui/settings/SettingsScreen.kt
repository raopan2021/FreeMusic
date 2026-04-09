package com.freemusic.presentation.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onThemeToggle: () -> Unit = {},
    onPureBlackToggle: () -> Unit = {},
    onImportClick: () -> Unit = {},
    isDarkTheme: Boolean = false,
    isPureBlack: Boolean = false
) {
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
        ) {
            // 外观设置
            Text(
                text = "外观",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // 暗黑模式
            ListItem(
                headlineContent = { Text("暗黑模式") },
                supportingContent = { Text(if (isDarkTheme) "已开启" else "已关闭") },
                leadingContent = {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null
                    )
                },
                trailingContent = {
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onThemeToggle() }
                    )
                },
                modifier = Modifier.clickable { onThemeToggle() }
            )
            
            // 纯黑模式（AMOLED）
            ListItem(
                headlineContent = { Text("纯黑模式") },
                supportingContent = { Text("使用真正的黑色（省电）") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Contrast,
                        contentDescription = null
                    )
                },
                trailingContent = {
                    Switch(
                        checked = isPureBlack,
                        onCheckedChange = { onPureBlackToggle() }
                    )
                },
                modifier = Modifier.clickable { onPureBlackToggle() }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 导入
            Text(
                text = "导入",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text("批量导入") },
                supportingContent = { Text("从链接或评论导入歌单") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = null
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable { onImportClick() }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 关于
            Text(
                text = "关于",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text("FreeMusic") },
                supportingContent = { Text("版本 0.1.0") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null
                    )
                }
            )
            
            ListItem(
                headlineContent = { Text("音乐 API") },
                supportingContent = { Text("网易云音乐 / Meting / LRCLIB") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null
                    )
                }
            )
        }
    }
}
