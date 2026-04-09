package com.freemusic.presentation.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.freemusic.data.preferences.CoverStyleType
import com.freemusic.presentation.ui.home.HomeScreen
import com.freemusic.presentation.ui.import.ImportScreen
import com.freemusic.presentation.ui.player.PlayerScreen
import com.freemusic.presentation.ui.queue.QueueScreen
import com.freemusic.presentation.ui.search.SearchScreen
import com.freemusic.presentation.ui.settings.SettingsScreen
import com.freemusic.presentation.viewmodel.ImportViewModel
import com.freemusic.presentation.viewmodel.PlayerViewModel
import com.freemusic.presentation.viewmodel.SettingsViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Player : Screen("player")
    data object Queue : Screen("queue")
    data object Settings : Screen("settings")
    data object Import : Screen("import")
}

@Composable
fun FreeMusicNavHost(
    navController: NavHostController = rememberNavController(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    importViewModel: ImportViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel? = null,
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
    lyricsFontSize: Int = 16
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onSongClick = { navController.navigate(Screen.Player.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                playerViewModel = playerViewModel
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                query = "",
                onQueryChange = { },
                onSearch = { },
                searchResults = emptyList(),
                hotSearchTags = listOf("周杰伦", "Taylor Swift", "告白气球", "稻香"),
                searchHistory = emptyList(),
                onSongClick = { song ->
                    playerViewModel.playSong(song)
                    if (autoPlay) {
                        playerViewModel.togglePlayPause()
                    }
                    navController.navigate(Screen.Player.route)
                },
                onPlaylistClick = { },
                onTagClick = { },
                onHistoryItemClick = { },
                onClearHistory = { }
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
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onImportClick = { navController.navigate(Screen.Import.route) },
                // 主题设置
                isDarkTheme = settingsViewModel?.isDarkTheme?.value ?: false,
                onDarkThemeToggle = { settingsViewModel?.setDarkTheme(it) },
                isPureBlack = settingsViewModel?.isPureBlack?.value ?: false,
                onPureBlackToggle = { settingsViewModel?.setPureBlack(it) },
                // 视觉效果设置
                particlesEnabled = particlesEnabled,
                onParticlesToggle = { settingsViewModel?.setParticlesEnabled(it) },
                particleIntensity = particleIntensity,
                onParticleIntensityChange = { settingsViewModel?.setParticleIntensity(it) },
                coverStyle = coverStyle,
                onCoverStyleChange = { settingsViewModel?.setCoverStyle(it) },
                visualizerEnabled = visualizerEnabled,
                onVisualizerToggle = { settingsViewModel?.setVisualizerEnabled(it) },
                // 音效设置
                equalizerPreset = equalizerPreset,
                onEqualizerPresetChange = { settingsViewModel?.setEqualizerPreset(it) },
                bassBoost = bassBoost,
                onBassBoostChange = { settingsViewModel?.setBassBoost(it) },
                virtualizer = virtualizer,
                onVirtualizerChange = { settingsViewModel?.setVirtualizer(it) },
                // 播放设置
                autoPlay = autoPlay,
                onAutoPlayToggle = { settingsViewModel?.setAutoPlay(it) },
                crossFadeEnabled = crossFadeEnabled,
                onCrossFadeToggle = { settingsViewModel?.setCrossFadeEnabled(it) },
                crossFadeDuration = crossFadeDuration,
                onCrossFadeDurationChange = { settingsViewModel?.setCrossFadeDuration(it) },
                // 歌词设置
                lyricsFontSize = lyricsFontSize,
                onLyricsFontSizeChange = { settingsViewModel?.setLyricsFontSize(it) }
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
                    // TODO: 保存到本地歌单
                    navController.popBackStack()
                }
            )
        }
    }
}
