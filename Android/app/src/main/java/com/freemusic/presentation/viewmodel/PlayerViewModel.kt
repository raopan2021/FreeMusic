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
import com.freemusic.data.preferences.PreferencesManager
import com.freemusic.domain.usecase.GetLyricsUseCase
import com.freemusic.domain.usecase.GetSongWithUrlUseCase
import com.freemusic.domain.usecase.SearchAlbumCoverUseCase
import com.freemusic.service.PlaybackService
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.freemusic.presentation.ui.player.controls.PlayRepeatMode

/**
 * 播放队列中的单曲项目
 */
data class QueueItem(
    val song: Song
)

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L, // 毫秒
    val duration: Long = 0L, // 毫秒
    val lyrics: Lyrics? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val playlist: List<Song> = emptyList(),
    val queueItems: List<QueueItem> = emptyList(),  // 播放队列（包含播放次数）
    val currentIndex: Int = 0,
    val isFavorite: Boolean = false,
    val repeatMode: PlayRepeatMode = PlayRepeatMode.OFF,
    val isShuffleEnabled: Boolean = false,
    val sleepTimerRemainingSeconds: Long = 0L  // 睡眠定时剩余秒数，0表示未设置
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSongWithUrlUseCase: GetSongWithUrlUseCase,
    private val getLyricsUseCase: GetLyricsUseCase,
    private val searchAlbumCoverUseCase: SearchAlbumCoverUseCase,
    private val localDataSource: LocalDataSource,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaController: MediaController? = null
    private var isMediaControllerReady = false
    private var playbackSpeed: Float = 1.0f
    
    // 睡眠定时器结束时间（毫秒）
    private var sleepTimerEndTimeMillis: Long = 0L
    
    // 待播放的本地歌曲队列（等待 mediaController 就绪）
    private val pendingLocalSongs = mutableListOf<Song>()
    private var pendingExternalUri: Uri? = null
    
    // 歌词缓存（避免重复搜索）
    private val lyricsCache = mutableMapOf<String, Lyrics>()
    
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlayerState()
            updateRepeatShuffleState()
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
        
        // 观察播放速度变化
        viewModelScope.launch {
            preferencesManager.playbackSpeed.collect { speed ->
                playbackSpeed = speed
                mediaController?.setPlaybackSpeed(speed)
            }
        }
        
        // 定期更新播放进度（解决进度条不实时更新问题）
        viewModelScope.launch {
            while (true) {
                delay(500)
                mediaController?.let { controller ->
                    _uiState.update { state ->
                        state.copy(currentPosition = controller.currentPosition)
                    }
                }
            }
        }

        // 观察睡眠定时器
        viewModelScope.launch {
            preferencesManager.sleepTimerMinutes.collect { minutes ->
                sleepTimerEndTimeMillis = if (minutes > 0) {
                    System.currentTimeMillis() + minutes * 60 * 1000L
                } else {
                    0L
                }
            }
        }
        
        // 检查睡眠定时器
        viewModelScope.launch {
            while (true) {
                delay(1000) // 每秒检查一次
                if (sleepTimerEndTimeMillis > 0) {
                    val remainingMs = sleepTimerEndTimeMillis - System.currentTimeMillis()
                    if (remainingMs <= 0) {
                        mediaController?.pause()
                        sleepTimerEndTimeMillis = 0L
                        preferencesManager.setSleepTimer(0)
                        _uiState.update { it.copy(sleepTimerRemainingSeconds = 0L) }
                    } else {
                        val remainingSeconds = (remainingMs / 1000).toLong()
                        _uiState.update { it.copy(sleepTimerRemainingSeconds = remainingSeconds) }
                    }
                } else {
                    _uiState.update { it.copy(sleepTimerRemainingSeconds = 0L) }
                }
            }
        }
    }

    fun setSleepTimer(minutes: Int) {
        preferencesManager.setSleepTimer(minutes)
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )

        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                mediaController?.addListener(playerListener)
                isMediaControllerReady = true
                
                // 应用保存的播放速度
                mediaController?.setPlaybackSpeed(preferencesManager.playbackSpeed.value)
                
                // 初始化重复和随机状态
                updateRepeatShuffleState()
                
                // MediaController 就绪后，播放待播放的歌曲
                processPendingPlaylists()
            } catch (e: Exception) {
                // 如果获取失败，尝试重连
                viewModelScope.launch {
                    delay(1000)
                    isMediaControllerReady = false
                    initializeMediaController()
                }
            }
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

    private fun updateRepeatShuffleState() {
        mediaController?.let { controller ->
            val repeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_ONE -> PlayRepeatMode.ONE
                Player.REPEAT_MODE_ALL -> PlayRepeatMode.ALL
                else -> PlayRepeatMode.OFF
            }
            _uiState.update { state ->
                state.copy(
                    repeatMode = repeatMode,
                    isShuffleEnabled = controller.shuffleModeEnabled
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

    fun playSong(song: Song, playlist: List<Song>? = null) {
        viewModelScope.launch {
            // 清除旧歌词，避免显示上一首歌的歌词
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    error = null,
                    lyrics = null,  // 清除旧歌词
                    playlist = playlist ?: listOf(song),  // 使用提供的歌单，或仅当前歌曲
                    queueItems = (playlist ?: listOf(song)).map { QueueItem(it) },  // 转换为队列项目
                    currentIndex = playlist?.indexOf(song) ?: 0
                ) 
            }
            
            // 记录播放历史
            try {
                localDataSource.addToPlayHistory(song)
            } catch (e: Exception) {
                // Ignore history errors
            }
            
            // 保存原始歌单用于后续切换上下首
            val originalPlaylist = playlist ?: listOf(song)
            
            // 判断是本地歌曲还是网易云歌曲
            if (song.isNetease && song.neteaseId != null) {
                // 网易云歌曲 - 需要获取播放链接
                try {
                    getSongWithUrlUseCase(song.id).firstOrNull()?.let { result ->
                        result.fold(
                            onSuccess = { songWithUrl ->
                                playMediaItem(songWithUrl)
                                loadLyrics(songWithUrl.song)
                                // 自动搜索封面（如果没有封面）
                                loadAlbumCover(songWithUrl.song)
                                observeFavoriteStatus(songWithUrl.song.id)
                                _uiState.update { state ->
                                    state.copy(
                                        currentSong = songWithUrl.song,
                                        isLoading = false,
                                        playlist = originalPlaylist,
                                        currentIndex = originalPlaylist.indexOf(song).coerceAtLeast(0)
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
                    } ?: run {
                        // flow 为空或没有发射
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "播放失败"
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "播放失败"
                        )
                    }
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
    private fun playLocalSong(song: Song, playlist: List<Song>? = null) {
        val uri = buildContentUri(song.id)
        
        // 更新播放列表（如果提供了新的播放列表）
        playlist?.let { newPlaylist ->
            _uiState.update { state ->
                state.copy(
                    playlist = newPlaylist,
                    currentIndex = newPlaylist.indexOf(song).coerceAtLeast(0)
                )
            }
        }
        
        // 如果 mediaController 已就绪，立即播放
        if (isMediaControllerReady && mediaController != null) {
            playLocalSongInternal(uri, song)
            return
        }
        
        // 加入待播放队列
        pendingLocalSongs.add(song)
        
        // 如果 mediaController 还没准备好，等待它就绪
        viewModelScope.launch {
            // 等待最多 10 秒
            var retryCount = 0
            val maxRetries = 40 // 40 * 250ms = 10 秒
            
            while (!isMediaControllerReady && retryCount < maxRetries) {
                delay(250)
                retryCount++
            }
            
            if (isMediaControllerReady && pendingLocalSongs.contains(song)) {
                pendingLocalSongs.remove(song)
                val currentUri = buildContentUri(song.id)
                playLocalSongInternal(currentUri, song)
            }
        }
    }
    
    /**
     * 内部方法：实际执行本地歌曲播放
     */
    private fun playLocalSongInternal(uri: Uri, song: Song) {
        val playlist = _uiState.value.playlist
        val currentIndex = _uiState.value.currentIndex
        
        mediaController?.let { controller ->
            // 将整个播放列表转换为 MediaItems
            val mediaItems = playlist.map { s ->
                val contentUri = buildContentUri(s.id)
                MediaItem.Builder()
                    .setMediaId(s.id)
                    .setUri(contentUri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(s.title)
                            .setArtist(s.artist)
                            .setAlbumTitle(s.album)
                            .setArtworkUri(s.coverUrl?.let { Uri.parse(it) })
                            .build()
                    )
                    .build()
            }
            
            controller.setMediaItems(mediaItems, currentIndex, 0)
            controller.prepare()
            controller.setPlaybackSpeed(playbackSpeed)
            controller.play()
            
            // 加载歌词
            loadLyrics(song)
            
            // 自动搜索封面（如果没有封面）
            loadAlbumCover(song)
            
            // 观察收藏状态
            observeFavoriteStatus(song.id)
            
            _uiState.update { state ->
                state.copy(
                    currentSong = song,
                    isLoading = false
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
            val song = playlist[index]
            _uiState.update { it.copy(currentIndex = index, currentSong = song) }
            // 重新设置播放列表（用于 MediaController）
            if (song.isNetease && song.neteaseId != null) {
                // 网络歌曲需要获取播放链接
                playSong(song, playlist)
            } else {
                // 本地歌曲直接播放，传递完整播放列表
                playLocalSong(song, playlist)
            }
        }
    }
    
    /**
     * 从队列中删除一首歌
     */
    fun removeFromQueue(index: Int) {
        val queueItems = _uiState.value.queueItems.toMutableList()
        if (index in queueItems.indices) {
            queueItems.removeAt(index)
            _uiState.update { 
                it.copy(
                    queueItems = queueItems,
                    playlist = queueItems.map { item -> item.song },
                    currentIndex = it.currentIndex.coerceIn(0, (queueItems.size - 1).coerceAtLeast(0))
                )
            }
        }
    }
    
    /**
     * 移动队列中的歌曲顺序
     */
    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val queueItems = _uiState.value.queueItems.toMutableList()
        if (fromIndex in queueItems.indices && toIndex in queueItems.indices) {
            val item = queueItems.removeAt(fromIndex)
            queueItems.add(toIndex, item)
            _uiState.update { 
                it.copy(
                    queueItems = queueItems,
                    playlist = queueItems.map { q -> q.song },
                    currentIndex = if (it.currentIndex == fromIndex) toIndex else it.currentIndex
                )
            }
        }
    }
    
    /**
     * 清空播放队列
     */
    fun clearQueue() {
        _uiState.update { 
            it.copy(
                queueItems = emptyList(),
                playlist = emptyList(),
                currentIndex = 0
            )
        }
    }

    private fun playMediaItem(songWithUrl: SongWithUrl) {
        val playlist = _uiState.value.playlist
        val currentIndex = _uiState.value.currentIndex
        
        mediaController?.let { controller ->
            // 将整个播放列表转换为 MediaItems
            val mediaItems = playlist.map { song ->
                if (song.id == songWithUrl.song.id) {
                    // 当前歌曲使用提供的 URL
                    MediaItem.Builder()
                        .setMediaId(song.id)
                        .setUri(songWithUrl.url)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .setAlbumTitle(song.album)
                                .setArtworkUri(song.coverUrl?.let { Uri.parse(it) })
                                .build()
                        )
                        .build()
                } else {
                    // 播放列表中的其他歌曲使用占位符，之后再更新
                    MediaItem.Builder()
                        .setMediaId(song.id)
                        .setUri("placeholder:${song.id}")
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .setAlbumTitle(song.album)
                                .setArtworkUri(song.coverUrl?.let { Uri.parse(it) })
                                .build()
                        )
                        .build()
                }
            }
            
            controller.setMediaItems(mediaItems, currentIndex, 0)
            controller.prepare()
            controller.setPlaybackSpeed(playbackSpeed)
            controller.play()
        } ?: run {
            // MediaController 还没准备好，等待后重试
            viewModelScope.launch {
                var retryCount = 0
                while (mediaController == null && retryCount < 20) {
                    delay(250)
                    retryCount++
                }
                mediaController?.let { ctrl ->
                    val mediaItems = playlist.map { song ->
                        if (song.id == songWithUrl.song.id) {
                            MediaItem.Builder()
                                .setMediaId(song.id)
                                .setUri(songWithUrl.url)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(song.title)
                                        .setArtist(song.artist)
                                        .setAlbumTitle(song.album)
                                        .setArtworkUri(song.coverUrl?.let { Uri.parse(it) })
                                        .build()
                                )
                                .build()
                        } else {
                            MediaItem.Builder()
                                .setMediaId(song.id)
                                .setUri("placeholder:${song.id}")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(song.title)
                                        .setArtist(song.artist)
                                        .setAlbumTitle(song.album)
                                        .setArtworkUri(song.coverUrl?.let { Uri.parse(it) })
                                        .build()
                                )
                                .build()
                        }
                    }
                    
                    ctrl.setMediaItems(mediaItems, currentIndex, 0)
                    ctrl.prepare()
                    ctrl.setPlaybackSpeed(playbackSpeed)
                    ctrl.play()
                }
            }
        }
    }

    private fun loadLyrics(song: Song) {
        // 检查缓存
        val cacheKey = "${song.id}_${song.title}_${song.artist}"
        lyricsCache[cacheKey]?.let { cachedLyrics ->
            _uiState.update { it.copy(lyrics = cachedLyrics) }
            return
        }
        
        viewModelScope.launch {
            try {
                getLyricsUseCase(song).firstOrNull()?.let { result ->
                    result.onSuccess { lyrics ->
                        // 缓存歌词
                        lyricsCache[cacheKey] = lyrics
                        _uiState.update { it.copy(lyrics = lyrics) }
                    }
                }
            } catch (e: Exception) {
                // Ignore lyrics errors
            }
        }
    }

    /**
     * 自动搜索专辑封面（当没有封面时）
     */
    private fun loadAlbumCover(song: Song) {
        // 如果已经有封面，不需要搜索
        if (!song.coverUrl.isNullOrBlank() && !song.coverUrl!!.startsWith("content://media")) {
            return
        }
        
        viewModelScope.launch {
            try {
                val result = searchAlbumCoverUseCase(song.artist, song.title)
                result.onSuccess { coverUrl ->
                    // 更新当前歌曲的封面 URL
                    _uiState.update { state ->
                        state.copy(
                            currentSong = state.currentSong?.copy(coverUrl = coverUrl)
                        )
                    }
                }
            } catch (e: Exception) {
                // 忽略封面搜索错误
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

    fun toggleRepeatMode() {
        mediaController?.let { controller ->
            controller.repeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            // 同步更新 UI 状态
            updateRepeatShuffleState()
        }
    }

    fun toggleShuffle() {
        mediaController?.let { controller ->
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
            // 同步更新 UI 状态
            updateRepeatShuffleState()
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = speed
        mediaController?.let { controller ->
            controller.setPlaybackSpeed(speed)
        }
    }

    fun skipToNext() {
        mediaController?.let { controller ->
            if (controller.hasNextMediaItem()) {
                controller.seekToNext()
                // 确保下一首自动播放
                if (!controller.isPlaying) {
                    controller.play()
                }
            }
        }
    }

    fun skipToPrevious() {
        mediaController?.let { controller ->
            if (controller.currentPosition > 3000) {
                controller.seekTo(0)
            } else if (controller.hasPreviousMediaItem()) {
                controller.seekToPrevious()
                // 确保上一首自动播放
                if (!controller.isPlaying) {
                    controller.play()
                }
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
                val newSong = playlist[index]
                _uiState.update { 
                    it.copy(
                        currentIndex = index,
                        currentSong = newSong,
                        lyrics = null  // 清除旧歌词，等新歌词加载
                    )
                }
                // 加载新歌曲的歌词
                loadLyrics(newSong)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
    }
}
