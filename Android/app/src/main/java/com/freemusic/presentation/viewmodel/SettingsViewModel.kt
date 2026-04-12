package com.freemusic.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.data.preferences.CoverStyleType
import com.freemusic.data.preferences.EqualizerPreset
import com.freemusic.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class SettingsUiState(
    val currentThemeName: String = "默认",
    val pureBlackEnabled: Boolean = false,
    val customPrimaryColor: Int = -1, // -1 表示使用默认色
    val particleEffectName: String = "无",
    val coverStyleName: String = "圆形",
    val coverSwitchInterval: Int = 3,
    val visualizerStyleName: String = "无",
    val equalizerPresetName: String = "平坦",
    val autoPlayEnabled: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val sleepTimerMinutes: Int = 0,
    val skipSilenceEnabled: Boolean = false,
    val highQualityEnabled: Boolean = false,
    val cacheSize: String = "0 MB",
    val showAboutDialog: Boolean = false,
    val lyricsFontSize: Int = 16
)

/**
 * 设置 ViewModel
 * 管理应用设置状态
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // ============ 主题设置 ============
    private val _themeMode = MutableStateFlow("默认")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isPureBlack = MutableStateFlow(false)
    val isPureBlack: StateFlow<Boolean> = _isPureBlack.asStateFlow()

    private val _customPrimaryColor = MutableStateFlow(-1)
    val customPrimaryColor: StateFlow<Int> = _customPrimaryColor.asStateFlow()

    // ============ 视觉效果设置 ============
    private val _particlesEnabled = MutableStateFlow(true)
    val particlesEnabled: StateFlow<Boolean> = _particlesEnabled.asStateFlow()

    private val _particleIntensity = MutableStateFlow(1f)
    val particleIntensity: StateFlow<Float> = _particleIntensity.asStateFlow()

    private val _coverStyle = MutableStateFlow(CoverStyleType.ROUND)
    val coverStyle: StateFlow<CoverStyleType> = _coverStyle.asStateFlow()

    private val _coverSwitchInterval = MutableStateFlow(3)
    val coverSwitchInterval: StateFlow<Int> = _coverSwitchInterval.asStateFlow()

    private val _visualizerEnabled = MutableStateFlow(false)
    val visualizerEnabled: StateFlow<Boolean> = _visualizerEnabled.asStateFlow()

    // ============ 音效设置 ============
    private val _equalizerPreset = MutableStateFlow(0)
    val equalizerPreset: StateFlow<Int> = _equalizerPreset.asStateFlow()

    private val _bassBoost = MutableStateFlow(0)
    val bassBoost: StateFlow<Int> = _bassBoost.asStateFlow()

    private val _virtualizer = MutableStateFlow(0)
    val virtualizer: StateFlow<Int> = _virtualizer.asStateFlow()

    // ============ 播放设置 ============
    private val _autoPlay = MutableStateFlow(true)
    val autoPlay: StateFlow<Boolean> = _autoPlay.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _crossFadeEnabled = MutableStateFlow(false)
    val crossFadeEnabled: StateFlow<Boolean> = _crossFadeEnabled.asStateFlow()

    private val _crossFadeDuration = MutableStateFlow(3000)
    val crossFadeDuration: StateFlow<Int> = _crossFadeDuration.asStateFlow()

    // 睡眠定时器结束时间戳（用于检测计时器是否过期）
    private val _sleepTimerEndTime = MutableStateFlow(0L)
    val sleepTimerEndTime: StateFlow<Long> = _sleepTimerEndTime.asStateFlow()

    val sleepTimerRemainingSeconds: Int
        get() {
            val endTime = _sleepTimerEndTime.value
            if (endTime == 0L || endTime < System.currentTimeMillis()) {
                return 0
            }
            return ((endTime - System.currentTimeMillis()) / 1000).toInt()
        }

    private val _skipSilence = MutableStateFlow(false)
    val skipSilence: StateFlow<Boolean> = _skipSilence.asStateFlow()
    
    private val _shakeToSkip = MutableStateFlow(false)
    val shakeToSkip: StateFlow<Boolean> = _shakeToSkip.asStateFlow()
    
    private val _autoCleanHistory = MutableStateFlow(false)
    val autoCleanHistory: StateFlow<Boolean> = _autoCleanHistory.asStateFlow()
    
    // ============ 歌词设置 ============
    private val _lyricsFontSize = MutableStateFlow(16)
    val lyricsFontSize: StateFlow<Int> = _lyricsFontSize.asStateFlow()

    init {
        // 从 PreferencesManager 加载设置
        viewModelScope.launch {
            preferencesManager.themeMode.collect { 
                _themeMode.value = it
                updateUiState()
            }
        }
        viewModelScope.launch {
            preferencesManager.isDarkTheme.collect { 
                _isDarkTheme.value = it
                updateUiState()
            }
        }
        viewModelScope.launch {
            preferencesManager.isPureBlack.collect { 
                _isPureBlack.value = it
                updateUiState()
            }
        }
        viewModelScope.launch {
            preferencesManager.customPrimaryColor.collect { 
                _customPrimaryColor.value = it
                updateUiState()
            }
        }
        viewModelScope.launch {
            preferencesManager.particlesEnabled.collect { _particlesEnabled.value = it }
        }
        viewModelScope.launch {
            preferencesManager.particleIntensity.collect { _particleIntensity.value = it }
        }
        viewModelScope.launch {
            preferencesManager.coverStyle.collect { 
                _coverStyle.value = it
                updateUiState()
            }
        }
        viewModelScope.launch {
            preferencesManager.coverSwitchInterval.collect { 
                _coverSwitchInterval.value = it
                updateUiState()
            }
        }
        viewModelScope.launch {
            preferencesManager.visualizerEnabled.collect { _visualizerEnabled.value = it }
        }
        viewModelScope.launch {
            preferencesManager.equalizerPreset.collect { 
                _equalizerPreset.value = it
                updateUiState()
            }
        }
        viewModelScope.launch {
            preferencesManager.bassBoost.collect { _bassBoost.value = it }
        }
        viewModelScope.launch {
            preferencesManager.virtualizer.collect { _virtualizer.value = it }
        }
        viewModelScope.launch {
            preferencesManager.autoPlay.collect { 
                _autoPlay.value = it
                updateUiState()
            }
        }
        viewModelScope.launch {
            preferencesManager.playbackSpeed.collect { 
                _playbackSpeed.value = it
                updateUiState()
            }
        }
        viewModelScope.launch {
            preferencesManager.crossFadeEnabled.collect { _crossFadeEnabled.value = it }
        }
        viewModelScope.launch {
            preferencesManager.crossFadeDuration.collect { _crossFadeDuration.value = it }
        }
        viewModelScope.launch {
            preferencesManager.sleepTimerEndTime.collect { _sleepTimerEndTime.value = it }
        }
        viewModelScope.launch {
            preferencesManager.skipSilence.collect { _skipSilence.value = it }
        }
        viewModelScope.launch {
            preferencesManager.shakeToSkip.collect { _shakeToSkip.value = it }
        }
        viewModelScope.launch {
            preferencesManager.autoCleanHistory.collect { _autoCleanHistory.value = it }
        }
        viewModelScope.launch {
            preferencesManager.lyricsFontSize.collect { size ->
                _lyricsFontSize.value = size
                _uiState.update { it.copy(lyricsFontSize = size) }
            }
        }
        
        updateCacheSize()
    }

    private fun updateUiState() {
        val themeName = when {
            _isPureBlack.value -> "纯黑"
            _isDarkTheme.value -> "暗色"
            else -> "默认"
        }
        _uiState.update { state ->
            state.copy(
                currentThemeName = themeName,
                pureBlackEnabled = _isPureBlack.value,
                customPrimaryColor = _customPrimaryColor.value,
                coverStyleName = _coverStyle.value.name,
                coverSwitchInterval = _coverSwitchInterval.value,
                equalizerPresetName = EqualizerPreset.entries.getOrNull(_equalizerPreset.value)?.displayName ?: "平坦",
                autoPlayEnabled = _autoPlay.value,
                playbackSpeed = _playbackSpeed.value,
                // sleepTimerMinutes no longer stored, using sleepTimerRemainingSeconds in UI
                skipSilenceEnabled = _skipSilence.value
            )
        }
    }

    private fun updateCacheSize() {
        viewModelScope.launch {
            val cacheDir = application.cacheDir
            val size = calculateCacheSize(cacheDir)
            _uiState.update { it.copy(cacheSize = formatFileSize(size)) }
        }
    }

    private fun calculateCacheSize(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                calculateCacheSize(file)
            } else {
                file.length()
            }
        }
        return size
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> "%.2f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576 -> "%.2f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024 -> "%.2f KB".format(bytes / 1_024.0)
            else -> "$bytes B"
        }
    }

    // ============ 主题控制 ============
    fun setDarkTheme(enabled: Boolean) {
        preferencesManager.setDarkTheme(enabled)
    }

    fun setPureBlack(enabled: Boolean) {
        preferencesManager.setPureBlack(enabled)
    }

    fun setCustomPrimaryColor(color: Int) {
        preferencesManager.setCustomPrimaryColor(color)
    }

    // ============ 视觉效果控制 ============
    fun setParticlesEnabled(enabled: Boolean) {
        preferencesManager.setParticlesEnabled(enabled)
    }

    fun setParticleIntensity(intensity: Float) {
        preferencesManager.setParticleIntensity(intensity)
    }

    fun setCoverStyle(style: CoverStyleType) {
        preferencesManager.setCoverStyle(style)
    }

    fun setCoverSwitchInterval(seconds: Int) {
        preferencesManager.setCoverSwitchInterval(seconds)
    }

    fun setVisualizerEnabled(enabled: Boolean) {
        preferencesManager.setVisualizerEnabled(enabled)
    }

    // ============ 音效控制 ============
    fun setEqualizerPreset(preset: Int) {
        preferencesManager.setEqualizerPreset(preset)
    }

    fun setBassBoost(level: Int) {
        preferencesManager.setBassBoost(level)
    }

    fun setVirtualizer(level: Int) {
        preferencesManager.setVirtualizer(level)
    }

    // ============ 播放控制 ============
    fun setAutoPlay(enabled: Boolean) {
        preferencesManager.setAutoPlay(enabled)
    }

    fun setPlaybackSpeed(speed: Float) {
        preferencesManager.setPlaybackSpeed(speed)
    }

    fun setHighQuality(enabled: Boolean) {
        // 高品质播放设置 - 目前仅保存在 UI 状态
        // 实际的高品质播放逻辑需要在播放器中实现
        _uiState.update { it.copy(highQualityEnabled = enabled) }
    }

    fun setCrossFadeEnabled(enabled: Boolean) {
        preferencesManager.setCrossFadeEnabled(enabled)
    }

    fun setCrossFadeDuration(durationMs: Int) {
        preferencesManager.setCrossFadeDuration(durationMs)
    }

    fun setSleepTimer(minutes: Int) {
        preferencesManager.setSleepTimer(minutes)
    }

    fun setSkipSilence(enabled: Boolean) {
        preferencesManager.setSkipSilence(enabled)
    }
    
    fun setShakeToSkip(enabled: Boolean) {
        preferencesManager.setShakeToSkip(enabled)
    }
    
    fun setAutoCleanHistory(enabled: Boolean) {
        preferencesManager.setAutoCleanHistory(enabled)
    }
    
    // ============ 歌词控制 ============
    fun setLyricsFontSize(size: Int) {
        preferencesManager.setLyricsFontSize(size)
    }

    // ============ 其他设置 ============
    fun setTheme(theme: String) {
        _uiState.update { it.copy(currentThemeName = theme) }
        preferencesManager.setThemeMode(theme)
        
        // 根据主题名称设置深色模式
        when (theme) {
            "暗色" -> {
                preferencesManager.setDarkTheme(true)
                preferencesManager.setPureBlack(false)
            }
            "纯黑" -> {
                preferencesManager.setDarkTheme(true)
                preferencesManager.setPureBlack(true)
            }
            "默认", "浅色" -> {
                preferencesManager.setDarkTheme(false)
                preferencesManager.setPureBlack(false)
            }
        }
    }

    fun setParticleEffect(effect: String) {
        // 粒子效果设置 - 目前仅保存在 UI 状态
        // 粒子效果的开关由粒子系统组件控制
        _uiState.update { it.copy(particleEffectName = effect) }
    }

    fun setVisualizerStyle(style: String) {
        // 可视化器设置 - 目前仅保存在 UI 状态
        // 可视化器的开关由可视化器组件控制
        _uiState.update { it.copy(visualizerStyleName = style) }
    }

    fun clearCache() {
        viewModelScope.launch {
            application.cacheDir.deleteRecursively()
            updateCacheSize()
        }
    }

    fun showAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = true) }
    }

    fun hideAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = false) }
    }
}
