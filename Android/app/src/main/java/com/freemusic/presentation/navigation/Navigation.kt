package com.freemusic.presentation.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.freemusic.presentation.ui.home.HomeScreen
import com.freemusic.presentation.ui.import.ImportScreen
import com.freemusic.presentation.ui.player.PlayerScreen
import com.freemusic.presentation.ui.queue.QueueScreen
import com.freemusic.presentation.ui.search.SearchScreen
import com.freemusic.presentation.ui.settings.SettingsScreen
import com.freemusic.presentation.viewmodel.ImportViewModel
import com.freemusic.presentation.viewmodel.PlayerViewModel

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
    importViewModel: ImportViewModel = hiltViewModel()
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
                onBackClick = { navController.popBackStack() },
                onSongClick = { song ->
                    playerViewModel.playSong(song)
                    navController.navigate(Screen.Player.route)
                }
            )
        }
        composable(Screen.Player.route) {
            PlayerScreen(
                onBackClick = { navController.popBackStack() },
                onQueueClick = { navController.navigate(Screen.Queue.route) },
                viewModel = playerViewModel
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
                onImportClick = { navController.navigate(Screen.Import.route) }
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
