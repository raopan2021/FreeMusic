package com.freemusic.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用设置管理器
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ============ 主题设置 ============
    
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean(KEY_DARK_THEME, false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        _isDarkTheme.value = enabled
    }

    private val _isPureBlack = MutableStateFlow(prefs.getBoolean(KEY_PURE_BLACK, false))
    val isPureBlack: StateFlow<Boolean> = _isPureBlack.asStateFlow()

    fun setPureBlack(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PURE_BLACK, enabled).apply()
        _isPureBlack.value = enabled
    }

    // ============ 特效设置 ============

    private val _particlesEnabled = MutableStateFlow(prefs.getBoolean(KEY_PARTICLES_ENABLED, true))
    val particlesEnabled: StateFlow<Boolean> = _particlesEnabled.asStateFlow()

    fun setParticlesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PARTICLES_ENABLED, enabled).apply()
        _particlesEnabled.value = enabled
    }

    private val _particleIntensity = MutableStateFlow(prefs.getFloat(KEY_PARTICLE_INTENSITY, 1.0f))
    val particleIntensity: StateFlow<Float> = _particleIntensity.asStateFlow()

    fun setParticleIntensity(intensity: Float) {
        prefs.edit().putFloat(KEY_PARTICLE_INTENSITY, intensity).apply()
        _particleIntensity.value = intensity
    }

    // ============ 封面样式设置 ============

    private val _coverStyle = MutableStateFlow(
        CoverStyleType.valueOf(prefs.getString(KEY_COVER_STYLE, CoverStyleType.ROUND.name) ?: CoverStyleType.ROUND.name)
    )
    val coverStyle: StateFlow<CoverStyleType> = _coverStyle.asStateFlow()

    fun setCoverStyle(style: CoverStyleType) {
        prefs.edit().putString(KEY_COVER_STYLE, style.name).apply()
        _coverStyle.value = style
    }

    // ============ 音频可视化设置 ============

    private val _visualizerEnabled = MutableStateFlow(prefs.getBoolean(KEY_VISUALIZER_ENABLED, false))
    val visualizerEnabled: StateFlow<Boolean> = _visualizerEnabled.asStateFlow()

    fun setVisualizerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VISUALIZER_ENABLED, enabled).apply()
        _visualizerEnabled.value = enabled
    }

    // ============ 均衡器设置 ============

    private val _equalizerPreset = MutableStateFlow(prefs.getInt(KEY_EQUALIZER_PRESET, 0))
    val equalizerPreset: StateFlow<Int> = _equalizerPreset.asStateFlow()

    fun setEqualizerPreset(preset: Int) {
        prefs.edit().putInt(KEY_EQUALIZER_PRESET, preset).apply()
        _equalizerPreset.value = preset
    }

    private val _bassBoost = MutableStateFlow(prefs.getInt(KEY_BASS_BOOST, 0))
    val bassBoost: StateFlow<Int> = _bassBoost.asStateFlow()

    fun setBassBoost(level: Int) {
        prefs.edit().putInt(KEY_BASS_BOOST, level.coerceIn(0, 1000)).apply()
        _bassBoost.value = level
    }

    private val _virtualizer = MutableStateFlow(prefs.getInt(KEY_VIRTUALIZER, 0))
    val virtualizer: StateFlow<Int> = _virtualizer.asStateFlow()

    fun setVirtualizer(level: Int) {
        prefs.edit().putInt(KEY_VIRTUALIZER, level.coerceIn(0, 1000)).apply()
        _virtualizer.value = level
    }

    // ============ 播放设置 ============

    private val _autoPlay = MutableStateFlow(prefs.getBoolean(KEY_AUTO_PLAY, true))
    val autoPlay: StateFlow<Boolean> = _autoPlay.asStateFlow()

    fun setAutoPlay(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_PLAY, enabled).apply()
        _autoPlay.value = enabled
    }

    private val _crossFadeEnabled = MutableStateFlow(prefs.getBoolean(KEY_CROSSFADE_ENABLED, false))
    val crossFadeEnabled: StateFlow<Boolean> = _crossFadeEnabled.asStateFlow()

    fun setCrossFadeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CROSSFADE_ENABLED, enabled).apply()
        _crossFadeEnabled.value = enabled
    }

    private val _crossFadeDuration = MutableStateFlow(prefs.getInt(KEY_CROSSFADE_DURATION, 3000))
    val crossFadeDuration: StateFlow<Int> = _crossFadeDuration.asStateFlow()

    fun setCrossFadeDuration(durationMs: Int) {
        prefs.edit().putInt(KEY_CROSSFADE_DURATION, durationMs.coerceIn(0, 10000)).apply()
        _crossFadeDuration.value = durationMs
    }

    // ============ 歌词设置 ============

    private val _lyricsFontSize = MutableStateFlow(prefs.getInt(KEY_LYRICS_FONT_SIZE, 16))
    val lyricsFontSize: StateFlow<Int> = _lyricsFontSize.asStateFlow()

    fun setLyricsFontSize(size: Int) {
        prefs.edit().putInt(KEY_LYRICS_FONT_SIZE, size.coerceIn(12, 32)).apply()
        _lyricsFontSize.value = size
    }

    private val _lyricsAlignment = MutableStateFlow(
        LyricsAlignment.valueOf(prefs.getString(KEY_LYRICS_ALIGNMENT, LyricsAlignment.CENTER.name) ?: LyricsAlignment.CENTER.name)
    )
    val lyricsAlignment: StateFlow<LyricsAlignment> = _lyricsAlignment.asStateFlow()

    fun setLyricsAlignment(alignment: LyricsAlignment) {
        prefs.edit().putString(KEY_LYRICS_ALIGNMENT, alignment.name).apply()
        _lyricsAlignment.value = alignment
    }

    companion object {
        private const val PREFS_NAME = "freemusic_prefs"

        // Theme
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_PURE_BLACK = "pure_black"

        // Effects
        private const val KEY_PARTICLES_ENABLED = "particles_enabled"
        private const val KEY_PARTICLE_INTENSITY = "particle_intensity"
        private const val KEY_COVER_STYLE = "cover_style"
        private const val KEY_VISUALIZER_ENABLED = "visualizer_enabled"

        // Equalizer
        private const val KEY_EQUALIZER_PRESET = "equalizer_preset"
        private const val KEY_BASS_BOOST = "bass_boost"
        private const val KEY_VIRTUALIZER = "virtualizer"

        // Playback
        private const val KEY_AUTO_PLAY = "auto_play"
        private const val KEY_CROSSFADE_ENABLED = "crossfade_enabled"
        private const val KEY_CROSSFADE_DURATION = "crossfade_duration"

        // Lyrics
        private const val KEY_LYRICS_FONT_SIZE = "lyrics_font_size"
        private const val KEY_LYRICS_ALIGNMENT = "lyrics_alignment"
    }
}

