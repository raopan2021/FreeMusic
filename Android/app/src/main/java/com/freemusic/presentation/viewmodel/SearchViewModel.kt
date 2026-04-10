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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            
            try {
                // 保存搜索历史
                withContext(Dispatchers.IO) {
                    try {
                        localDataSource.addSearchHistory(query)
                    } catch (e: Exception) {
                        // Ignore history save errors
                    }
                }
                
                // 搜索 - 使用 first() 而不是 firstOrNull() 确保能取到结果
                val result = withContext(Dispatchers.IO) {
                    try {
                        musicRepository.searchSongs(query).first()
                    } catch (e: Exception) {
                        // 搜索失败返回空结果
                        Result.success(com.freemusic.domain.model.SearchResult(
                            songs = emptyList(),
                            hasMore = false,
                            total = 0
                        ))
                    }
                }
                
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
                val result = withContext(Dispatchers.IO) {
                    try {
                        musicRepository.getPlaylist(playlistId).first()
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }
                
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
