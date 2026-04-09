package com.freemusic.presentation.ui.share

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 分享平台数据
 */
data class SharePlatform(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val packageName: String,
    val shareTextTemplate: String,
    val shareUrlTemplate: String? = null
)

val SharePlatforms = listOf(
    SharePlatform(
        name = "微信",
        icon = Icons.Default.Share,
        color = Color(0xFF07C160),
        packageName = "com.tencent.mm",
        shareTextTemplate = "分享歌曲《%title》- %artist"
    ),
    SharePlatform(
        name = "微博",
        icon = Icons.Default.Public,
        color = Color(0xFFEA5D5C),
        packageName = "com.sina.weibo",
        shareTextTemplate = "我正在听《%title》- %artist，歌曲很好听，推荐给大家！"
    ),
    SharePlatform(
        name = "QQ",
        icon = Icons.Default.Chat,
        color = Color(0xFF12B7F5),
        packageName = "com.tencent.mobileqq",
        shareTextTemplate = "分享歌曲《%title》- %artist"
    ),
    SharePlatform(
        name = "QQ空间",
        icon = Icons.Default.PhotoAlbum,
        color = Color(0xFFFFC107),
        packageName = "com.qzone",
        shareTextTemplate = "我正在听《%title》- %artist，歌曲很好听，推荐给大家！"
    ),
    SharePlatform(
        name = "Twitter",
        icon = Icons.Default.AlternateEmail,
        color = Color(0xFF1DA1F2),
        packageName = "com.twitter.android",
        shareTextTemplate = "Listening to %title by %artist 🎵"
    ),
    SharePlatform(
        name = "Telegram",
        icon = Icons.Default.Send,
        color = Color(0xFF0088CC),
        packageName = "org.telegram.messenger",
        shareTextTemplate = "🎵 Listening to %title - %artist"
    ),
    SharePlatform(
        name = "复制链接",
        icon = Icons.Default.Link,
        color = Color(0xFF9E9E9E),
        packageName = "",
        shareTextTemplate = "%title - %artist"
    ),
    SharePlatform(
        name = "更多",
        icon = Icons.Default.MoreHoriz,
        color = Color(0xFF757575),
        packageName = "",
        shareTextTemplate = "分享歌曲《%title》- %artist"
    )
)

/**
 * 分享面板
 */
@Composable
fun SharePanel(
    song: Song?,
    onShare: (SharePlatform, String) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    var selectedPlatform by remember { mutableStateOf<SharePlatform?>(null) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = primaryColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "分享到",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 歌曲预览
            if (song != null) {
                ShareSongPreview(
                    song = song,
                    primaryColor = primaryColor
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 平台网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(SharePlatforms) { platform ->
                    SharePlatformItem(
                        platform = platform,
                        isSelected = selectedPlatform == platform,
                        onClick = {
                            selectedPlatform = platform
                            val text = buildShareText(platform.shareTextTemplate, song)
                            onShare(platform, text)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareSongPreview(
    song: Song,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                primaryColor.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SharePlatformItem(
    platform: SharePlatform,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isSelected) platform.color.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                )
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) platform.color else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = platform.icon,
                contentDescription = null,
                tint = platform.color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = platform.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = if (isSelected) 1f else 0.7f
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 分享对话框
 */
@Composable
fun ShareDialog(
    song: Song,
    onDismiss: () -> Unit,
    onShare: (SharePlatform, String) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = primaryColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("分享歌曲")
            }
        },
        text = {
            Column {
                ShareSongPreview(song = song, primaryColor = primaryColor)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SharePlatforms.take(4)) { platform ->
                        SharePlatformItem(
                            platform = platform,
                            isSelected = false,
                            onClick = {
                                val text = buildShareText(platform.shareTextTemplate, song)
                                onShare(platform, text)
                                onDismiss()
                            }
                        )
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
 * 软件分享卡片
 */
@Composable
fun AppShareCard(
    appName: String,
    appVersion: String,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = appName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "版本 $appVersion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("分享应用")
            }
        }
    }
}

/**
 * 生成分享文本
 */
private fun buildShareText(template: String, song: Song?): String {
    if (song == null) return template
    
    return template
        .replace("%title", song.title)
        .replace("%artist", song.artist)
        .replace("%album", song.album)
}

/**
 * Android分享意图处理
 */
fun createShareIntent(platform: SharePlatform, text: String): Intent {
    return when (platform.name) {
        "微信" -> {
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage(platform.packageName)
                type = "text/plain"
            }
        }
        "微博" -> {
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra("com.sina.weibo.intentExtra.TEXT", text)
                setPackage(platform.packageName)
                type = "text/plain"
            }
        }
        "QQ" -> {
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage(platform.packageName)
                type = "text/plain"
            }
        }
        "复制链接" -> {
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
        }
        else -> {
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
        }
    }
}

/**
 * 检查平台是否已安装
 */
fun isPlatformInstalled(context: Context, platform: SharePlatform): Boolean {
    if (platform.packageName.isEmpty()) return true
    
    return try {
        context.packageManager.getPackageInfo(platform.packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

/**
 * 分享到系统分享菜单
 */
@Composable
fun ShareButton(
    song: Song,
    onShare: (String) -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    
    IconButton(
        onClick = { showDialog = true },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "分享",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
    
    if (showDialog) {
        ShareDialog(
            song = song,
            onDismiss = { showDialog = false },
            onShare = { platform, text ->
                if (platform.name == "复制链接") {
                    onShare(text)
                } else {
                    val intent = createShareIntent(platform, text)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // 如果特定平台不可用，使用通用分享
                        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, text)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(fallbackIntent, "分享到"))
                    }
                }
                showDialog = false
            }
        )
    }
}

/**
 * 分享进度指示器
 */
@Composable
fun ShareProgressIndicator(
    platformName: String,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = primaryColor,
            strokeWidth = 2.dp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "正在分享到 $platformName...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 分享成功/失败反馈
 */
@Composable
fun ShareResultFeedback(
    isSuccess: Boolean,
    platformName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSuccess)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                Color(0xFFF44336).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDismiss)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = if (isSuccess)
                    "已分享到 $platformName"
                else
                    "分享到 $platformName 失败",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "关闭",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
