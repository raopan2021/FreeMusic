package com.freemusic.presentation.ui.util

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freemusic.presentation.ui.theme.PrimaryIndigo

/**
 * 加载状态
 */
@Composable
fun LoadingView(
    message: String = "加载中...",
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = primaryColor,
            strokeWidth = 3.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * 错误状态
 */
@Composable
fun ErrorView(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "出错了",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("重试")
            }
        }
    }
}

/**
 * 网络错误状态
 */
@Composable
fun NetworkErrorView(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryIndigo
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "网络连接失败",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "请检查网络设置后重试",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("重试")
        }
    }
}

/**
 * 空列表状态
 */
@Composable
fun EmptyListView(
    icon: @Composable () -> Unit,
    title: String,
    message: String,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            action()
        }
    }
}

/**
 * 格式化工具函数
 */
object FormatUtils {
    
    fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return when {
            hours > 0 -> "%d:%02d:%02d".format(hours, minutes, seconds)
            else -> "%d:%02d".format(minutes, seconds)
        }
    }
    
    fun formatCount(count: Long): String {
        return when {
            count >= 1_000_000_000 -> "%.1fB".format(count / 1_000_000_000.0)
            count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
            count >= 1_000 -> "%.1fK".format(count / 1_000.0)
            else -> count.toString()
        }
    }
    
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> "%.2f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576 -> "%.2f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024 -> "%.2f KB".format(bytes / 1_024.0)
            else -> "$bytes B"
        }
    }
    
    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            seconds < 60 -> "刚刚"
            minutes < 60 -> "${minutes}分钟前"
            hours < 24 -> "${hours}小时前"
            days < 7 -> "${days}天前"
            else -> {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            }
        }
    }
}

/**
 * 颜色工具
 */
object ColorUtils {
    
    fun blendColors(color1: Color, color2: Color, ratio: Float): Color {
        val inverseRatio = 1f - ratio
        return Color(
            red = color1.red * inverseRatio + color2.red * ratio,
            green = color1.green * inverseRatio + color2.green * ratio,
            blue = color1.blue * inverseRatio + color2.blue * ratio,
            alpha = color1.alpha * inverseRatio + color2.alpha * ratio
        )
    }
    
    fun isColorDark(color: Color): Boolean {
        val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
        return luminance < 0.5
    }
    
    fun getContrastColor(backgroundColor: Color): Color {
        return if (isColorDark(backgroundColor)) Color.White else Color.Black
    }
}

/**
 * 隐私信息类型
 */
enum class PrivacyInfoType {
    LOCATION,
    CAMERA,
    MICROPHONE,
    STORAGE,
    CONTACTS,
    PHONE_STATE
}

/**
 * 显示隐私信息对话框
 */
@Composable
fun PrivacyInfoDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("隐私说明") },
        text = {
            Column {
                Text("为了提供更好的服务，应用需要以下权限：")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• 存储权限：读取本地音乐文件")
                Text("• 网络权限：在线搜索和播放音乐")
                Text("• 后台播放：持续播放音乐")
            }
        },
        confirmButton = {
            Button(onClick = onAccept) {
                Text("同意")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("不同意")
            }
        }
    )
}
