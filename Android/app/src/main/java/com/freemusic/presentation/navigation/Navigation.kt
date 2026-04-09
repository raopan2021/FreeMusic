package com.freemusic.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.freemusic.presentation.ui.home.HomeScreen
import com.freemusic.presentation.ui.player.PlayerScreen
import com.freemusic.presentation.ui.search.SearchScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Player : Screen("player")
}

@Composable
fun FreeMusicNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onSongClick = { navController.navigate(Screen.Player.route) }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = { navController.navigate(Screen.Player.route) }
            )
        }
        composable(Screen.Player.route) {
            PlayerScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
