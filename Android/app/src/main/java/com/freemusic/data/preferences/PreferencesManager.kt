package com.freemusic.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    
    // 主题模式: "默认"(跟随系统), "浅色", "深色"
    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "默认") ?: "默认")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

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

    // 自定义主题色（ARGB 颜色值，默认 -1 表示使用默认色）
    private val _customPrimaryColor = MutableStateFlow(prefs.getLong(KEY_CUSTOM_PRIMARY_COLOR, -1L).toInt())
    val customPrimaryColor: StateFlow<Int> = _customPrimaryColor.asStateFlow()

    fun setCustomPrimaryColor(color: Int) {
        prefs.edit().putLong(KEY_CUSTOM_PRIMARY_COLOR, color.toLong()).apply()
        _customPrimaryColor.value = color
    }
    
    // 主题预设ID（null表示不使用预设）
    private val _themePresetId = MutableStateFlow(prefs.getString(KEY_THEME_PRESET_ID, null))
    val themePresetId: StateFlow<String?> = _themePresetId.asStateFlow()
    
    fun setThemePresetId(presetId: String?) {
        if (presetId == null) {
            prefs.edit().remove(KEY_THEME_PRESET_ID).apply()
        } else {
            prefs.edit().putString(KEY_THEME_PRESET_ID, presetId).apply()
        }
        _themePresetId.value = presetId
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

    // ============ 封面自动切换间隔 ============
    // 默认 3 秒，0 表示不自动切换
    private val _coverSwitchInterval = MutableStateFlow(prefs.getInt(KEY_COVER_SWITCH_INTERVAL, 3))
    val coverSwitchInterval: StateFlow<Int> = _coverSwitchInterval.asStateFlow()

    fun setCoverSwitchInterval(seconds: Int) {
        prefs.edit().putInt(KEY_COVER_SWITCH_INTERVAL, seconds).apply()
        _coverSwitchInterval.value = seconds
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

    // 播放速度（0.5 - 2.0，默认 1.0）
    private val _playbackSpeed = MutableStateFlow(prefs.getFloat(KEY_PLAYBACK_SPEED, 1.0f))
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun setPlaybackSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_PLAYBACK_SPEED, speed).apply()
        _playbackSpeed.value = speed
    }

    // 睡眠定时器结束时间戳（毫秒，0 表示未设置）
    private val _sleepTimerEndTime = MutableStateFlow(prefs.getLong(KEY_SLEEP_TIMER_END_TIME, 0L))
    val sleepTimerEndTime: StateFlow<Long> = _sleepTimerEndTime.asStateFlow()

    fun setSleepTimer(minutes: Int) {
        val endTime = if (minutes > 0) {
            System.currentTimeMillis() + minutes * 60 * 1000L
        } else {
            0L
        }
        prefs.edit().putLong(KEY_SLEEP_TIMER_END_TIME, endTime).apply()
        _sleepTimerEndTime.value = endTime
    }

    fun clearSleepTimer() {
        prefs.edit().putLong(KEY_SLEEP_TIMER_END_TIME, 0L).apply()
        _sleepTimerEndTime.value = 0L
    }

    // 跳过静音
    private val _skipSilence = MutableStateFlow(prefs.getBoolean(KEY_SKIP_SILENCE, false))
    val skipSilence: StateFlow<Boolean> = _skipSilence.asStateFlow()

    fun setSkipSilence(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SKIP_SILENCE, enabled).apply()
        _skipSilence.value = enabled
    }
    
    // 摇一摇切歌
    private val _shakeToSkip = MutableStateFlow(prefs.getBoolean(KEY_SHAKE_TO_SKIP, false))
    val shakeToSkip: StateFlow<Boolean> = _shakeToSkip.asStateFlow()
    
    fun setShakeToSkip(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHAKE_TO_SKIP, enabled).apply()
        _shakeToSkip.value = enabled
    }
    
    // 自动清理历史记录
    private val _autoCleanHistory = MutableStateFlow(prefs.getBoolean(KEY_AUTO_CLEAN_HISTORY, false))
    val autoCleanHistory: StateFlow<Boolean> = _autoCleanHistory.asStateFlow()
    
    fun setAutoCleanHistory(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CLEAN_HISTORY, enabled).apply()
        _autoCleanHistory.value = enabled
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

    // ============ 毛玻璃效果设置 ============

    private val _blurEffectEnabled = MutableStateFlow(prefs.getBoolean(KEY_BLUR_EFFECT_ENABLED, true))
    val blurEffectEnabled: StateFlow<Boolean> = _blurEffectEnabled.asStateFlow()

    fun setBlurEffectEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BLUR_EFFECT_ENABLED, enabled).apply()
        _blurEffectEnabled.value = enabled
    }

    // ============ 背景动画设置 ============

    private val _backgroundAnimationEnabled = MutableStateFlow(prefs.getBoolean(KEY_BACKGROUND_ANIMATION_ENABLED, true))
    val backgroundAnimationEnabled: StateFlow<Boolean> = _backgroundAnimationEnabled.asStateFlow()

    fun setBackgroundAnimationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BACKGROUND_ANIMATION_ENABLED, enabled).apply()
        _backgroundAnimationEnabled.value = enabled
    }

    // ============ 歌词翻译设置 ============

    private val _showLyricsTranslation = MutableStateFlow(prefs.getBoolean(KEY_SHOW_LYRICS_TRANSLATION, true))
    val showLyricsTranslation: StateFlow<Boolean> = _showLyricsTranslation.asStateFlow()

    fun setShowLyricsTranslation(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_LYRICS_TRANSLATION, show).apply()
        _showLyricsTranslation.value = show
    }

    // ============ 自动下一首设置 ============

    private val _autoPlayNext = MutableStateFlow(prefs.getBoolean(KEY_AUTO_PLAY_NEXT, true))
    val autoPlayNext: StateFlow<Boolean> = _autoPlayNext.asStateFlow()

    fun setAutoPlayNext(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_PLAY_NEXT, enabled).apply()
        _autoPlayNext.value = enabled
    }

    // 本地音乐排序（0=名称，1=艺术家，2=时长，3=添加时间）
    private val _localMusicSort = MutableStateFlow(prefs.getInt(KEY_LOCAL_MUSIC_SORT, 0))
    val localMusicSort: StateFlow<Int> = _localMusicSort.asStateFlow()

    fun setLocalMusicSort(sort: Int) {
        prefs.edit().putInt(KEY_LOCAL_MUSIC_SORT, sort).apply()
        _localMusicSort.value = sort
    }
    
    // 最短歌曲时长（毫秒）
    private val _minSongDuration = MutableStateFlow(prefs.getInt(KEY_MIN_SONG_DURATION, 60000))
    val minSongDuration: StateFlow<Int> = _minSongDuration.asStateFlow()
    
    fun setMinSongDuration(durationMs: Int) {
        prefs.edit().putInt(KEY_MIN_SONG_DURATION, durationMs).apply()
        _minSongDuration.value = durationMs
    }
    
    // ============ 播放模式设置 ============

    private val _repeatMode = MutableStateFlow(
        RepeatModeType.valueOf(prefs.getString(KEY_REPEAT_MODE, RepeatModeType.ALL.name) ?: RepeatModeType.ALL.name)
    )
    val repeatMode: StateFlow<RepeatModeType> = _repeatMode.asStateFlow()

    fun setRepeatMode(mode: RepeatModeType) {
        prefs.edit().putString(KEY_REPEAT_MODE, mode.name).apply()
        _repeatMode.value = mode
    }

    // ============ 缓存设置 ============

    private val _cacheEnabled = MutableStateFlow(prefs.getBoolean(KEY_CACHE_ENABLED, true))
    val cacheEnabled: StateFlow<Boolean> = _cacheEnabled.asStateFlow()

    fun setCacheEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CACHE_ENABLED, enabled).apply()
        _cacheEnabled.value = enabled
    }

    private val _cacheSize = MutableStateFlow(prefs.getInt(KEY_CACHE_SIZE, 500))
    val cacheSize: StateFlow<Int> = _cacheSize.asStateFlow()

    fun setCacheSize(sizeMb: Int) {
        prefs.edit().putInt(KEY_CACHE_SIZE, sizeMb.coerceIn(100, 2000)).apply()
        _cacheSize.value = sizeMb
    }

    // ============ 播放队列持久化 ============
    // 保存播放队列（歌曲ID列表和当前索引）
    fun savePlaybackQueue(songIds: List<String>, currentIndex: Int) {
        if (songIds.isEmpty()) {
            prefs.edit().remove(KEY_PLAYBACK_QUEUE).commit()
            prefs.edit().remove(KEY_PLAYBACK_INDEX).commit()
        } else {
            prefs.edit().putString(KEY_PLAYBACK_QUEUE, songIds.joinToString(",")).commit()
            prefs.edit().putInt(KEY_PLAYBACK_INDEX, currentIndex).commit()
        }
    }

    fun getPlaybackQueueIds(): List<String> {
        val queueStr = prefs.getString(KEY_PLAYBACK_QUEUE, null) ?: return emptyList()
        return if (queueStr.isEmpty()) emptyList() else queueStr.split(",")
    }

    fun getPlaybackQueueIndex(): Int {
        return prefs.getInt(KEY_PLAYBACK_INDEX, 0)
    }

    // ============ 歌单持久化 ============
    // 格式: playlistId\tplaylistName\tsong1Data\n song2Data\n ...
    // 使用 JSON 编码歌单，避免分隔符冲突
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }
    
    private val _localPlaylists = MutableStateFlow<List<String>>(emptyList())
    val localPlaylists: StateFlow<List<String>> = _localPlaylists.asStateFlow()

    init {
        // 从 SharedPreferences 加载歌单
        val stored = prefs.getString(KEY_LOCAL_PLAYLISTS, null)
        if (stored != null && stored.isNotEmpty()) {
            try {
                val playlists = json.decodeFromString<List<PlaylistData>>(stored)
                _localPlaylists.value = playlists.map { "${it.id}|||INFO|||${it.name}|||INFO|||${it.songs.size}" }
            } catch (e: Exception) {
                _localPlaylists.value = emptyList()
            }
        }
    }

    fun saveLocalPlaylists(playlists: List<PlaylistData>) {
        if (playlists.isEmpty()) {
            prefs.edit().remove(KEY_LOCAL_PLAYLISTS).commit()
            _localPlaylists.value = emptyList()
        } else {
            val encoded = json.encodeToString(playlists)
            prefs.edit().putString(KEY_LOCAL_PLAYLISTS, encoded).commit()
            _localPlaylists.value = playlists.map { "${it.id}|||INFO|||${it.name}|||INFO|||${it.songs.size}" }
        }
    }

    fun getLocalPlaylists(): List<PlaylistData> {
        val stored = prefs.getString(KEY_LOCAL_PLAYLISTS, null) ?: return emptyList()
        if (stored.isEmpty()) return emptyList()
        
        return try {
            json.decodeFromString(stored)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ============ 收藏歌曲持久化 ============
    fun saveFavorites(songs: List<SongData>) {
        if (songs.isEmpty()) {
            prefs.edit().remove(KEY_FAVORITES).commit()
        } else {
            val encoded = json.encodeToString(songs)
            prefs.edit().putString(KEY_FAVORITES, encoded).commit()
        }
    }

    fun getFavorites(): List<SongData> {
        val stored = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        if (stored.isEmpty()) return emptyList()
        
        return try {
            json.decodeFromString(stored)
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val PREFS_NAME = "freemusic_prefs"

        // Theme
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_PURE_BLACK = "pure_black"
        private const val KEY_CUSTOM_PRIMARY_COLOR = "custom_primary_color"
        private const val KEY_THEME_PRESET_ID = "theme_preset_id"

        // Effects
        private const val KEY_PARTICLES_ENABLED = "particles_enabled"
        private const val KEY_PARTICLE_INTENSITY = "particle_intensity"
        private const val KEY_COVER_STYLE = "cover_style"
        private const val KEY_COVER_SWITCH_INTERVAL = "cover_switch_interval"
        private const val KEY_VISUALIZER_ENABLED = "visualizer_enabled"
        private const val KEY_BLUR_EFFECT_ENABLED = "blur_effect_enabled"
        private const val KEY_BACKGROUND_ANIMATION_ENABLED = "background_animation_enabled"

        // Equalizer
        private const val KEY_EQUALIZER_PRESET = "equalizer_preset"
        private const val KEY_BASS_BOOST = "bass_boost"
        private const val KEY_VIRTUALIZER = "virtualizer"

        // Playback
        private const val KEY_AUTO_PLAY = "auto_play"
        private const val KEY_PLAYBACK_SPEED = "playback_speed"
        private const val KEY_CROSSFADE_ENABLED = "crossfade_enabled"
        private const val KEY_CROSSFADE_DURATION = "crossfade_duration"
        private const val KEY_SLEEP_TIMER_END_TIME = "sleep_timer_end_time"
        private const val KEY_SKIP_SILENCE = "skip_silence"
        private const val KEY_SHAKE_TO_SKIP = "shake_to_skip"
        private const val KEY_AUTO_CLEAN_HISTORY = "auto_clean_history"
        private const val KEY_AUTO_PLAY_NEXT = "auto_play_next"
        private const val KEY_LOCAL_MUSIC_SORT = "local_music_sort"
        private const val KEY_REPEAT_MODE = "repeat_mode"

        // Lyrics
        private const val KEY_LYRICS_FONT_SIZE = "lyrics_font_size"
        private const val KEY_LYRICS_ALIGNMENT = "lyrics_alignment"
        private const val KEY_SHOW_LYRICS_TRANSLATION = "show_lyrics_translation"

        // Cache
        private const val KEY_CACHE_ENABLED = "cache_enabled"
        private const val KEY_CACHE_SIZE = "cache_size"
        private const val KEY_MIN_SONG_DURATION = "min_song_duration"

        // Playback Queue
        private const val KEY_PLAYBACK_QUEUE = "playback_queue"
        private const val KEY_PLAYBACK_INDEX = "playback_index"

        // Local Playlists
        private const val KEY_LOCAL_PLAYLISTS = "local_playlists"

        // Favorites
        private const val KEY_FAVORITES = "favorites"
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

enum class RepeatModeType {
    ALL,      // 列表循环
    ONE,      // 单曲循环
    SHUFFLE   // 随机播放
}

enum class EqualizerStyleType {
    AUTO,     // 自动（跟随封面样式）
    SPECTRUM, // 频谱
    CIRCULAR, // 圆形
    BAR,      // 柱状
    WAVEFORM, // 波形
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

/**
 * 歌单数据（用于本地持久化）
 */
@kotlinx.serialization.Serializable
data class PlaylistData(
    val id: String,
    val name: String,
    val songs: List<SongData>
)

/**
 * 歌曲数据（用于本地持久化）
 */
@kotlinx.serialization.Serializable
data class SongData(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String?,
    val duration: Long,
    val neteaseId: String?,
    val isNetease: Boolean,
    val playCount: Int = 0
)