enum class CoverStyleType {
    ROUND,           // 圆形
    SQUARE,          // 圆角方形
    SQUARE_NO_ROUND, // 方形
    DIAMOND,         // 菱形
    BORDER_ROUND,    // 带边框圆形
    HEXAGON,         // 六边形
    PARALLELOGRAM,   // 平行四边形
}

enum class LyricsAlignment {
    LEFT,
    CENTER,
    RIGHT
}

/**
 * 均衡器预设
 */
enum class EqualizerPreset(val displayName: String, val bands: List<Int>) {
    FLAT("平坦", listOf(0, 0, 0, 0, 0)),
    BASS_BOOST("低音增强", listOf(400, 300, 0, 0, 0)),
    TREBLE_BOOST("高音增强", listOf(0, 0, 0, 300, 400)),
    VOCAL("人声", listOf(-100, 0, 300, 0, -100)),
    ROCK("摇滚", listOf(400, 200, -100, 200, 400)),
    POP("流行", listOf(-100, 200, 400, 200, -100)),
    JAZZ("爵士", listOf(200, 0, 100, 200, 300)),
    CLASSICAL("古典", listOf(300, 200, 0, 200, 300)),
    DANCE("电子", listOf(400, 300, 0, 200, 400)),
    CUSTOM("自定义", listOf(0, 0, 0, 0, 0))
}
