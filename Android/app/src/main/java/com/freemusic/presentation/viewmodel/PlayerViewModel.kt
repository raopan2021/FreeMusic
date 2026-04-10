package com.freemusic.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.freemusic.domain.model.Lyrics
import com.freemusic.domain.model.Song
import com.freemusic.domain.model.SongWithUrl
import com.freemusic.data.local.LocalDataSource
import com.freemusic.domain.usecase.GetLyricsUseCase
import com.freemusic.domain.usecase.GetSongWithUrlUseCase
import com.freemusic.service.PlaybackService
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L, // 毫秒
    val duration: Long = 0L, // 毫秒
    val lyrics: Lyrics? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val isFavorite: Boolean = false
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSongWithUrlUseCase: GetSongWithUrlUseCase,
    private val getLyricsUseCase: GetLyricsUseCase,
    private val localDataSource: LocalDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaController: MediaController? = null
    private var isMediaControllerReady = false
    
    // 待播放的本地歌曲队列（等待 mediaController 就绪）
    private val pendingLocalSongs = mutableListOf<Song>()
    private var pendingExternalUri: Uri? = null
    
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayerState()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlayerState()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateCurrentIndex()
        }
    }

    init {
        initializeMediaController()
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )

        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(playerListener)
            isMediaControllerReady = true
            
            // MediaController 就绪后，播放待播放的歌曲
            processPendingPlaylists()
        }, MoreExecutors.directExecutor())
    }
    
    /**
     * 处理待播放的队列
     */
    private fun processPendingPlaylists() {
        // 处理待播放的外部 URI
        pendingExternalUri?.let { uri ->
            val song = Song(
                id = uri.toString(),
                title = "外部文件",
                artist = "未知艺术家",
                album = "本地音乐",
                coverUrl = null,
                duration = 0,
                neteaseId = null,
                isNetease = false
            )
            playLocalSongInternal(uri, song)
            pendingExternalUri = null
        }
        
        // 处理待播放的本地歌曲
        if (pendingLocalSongs.isNotEmpty()) {
            val song = pendingLocalSongs.removeAt(0)
            val uri = buildContentUri(song.id)
            playLocalSongInternal(uri, song)
        }
    }

    private fun updatePlayerState() {
        mediaController?.let { controller ->
            _uiState.update { state ->
                state.copy(
                    isPlaying = controller.isPlaying,
                    currentPosition = controller.currentPosition,
                    duration = controller.duration.coerceAtLeast(0)
                )
            }
        }
    }
    
    /**
     * 构建 MediaStore Content URI
     */
    private fun buildContentUri(id: String): Uri {
        return when {
            id.startsWith("content://") -> Uri.parse(id)
            id.startsWith("file://") -> Uri.parse(id)
            id.all { it.isDigit() } -> {
                android.content.ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id.toLongOrNull() ?: 0L
                )
            }
            else -> Uri.parse("file://$id")
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 记录播放历史
            try {
                localDataSource.addToPlayHistory(song)
            } catch (e: Exception) {
                // Ignore history errors
            }
            
            // 判断是本地歌曲还是网易云歌曲
            if (song.isNetease && song.neteaseId != null) {
                // 网易云歌曲 - 需要获取播放链接
                getSongWithUrlUseCase(song.id).collect { result ->
                    result.fold(
                        onSuccess = { songWithUrl ->
                            playMediaItem(songWithUrl)
                            loadLyrics(songWithUrl.song)
                            observeFavoriteStatus(songWithUrl.song.id)
                            _uiState.update { state ->
                                state.copy(
                                    currentSong = songWithUrl.song,
                                    isLoading = false,
                                    playlist = listOf(songWithUrl.song),
                                    currentIndex = 0
                                )
                            }
                        },
                        onFailure = { exception ->
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = exception.message ?: "播放失败"
                                )
                            }
                        }
                    )
                }
            } else {
                // 本地歌曲 - 直接使用 Content URI 播放
                playLocalSong(song)
            }
        }
    }
    
    /**
     * 播放本地歌曲
     */
    private fun playLocalSong(song: Song) {
        val uri = buildContentUri(song.id)
        
        if (isMediaControllerReady && mediaController != null) {
            playLocalSongInternal(uri, song)
        } else {
            // 加入待播放队列
            pendingLocalSongs.add(song)
            
            // 如果 mediaController 还没准备好，等待它就绪
            if (!isMediaControllerReady) {
                viewModelScope.launch {
                    var retryCount = 0
                    while (!isMediaControllerReady && retryCount < 20) {
                        delay(250)
                        retryCount++
                    }
                    if (isMediaControllerReady) {
                        processPendingPlaylists()
                    }
                }
            }
        }
    }
    
    /**
     * 内部方法：实际执行本地歌曲播放
     */
    private fun playLocalSongInternal(uri: Uri, song: Song) {
        mediaController?.let { controller ->
            val mediaItem = MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.coverUrl?.let { Uri.parse(it) })
                        .build()
                )
                .build()

            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
            
            _uiState.update { state ->
                state.copy(
                    currentSong = song,
                    isLoading = false,
                    playlist = listOf(song),
                    currentIndex = 0
                )
            }
        }
    }
    
    /**
     * 从外部 URI 播放（如从文件管理器打开 mp3）
     */
    fun playFromExternalUri(uri: Uri, title: String = "外部文件") {
        val song = Song(
            id = uri.toString(),
            title = title,
            artist = "未知艺术家",
            album = "本地音乐",
            coverUrl = null,
            duration = 0,
            neteaseId = null,
            isNetease = false
        )
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 记录播放历史
            try {
                localDataSource.addToPlayHistory(song)
            } catch (e: Exception) {
                // Ignore history errors
            }
            
            if (isMediaControllerReady && mediaController != null) {
                playLocalSongInternal(uri, song)
            } else {
                // 保存待播放的 URI，等待 MediaController 就绪
                pendingExternalUri = uri
                
                // 如果 mediaController 还没准备好，等待它就绪
                if (!isMediaControllerReady) {
                    var retryCount = 0
                    while (!isMediaControllerReady && retryCount < 20) {
                        delay(250)
                        retryCount++
                    }
                    if (isMediaControllerReady) {
                        processPendingPlaylists()
                    }
                }
            }
        }
    }

    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return

        _uiState.update { it.copy(playlist = songs, currentIndex = startIndex) }
        playSong(songs[startIndex])
    }

    fun playFromQueue(index: Int) {
        val playlist = _uiState.value.playlist
        if (index in playlist.indices) {
            _uiState.update { it.copy(currentIndex = index) }
            playSong(playlist[index])
        }
    }

    private fun playMediaItem(songWithUrl: SongWithUrl) {
        mediaController?.let { controller ->
            val mediaItem = MediaItem.Builder()
                .setMediaId(songWithUrl.song.id)
                .setUri(songWithUrl.url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(songWithUrl.song.title)
                        .setArtist(songWithUrl.song.artist)
                        .setAlbumTitle(songWithUrl.song.album)
                        .setArtworkUri(songWithUrl.song.coverUrl?.let { Uri.parse(it) })
                        .build()
                )
                .build()

            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        } ?: run {
            // MediaController 还没准备好，等待后重试
            viewModelScope.launch {
                var retryCount = 0
                while (mediaController == null && retryCount < 20) {
                    delay(250)
                    retryCount++
                }
                mediaController?.let { controller ->
                    val mediaItem = MediaItem.Builder()
                        .setMediaId(songWithUrl.song.id)
                        .setUri(songWithUrl.url)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(songWithUrl.song.title)
                                .setArtist(songWithUrl.song.artist)
                                .setAlbumTitle(songWithUrl.song.album)
                                .setArtworkUri(songWithUrl.song.coverUrl?.let { Uri.parse(it) })
                                .build()
                        )
                        .build()

                    controller.setMediaItem(mediaItem)
                    controller.prepare()
                    controller.play()
                }
            }
        }
    }

    private fun loadLyrics(song: Song) {
        if (song.neteaseId == null) return
        
        viewModelScope.launch {
            try {
                getLyricsUseCase(song).collect { result ->
                    result.onSuccess { lyrics ->
                        _uiState.update { it.copy(lyrics = lyrics) }
                    }
                }
            } catch (e: Exception) {
                // Ignore lyrics errors
            }
        }
    }

    private fun observeFavoriteStatus(songId: String) {
        viewModelScope.launch {
            try {
                localDataSource.isFavorite(songId).collect { isFav ->
                    _uiState.update { it.copy(isFavorite = isFav) }
                }
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }

    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }

    fun skipToNext() {
        mediaController?.let { controller ->
            if (controller.hasNextMediaItem()) {
                controller.seekToNext()
            }
        }
    }

    fun skipToPrevious() {
        mediaController?.let { controller ->
            if (controller.currentPosition > 3000) {
                controller.seekTo(0)
            } else if (controller.hasPreviousMediaItem()) {
                controller.seekToPrevious()
            } else {
                controller.seekTo(0)
            }
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }
    
    fun toggleFavorite() {
        val currentSong = _uiState.value.currentSong ?: return
        val isFavorite = _uiState.value.isFavorite
        setFavorite(currentSong.id, !isFavorite)
    }
    
    fun startProgressUpdates() {
        // 进度更新由 Player.Listener 的 onIsPlayingChanged 和 onPlaybackStateChanged 处理
        // 这里不需要额外的实现
    }

    fun setFavorite(songId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                if (isFavorite) {
                    // 需要获取完整的 Song 对象才能添加收藏
                    // 这里暂时不做任何操作，因为 setFavorite 通常需要完整的 Song 对象
                    val currentSong = _uiState.value.currentSong
                    if (currentSong != null) {
                        localDataSource.addFavorite(currentSong)
                    }
                } else {
                    localDataSource.removeFavorite(songId)
                }
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }

    private fun updateCurrentIndex() {
        mediaController?.let { controller ->
            val playlist = _uiState.value.playlist
            val mediaId = controller.currentMediaItem?.mediaId
            val index = playlist.indexOfFirst { it.id == mediaId }
            if (index >= 0) {
                _uiState.update { it.copy(currentIndex = index) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
    }
}
