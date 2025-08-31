package com.example.lexipath.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexipath.data.models.GoalType
import com.example.lexipath.data.models.LanguageLevel
import com.example.lexipath.data.models.UpsertProfileRequest
import com.example.lexipath.data.repository.LexiPathRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: LexiPathRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateGoalType(goalType: GoalType) {
        _uiState.value = _uiState.value.copy(goalType = goalType)
    }

    fun updateLanguageLevel(level: LanguageLevel) {
        _uiState.value = _uiState.value.copy(level = level)
    }

    fun updateTargetLanguage(language: String) {
        _uiState.value = _uiState.value.copy(targetLang = language)
    }

    fun updateBaseLanguage(language: String) {
        _uiState.value = _uiState.value.copy(baseLang = language)
    }

    fun updateIndustrySector(sector: String) {
        _uiState.value = _uiState.value.copy(industrySector = sector)
    }

    fun saveProfile() {
        val state = _uiState.value
        if (!isProfileValid(state)) {
            _uiState.value = state.copy(error = "Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            
            val request = UpsertProfileRequest(
                goalType = state.goalType,
                targetLang = state.targetLang.takeIf { state.goalType == GoalType.language },
                baseLang = state.baseLang.takeIf { state.goalType == GoalType.language },
                level = state.level,
                industrySector = state.industrySector.takeIf { state.goalType == GoalType.industry }
            )

            repository.upsertProfile(request)
                .onSuccess {
                    _uiState.value = state.copy(
                        isLoading = false,
                        isCompleted = true,
                        error = null
                    )
                }
                .onFailure { error ->
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to save profile"
                    )
                }
        }
    }

    private fun isProfileValid(state: OnboardingUiState): Boolean {
        return when (state.goalType) {
            GoalType.language -> {
                state.targetLang.isNotBlank() && state.baseLang.isNotBlank()
            }
            GoalType.industry -> {
                state.industrySector.isNotBlank()
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class OnboardingUiState(
    val goalType: GoalType = GoalType.language,
    val level: LanguageLevel = LanguageLevel.beginner,
    val targetLang: String = "",
    val baseLang: String = "",
    val industrySector: String = "",
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)
