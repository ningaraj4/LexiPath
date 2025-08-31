package com.example.lexipath.data.models

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class User(
    val id: String,
    val firebaseUid: String,
    val email: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class Profile(
    val id: String,
    val userId: String,
    val goalType: GoalType,
    val targetLang: String? = null,
    val baseLang: String? = null,
    val level: LanguageLevel,
    val industrySector: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
enum class GoalType {
    language, industry
}

@Serializable
enum class LanguageLevel {
    beginner, intermediate, advanced
}

@Serializable
data class DailyContent(
    val id: String,
    val userId: String,
    val date: String,
    val word: String,
    val meaning: String,
    val examplesTarget: List<String>,
    val examplesBase: List<String> = emptyList(),
    val createdAt: String
)

@Serializable
data class QuizLog(
    val id: String,
    val userId: String,
    val contentId: String,
    val quizType: QuizType,
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val createdAt: String
)

@Serializable
enum class QuizType {
    mcq, fill_blank, situation
}

@Serializable
data class WeeklyPlan(
    val id: String,
    val userId: String,
    val weekStart: String,
    val plan: List<PlanItem>,
    val createdAt: String
)

@Serializable
data class PlanItem(
    val date: String,
    val contentIds: List<String>,
    val isReviewDay: Boolean
)

// Request DTOs
@Serializable
data class UpsertProfileRequest(
    val goalType: GoalType,
    val targetLang: String? = null,
    val baseLang: String? = null,
    val level: LanguageLevel,
    val industrySector: String? = null
)

@Serializable
data class DailyContentRequest(
    val date: String
)

@Serializable
data class QuizSubmissionRequest(
    val contentId: String,
    val quizType: QuizType,
    val userAnswer: String
)

@Serializable
data class TranslateRequest(
    val text: String,
    val targetLang: String,
    val baseLang: String
)

@Serializable
data class TranslateResponse(
    val translation: String
)

// Response wrappers
@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class ContentHistoryResponse(
    val content: List<DailyContent>,
    val limit: Int,
    val offset: Int
)
