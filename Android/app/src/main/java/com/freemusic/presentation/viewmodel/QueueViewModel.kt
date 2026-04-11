package com.freemusic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freemusic.domain.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 播放队列管理
 */
data class QueueUiState(
    val currentQueue: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
)

enum class RepeatMode {
    OFF,    // 不重复
    ONE,    // 单曲循环
    ALL     // 列表循环
}

@HiltViewModel
class QueueViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(QueueUiState())
    val uiState: StateFlow<QueueUiState> = _uiState.asStateFlow()

    fun setQueue(songs: List<Song>, startIndex: Int = 0) {
        _uiState.update { 
            it.copy(
                currentQueue = songs,
                currentIndex = startIndex
            )
        }
    }

    fun addToQueue(song: Song) {
        _uiState.update { state ->
            state.copy(currentQueue = state.currentQueue + song)
        }
    }

    fun addToQueueNext(song: Song) {
        _uiState.update { state ->
            val newQueue = state.currentQueue.toMutableList()
            val insertIndex = (state.currentIndex + 1).coerceAtMost(newQueue.size)
            newQueue.add(insertIndex, song)
            state.copy(currentQueue = newQueue)
        }
    }

    fun removeFromQueue(index: Int) {
        _uiState.update { state ->
            if (state.currentQueue.size <= 1) return@update state
            
            val newQueue = state.currentQueue.toMutableList()
            newQueue.removeAt(index)
            
            val newIndex = when {
                index < state.currentIndex -> state.currentIndex - 1
                index == state.currentIndex && index >= newQueue.size -> newQueue.size - 1
                else -> state.currentIndex
            }
            
            state.copy(currentQueue = newQueue, currentIndex = newIndex)
        }
    }

    fun clearQueue() {
        _uiState.update { it.copy(currentQueue = emptyList(), currentIndex = 0) }
    }

    fun moveItem(from: Int, to: Int) {
        _uiState.update { state ->
            val newQueue = state.currentQueue.toMutableList()
            val song = newQueue.removeAt(from)
            newQueue.add(to, song)
            
            var newIndex = state.currentIndex
            when {
                from == state.currentIndex -> newIndex = to
                from < state.currentIndex && to >= state.currentIndex -> newIndex--
                from > state.currentIndex && to <= state.currentIndex -> newIndex++
            }
            
            state.copy(currentQueue = newQueue, currentIndex = newIndex)
        }
    }
    
    fun moveToNext(index: Int) {
        _uiState.update { state ->
            if (index == state.currentIndex || index == state.currentIndex + 1) return@update state
            
            val newQueue = state.currentQueue.toMutableList()
            val song = newQueue.removeAt(index)
            val insertIndex = state.currentIndex + 1
            newQueue.add(insertIndex, song)
            
            state.copy(currentQueue = newQueue)
        }
    }

    fun toggleShuffle() {
        _uiState.update { state ->
            state.copy(isShuffleEnabled = !state.isShuffleEnabled)
        }
    }

    fun cycleRepeatMode() {
        _uiState.update { state ->
            val nextMode = when (state.repeatMode) {
                RepeatMode.OFF -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.OFF
            }
            state.copy(repeatMode = nextMode)
        }
    }

    fun getNextIndex(): Int? {
        val state = _uiState.value
        return when (state.repeatMode) {
            RepeatMode.ONE -> state.currentIndex
            RepeatMode.ALL -> (state.currentIndex + 1) % state.currentQueue.size
            RepeatMode.OFF -> {
                val next = state.currentIndex + 1
                if (next < state.currentQueue.size) next else null
            }
        }
    }

    fun getPreviousIndex(): Int? {
        val state = _uiState.value
        return when (state.repeatMode) {
            RepeatMode.ONE -> state.currentIndex
            RepeatMode.ALL -> (state.currentIndex - 1 + state.currentQueue.size) % state.currentQueue.size
            RepeatMode.OFF -> {
                val prev = state.currentIndex - 1
                if (prev >= 0) prev else null
            }
        }
    }
}
