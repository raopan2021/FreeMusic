package com.freemusic.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.view.KeyEvent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
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

    // 耳机按键多击处理
    private var lastPlayPauseClick = 0L
    private var lastPreviousClick = 0L
    private var playPauseClickCount = 0
    private var previousClickCount = 0
    private val doubleClickInterval = 400L // 双击判定间隔（稍微放宽）

    // 耳机类型标记
    private var isAirPods = false
    private var isSamsungBuds = false

    override fun onCreate() {
        super.onCreate()

        // 创建 MediaSession
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())
            .build()

        // 注册广播接收器
        val filter = IntentFilter("com.freemusic.PLAY_SONG")
        registerReceiver(audioReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    /**
     * 媒体会话回调
     */
    private inner class MediaSessionCallback : MediaSession.Callback {
        // 使用默认回调即可，MediaSession 会自动处理媒体按键
    }

    /**
     * 处理媒体按键事件（蓝牙耳机、线控）
     */
    fun handleMediaKeyEvent(keyEvent: KeyEvent): Boolean {
        if (keyEvent.action != KeyEvent.ACTION_DOWN) return true

        val currentTime = System.currentTimeMillis()

        when (keyEvent.keyCode) {
            // 标准暂停/播放键
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                if (currentTime - lastPlayPauseClick < doubleClickInterval) {
                    playPauseClickCount++
                } else {
                    playPauseClickCount = 1
                }
                lastPlayPauseClick = currentTime

                // AirPods 双击、三击检测
                if (isAirPods) {
                    when (playPauseClickCount) {
                        1 -> player.play() // AirPods 单击通常不操作
                        2 -> player.pause() // 双击暂停
                    }
                } else {
                    // 普通耳机：单击暂停/播放
                    if (currentTime - lastPlayPauseClick >= doubleClickInterval) {
                        togglePlayPause()
                    } else if (playPauseClickCount >= 3) {
                        // 三击：下一首
                        player.seekToNext()
                        playPauseClickCount = 0
                    }
                }
                return true
            }

            // 耳机线控按钮（单击通常发送这个）
            KeyEvent.KEYCODE_HEADSETHOOK -> {
                if (currentTime - lastPlayPauseClick < doubleClickInterval) {
                    playPauseClickCount++
                } else {
                    playPauseClickCount = 1
                }
                lastPlayPauseClick = currentTime

                when {
                    // Samsung AirPods 风格：双击下一首，三击上一首
                    isSamsungBuds || playPauseClickCount >= 3 -> {
                        player.seekToNext()
                        playPauseClickCount = 0
                    }
                    playPauseClickCount == 2 -> {
                        player.seekToPrevious()
                    }
                    else -> {
                        // 单击：暂停/播放
                        togglePlayPause()
                    }
                }
                return true
            }

            // 下一首
            KeyEvent.KEYCODE_MEDIA_NEXT,
            KeyEvent.KEYCODE_MEDIA_FAST -> {
                player.seekToNext()
                playPauseClickCount = 0
                return true
            }

            // 上一首
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_MEDIA_REWIND -> {
                if (currentTime - lastPreviousClick < doubleClickInterval) {
                    previousClickCount++
                } else {
                    previousClickCount = 1
                }
                lastPreviousClick = currentTime

                when {
                    previousClickCount >= 3 -> {
                        // 三连击：上一首
                        player.seekToPrevious()
                        previousClickCount = 0
                    }
                    previousClickCount == 2 -> {
                        // 双击：重新播放当前
                        player.seekTo(0)
                    }
                    else -> {
                        // 单击：上一首
                        player.seekToPrevious()
                    }
                }
                return true
            }

            // 停止
            KeyEvent.KEYCODE_MEDIA_STOP -> {
                player.stop()
                return true
            }

            // Apple AirPods 特定按键（如果有的话）
            KeyEvent.KEYCODE_VOLUME_UP -> {
                // 某些耳机音量键同时发送媒体命令
                return false // 交给系统处理
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                return false // 交给系统处理
            }

            // 未知按键，尝试检测是否为厂商自定义
            else -> {
                // 检测是否为 AirPods 系列
                if (keyEvent.keyCode == 126 || keyEvent.keyCode == 127) {
                    isAirPods = true
                }
                // Samsung buds 可能发送的按键码
                if (keyEvent.keyCode == 200 || keyEvent.keyCode == 201) {
                    isSamsungBuds = true
                }
                return false
            }
        }
    }

    private fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                "com.freemusic.PLAY_SONG" -> {
                    intent.getStringExtra("song_uri")?.let { uriString ->
                        val uri = Uri.parse(uriString)
                        playAudio(uri, intent)
                    }
                }
                Intent.ACTION_MEDIA_BUTTON -> {
                    val keyEvent = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                    if (keyEvent != null) {
                        handleMediaKeyEvent(keyEvent)
                    }
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
        context?.startService(intent?.setClass(context, PlaybackService::class.java))
    }
}
