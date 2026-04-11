package com.freemusic.presentation.viewmodel

import android.app.Application
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtistBrowserUiState(
    val artists: List<Pair<String, Int>> = emptyList(),  // (artistName, songCount)
    val selectedArtist: String? = null,
    val artistSongs: List<Song> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ArtistBrowserViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(ArtistBrowserUiState())
    val uiState: StateFlow<ArtistBrowserUiState> = _uiState.asStateFlow()
    
    private var allSongs = listOf<Song>()
    
    fun loadArtists() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            
            allSongs = emptyList()
            
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
            )
            
            val selection = "${MediaStore.Audio.Media.DURATION} >= ? AND ${MediaStore.Audio.Media.IS_MUSIC} = ?"
            val selectionArgs = arrayOf("60000", "1")
            val sortOrder = "${MediaStore.Audio.Media.ARTIST} ASC"
            
            getApplication<Application>().contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "未知标题"
                    val artist = cursor.getString(artistColumn) ?: "未知艺术家"
                    val album = cursor.getString(albumColumn) ?: "未知专辑"
                    val duration = cursor.getLong(durationColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    
                    // 过滤时长小于60秒
                    if (duration < 60000) continue
                    
                    val contentUri = android.content.ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    
                    val coverUrl = if (albumId > 0) {
                        android.content.ContentUris.withAppendedId(
                            android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            albumId
                        ).toString()
                    } else null
                    
                    allSongs = allSongs + Song(
                        id = contentUri.toString(),
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        coverUrl = coverUrl,
                        neteaseId = null,
                        isNetease = false
                    )
                }
            }
            
            // 按艺术家分组（使用规范化名称避免大小写/空格导致重复）
            val artistMap = allSongs.groupBy { song -> normalizeArtistName(song.artist) }
            
            val artists = artistMap
                .filter { (artist, _) -> artist.isNotBlank() && artist != "未知艺术家" }
                .map { (artist, songs) -> artist to songs.size }
                .sortedBy { it.first.lowercase() }
            
            _uiState.update { it.copy(artists = artists, isLoading = false) }
        }
    }
    
    fun selectArtist(artistName: String) {
        val normalizedName = normalizeArtistName(artistName)
        val songs = allSongs.filter { normalizeArtistName(it.artist) == normalizedName }.sortedBy { it.title }
        _uiState.update { it.copy(selectedArtist = artistName, artistSongs = songs) }
    }
    
    fun clearSelection() {
        _uiState.update { it.copy(selectedArtist = null, artistSongs = emptyList()) }
    }
    
    /**
     * 规范化艺术家名称，避免大小写/多余空格导致同一艺术家出现多次
     * 例如: "BEYOND", "Beyond", "beyond  " 都归一化为 "beyond"
     */
    private fun normalizeArtistName(name: String): String {
        return name
            .trim()
            .replace("\\s+".toRegex(), " ")
            .lowercase()
    }
}
