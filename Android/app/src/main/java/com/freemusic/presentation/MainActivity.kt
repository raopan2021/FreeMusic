package com.freemusic.presentation

import android.os.Bundle
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
import com.freemusic.presentation.navigation.FreeMusicNavHost
import com.freemusic.presentation.theme.FreeMusicTheme
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
                        lyricsFontSize = lyricsFontSize
                    )
                }
            }
        }
    }
}
