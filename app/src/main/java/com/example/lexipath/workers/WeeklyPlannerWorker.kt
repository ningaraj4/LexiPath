package com.example.lexipath.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import com.example.lexipath.data.repository.LexiPathRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@HiltWorker
class WeeklyPlannerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: LexiPathRepository,
    private val firebaseAuth: FirebaseAuth
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Check if user is authenticated
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return Result.failure(
                    Data.Builder()
                        .putString("error", "User not authenticated")
                        .build()
                )
            }

            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            
            // Generate weekly plan (should run on Sundays)
            if (today.dayOfWeek.value == 7) { // Sunday
                val weekStart = today.format(formatter)
                var retryCount = 0
                val maxRetries = 2
                
                while (retryCount < maxRetries) {
                    val result = repository.generateWeeklyPlan()
                    result.onSuccess { plan ->
                        return Result.success(
                            Data.Builder()
                                .putString("plan_title", "Weekly Plan Generated")
                                .putString("generation_date", LocalDate.now().toString())
                                .putString("plan_description", "Plan created successfully")
                                .build()
                        )
                    }
                    result.onFailure { error ->
                            retryCount++
                            if (retryCount < maxRetries) {
                                delay(5000L * retryCount) // 5s, 10s delays
                            } else {
                                return Result.failure(
                                    Data.Builder()
                                        .putString("error", error.message ?: "Failed to generate weekly plan")
                                        .putString("week_start", weekStart)
                                        .putInt("retry_count", retryCount)
                                        .build()
                                )
                            }
                        }
                }
            } else {
                // Not Sunday, skip execution
                return Result.success(
                    Data.Builder()
                        .putString("title", "Weekly Plan Generated")
                        .putString("description", "Plan created successfully")
                        .build()
                )
            }
            
            Result.failure()
        } catch (e: Exception) {
            Result.failure(
                Data.Builder()
                    .putString("error", e.message ?: "Unknown error during weekly planning")
                    .build()
            )
        }
    }
}
