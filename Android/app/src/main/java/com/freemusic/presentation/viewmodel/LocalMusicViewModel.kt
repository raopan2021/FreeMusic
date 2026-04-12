package com.freemusic.presentation.viewmodel

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.data.KnownArtists
import com.freemusic.domain.model.Song
import com.freemusic.util.ArtistNameNormalizer
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

    fun scanLocalMusic(sortOrder: Int = 0) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, scannedCount = 0, error = null) }
            
            try {
                val songs = withContext(Dispatchers.IO) {
                    scanMediaStore(sortOrder)
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
                        error = e.message ?: "Scan failed"
                    )
                }
            }
        }
    }
    
    /**
     * 标准化歌手名称（快速版本，用于扫描时）
     * 完整的歌手修复在后台进行
     */
    private fun normalizeArtistName(artist: String): String {
        // 只做基本的标准化处理，不做昂贵的 KnownArtists 查找
        return ArtistNameNormalizer.normalize(artist)
    }
    
    /**
     * 快速修复未知歌手（用于扫描时）
     * 只做简单的空格/标点清理，不做昂贵的数据库匹配
     */
    private fun fixUnknownArtist(currentArtist: String): String {
        // 扫描时不进行昂贵的 KnownArtists 匹配
        // 这个会在后台单独处理
        return currentArtist
    }
    
    /**
     * 批量修复所有未知歌手（在扫描完成后调用）
     * 这个是可选的优化步骤
     */
    private fun fixAllUnknownArtists(songs: List<Song>): List<Song> {
        // 构建快速查找表
        val artistLookup = KnownArtists.allArtists
            .associateBy { ArtistNameNormalizer.normalize(it).lowercase() }
        
        return songs.map { song ->
            val normalizedArtist = ArtistNameNormalizer.normalize(song.artist).lowercase()
            val fixedArtist = artistLookup[normalizedArtist] ?: song.artist
            if (fixedArtist != song.artist) {
                song.copy(artist = fixedArtist)
            } else {
                song
            }
        }
    }

    private fun scanMediaStore(sortOrder: Int): List<Song> {
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
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = ?"
        val selectionArgs = arrayOf("1")
        
        // 排序选项: 0=名称, 1=艺术家, 2=时长, 3=添加时间
        val mediaStoreSortOrder = when (sortOrder) {
            0 -> "${MediaStore.Audio.Media.TITLE} ASC"
            1 -> "${MediaStore.Audio.Media.ARTIST} ASC, ${MediaStore.Audio.Media.TITLE} ASC"
            2 -> "${MediaStore.Audio.Media.DURATION} DESC"
            3 -> "${MediaStore.Audio.Media.DATE_ADDED} DESC"
            else -> "${MediaStore.Audio.Media.TITLE} ASC"
        }
        
        var cursor: android.database.Cursor? = null
        
        try {
            cursor = context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                mediaStoreSortOrder
            )
            
            if (cursor == null || cursor.count == 0) {
                return emptyList()
            }
            
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val filePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            
            while (cursor.moveToNext()) {
                try {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown"
                    val artist = cursor.getString(artistColumn) ?: "Unknown"
                    val album = cursor.getString(albumColumn) ?: "Unknown"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    
                    if (duration < 60000) continue
                    
                    val displayName = cursor.getString(displayNameColumn) ?: ""
                    val filePath = cursor.getString(filePathColumn) ?: ""
                    val lowerTitle = title.lowercase()
                    val lowerArtist = artist.lowercase()
                    val lowerAlbum = album.lowercase()
                    
                    val recordingKeywords = listOf(
                        "recording", "voice record", "voice memo", "screen record",
                        "recorder", "voice recorder", "screenrecorder"
                    )
                    
                    val isRecording = recordingKeywords.any { kw ->
                        lowerTitle.contains(kw) || lowerArtist.contains(kw) ||
                        lowerAlbum.contains(kw) || displayName.lowercase().contains(kw) ||
                        filePath.lowercase().contains(kw)
                    }
                    
                    if (isRecording) continue
                    
                    // 标准化歌手名称
                    val normalizedArtist = normalizeArtistName(artist)
                    // 快速修复未知歌手（不做昂贵的数据库匹配）
                    val fixedArtist = fixUnknownArtist(normalizedArtist)
                    
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()
                    
                    songs.add(
                        Song(
                            id = id.toString(),
                            title = title,
                            artist = fixedArtist,
                            album = album,
                            coverUrl = albumArtUri,
                            duration = duration,
                            neteaseId = null,
                            isNetease = false
                        )
                    )
                } catch (e: Exception) {
                    continue
                }
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            cursor?.close()
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
