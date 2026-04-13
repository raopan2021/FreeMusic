package com.freemusic.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent

/**
 * 媒体按钮接收器，处理蓝牙耳机和线控按钮事件
 * 将按钮事件转发给 MediaSessionService 处理
 */
class MediaButtonReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        if (Intent.ACTION_MEDIA_BUTTON == intent.action) {
            val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                // 将事件包装成 PlaybackService 可处理的格式
                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = Intent.ACTION_MEDIA_BUTTON
                    putExtra(Intent.EXTRA_KEY_EVENT, keyEvent)
                }
                context.startService(serviceIntent)
            }
        }
    }
}
