package com.freemusic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.data.local.LocalDataSource
import com.freemusic.domain.model.Song
import com.freemusic.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Song> = emptyList(),
    val history: List<String> = emptyList(),
    val hotSearchTags: List<String> = listOf(
        "周杰伦", "Taylor Swift", "告白气球", "稻香",
        "林俊杰", "邓紫棋", "陈奕迅", "张学友"
    ),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val localDataSource: LocalDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadSearchHistory()
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            try {
                localDataSource.getSearchHistory(10).collect { history ->
                    _uiState.update { it.copy(history = history) }
                }
            } catch (e: Exception) {
                // Ignore errors loading history
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun search() {
        val query = _uiState.value.query
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 保存搜索历史（不阻塞搜索）
            launch(Dispatchers.IO) {
                try {
                    localDataSource.addSearchHistory(query)
                } catch (e: Exception) {
                    // Ignore history save errors
                }
            }
            
            // 搜索 - 使用 firstOrNull() 更安全，避免 flow 为空时崩溃
            try {
                val result = musicRepository.searchSongs(query).firstOrNull()
                
                if (result != null) {
                    result.fold(
                        onSuccess = { searchResult ->
                            _uiState.update { 
                                it.copy(
                                    results = searchResult.songs,
                                    isLoading = false
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    error = error.message,
                                    isLoading = false,
                                    results = emptyList()
                                )
                            }
                        }
                    )
                } else {
                    // result 为 null，说明 flow 没有发射任何元素
                    _uiState.update { 
                        it.copy(
                            error = "搜索失败",
                            isLoading = false,
                            results = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "搜索失败",
                        isLoading = false,
                        results = emptyList()
                    )
                }
            }
        }
    }

    fun searchPlaylist(playlistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val result = musicRepository.getPlaylist(playlistId).firstOrNull()
                
                if (result != null) {
                    result.fold(
                        onSuccess = { playlist ->
                            _uiState.update { 
                                it.copy(
                                    results = playlist.songs,
                                    isLoading = false
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { 
                                it.copy(
                                    error = error.message,
                                    isLoading = false
                                )
                            }
                        }
                    )
                } else {
                    _uiState.update { 
                        it.copy(
                            error = "获取歌单失败",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                localDataSource.clearSearchHistory()
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }
}
