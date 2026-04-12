package com.freemusic.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.freemusic.presentation.navigation.FreeMusicNavHost
import com.freemusic.presentation.theme.FreeMusicTheme
import com.freemusic.presentation.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // 使用 mutableStateOf 确保 recomposition
    private val _pendingAudioUri = androidx.compose.runtime.mutableStateOf<Uri?>(null)
    private val pendingAudioUri: Uri? get() = _pendingAudioUri.value
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 设置导航栏透明，覆盖系统导航条
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarDividerColor = android.graphics.Color.TRANSPARENT
        
        // 处理初始音频文件intent
        _pendingAudioUri.value = handleAudioIntent(intent)
        
        setContent {
            val settingsViewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            
            // 收集设置状态
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val isPureBlack by settingsViewModel.isPureBlack.collectAsState()
            val customPrimaryColor by settingsViewModel.customPrimaryColor.collectAsState()
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

            // 根据主题模式计算实际使用的深色模式
            val effectiveDarkTheme = when (themeMode) {
                "默认" -> isSystemInDarkTheme()
                "暗色", "纯黑" -> true
                else -> false
            }

            FreeMusicTheme(
                darkTheme = effectiveDarkTheme,
                pureBlack = isPureBlack && themeMode == "纯黑",
                customPrimaryColor = if (customPrimaryColor == -1) null else customPrimaryColor
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
                        onPendingAudioUriConsumed = { 
                            _pendingAudioUri.value = null 
                        }
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 处理新的音频文件intent
        val uri = handleAudioIntent(intent)
        if (uri != null) {
            _pendingAudioUri.value = uri
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
}
