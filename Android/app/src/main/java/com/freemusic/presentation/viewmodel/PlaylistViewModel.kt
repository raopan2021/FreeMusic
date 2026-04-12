package com.freemusic.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.data.preferences.PlaylistData
import com.freemusic.data.preferences.PreferencesManager
import com.freemusic.data.preferences.SongData
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
    application: Application,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // 加载收藏歌曲
        val savedFavorites = preferencesManager.getFavorites().map { it.toSong() }
        
        // 加载本地歌单
        val savedPlaylists = preferencesManager.getLocalPlaylists().map { it.toPlaylist() }
        
        // 创建"我喜欢的音乐"歌单（包含收藏的歌曲）
        val favoritesPlaylist = Playlist(
            id = "favorites",
            name = "我喜欢的音乐",
            coverUrl = null,
            songs = savedFavorites
        )
        
        // 歌单列表 = "我喜欢的音乐" + 用户创建的歌单
        _uiState.update {
            it.copy(
                playlists = listOf(favoritesPlaylist) + savedPlaylists,
                favorites = savedFavorites
            )
        }
    }

    private fun savePlaylists() {
        // 只保存非收藏歌单（收藏歌单通过 saveFavorites 保存）
        val nonFavoritePlaylists = _uiState.value.playlists.filter { it.id != "favorites" }
        val playlistDataList = nonFavoritePlaylists.map { it.toPlaylistData() }
        preferencesManager.saveLocalPlaylists(playlistDataList)
    }

    private fun saveFavorites() {
        val favoriteData = _uiState.value.favorites.map { it.toSongData() }
        preferencesManager.saveFavorites(favoriteData)
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
        savePlaylists()
    }

    fun deletePlaylist(playlistId: String) {
        if (playlistId == "favorites") return // 不能删除默认歌单
        
        _uiState.update { state ->
            state.copy(playlists = state.playlists.filter { it.id != playlistId })
        }
        savePlaylists()
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
        if (playlistId == "favorites") {
            saveFavorites()
        } else {
            savePlaylists()
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
        if (playlistId == "favorites") {
            saveFavorites()
        } else {
            savePlaylists()
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
                },
                // 同时从收藏中移除
                favorites = if (playlistId == "favorites") {
                    state.favorites.filter { it.id != songId }
                } else {
                    state.favorites
                }
            )
        }
        if (playlistId == "favorites") {
            saveFavorites()
        } else {
            savePlaylists()
        }
    }

    // ============ 我喜欢的音乐（收藏）管理 ============
    
    fun addToFavorites(song: Song) {
        _uiState.update { state ->
            if (state.favorites.any { it.id == song.id }) {
                state // 已收藏，不重复添加
            } else {
                // 同时添加到收藏列表和收藏歌单
                val newFavorites = state.favorites + song
                state.copy(
                    favorites = newFavorites,
                    playlists = state.playlists.map { p ->
                        if (p.id == "favorites") p.copy(songs = newFavorites) else p
                    }
                )
            }
        }
        saveFavorites()
    }

    fun removeFromFavorites(songId: String) {
        _uiState.update { state ->
            val newFavorites = state.favorites.filter { it.id != songId }
            state.copy(
                favorites = newFavorites,
                playlists = state.playlists.map { p ->
                    if (p.id == "favorites") p.copy(songs = newFavorites) else p
                }
            )
        }
        saveFavorites()
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

// ============ 数据转换扩展函数 ============

fun Song.toSongData() = SongData(
    id = id,
    title = title,
    artist = artist,
    album = album,
    coverUrl = coverUrl,
    duration = duration,
    neteaseId = neteaseId,
    isNetease = isNetease
)

fun SongData.toSong() = Song(
    id = id,
    title = title,
    artist = artist,
    album = album,
    coverUrl = coverUrl,
    duration = duration,
    neteaseId = neteaseId,
    isNetease = isNetease
)

fun Playlist.toPlaylistData() = PlaylistData(
    id = id,
    name = name,
    songs = songs.map { it.toSongData() }
)

fun PlaylistData.toPlaylist() = Playlist(
    id = id,
    name = name,
    coverUrl = songs.firstOrNull()?.coverUrl,
    songs = songs.map { it.toSong() }
)
