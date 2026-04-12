package com.freemusic.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.domain.model.Playlist
import com.freemusic.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistUiState(
    val playlists: List<Playlist> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    init {
        // 默认创建"我喜欢的音乐"歌单
        _uiState.update {
            it.copy(playlists = listOf(
                Playlist(
                    id = "favorites",
                    name = "我喜欢的音乐",
                    coverUrl = null,
                    songs = emptyList()
                )
            ))
        }
    }

    fun createPlaylist(name: String, songs: List<Song>) {
        val newPlaylist = Playlist(
            id = "local_${System.currentTimeMillis()}",
            name = name,
            coverUrl = songs.firstOrNull()?.coverUrl,
            songs = songs
        )
        
        _uiState.update { state ->
            state.copy(playlists = state.playlists + newPlaylist)
        }
    }

    fun deletePlaylist(playlistId: String) {
        if (playlistId == "favorites") return // 不能删除默认歌单
        
        _uiState.update { state ->
            state.copy(playlists = state.playlists.filter { it.id != playlistId })
        }
    }

    fun addToPlaylist(playlistId: String, song: Song) {
        _uiState.update { state ->
            state.copy(
                playlists = state.playlists.map { playlist ->
                    if (playlist.id == playlistId) {
                        playlist.copy(songs = playlist.songs + song)
                    } else {
                        playlist
                    }
                }
            )
        }
    }
    
    fun addSongsToPlaylist(playlistId: String, songs: List<Song>) {
        _uiState.update { state ->
            state.copy(
                playlists = state.playlists.map { playlist ->
                    if (playlist.id == playlistId) {
                        playlist.copy(songs = playlist.songs + songs)
                    } else {
                        playlist
                    }
                }
            )
        }
    }

    fun removeFromPlaylist(playlistId: String, songId: String) {
        _uiState.update { state ->
            state.copy(
                playlists = state.playlists.map { playlist ->
                    if (playlist.id == playlistId) {
                        playlist.copy(songs = playlist.songs.filter { it.id != songId })
                    } else {
                        playlist
                    }
                }
            )
        }
    }

    // ============ 我喜欢的音乐（收藏）管理 ============
    
    fun addToFavorites(song: Song) {
        _uiState.update { state ->
            if (state.favorites.any { it.id == song.id }) {
                state // 已收藏，不重复添加
            } else {
                state.copy(favorites = state.favorites + song)
            }
        }
    }

    fun removeFromFavorites(songId: String) {
        _uiState.update { state ->
            state.copy(favorites = state.favorites.filter { it.id != songId })
        }
    }

    fun toggleFavorite(song: Song) {
        if (_uiState.value.favorites.any { it.id == song.id }) {
            removeFromFavorites(song.id)
        } else {
            addToFavorites(song)
        }
    }

    fun isFavorite(songId: String): Boolean {
        return _uiState.value.favorites.any { it.id == songId }
    }
}
