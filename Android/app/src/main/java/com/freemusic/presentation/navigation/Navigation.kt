package com.freemusic.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.*
import com.freemusic.data.preferences.CoverStyleType
import com.freemusic.data.preferences.EqualizerPreset
import com.freemusic.presentation.ui.history.PlayHistoryScreen
import com.freemusic.presentation.ui.folder.FolderBrowserScreen
import com.freemusic.presentation.ui.artist.ArtistBrowserScreen
import com.freemusic.presentation.ui.album.AlbumBrowserScreen
import com.freemusic.presentation.ui.home.HomeScreen
import com.freemusic.presentation.ui.import.ImportScreen
import com.freemusic.presentation.ui.local.LocalMusicScreen
import com.freemusic.presentation.ui.player.MiniPlayer
import com.freemusic.presentation.ui.player.PlayerScreen
import com.freemusic.presentation.ui.playlist.PlaylistDetailScreen
import com.freemusic.presentation.ui.playlist.PlaylistScreen
import com.freemusic.presentation.ui.queue.QueueScreen
import com.freemusic.presentation.ui.search.SearchScreen
import com.freemusic.presentation.ui.settings.SettingsScreen
import com.freemusic.presentation.viewmodel.ImportViewModel
import com.freemusic.presentation.viewmodel.LocalMusicViewModel
import com.freemusic.presentation.viewmodel.PlayHistoryViewModel
import com.freemusic.presentation.viewmodel.PlayerViewModel
import com.freemusic.presentation.viewmodel.PlaylistViewModel
import com.freemusic.presentation.viewmodel.SearchViewModel
import com.freemusic.presentation.viewmodel.AlbumBrowserViewModel
import com.freemusic.presentation.viewmodel.ArtistBrowserViewModel
import com.freemusic.presentation.viewmodel.FolderBrowserViewModel
import com.freemusic.presentation.viewmodel.SettingsViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Player : Screen("player")
    data object Queue : Screen("queue")
    data object Settings : Screen("settings")
    data object Import : Screen("import")
    data object LocalMusic : Screen("local_music")
    data object Playlist : Screen("playlist")
    data object PlaylistDetail : Screen("playlist_detail/{playlistId}") {
        fun createRoute(playlistId: String) = "playlist_detail/$playlistId"
    }
    data object PlayHistory : Screen("play_history")
    data object FolderBrowser : Screen("folder_browser")
    data object ArtistBrowser : Screen("artist_browser")
    data object AlbumBrowser : Screen("album_browser")
}

