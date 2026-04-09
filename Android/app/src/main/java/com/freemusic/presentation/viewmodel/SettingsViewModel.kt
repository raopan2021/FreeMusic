package com.freemusic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.data.preferences.CoverStyleType
import com.freemusic.data.preferences.EqualizerPreset
import com.freemusic.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置 ViewModel
 * 管理应用设置状态
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // ============ 主题设置 ============
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isPureBlack = MutableStateFlow(false)
    val isPureBlack: StateFlow<Boolean> = _isPureBlack.asStateFlow()

    // ============ 视觉效果设置 ============
    private val _particlesEnabled = MutableStateFlow(true)
    val particlesEnabled: StateFlow<Boolean> = _particlesEnabled.asStateFlow()

    private val _particleIntensity = MutableStateFlow(1f)
    val particleIntensity: StateFlow<Float> = _particleIntensity.asStateFlow()

    private val _coverStyle = MutableStateFlow(CoverStyleType.ROUND)
    val coverStyle: StateFlow<CoverStyleType> = _coverStyle.asStateFlow()

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

    private val _crossFadeEnabled = MutableStateFlow(false)
    val crossFadeEnabled: StateFlow<Boolean> = _crossFadeEnabled.asStateFlow()

    private val _crossFadeDuration = MutableStateFlow(3000)
    val crossFadeDuration: StateFlow<Int> = _crossFadeDuration.asStateFlow()

    // ============ 歌词设置 ============
    private val _lyricsFontSize = MutableStateFlow(16)
    val lyricsFontSize: StateFlow<Int> = _lyricsFontSize.asStateFlow()

    init {
        // 从 PreferencesManager 加载设置
        viewModelScope.launch {
            preferencesManager.isDarkTheme.collect { _isDarkTheme.value = it }
        }
        viewModelScope.launch {
            preferencesManager.isPureBlack.collect { _isPureBlack.value = it }
        }
        viewModelScope.launch {
            preferencesManager.particlesEnabled.collect { _particlesEnabled.value = it }
        }
        viewModelScope.launch {
            preferencesManager.particleIntensity.collect { _particleIntensity.value = it }
        }
        viewModelScope.launch {
            preferencesManager.coverStyle.collect { _coverStyle.value = it }
        }
        viewModelScope.launch {
            preferencesManager.visualizerEnabled.collect { _visualizerEnabled.value = it }
        }
        viewModelScope.launch {
            preferencesManager.equalizerPreset.collect { _equalizerPreset.value = it }
        }
        viewModelScope.launch {
            preferencesManager.bassBoost.collect { _bassBoost.value = it }
        }
        viewModelScope.launch {
            preferencesManager.virtualizer.collect { _virtualizer.value = it }
        }
        viewModelScope.launch {
            preferencesManager.autoPlay.collect { _autoPlay.value = it }
        }
        viewModelScope.launch {
            preferencesManager.crossFadeEnabled.collect { _crossFadeEnabled.value = it }
        }
        viewModelScope.launch {
            preferencesManager.crossFadeDuration.collect { _crossFadeDuration.value = it }
        }
        viewModelScope.launch {
            preferencesManager.lyricsFontSize.collect { _lyricsFontSize.value = it }
        }
    }

    // ============ 主题控制 ============
    fun setDarkTheme(enabled: Boolean) {
        preferencesManager.setDarkTheme(enabled)
    }

    fun setPureBlack(enabled: Boolean) {
        preferencesManager.setPureBlack(enabled)
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

    fun setCrossFadeEnabled(enabled: Boolean) {
        preferencesManager.setCrossFadeEnabled(enabled)
    }

    fun setCrossFadeDuration(durationMs: Int) {
        preferencesManager.setCrossFadeDuration(durationMs)
    }

    // ============ 歌词控制 ============
    fun setLyricsFontSize(size: Int) {
        preferencesManager.setLyricsFontSize(size)
    }

    /**
     * 获取均衡器预设名称
     */
    fun getEqualizerPresetName(): String {
        return EqualizerPreset.entries.getOrNull(_equalizerPreset.value)?.displayName ?: "平坦"
    }
}
