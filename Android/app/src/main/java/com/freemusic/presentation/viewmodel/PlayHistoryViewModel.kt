package com.freemusic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.data.local.LocalDataSource
import com.freemusic.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayHistoryUiState(
    val recentPlays: List<Song> = emptyList(),
    val mostPlayed: List<Song> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class PlayHistoryViewModel @Inject constructor(
    private val localDataSource: LocalDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayHistoryUiState())
    val uiState: StateFlow<PlayHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            launch {
                localDataSource.getPlayHistory().collect { songs ->
                    _uiState.value = _uiState.value.copy(
                        recentPlays = songs,
                        isLoading = false
                    )
                }
            }
            
            launch {
                localDataSource.getMostPlayed().collect { songs ->
                    _uiState.value = _uiState.value.copy(
                        mostPlayed = songs
                    )
                }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            localDataSource.clearPlayHistory()
        }
    }
}
