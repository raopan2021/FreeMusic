package com.freemusic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.data.local.LocalDataSource
import com.freemusic.domain.model.Song
import com.freemusic.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            localDataSource.getSearchHistory(10).collect { history ->
                _uiState.update { it.copy(history = history) }
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
            
            // 保存搜索历史
            localDataSource.addSearchHistory(query)
            
            musicRepository.searchSongs(query).collect { result ->
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
                                isLoading = false
                            )
                        }
                    }
                )
            }
        }
    }

    fun searchPlaylist(playlistId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            musicRepository.getPlaylist(playlistId).collect { result ->
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
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            localDataSource.clearSearchHistory()
        }
    }
}
