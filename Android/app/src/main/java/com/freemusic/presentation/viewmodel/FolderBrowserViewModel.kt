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

data class FolderBrowserUiState(
    val folders: List<Pair<String, Int>> = emptyList(),
    val currentPath: String? = null,
    val currentSongs: List<Song> = emptyList(),
    val isLoading: Boolean = false
)

// 内部使用：保存歌曲及其文件路径
private data class SongWithPath(
    val song: Song,
    val filePath: String
)

@HiltViewModel
class FolderBrowserViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(FolderBrowserUiState())
    val uiState: StateFlow<FolderBrowserUiState> = _uiState.asStateFlow()
    
    // 使用 SongWithPath 而不是直接用 Song，这样可以保存文件路径
    private val allSongsWithPath = mutableListOf<SongWithPath>()
    
    fun loadFolders() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            
            allSongsWithPath.clear()
            
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
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
            )
            
            val selection = "${MediaStore.Audio.Media.DURATION} >= ? AND ${MediaStore.Audio.Media.IS_MUSIC} = ?"
            val selectionArgs = arrayOf("60000", "1")
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
            
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
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "未知标题"
                    val artist = cursor.getString(artistColumn) ?: "未知艺术家"
                    val album = cursor.getString(albumColumn) ?: "未知专辑"
                    val duration = cursor.getLong(durationColumn)
                    val data = cursor.getString(dataColumn) ?: ""
                    val albumId = cursor.getLong(albumIdColumn)
                    
                    // 过滤时长小于60秒和包含"录音"的音频
                    if (duration < 60000) continue
                    if (data.contains("录音", ignoreCase = true)) continue
                    
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
                    
                    val song = Song(
                        id = contentUri.toString(),
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        coverUrl = coverUrl,
                        neteaseId = null,
                        isNetease = false
                    )
                    
                    allSongsWithPath.add(SongWithPath(song, data))
                }
            }
            
            // 按文件夹分组
            val folderMap = allSongsWithPath.groupBy { songWithPath ->
                val filePath = songWithPath.filePath
                // 获取文件夹路径
                val folderPath = filePath.substringBeforeLast("/", "")
                folderPath
            }
            
            val folders = folderMap
                .filter { (path, songs) -> 
                    // 过滤掉根目录
                    path.isNotEmpty() && 
                    // 过滤掉只有一个文件的文件夹（通常是系统文件）
                    songs.size >= 1
                }
                .map { (path, songs) -> path to songs.size }
                .sortedBy { it.first.substringAfterLast("/").lowercase() }
            
            _uiState.update { it.copy(folders = folders, isLoading = false) }
        }
    }
    
    fun enterFolder(path: String) {
        val songsInFolder = allSongsWithPath
            .filter { it.filePath.substringBeforeLast("/", "") == path }
            .map { it.song }
            .sortedBy { it.title }
        
        _uiState.update { it.copy(currentPath = path, currentSongs = songsInFolder) }
    }
    
    fun navigateUp() {
        val currentPath = _uiState.value.currentPath ?: return
        val parentPath = currentPath.substringBeforeLast("/", "")
        
        if (parentPath.isEmpty()) {
            _uiState.update { it.copy(currentPath = null, currentSongs = emptyList()) }
        } else {
            enterFolder(parentPath)
        }
    }
}
