package com.freemusic.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.freemusic.presentation.ui.components.MiniPlayer
import com.freemusic.presentation.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearchClick: () -> Unit,
    onSongClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val playerState by playerViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FreeMusic") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置"
                        )
                    }
                    TextButton(onClick = onSearchClick) {
                        Text("搜索")
                    }
                }
            )
        },
        bottomBar = {
            MiniPlayer(
                song = playerState.currentSong,
                isPlaying = playerState.isPlaying,
                onPlayPause = playerViewModel::togglePlayPause,
                onClick = onSongClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎵 FreeMusic",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击搜索开始听歌",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onSearchClick) {
                    Text("搜索歌曲")
                }
            }
        }
    }
}
