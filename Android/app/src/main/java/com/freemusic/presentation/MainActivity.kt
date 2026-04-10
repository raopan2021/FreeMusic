package com.freemusic.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import com.freemusic.domain.model.Song
import com.freemusic.FreeMusicApp
import com.freemusic.presentation.navigation.FreeMusicNavHost
import com.freemusic.presentation.theme.FreeMusicTheme
import com.freemusic.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var pendingAudioUri: Uri? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 处理音频文件intent
        pendingAudioUri = handleAudioIntent(intent)
        
        setContent {
            val settingsViewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            
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
                        pendingAudioUri = pendingAudioUri,
                        onPendingAudioUriConsumed = { pendingAudioUri = null }
                    )
                }
            }
        }
        
        // 如果有音频URI，立即播放
        pendingAudioUri?.let { uri ->
            playAudio(uri)
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 处理新的音频文件intent
        val uri = handleAudioIntent(intent)
        if (uri != null) {
            pendingAudioUri = uri
            playAudio(uri)
        }
    }
    
    private fun handleAudioIntent(intent: Intent?): Uri? {
        if (intent == null) return null
        
        return when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.data?.takeIf { isAudioUri(it) }
            }
            "com.freemusic.PLAY_MUSIC" -> {
                intent.data
            }
            else -> null
        }
    }
    
    private fun isAudioUri(uri: Uri): Boolean {
        val mimeType = contentResolver.getType(uri)
        return mimeType?.startsWith("audio/") == true || 
               uri.toString().endsWith(".mp3") ||
               uri.toString().endsWith(".flac") ||
               uri.toString().endsWith(".m4a") ||
               uri.toString().endsWith(".wav") ||
               uri.toString().endsWith(".ogg") ||
               uri.toString().endsWith(".aac")
    }
    
    private fun playAudio(uri: Uri) {
        try {
            // 直接使用 ExoPlayer 播放
            val player = ExoPlayer.Builder(this).build()
            
            val title = getFileName(uri) ?: "未知歌曲"
            
            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist("本地文件")
                        .build()
                )
                .build()
            
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            
            // 保存 player 引用以便后续使用
            (application as? FreeMusicApp)?.currentPlayer = player
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun getFileName(uri: Uri): String? {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        cursor.getString(nameIndex)?.substringBeforeLast(".")
                    } else null
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
