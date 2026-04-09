package com.freemusic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.domain.model.Playlist
import com.freemusic.domain.model.Song
import com.freemusic.domain.usecase.GetPlaylistUseCase
import com.freemusic.domain.usecase.SearchSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val searchSongsUseCase: SearchSongsUseCase,
    private val getPlaylistUseCase: GetPlaylistUseCase
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val _importedSongs = MutableStateFlow<List<Song>>(emptyList())
    val importedSongs: StateFlow<List<Song>> = _importedSongs.asStateFlow()

    /**
     * 导入歌单链接
     */
    fun importPlaylist(link: String) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            
            // 提取歌单ID
            val playlistId = extractPlaylistId(link)
            if (playlistId != null) {
                getPlaylistUseCase(playlistId).collect { result ->
                    result.fold(
                        onSuccess = { playlist ->
                            _importedSongs.value = playlist.songs
                            _importState.value = ImportState.Success(
                                songs = playlist.songs,
                                playlistName = playlist.name
                            )
                        },
                        onFailure = { error ->
                            _importState.value = ImportState.Error(
                                error.message ?: "导入歌单失败"
                            )
                        }
                    )
                }
            } else {
                _importState.value = ImportState.Error("无法识别歌单链接")
            }
        }
    }

    /**
     * 搜索歌曲名称列表
     */
    fun searchSongs(songNames: List<String>) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            val allSongs = mutableListOf<Song>()
            
            for (name in songNames.take(20)) { // 限制最多20首
                searchSongsUseCase(name, page = 0, pageSize = 1).collect { result ->
                    result.onSuccess { searchResult ->
                        searchResult.songs.firstOrNull()?.let { song ->
                            if (!allSongs.any { it.id == song.id }) {
                                allSongs.add(song)
                            }
                        }
                    }
                }
            }
            
            if (allSongs.isNotEmpty()) {
                _importedSongs.value = allSongs
                _importState.value = ImportState.Success(
                    songs = allSongs,
                    playlistName = null
                )
            } else {
                _importState.value = ImportState.Error("未找到匹配的歌曲")
            }
        }
    }

    /**
     * 移除导入的歌曲
     */
    fun removeSong(songId: String) {
        _importedSongs.value = _importedSongs.value.filter { it.id != songId }
    }

    /**
     * 清空导入列表
     */
    fun clearImport() {
        _importedSongs.value = emptyList()
        _importState.value = ImportState.Idle
    }

    private fun extractPlaylistId(link: String): String? {
        val patterns = listOf(
            Regex("""id=(\d+)"""),
            Regex("""playlist/(\d+)"""),
            Regex("""/(\d+)$""")
        )
        
        for (pattern in patterns) {
            pattern.find(link)?.let { match ->
                return match.groupValues.getOrNull(1)
            }
        }
        
        return null
    }
}

sealed class ImportState {
    data object Idle : ImportState()
    data object Loading : ImportState()
    data class Success(val songs: List<Song>, val playlistName: String?) : ImportState()
    data class Error(val message: String) : ImportState()
}
