package com.example.lexipath.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexipath.data.models.DailyContent
import com.example.lexipath.data.models.Profile
import com.example.lexipath.data.repository.LexiPathRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: LexiPathRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayContent()
        observeProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            repository.getProfileFlow().collect { profile ->
                _uiState.value = _uiState.value.copy(profile = profile)
            }
        }
    }

    fun loadTodayContent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getDailyContent(LocalDate.now())
                .onSuccess { content ->
                    _uiState.value = _uiState.value.copy(
                        todayContent = content,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load today's content"
                    )
                }
        }
    }

    fun refreshContent() {
        loadTodayContent()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HomeUiState(
    val profile: Profile? = null,
    val todayContent: DailyContent? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
