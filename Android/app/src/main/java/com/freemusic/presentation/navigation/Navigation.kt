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
import androidx.navigation.compose.rememberNavController
import com.freemusic.data.preferences.CoverStyleType
import com.freemusic.presentation.ui.history.PlayHistoryScreen
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
    
    // 判断是否显示迷你播放器（不在Player页面且有当前歌曲）
    val showMiniPlayer = currentSong != null && !navController.currentBackStackEntry?.destination?.route.equals(Screen.Player.route)
    
    // 使用 Box 确保 MiniPlayer 可以覆盖在内容之上
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
        composable(Screen.Home.route) {
            // 简化版首页 - 直接导航到搜索
            com.freemusic.presentation.ui.home.HomeScreen(
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onLocalMusicClick = { navController.navigate(Screen.LocalMusic.route) },
                onPlaylistClick = { navController.navigate(Screen.Playlist.route) }
            )
        }
        
        composable(Screen.Search.route) {
            val searchState by searchViewModel.uiState.collectAsState()
            
            SearchScreen(
                query = searchState.query,
                onQueryChange = searchViewModel::onQueryChange,
                onSearch = { searchViewModel.search() },
                searchResults = searchState.results,
                hotSearchTags = searchState.hotSearchTags,
                searchHistory = searchState.history,
                isLoading = searchState.isLoading,
                onSongClick = { song ->
                    playerViewModel.playSong(song)
                    if (autoPlay) {
                        playerViewModel.togglePlayPause()
                    }
                    navController.navigate(Screen.Player.route)
                },
                onPlaylistClick = { /* TODO */ },
                onTagClick = { tag ->
                    searchViewModel.onQueryChange(tag)
                    searchViewModel.search()
                },
                onHistoryItemClick = { keyword ->
                    searchViewModel.onQueryChange(keyword)
                    searchViewModel.search()
                },
                onClearHistory = { searchViewModel.clearHistory() },
                onBackClick = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Player.route) {
            PlayerScreen(
                onBackClick = { navController.popBackStack() },
                onQueueClick = { navController.navigate(Screen.Queue.route) },
                viewModel = playerViewModel,
                // 传递设置参数
                particlesEnabled = particlesEnabled,
                particleIntensity = particleIntensity,
                coverStyleType = coverStyle,
                visualizerEnabled = visualizerEnabled
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
        
        composable(Screen.Settings.route) {
            val settingsState by settingsViewModel.uiState.collectAsState()
            
            SettingsScreen(
                currentTheme = settingsState.currentThemeName,
                onThemeChange = settingsViewModel::setTheme,
                pureBlackEnabled = settingsState.pureBlackEnabled,
                onPureBlackToggle = settingsViewModel::setPureBlack,
                particleEffect = settingsState.particleEffectName,
                onParticleEffectChange = settingsViewModel::setParticleEffect,
                coverStyle = settingsState.coverStyleName,
                onCoverStyleChange = { /* TODO: 显示风格选择对话框 */ },
                visualizerStyle = settingsState.visualizerStyleName,
                onVisualizerStyleChange = settingsViewModel::setVisualizerStyle,
                equalizerPreset = settingsState.equalizerPresetName,
                onEqualizerPresetChange = { /* TODO: 均衡器预设选择 */ },
                autoPlayEnabled = settingsState.autoPlayEnabled,
                onAutoPlayToggle = settingsViewModel::setAutoPlay,
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
            
            LocalMusicScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = { song ->
                    playerViewModel.playSong(song)
                    if (autoPlay) {
                        playerViewModel.togglePlayPause()
                    }
                    navController.navigate(Screen.Player.route)
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
                        playerViewModel.playSong(song)
                        if (autoPlay) {
                            playerViewModel.togglePlayPause()
                        }
                        navController.navigate(Screen.Player.route)
                    },
                    onAddSongs = {
                        // TODO: 显示添加歌曲对话框或导航到搜索页
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
            
            PlayHistoryScreen(
                recentPlays = playHistoryState.recentPlays,
                mostPlayed = playHistoryState.mostPlayed,
                onBackClick = { navController.popBackStack() },
                onSongClick = { song ->
                    playerViewModel.playSong(song)
                    if (autoPlay) {
                        playerViewModel.togglePlayPause()
                    }
                    navController.navigate(Screen.Player.route)
                },
                onClearHistory = { playHistoryViewModel.clearHistory() }
            )
        }
    }
    
    // 底部迷你播放器 - 添加 navigationBarsPadding 确保不被底部导航遮挡
    if (showMiniPlayer && currentSong != null) {
        MiniPlayer(
            currentSong = currentSong,
            isPlaying = isPlaying,
            onPlayPauseClick = { playerViewModel.togglePlayPause() },
            onNextClick = { playerViewModel.skipToNext() },
            onPreviousClick = { playerViewModel.skipToPrevious() },
            onPlayerClick = { navController.navigate(Screen.Player.route) },
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}
}
