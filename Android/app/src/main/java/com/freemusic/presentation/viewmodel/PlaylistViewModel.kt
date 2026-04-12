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
    val playlists: List<Playlist> = emptyList(),  // 包含"我喜欢的音乐"
    val isLoading: Boolean = false,
    val error: String? = null
) {
    // 便捷方法：获取收藏歌单
    val favoritesPlaylist: Playlist?
        get() = playlists.find { it.id == "favorites" }
    
    // 便捷方法：获取收藏歌曲列表
    val favorites: List<Song>
        get() = favoritesPlaylist?.songs ?: emptyList()
}

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
        // 加载本地歌单
        val savedPlaylists = preferencesManager.getLocalPlaylists().map { it.toPlaylist() }
        
        // 创建"我喜欢的音乐"歌单
        val favoritesPlaylist = Playlist(
            id = "favorites",
            name = "我喜欢的音乐",
            coverUrl = null,
            songs = emptyList()
        )
        
        // 歌单列表 = "我喜欢的音乐" + 用户创建的歌单
        _uiState.update {
            it.copy(playlists = listOf(favoritesPlaylist) + savedPlaylists)
        }
        
        // 加载收藏歌曲到"我喜欢的音乐"歌单
        viewModelScope.launch {
            val savedFavorites = preferencesManager.getFavorites().map { it.toSong() }
            _uiState.update { state ->
                state.copy(
                    playlists = state.playlists.map { p ->
                        if (p.id == "favorites") p.copy(songs = savedFavorites) else p
                    }
                )
            }
        }
    }

    private fun savePlaylists() {
        // 只保存非收藏歌单
        val nonFavoritePlaylists = _uiState.value.playlists.filter { it.id != "favorites" }
        val playlistDataList = nonFavoritePlaylists.map { it.toPlaylistData() }
        preferencesManager.saveLocalPlaylists(playlistDataList)
    }

    private fun saveFavorites() {
        val favoriteData = _uiState.value.favorites.map { it.toSongData() }
        preferencesManager.saveFavorites(favoriteData)
    }

    // ============ 歌单管理 ============

    fun createPlaylist(name: String, songs: List<Song> = emptyList()) {
        val newPlaylist = Playlist(
            id = "local_${System.currentTimeMillis()}",
            name = name,
            coverUrl = songs.firstOrNull()?.coverUrl,
            songs = songs.distinctBy { it.id }  // 去重
        )
        
        _uiState.update { state ->
            state.copy(playlists = state.playlists + newPlaylist)
        }
        savePlaylists()
    }

    fun deletePlaylist(playlistId: String) {
        if (playlistId == "favorites") return // 不能删除收藏歌单
        
        _uiState.update { state ->
            state.copy(playlists = state.playlists.filter { it.id != playlistId })
        }
        savePlaylists()
    }

    // ============ 歌曲管理（防重复） ============

    /**
     * 添加歌曲到歌单（如果已存在则不添加）
     */
    fun addToPlaylist(playlistId: String, song: Song) {
        _uiState.update { state ->
            state.copy(
                playlists = state.playlists.map { playlist ->
                    if (playlist.id == playlistId) {
                        // 检查歌曲是否已存在
                        if (playlist.songs.any { it.id == song.id }) {
                            playlist // 已存在，不重复添加
                        } else {
                            playlist.copy(songs = playlist.songs + song)
                        }
                    } else {
                        playlist
                    }
                }
            )
        }
        
        // 保存
        if (playlistId == "favorites") {
            saveFavorites()
        } else {
            savePlaylists()
        }
    }
    
    /**
     * 批量添加歌曲到歌单（去重）
     */
    fun addSongsToPlaylist(playlistId: String, songs: List<Song>) {
        _uiState.update { state ->
            state.copy(
                playlists = state.playlists.map { playlist ->
                    if (playlist.id == playlistId) {
                        val existingIds = playlist.songs.map { it.id }.toSet()
                        val newSongs = songs.filter { it.id !in existingIds }
                        playlist.copy(songs = playlist.songs + newSongs)
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

    /**
     * 从歌单移除歌曲
     */
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
        
        if (playlistId == "favorites") {
            saveFavorites()
        } else {
            savePlaylists()
        }
    }

    // ============ 收藏/我喜欢管理 ============
    
    /**
     * 添加到收藏（快捷方法）
     */
    fun addToFavorites(song: Song) {
        addToPlaylist("favorites", song)
    }

    /**
     * 从收藏移除（快捷方法）
     */
    fun removeFromFavorites(songId: String) {
        removeFromPlaylist("favorites", songId)
    }

    /**
     * 切换收藏状态
     */
    fun toggleFavorite(song: Song) {
        if (isFavorite(song.id)) {
            removeFromFavorites(song.id)
        } else {
            addToFavorites(song)
        }
    }

    /**
     * 检查是否已收藏
     */
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
