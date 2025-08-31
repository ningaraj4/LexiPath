package com.example.lexipath.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexipath.data.models.DailyContent
import com.example.lexipath.data.models.QuizSubmissionRequest
import com.example.lexipath.data.models.QuizType
import com.example.lexipath.data.repository.LexiPathRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: LexiPathRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun setContent(content: DailyContent) {
        _uiState.value = _uiState.value.copy(
            content = content,
            currentQuizType = QuizType.mcq
        )
        generateQuizQuestion()
    }

    fun selectQuizType(quizType: QuizType) {
        _uiState.value = _uiState.value.copy(currentQuizType = quizType)
        generateQuizQuestion()
    }

    private fun generateQuizQuestion() {
        val state = _uiState.value
        val content = state.content ?: return
        
        val question = when (state.currentQuizType) {
            QuizType.mcq -> generateMCQQuestion(content)
            QuizType.fill_blank -> generateFillBlankQuestion(content)
            QuizType.situation -> generateSituationQuestion(content)
        }
        
        _uiState.value = state.copy(
            currentQuestion = question,
            userAnswer = "",
            showResult = false
        )
    }

    private fun generateMCQQuestion(content: DailyContent): QuizQuestion {
        val options = listOf(
            content.meaning,
            "Alternative meaning 1",
            "Alternative meaning 2", 
            "Alternative meaning 3"
        ).shuffled()
        
        return QuizQuestion(
            question = "What does '${content.word}' mean?",
            options = options,
            correctAnswer = content.meaning,
            type = QuizType.mcq
        )
    }

    private fun generateFillBlankQuestion(content: DailyContent): QuizQuestion {
        val example = content.examplesTarget.firstOrNull() ?: ""
        val questionText = example.replace(content.word, "_____", ignoreCase = true)
        
        return QuizQuestion(
            question = "Fill in the blank: $questionText",
            options = emptyList(),
            correctAnswer = content.word,
            type = QuizType.fill_blank
        )
    }

    private fun generateSituationQuestion(content: DailyContent): QuizQuestion {
        return QuizQuestion(
            question = "In what situation would you use a word that means '${content.meaning}'?",
            options = emptyList(),
            correctAnswer = content.word,
            type = QuizType.situation
        )
    }

    fun submitAnswer(answer: String) {
        val state = _uiState.value
        val content = state.content ?: return
        val question = state.currentQuestion ?: return

        _uiState.value = state.copy(
            userAnswer = answer,
            isSubmitting = true
        )

        viewModelScope.launch {
            val request = QuizSubmissionRequest(
                contentId = content.id,
                quizType = question.type,
                userAnswer = answer
            )

            repository.submitQuiz(request)
                .onSuccess { quizLog ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        showResult = true,
                        isCorrect = quizLog.isCorrect,
                        correctAnswer = quizLog.correctAnswer
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = error.message ?: "Failed to submit quiz"
                    )
                }
        }
    }

    fun nextQuestion() {
        val currentType = _uiState.value.currentQuizType
        val nextType = when (currentType) {
            QuizType.mcq -> QuizType.fill_blank
            QuizType.fill_blank -> QuizType.situation
            QuizType.situation -> QuizType.mcq
        }
        selectQuizType(nextType)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class QuizUiState(
    val content: DailyContent? = null,
    val currentQuizType: QuizType = QuizType.mcq,
    val currentQuestion: QuizQuestion? = null,
    val userAnswer: String = "",
    val isSubmitting: Boolean = false,
    val showResult: Boolean = false,
    val isCorrect: Boolean = false,
    val correctAnswer: String = "",
    val error: String? = null
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val type: QuizType
)
