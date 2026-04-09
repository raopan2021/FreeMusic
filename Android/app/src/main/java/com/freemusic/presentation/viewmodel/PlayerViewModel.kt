package com.freemusic.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
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
    val currentIndex: Int = 0
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSongWithUrlUseCase: GetSongWithUrlUseCase,
    private val getLyricsUseCase: GetLyricsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var mediaController: MediaController? = null
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
        }, MoreExecutors.directExecutor())
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

    fun playSong(song: Song) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getSongWithUrlUseCase(song.id).collect { result ->
                result.fold(
                    onSuccess = { songWithUrl ->
                        playMediaItem(songWithUrl)
                        loadLyrics(song.id)
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
        }
    }

    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return

        _uiState.update { it.copy(playlist = songs, currentIndex = startIndex) }
        playSong(songs[startIndex])
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
                        .setArtworkUri(songWithUrl.song.coverUrl?.let { android.net.Uri.parse(it) })
                        .build()
                )
                .build()

            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
    }

    private fun loadLyrics(songId: String) {
        viewModelScope.launch {
            getLyricsUseCase(songId).collect { result ->
                result.fold(
                    onSuccess = { lyrics ->
                        _uiState.update { it.copy(lyrics = lyrics) }
                    },
                    onFailure = {
                        // 歌词加载失败不显示错误，静默忽略
                        _uiState.update { it.copy(lyrics = null) }
                    }
                )
            }
        }
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
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

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
        updateCurrentIndex()
    }

    fun skipToPrevious() {
        mediaController?.seekToPreviousMediaItem()
        updateCurrentIndex()
    }

    private fun updateCurrentIndex() {
        mediaController?.let { controller ->
            val newIndex = controller.currentMediaItemIndex
            val playlist = _uiState.value.playlist
            if (newIndex in playlist.indices) {
                val newSong = playlist[newIndex]
                _uiState.update { it.copy(currentIndex = newIndex, currentSong = newSong) }
                loadLyrics(newSong.id)
            }
        }
    }

    // 定期更新播放进度
    fun startProgressUpdates() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                updatePlayerState()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
    }
}
