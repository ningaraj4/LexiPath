package com.example.lexipath.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexipath.data.models.DailyContent
import com.example.lexipath.data.repository.LexiPathRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: LexiPathRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val pageSize = 20
    private var currentOffset = 0

    fun loadHistory() {
        if (_uiState.value.historyItems.isNotEmpty()) return // Already loaded

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getContentHistory(pageSize, 0)
                .onSuccess { items ->
                    _uiState.value = _uiState.value.copy(
                        historyItems = items,
                        isLoading = false,
                        hasMore = items.size == pageSize,
                        error = null
                    )
                    currentOffset = items.size
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load history"
                    )
                }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            
            repository.getContentHistory(pageSize, currentOffset)
                .onSuccess { newItems ->
                    val allItems = _uiState.value.historyItems + newItems
                    _uiState.value = _uiState.value.copy(
                        historyItems = allItems,
                        isLoadingMore = false,
                        hasMore = newItems.size == pageSize
                    )
                    currentOffset = allItems.size
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        error = error.message ?: "Failed to load more items"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HistoryUiState(
    val historyItems: List<DailyContent> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val error: String? = null
)
