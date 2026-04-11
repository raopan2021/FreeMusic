package com.freemusic.presentation.ui.folder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.freemusic.domain.model.Song
import com.freemusic.presentation.ui.theme.PrimaryIndigo
import com.freemusic.presentation.viewmodel.FolderBrowserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderBrowserScreen(
    onBackClick: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    viewModel: FolderBrowserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    )}
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredFolders = remember(uiState.folders, searchQuery) {
        if (searchQuery.isBlank()) uiState.folders
        else uiState.folders.filter { (path, _) -> path.contains(searchQuery, ignoreCase = true) }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.loadFolders()
        }
    }
    
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.loadFolders()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.currentPath == null) "文件夹" else uiState.currentPath!!.substringAfterLast("/")) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentPath != null) {
                            viewModel.navigateUp()
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            !hasPermission -> {
                PermissionRequest(
                    onRequestPermission = {
                        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_AUDIO
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        permissionLauncher.launch(permission)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.currentPath != null -> {
                // 显示文件夹内容
                FolderContent(
                    songs = uiState.currentSongs,
                    onSongClick = { song -> onSongClick(song, uiState.currentSongs) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                // 显示文件夹列表
                Column(modifier = Modifier.padding(paddingValues)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("搜索文件夹...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "清除")
                                }
                            }
                        },
                        singleLine = true
                    )
                    FolderList(
                        folders = filteredFolders,
                        onFolderClick = { path -> viewModel.enterFolder(path) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderList(
    folders: List<Pair<String, Int>>,
    onFolderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (folders.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "没有找到文件夹",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(folders) { (path, songCount) ->
                val folderName = path.substringAfterLast("/")
                ListItem(
                    headlineContent = {
                        Text(
                            text = folderName,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    supportingContent = {
                        Text("$songCount 首歌曲")
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    modifier = Modifier.clickable { onFolderClick(path) }
                )
            }
        }
    }
}

@Composable
private fun FolderContent(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    if (songs.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "文件夹为空",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                ListItem(
                    headlineContent = { Text("共 ${songs.size} 首歌曲") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = PrimaryIndigo
                        )
                    }
                )
                Divider()
            }
            items(songs) { song ->
                SongItem(
                    song = song,
                    onClick = { onSongClick(song) }
                )
            }
        }
    }
}

@Composable
private fun SongItem(
    song: Song,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
            
            Text(
                text = formatDuration(song.duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun PermissionRequest(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "需要音频文件访问权限",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "请授予权限以浏览本地音乐文件夹",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("授予权限")
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
