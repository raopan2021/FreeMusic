package com.freemusic.presentation.viewmodel

import android.app.Application
import android.content.ContentUris
import android.net.Uri
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
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LocalMusicUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val scannedCount: Int = 0,
    val totalDuration: String = "0:00",
    val error: String? = null
)

@HiltViewModel
class LocalMusicViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LocalMusicUiState())
    val uiState: StateFlow<LocalMusicUiState> = _uiState.asStateFlow()

    fun scanLocalMusic() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, scannedCount = 0, error = null) }
            
            try {
                val songs = withContext(Dispatchers.IO) {
                    scanMediaStore()
                }
                
                val totalMs = songs.sumOf { it.duration }
                val duration = formatDuration(totalMs)
                
                _uiState.update {
                    it.copy(
                        songs = songs,
                        isLoading = false,
                        scannedCount = songs.size,
                        totalDuration = duration
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "扫描失败"
                    )
                }
            }
        }
    }

    private fun scanMediaStore(): List<Song> {
        val songs = mutableListOf<Song>()
        val context = getApplication<Application>()
        
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
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                if (cursor.count == 0) {
                    return emptyList()
                }
                
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                
                while (cursor.moveToNext()) {
                    try {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn) ?: "未知标题"
                        val artist = cursor.getString(artistColumn) ?: "未知艺术家"
                        val album = cursor.getString(albumColumn) ?: "未知专辑"
                        val albumId = cursor.getLong(albumIdColumn)
                        val duration = cursor.getLong(durationColumn)
                        
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        
                        // 获取专辑封面
                        val albumArtUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                        
                        songs.add(
                            Song(
                                id = id.toString(),
                                title = title,
                                artist = artist,
                                album = album,
                                coverUrl = albumArtUri,
                                duration = duration,
                                neteaseId = null,
                                isNetease = false
                            )
                        )
                    } catch (e: Exception) {
                        // Skip this song if there's an error
                        continue
                    }
                }
            }
        } catch (e: Exception) {
            // Return empty list on error
        }
        
        return songs
    }

    private fun formatDuration(totalMs: Long): String {
        val totalSeconds = totalMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }
}
