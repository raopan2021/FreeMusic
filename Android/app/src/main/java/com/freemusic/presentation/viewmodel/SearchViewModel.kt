package com.freemusic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.domain.model.Song
import com.freemusic.domain.usecase.SearchSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = false,
    val currentPage: Int = 0
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchSongsUseCase: SearchSongsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }

        // Debounce search
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // 300ms debounce
            if (query.isNotBlank()) {
                search(query, reset = true)
            } else {
                _uiState.update { it.copy(songs = emptyList(), error = null) }
            }
        }
    }

    fun search(query: String = _uiState.value.query, reset: Boolean = false) {
        if (query.isBlank()) return

        viewModelScope.launch {
            val page = if (reset) 0 else _uiState.value.currentPage + 1
            
            _uiState.update { 
                it.copy(
                    isLoading = true,
                    error = null,
                    currentPage = page,
                    songs = if (reset) emptyList() else it.songs
                )
            }

            searchSongsUseCase(query, page).collect { result ->
                result.fold(
                    onSuccess = { searchResult ->
                        _uiState.update { state ->
                            state.copy(
                                songs = if (reset) searchResult.songs 
                                        else state.songs + searchResult.songs,
                                isLoading = false,
                                hasMore = searchResult.hasMore
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "搜索失败"
                            )
                        }
                    }
                )
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.hasMore && !_uiState.value.isLoading) {
            search(reset = false)
        }
    }

    fun clearSearch() {
        _uiState.update { SearchUiState() }
    }
}