@Composable
fun FreeMusicNavHost(
    navController: NavHostController = rememberNavController(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    importViewModel: ImportViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    // 视觉效果设置
    particlesEnabled: Boolean = true,
    particleIntensity: Float = 1f,
    coverStyle: CoverStyleType = CoverStyleType.ROUND,
    visualizerEnabled: Boolean = false,
    // 音效设置
    equalizerPreset: Int = 0,
    bassBoost: Int = 0,
    virtualizer: Int = 0,
    // 播放设置
    autoPlay: Boolean = true,
    crossFadeEnabled: Boolean = false,
    crossFadeDuration: Int = 3000,
    // 歌词设置
    lyricsFontSize: Int = 16,
    // 播放交互
    shakeToSkip: Boolean = false,
    // 外部音频文件
    pendingAudioUri: Uri? = null,
    onPendingAudioUriConsumed: () -> Unit = {}
) {
    // 处理外部音频文件
    LaunchedEffect(pendingAudioUri) {
        if (pendingAudioUri != null) {
            // 使用 playFromExternalUri 播放外部音频文件
            playerViewModel.playFromExternalUri(pendingAudioUri)
            
            // 导航到播放页面
            navController.navigate(Screen.Player.route)
            
            onPendingAudioUriConsumed()
        }
    }
    
    // 收集播放状态
    val playerState by playerViewModel.uiState.collectAsState()
    val currentSong = playerState.currentSong
    val isPlaying = playerState.isPlaying
    
    // 获取当前路由（使用 currentBackStackEntryAsState 使其可观察）
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // 判断是否显示迷你播放器（不在Player页面且有当前歌曲）
    val showMiniPlayer = currentSong != null && currentRoute != Screen.Player.route
    
    // 使用 Box 确保 MiniPlayer 可以覆盖在内容之上
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(
                bottom = if (showMiniPlayer) 64.dp else 0.dp
            )
        ) {
        composable(Screen.Home.route) {
            // 简化版首页 - 直接导航到搜索
            com.freemusic.presentation.ui.home.HomeScreen(
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onLocalMusicClick = { navController.navigate(Screen.LocalMusic.route) },
                onPlaylistClick = { navController.navigate(Screen.Playlist.route) },
                onFolderBrowserClick = { navController.navigate(Screen.FolderBrowser.route) },
                onArtistBrowserClick = { navController.navigate(Screen.ArtistBrowser.route) },
                onAlbumBrowserClick = { navController.navigate(Screen.AlbumBrowser.route) },
                onHistoryClick = { navController.navigate(Screen.PlayHistory.route) }
            )
        }
        
        composable(Screen.Search.route) {
            val searchState by searchViewModel.uiState.collectAsState()
            val playlistState by playlistViewModel.uiState.collectAsState()
            
            SearchScreen(
                query = searchState.query,
                onQueryChange = searchViewModel::onQueryChange,
                onSearch = { searchViewModel.search() },
                searchResults = searchState.results,
                hotSearchTags = searchState.hotSearchTags,
                searchHistory = searchState.history,
                isLoading = searchState.isLoading,
                onSongClick = { song ->
                    playerViewModel.playSong(song, searchState.results)
                    navController.navigate(Screen.Player.route)
                },
                onPlaylistClick = { navController.navigate(Screen.Playlist.route) },
                onTagClick = { tag ->
                    searchViewModel.onQueryChange(tag)
                    searchViewModel.search()
                },
                onHistoryItemClick = { keyword ->
                    searchViewModel.onQueryChange(keyword)
                    searchViewModel.search()
                },
                onClearHistory = { searchViewModel.clearHistory() },
                onAddToPlaylist = { song, playlist ->
                    playlistViewModel.addToPlaylist(playlist.id, song)
                },
                playlists = playlistState.playlists,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(
            Screen.Player.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
        ) {
            val playlistState by playlistViewModel.uiState.collectAsState()
            PlayerScreen(
                onBackClick = { navController.popBackStack() },
                onQueueClick = { navController.navigate(Screen.Queue.route) },
                viewModel = playerViewModel,
                // 传递设置参数
                particlesEnabled = particlesEnabled,
                particleIntensity = particleIntensity,
                coverStyleType = coverStyle,
                visualizerEnabled = visualizerEnabled,
                shakeToSkipEnabled = shakeToSkip,
                // 歌单
                playlists = playlistState.playlists,
                onAddSongsToPlaylist = { songs, playlist ->
                    playlistViewModel.addSongsToPlaylist(playlist.id, songs)
                }
            )
        }
        
        composable(Screen.Queue.route) {
            QueueScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = { index ->
                    playerViewModel.playFromQueue(index)
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            Screen.Settings.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
        ) {
            val settingsState by settingsViewModel.uiState.collectAsState()
            
            SettingsScreen(
                currentTheme = settingsState.currentThemeName,
                onThemeChange = settingsViewModel::setTheme,
                pureBlackEnabled = settingsState.pureBlackEnabled,
                onPureBlackToggle = settingsViewModel::setPureBlack,
                customPrimaryColor = settingsState.customPrimaryColor,
                onCustomPrimaryColorChange = settingsViewModel::setCustomPrimaryColor,
                particleEffect = settingsState.particleEffectName,
                onParticleEffectChange = settingsViewModel::setParticleEffect,
                coverStyle = settingsState.coverStyleName,
                onCoverStyleChange = { styleName: String ->
                    try {
                        val style = CoverStyleType.valueOf(styleName)
                        settingsViewModel.setCoverStyle(style)
                    } catch (e: Exception) {
                        // Ignore invalid style names
                    }
                },
                coverSwitchInterval = settingsState.coverSwitchInterval,
                onCoverSwitchIntervalChange = settingsViewModel::setCoverSwitchInterval,
                visualizerStyle = settingsState.visualizerStyleName,
                onVisualizerStyleChange = settingsViewModel::setVisualizerStyle,
                equalizerPreset = settingsState.equalizerPresetName,
                onEqualizerPresetChange = { presetName: String ->
                    val index = EqualizerPreset.entries.indexOfFirst { it.displayName == presetName }
                    if (index >= 0) {
                        settingsViewModel.setEqualizerPreset(index)
                    }
                },
                autoPlayEnabled = settingsState.autoPlayEnabled,
                onAutoPlayToggle = settingsViewModel::setAutoPlay,
                playbackSpeed = settingsState.playbackSpeed,
                onPlaybackSpeedChange = settingsViewModel::setPlaybackSpeed,
                sleepTimerMinutes = settingsState.sleepTimerMinutes,
                onSleepTimerChange = settingsViewModel::setSleepTimer,
                skipSilenceEnabled = settingsState.skipSilenceEnabled,
                onSkipSilenceToggle = settingsViewModel::setSkipSilence,
                highQualityEnabled = settingsState.highQualityEnabled,
                onHighQualityToggle = settingsViewModel::setHighQuality,
                cacheSize = settingsState.cacheSize,
                onClearCache = settingsViewModel::clearCache,
                onBackClick = { navController.popBackStack() },
                onImportClick = { navController.navigate(Screen.Import.route) },
                onAboutClick = settingsViewModel::showAboutDialog
            )
        }
        
        composable(Screen.Import.route) {
            ImportScreen(
                onBackClick = { navController.popBackStack() },
                onImportPlaylist = { link ->
                    importViewModel.importPlaylist(link)
                    navController.popBackStack()
                },
                onImportSongs = { songNames ->
                    importViewModel.searchSongs(songNames)
                },
                onCreatePlaylist = { name, songs ->
                    playlistViewModel.createPlaylist(name, songs)
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.LocalMusic.route) {
            val localMusicViewModel: LocalMusicViewModel = hiltViewModel()
            val localMusicState by localMusicViewModel.uiState.collectAsState()
            val playlistState by playlistViewModel.uiState.collectAsState()
            
            LocalMusicScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = { song, playlist ->
                    playerViewModel.playSong(song, playlist)
                    navController.navigate(Screen.Player.route)
                },
                playlists = playlistState.playlists,
                onAddSongsToPlaylist = { songs, playlist ->
                    playlistViewModel.addSongsToPlaylist(playlist.id, songs)
                },
                viewModel = localMusicViewModel
            )
        }
        
        composable(Screen.Playlist.route) {
            val playlistState by playlistViewModel.uiState.collectAsState()
            
            PlaylistScreen(
                playlists = playlistState.playlists,
                onBackClick = { navController.popBackStack() },
                onPlaylistClick = { playlist ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                },
                onCreatePlaylist = { name ->
                    playlistViewModel.createPlaylist(name, emptyList())
                },
                onDeletePlaylist = { playlistId ->
                    playlistViewModel.deletePlaylist(playlistId)
                }
            )
        }
        
        composable(Screen.PlaylistDetail.route) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: return@composable
            val playlistState by playlistViewModel.uiState.collectAsState()
            val playlist = playlistState.playlists.find { it.id == playlistId }
            
            if (playlist != null) {
                PlaylistDetailScreen(
                    playlist = playlist,
                    onBackClick = { navController.popBackStack() },
                    onSongClick = { song ->
                        playerViewModel.playSong(song, playlist.songs)
                        navController.navigate(Screen.Player.route)
                    },
                    onAddSongs = {
                        // 导航到搜索页，用户可以搜索歌曲并添加到歌单
                        navController.navigate(Screen.Search.route)
                    },
                    onRemoveSong = { song ->
                        playlistViewModel.removeFromPlaylist(playlistId, song.id)
                    }
                )
            }
        }
        
        composable(Screen.PlayHistory.route) {
            val playHistoryViewModel: PlayHistoryViewModel = hiltViewModel()
            val playHistoryState by playHistoryViewModel.uiState.collectAsState()
            val playlistState by playlistViewModel.uiState.collectAsState()
            
            PlayHistoryScreen(
                recentPlays = playHistoryState.recentPlays,
                mostPlayed = playHistoryState.mostPlayed,
                onBackClick = { navController.popBackStack() },
                onSongClick = { song ->
                    playerViewModel.playSong(song, playHistoryState.recentPlays)
                    navController.navigate(Screen.Player.route)
                },
                onAddToPlaylist = { song, playlist ->
                    playlistViewModel.addToPlaylist(playlist.id, song)
                },
                playlists = playlistState.playlists,
                onClearHistory = { playHistoryViewModel.clearHistory() }
            )
        }
        
        composable(Screen.FolderBrowser.route) {
            val folderBrowserViewModel: FolderBrowserViewModel = hiltViewModel()
            val folderBrowserState by folderBrowserViewModel.uiState.collectAsState()
            
            FolderBrowserScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = { song, songs ->
                    playerViewModel.playSong(song, songs)
                    navController.navigate(Screen.Player.route)
                },
                viewModel = folderBrowserViewModel
            )
        }
        
        composable(Screen.ArtistBrowser.route) {
            val artistBrowserViewModel: ArtistBrowserViewModel = hiltViewModel()
            val artistBrowserState by artistBrowserViewModel.uiState.collectAsState()
            
            ArtistBrowserScreen(
                onBackClick = { navController.popBackStack() },
                onArtistClick = { artist -> artistBrowserViewModel.selectArtist(artist) },
                onSongClick = { song, songs ->
                    playerViewModel.playSong(song, songs)
                    navController.navigate(Screen.Player.route)
                },
                viewModel = artistBrowserViewModel
            )
        }
        
        composable(Screen.AlbumBrowser.route) {
            val albumBrowserViewModel: AlbumBrowserViewModel = hiltViewModel()
            val albumBrowserState by albumBrowserViewModel.uiState.collectAsState()
            
            AlbumBrowserScreen(
                onBackClick = { navController.popBackStack() },
                onAlbumClick = { album -> albumBrowserViewModel.selectAlbum(album) },
                onSongClick = { song, songs ->
                    playerViewModel.playSong(song, songs)
                    navController.navigate(Screen.Player.route)
                },
                viewModel = albumBrowserViewModel
            )
        }
    }
    
    // 底部迷你播放器 - 占满底部，不留导航栏padding
    if (showMiniPlayer && currentSong != null) {
        MiniPlayer(
            currentSong = currentSong,
            isPlaying = isPlaying,
            onPlayPauseClick = { playerViewModel.togglePlayPause() },
            onNextClick = { playerViewModel.skipToNext() },
            onPreviousClick = { playerViewModel.skipToPrevious() },
            onPlayerClick = { navController.navigate(Screen.Player.route) },
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
        )
    }
}
}
