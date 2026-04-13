package com.freemusic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.data.remote.api.LrclibApi
import com.freemusic.data.remote.api.LrclibLyric
import com.freemusic.domain.model.Lyrics
import com.freemusic.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

/**
 * 歌词搜索页面的 UI 状态
 * @property results 搜索结果列表
 * @property isLoading 是否正在加载
 * @property error 错误信息
 */
data class LyricSearchUiState(
    val results: List<LrclibLyric> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 歌词搜索页面的 ViewModel
 * 负责从 Lrclib API 搜索歌词
 */
@HiltViewModel
class LyricSearchViewModel @Inject constructor(
    @Named("lrclib") private val lrclibApi: LrclibApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LyricSearchUiState())
    val uiState: StateFlow<LyricSearchUiState> = _uiState.asStateFlow()
    
    /**
     * 搜索歌词
     * @param song 当前播放的歌曲，用于提取歌手名和歌曲名进行搜索
     */
    fun searchLyrics(song: Song) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // 调用 Lrclib API 搜索歌词
                val results = lrclibApi.searchLyrics(
                    artistName = song.artist,
                    trackName = song.title,
                    duration = (song.duration / 1000).toInt()
                )
                _uiState.value = _uiState.value.copy(
                    results = results,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 将 LrclibLyric 转换为 domain Lyrics 对象
     * @param lrclibLyric Lrclib API 返回的歌词数据
     * @return 转换后的 Lyrics 对象
     */
    fun parseLrclibLyric(lrclibLyric: LrclibLyric): Lyrics {
        return Lyrics(
            songId = "",
            lrc = lrclibLyric.syncedLyrics ?: lrclibLyric.plainLyrics,
            yrc = null,
            translation = null,
            ttml = null
        )
    }
}
