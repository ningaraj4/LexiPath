package com.example.lexipath.data.repository

import com.example.lexipath.data.local.LexiPathDatabase
import com.example.lexipath.data.local.entities.DailyContentEntity
import com.example.lexipath.data.local.entities.ProfileEntity
import com.example.lexipath.data.models.*
import com.example.lexipath.data.remote.ApiService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Singleton
class LexiPathRepository @Inject constructor(
    private val apiService: ApiService,
    private val database: LexiPathDatabase,
    private val firebaseAuth: FirebaseAuth
) {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    suspend fun upsertProfile(request: UpsertProfileRequest): Result<Profile> {
        return try {
            val response = apiService.upsertProfile(request)
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                // Cache locally
                database.profileDao().insertProfile(profile.toEntity())
                Result.success(profile)
            } else {
                Result.failure(Exception("Failed to update profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTodaysContent(): Result<DailyContent> {
        return getDailyContent(LocalDate.now())
    }
    
    suspend fun getDailyContent(date: LocalDate): Result<DailyContent> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
        val dateString = date.format(dateFormatter)
        
        return try {
            // Try local cache first
            val cached = database.dailyContentDao().getContentByDate(userId, dateString)
            if (cached != null) {
                return Result.success(cached.toDailyContent())
            }
            
            // Fetch from API
            val response = apiService.getDailyContent(DailyContentRequest(dateString))
            if (response.isSuccessful && response.body() != null) {
                val content = response.body()!!
                // Cache locally
                database.dailyContentDao().insertContent(content.toEntity())
                Result.success(content)
            } else {
                Result.failure(Exception("Failed to get daily content: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun submitQuiz(request: QuizSubmissionRequest): Result<QuizLog> {
        return try {
            val response = apiService.submitQuiz(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to submit quiz: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun generateWeeklyPlan(): Result<WeeklyPlan> {
        return try {
            val response = apiService.generateWeeklyPlan()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to generate weekly plan: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun translate(request: TranslateRequest): Result<TranslateResponse> {
        return try {
            val response = apiService.translate(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to translate: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getContentHistory(limit: Int = 20, offset: Int = 0): Result<List<DailyContent>> {
        val userId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            // Try local cache first for offline support
            if (offset == 0) {
                val cached = database.dailyContentDao().getContentHistory(userId, limit, offset)
                if (cached.isNotEmpty()) {
                    return Result.success(cached.map { it.toDailyContent() })
                }
            }
            
            // Fetch from API
            val response = apiService.getContentHistory(limit, offset)
            if (response.isSuccessful && response.body() != null) {
                val content = response.body()!!.content
                // Cache first page locally
                if (offset == 0) {
                    database.dailyContentDao().insertAll(content.map { it.toEntity() })
                }
                Result.success(content)
            } else {
                Result.failure(Exception("Failed to get content history: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getProfileFlow(): Flow<Profile?> {
        val userId = getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(null)
        return database.profileDao().getProfileFlow(userId).map { it?.toProfile() }
    }
    
    fun getRecentContentFlow(): Flow<List<DailyContent>> {
        val userId = getCurrentUserId() ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return database.dailyContentDao().getRecentContent(userId).map { entities ->
            entities.map { it.toDailyContent() }
        }
    }
    
    suspend fun clearUserData() {
        val userId = getCurrentUserId() ?: return
        database.dailyContentDao().deleteUserContent(userId)
        database.profileDao().deleteUserProfile(userId)
    }
    
    suspend fun cleanOldCache() {
        val cutoffTime = System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000L) // 14 days
        database.dailyContentDao().deleteOldCache(cutoffTime)
    }
    
    private fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
}

// Extension functions for mapping between API models and local entities
private fun Profile.toEntity(): ProfileEntity {
    return ProfileEntity(
        id = id,
        userId = userId,
        goalType = goalType.name,
        targetLang = targetLang,
        baseLang = baseLang,
        level = level.name,
        industrySector = industrySector,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun ProfileEntity.toProfile(): Profile {
    return Profile(
        id = id,
        userId = userId,
        goalType = GoalType.valueOf(goalType),
        targetLang = targetLang,
        baseLang = baseLang,
        level = LanguageLevel.valueOf(level),
        industrySector = industrySector,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

private fun DailyContent.toEntity(): DailyContentEntity {
    return DailyContentEntity(
        id = id,
        userId = userId,
        date = date,
        word = word,
        meaning = meaning,
        examplesTarget = examplesTarget,
        examplesBase = examplesBase,
        createdAt = createdAt
    )
}

private fun DailyContentEntity.toDailyContent(): DailyContent {
    return DailyContent(
        id = id,
        userId = userId,
        date = date,
        word = word,
        meaning = meaning,
        examplesTarget = examplesTarget,
        examplesBase = examplesBase,
        createdAt = createdAt
    )
}
