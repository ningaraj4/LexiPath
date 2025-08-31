package com.example.lexipath.data.remote

import com.example.lexipath.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @POST("v1/profile/upsert")
    suspend fun upsertProfile(
        @Body request: UpsertProfileRequest
    ): Response<Profile>
    
    @POST("v1/daily-content")
    suspend fun getDailyContent(
        @Body request: DailyContentRequest
    ): Response<DailyContent>
    
    @POST("v1/quiz/submit")
    suspend fun submitQuiz(
        @Body request: QuizSubmissionRequest
    ): Response<QuizLog>
    
    @POST("v1/weekly-plan/generate")
    suspend fun generateWeeklyPlan(): Response<WeeklyPlan>
    
    @POST("v1/translate")
    suspend fun translate(
        @Body request: TranslateRequest
    ): Response<TranslateResponse>
    
    @GET("v1/content/history")
    suspend fun getContentHistory(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): Response<ContentHistoryResponse>
    
    @GET("healthz")
    suspend fun healthCheck(): Response<Map<String, Any>>
}
