package com.freemusic.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    
    @Inject
    lateinit var player: ExoPlayer
    
    private val audioReceiver = AudioIntentReceiver()

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, player).build()
        
        // 注册广播接收器
        val filter = IntentFilter("com.freemusic.PLAY_SONG")
        registerReceiver(audioReceiver, filter, RECEIVER_NOT_EXPORTED)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 处理音频播放请求
        intent?.action?.let { action ->
            when (action) {
                "com.freemusic.PLAY_SONG" -> {
                    intent.getStringExtra("song_uri")?.let { uriString ->
                        val uri = Uri.parse(uriString)
                        playAudio(uri, intent)
                    }
                }
                else -> {
                    // Unknown action, ignore
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(audioReceiver)
        } catch (e: Exception) {
            // Ignore if not registered
        }
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
    
    private fun playAudio(uri: Uri, intent: Intent) {
        val title = intent.getStringExtra("song_title") ?: "未知歌曲"
        val artist = intent.getStringExtra("song_artist") ?: "未知艺术家"
        
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .build()
            )
            .build()
        
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }
}

class AudioIntentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // 转发到服务
        context?.startService(intent?.setClass(context, PlaybackService::class.java))
    }
}
