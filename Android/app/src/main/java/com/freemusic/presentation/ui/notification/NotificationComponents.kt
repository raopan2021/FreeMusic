package com.freemusic.presentation.ui.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * 通知渠道ID
 */
object NotificationChannels {
    const val PLAYBACK_CHANNEL_ID = "freemusic_playback"
    const val DOWNLOAD_CHANNEL_ID = "freemusic_download"
    const val IMPORT_CHANNEL_ID = "freemusic_import"
}

/**
 * 通知ID
 */
object NotificationIds {
    const val PLAYBACK_NOTIFICATION_ID = 1
    const val DOWNLOAD_NOTIFICATION_ID = 2
    const val IMPORT_NOTIFICATION_ID = 3
}

/**
 * 播放通知数据
 */
data class PlaybackNotificationData(
    val songTitle: String,
    val artistName: String,
    val albumName: String,
    val coverBitmap: Bitmap?,
    val isPlaying: Boolean,
    val duration: Long,
    val position: Long
)

/**
 * 创建播放通知
 */
fun createPlaybackNotification(
    context: Context,
    data: PlaybackNotificationData,
    actions: List<NotificationAction>
): Notification {
    val channelId = NotificationChannels.PLAYBACK_CHANNEL_ID
    createNotificationChannel(context, channelId, "音乐播放", "音乐播放控制")
    
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_media_play)
        .setContentTitle(data.songTitle)
        .setContentText(data.artistName)
        .setSubText(data.albumName)
        .setLargeIcon(data.coverBitmap)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setOnlyAlertOnce(true)
        .setShowWhen(false)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
    
    // 添加Action按钮
    actions.forEach { action ->
        builder.addAction(action.iconResId, action.title, action.pendingIntent)
    }
    
    return builder.build()
}

/**
 * 通知Action
 */
data class NotificationAction(
    val action: String,
    val title: String,
    val iconResId: Int,
    val pendingIntent: PendingIntent
)

/**
 * 通知Action类型
 */
object NotificationActionTypes {
    const val ACTION_PLAY_PAUSE = "com.freemusic.ACTION_PLAY_PAUSE"
    const val ACTION_NEXT = "com.freemusic.ACTION_NEXT"
    const val ACTION_PREVIOUS = "com.freemusic.ACTION_PREVIOUS"
    const val ACTION_DISMISS = "com.freemusic.ACTION_DISMISS"
}

/**
 * 创建通知Action
 */
fun createNotificationAction(
    context: Context,
    action: String,
    iconResId: Int,
    title: String
): NotificationAction {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        this.action = action
    }
    
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        action.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    return NotificationAction(
        action = action,
        title = title,
        iconResId = iconResId,
        pendingIntent = pendingIntent
    )
}

/**
 * 创建通知渠道
 */
fun createNotificationChannel(
    context: Context,
    channelId: String,
    name: String,
    description: String
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(channelId, name, importance).apply {
            this.description = description
            setShowBadge(false)
        }
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}

/**
 * 通知接收器
 */
class NotificationReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationActionTypes.ACTION_PLAY_PAUSE -> {
                // Handle play/pause
            }
            NotificationActionTypes.ACTION_NEXT -> {
                // Handle next
            }
            NotificationActionTypes.ACTION_PREVIOUS -> {
                // Handle previous
            }
            NotificationActionTypes.ACTION_DISMISS -> {
                // Handle dismiss
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.cancel(NotificationIds.PLAYBACK_NOTIFICATION_ID)
            }
        }
    }
}

/**
 * 下载通知数据
 */
data class DownloadNotificationData(
    val songTitle: String,
    val progress: Int, // 0-100
    val isComplete: Boolean,
    val errorMessage: String? = null
)

/**
 * 创建下载通知
 */
fun createDownloadNotification(
    context: Context,
    data: DownloadNotificationData
): Notification {
    val channelId = NotificationChannels.DOWNLOAD_CHANNEL_ID
    createNotificationChannel(context, channelId, "下载管理", "歌曲下载进度")
    
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentTitle(if (data.isComplete) "下载完成" else "正在下载")
        .setContentText(data.songTitle)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
    
    if (!data.isComplete) {
        builder.setProgress(100, data.progress, false)
    }
    
    if (data.errorMessage != null) {
        builder.setSubText(data.errorMessage)
    }
    
    return builder.build()
}

/**
 * 导入通知数据
 */
data class ImportNotificationData(
    val playlistName: String,
    val current: Int,
    val total: Int,
    val currentSongTitle: String
)

/**
 * 创建导入通知
 */
fun createImportNotification(
    context: Context,
    data: ImportNotificationData
): Notification {
    val channelId = NotificationChannels.IMPORT_CHANNEL_ID
    createNotificationChannel(context, channelId, "导入管理", "歌单导入进度")
    
    val progress = (data.current.toFloat() / data.total * 100).toInt()
    
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_menu_upload)
        .setContentTitle("正在导入: ${data.playlistName}")
        .setContentText(data.currentSongTitle)
        .setSubText("${data.current}/${data.total}")
        .setProgress(100, progress, false)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
    
    return builder.build()
}

/**
 * 通知管理器
 */
class MusicNotificationManager(private val context: Context) {
    
    private val notificationManager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)
    
    fun showPlaybackNotification(data: PlaybackNotificationData, actions: List<NotificationAction>) {
        val notification = createPlaybackNotification(context, data, actions)
        notificationManager.notify(NotificationIds.PLAYBACK_NOTIFICATION_ID, notification)
    }
    
    fun updatePlaybackNotification(data: PlaybackNotificationData, actions: List<NotificationAction>) {
        val notification = createPlaybackNotification(context, data, actions)
        notificationManager.notify(NotificationIds.PLAYBACK_NOTIFICATION_ID, notification)
    }
    
    fun cancelPlaybackNotification() {
        notificationManager.cancel(NotificationIds.PLAYBACK_NOTIFICATION_ID)
    }
    
    fun showDownloadNotification(data: DownloadNotificationData) {
        val notification = createDownloadNotification(context, data)
        notificationManager.notify(NotificationIds.DOWNLOAD_NOTIFICATION_ID, notification)
    }
    
    fun showImportNotification(data: ImportNotificationData) {
        val notification = createImportNotification(context, data)
        notificationManager.notify(NotificationIds.IMPORT_NOTIFICATION_ID, notification)
    }
    
    fun cancelImportNotification() {
        notificationManager.cancel(NotificationIds.IMPORT_NOTIFICATION_ID)
    }
}
