package com.freemusic.presentation.navigation

import com.freemusic.domain.model.Playlist
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.freemusic.presentation.ui.settings.ThemePresetScreen
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
import kotlinx.coroutines.launch

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
    data object ThemePreset : Screen("theme_preset")
}

@OptIn(ExperimentalMaterial3Api::class)
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
    // 特效回调（由 SettingsViewModel 提供）
    onParticlesToggle: () -> Unit = {},
    onVisualizerToggle: () -> Unit = {},
    onEqualizerToggle: () -> Unit = {},
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
    // Player sheet 状态（需要在使用前声明）
    val scope = rememberCoroutineScope()
    var showPlayerSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // BackHandler: sheet 展开时按返回键关闭 sheet
    BackHandler(enabled = showPlayerSheet) {
        scope.launch {
            sheetState.hide()
            showPlayerSheet = false
        }
    }

    // 处理外部音频文件
    LaunchedEffect(pendingAudioUri) {
        if (pendingAudioUri != null) {
            playerViewModel.playFromExternalUri(pendingAudioUri)
            // 展开播放页面 sheet
            scope.launch {
                showPlayerSheet = true
                sheetState.show()
            }
            onPendingAudioUriConsumed()
        }
    }
    
    // 收集播放状态
    val playerState by playerViewModel.uiState.collectAsState()
    val currentSong = playerState.currentSong
    val isPlaying = playerState.isPlaying

    // 获取当前页面
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isOnPlayerScreen = currentRoute == Screen.Player.route

    // Mini Player 显示逻辑：有歌曲且不在播放页面时显示
    val showMiniPlayer = currentSong != null

    Box(modifier = Modifier.fillMaxSize()) {
        // 主内容: Column(NavHost + MiniPlayer + NavigationBar)
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 页面内容 (NavHost 会填充剩余空间，留出底部空间)
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.weight(1f)
            ) {
        composable(Screen.Home.route) {
            val playlistState by playlistViewModel.uiState.collectAsState()
            // 显示所有歌单（包括"我喜欢的音乐"）
            val playlists = playlistState.playlists.map { p ->
                com.freemusic.presentation.ui.home.PlaylistUiModel(
                    id = p.id,
                    name = p.name,
                    songCount = p.songs.size
                )
            }
            com.freemusic.presentation.ui.home.HomeScreen(
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onLocalMusicClick = { navController.navigate(Screen.LocalMusic.route) },
                onPlaylistClick = { playlistId -> 
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                },
                onFolderBrowserClick = { navController.navigate(Screen.FolderBrowser.route) },
                onArtistBrowserClick = { navController.navigate(Screen.ArtistBrowser.route) },
                onAlbumBrowserClick = { navController.navigate(Screen.AlbumBrowser.route) },
                onHistoryClick = { navController.navigate(Screen.PlayHistory.route) },
                onFavoritesClick = { 
                    // 导航到"我喜欢的音乐"歌单
                    navController.navigate(Screen.PlaylistDetail.createRoute("favorites"))
                },
                playlists = playlists,
                onCreatePlaylist = { name -> 
                    playlistViewModel.createPlaylist(name, emptyList())
                }
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
                equalizerPreset = equalizerPreset,
                onParticlesToggle = { settingsViewModel.setParticlesEnabled(!particlesEnabled) },
                onVisualizerToggle = { settingsViewModel.setVisualizerEnabled(!visualizerEnabled) },
                onEqualizerToggle = {
                    val nextIdx = if (equalizerPreset >= EqualizerPreset.entries.size - 1) 0 else equalizerPreset + 1
                    settingsViewModel.setEqualizerPreset(nextIdx)
                },
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
                themePresetId = settingsState.themePresetId,
                onThemePresetChange = settingsViewModel::setThemePresetId,
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
                skipSilenceEnabled = settingsState.skipSilenceEnabled,
                onSkipSilenceToggle = settingsViewModel::setSkipSilence,
                highQualityEnabled = settingsState.highQualityEnabled,
                onHighQualityToggle = settingsViewModel::setHighQuality,
                cacheSize = settingsState.cacheSize,
                onClearCache = settingsViewModel::clearCache,
                lyricsFontSize = settingsState.lyricsFontSize,
                onLyricsFontSizeChange = settingsViewModel::setLyricsFontSize,
                onBackClick = { navController.popBackStack() },
                onImportClick = { navController.navigate(Screen.Import.route) },
                onAboutClick = settingsViewModel::showAboutDialog,
                onThemePresetClick = { navController.navigate(Screen.ThemePreset.route) }
            )
        }
        
        composable(
            Screen.ThemePreset.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
        ) {
            val settingsState by settingsViewModel.uiState.collectAsState()
            
            ThemePresetScreen(
                currentPresetId = settingsState.themePresetId,
                onPresetChange = settingsViewModel::setThemePresetId,
                onBackClick = { navController.popBackStack() }
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
            
            // 从 playlists 中查找（现在 favorites 也在列表中）
            val displayPlaylist = playlistState.playlists.find { it.id == playlistId }
            
            if (displayPlaylist != null) {
                PlaylistDetailScreen(
                    playlist = displayPlaylist,
                    onBackClick = { navController.popBackStack() },
                    onSongClick = { song ->
                        playerViewModel.playSong(song, displayPlaylist.songs)
                        navController.navigate(Screen.Player.route)
                    },
                    onAddSongs = {
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

        // 迷你播放器 (位于 NavigationBar 上方)
        // 有歌曲、不在播放页面、不在 Queue 页面时显示
        if (showMiniPlayer && currentSong != null && !isOnPlayerScreen && currentRoute != Screen.Queue.route) {
            MiniPlayer(
                currentSong = currentSong,
                isPlaying = isPlaying,
                onPlayPauseClick = { playerViewModel.togglePlayPause() },
                onNextClick = { playerViewModel.skipToNext() },
                onPreviousClick = { playerViewModel.skipToPrevious() },
                onPlayerClick = {
                    // 直接打开播放页面
                    navController.navigate(Screen.Player.route)
                }
            )
        }
    }

    // 播放页面 Bottom Sheet
    val playlistState by playlistViewModel.uiState.collectAsState()
    if (showPlayerSheet && currentSong != null) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    showPlayerSheet = false
                }
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null
        ) {
            PlayerScreen(
                onBackClick = {
                    scope.launch {
                        sheetState.hide()
                        showPlayerSheet = false
                    }
                },
                onQueueClick = { navController.navigate(Screen.Queue.route) },
                viewModel = playerViewModel,
                particlesEnabled = particlesEnabled,
                particleIntensity = particleIntensity,
                coverStyleType = coverStyle,
                visualizerEnabled = visualizerEnabled,
                equalizerPreset = equalizerPreset,
                onParticlesToggle = onParticlesToggle,
                onVisualizerToggle = onVisualizerToggle,
                onEqualizerToggle = onEqualizerToggle,
                shakeToSkipEnabled = shakeToSkip,
                playlists = playlistState.playlists,
                onAddSongsToPlaylist = { songs, playlist ->
                    playlistViewModel.addSongsToPlaylist(playlist.id, songs)
                }
            )
        }
    }
}
}
