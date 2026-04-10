package com.freemusic.presentation

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.freemusic.domain.model.Song
import com.freemusic.presentation.navigation.FreeMusicNavHost
import com.freemusic.presentation.navigation.Screen
import com.freemusic.presentation.theme.FreeMusicTheme
import com.freemusic.presentation.viewmodel.PlayerViewModel
import com.freemusic.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            
            // 收集设置状态
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
            val isPureBlack by settingsViewModel.isPureBlack.collectAsState()
            val particlesEnabled by settingsViewModel.particlesEnabled.collectAsState()
            val particleIntensity by settingsViewModel.particleIntensity.collectAsState()
            val coverStyle by settingsViewModel.coverStyle.collectAsState()
            val visualizerEnabled by settingsViewModel.visualizerEnabled.collectAsState()
            val equalizerPreset by settingsViewModel.equalizerPreset.collectAsState()
            val bassBoost by settingsViewModel.bassBoost.collectAsState()
            val virtualizer by settingsViewModel.virtualizer.collectAsState()
            val autoPlay by settingsViewModel.autoPlay.collectAsState()
            val crossFadeEnabled by settingsViewModel.crossFadeEnabled.collectAsState()
            val crossFadeDuration by settingsViewModel.crossFadeDuration.collectAsState()
            val lyricsFontSize by settingsViewModel.lyricsFontSize.collectAsState()

            FreeMusicTheme(
                darkTheme = isDarkTheme,
                pureBlack = isPureBlack
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FreeMusicNavHost(
                        settingsViewModel = settingsViewModel,
                        // 视觉效果设置
                        particlesEnabled = particlesEnabled,
                        particleIntensity = particleIntensity,
                        coverStyle = coverStyle,
                        visualizerEnabled = visualizerEnabled,
                        // 音效设置
                        equalizerPreset = equalizerPreset,
                        bassBoost = bassBoost,
                        virtualizer = virtualizer,
                        // 播放设置
                        autoPlay = autoPlay,
                        crossFadeEnabled = crossFadeEnabled,
                        crossFadeDuration = crossFadeDuration,
                        // 歌词设置
                        lyricsFontSize = lyricsFontSize,
                        // 处理外部音频文件
                        pendingAudioUri = intent?.data,
                        onPendingAudioUriConsumed = { }
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 处理新的音频文件intent
        handleAudioIntent(intent)
    }
    
    private fun handleAudioIntent(intent: Intent?) {
        if (intent == null) return
        
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    if (isAudioUri(uri)) {
                        // 直接播放
                        playAudio(uri)
                    }
                }
            }
            "com.freemusic.PLAY_MUSIC" -> {
                intent.data?.let { uri ->
                    playAudio(uri)
                }
            }
        }
    }
    
    private fun isAudioUri(uri: Uri): Boolean {
        val mimeType = contentResolver.getType(uri)
        return mimeType?.startsWith("audio/") == true || 
               uri.toString().endsWith(".mp3") ||
               uri.toString().endsWith(".flac") ||
               uri.toString().endsWith(".m4a") ||
               uri.toString().endsWith(".wav") ||
               uri.toString().endsWith(".ogg")
    }
    
    private fun playAudio(uri: Uri) {
        // 从URI获取文件信息
        val song = createSongFromUri(uri)
        if (song != null) {
            // 使用MediaSessionService播放
            // 这里我们发送广播让PlaybackService处理
            val playIntent = Intent("com.freemusic.PLAY_SONG").apply {
                setPackage(packageName)
                putExtra("song_id", song.id)
                putExtra("song_title", song.title)
                putExtra("song_artist", song.artist)
                putExtra("song_uri", uri.toString())
            }
            sendBroadcast(playIntent)
        }
    }
    
    private fun createSongFromUri(uri: Uri): Song? {
        return try {
            var name = "未知歌曲"
            var size = 0L
            
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex >= 0) size = cursor.getLong(sizeIndex)
                }
            }
            
            Song(
                id = uri.toString(),
                title = name.substringBeforeLast("."),
                artist = "本地文件",
                album = "本地音乐",
                coverUrl = null,
                duration = 0, // 无法获取时长
                neteaseId = null,
                isNetease = false
            )
        } catch (e: Exception) {
            null
        }
    }
}
