package com.example.lexipath.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import com.example.lexipath.data.repository.LexiPathRepository
import com.example.lexipath.widget.LexiPathWidgetReceiver
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import kotlinx.coroutines.delay

@HiltWorker
class DailyPrefetchWorker @AssistedInject constructor(
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

            val today = LocalDate.now().toString()
            var retryCount = 0
            val maxRetries = 3
            
            // Prefetch today's content with retry logic
            while (retryCount < maxRetries) {
                val result = repository.getTodaysContent()
                result.onSuccess { content ->
                    // Update widget after successful prefetch
                    LexiPathWidgetReceiver.updateWidget(applicationContext)
                    
                    return Result.success(
                        Data.Builder()
                            .putString("content_word", content.word)
                            .putString("prefetch_date", today)
                            .build()
                    )
                }
                result.onFailure { error ->
                    retryCount++
                    if (retryCount < maxRetries) {
                        // Exponential backoff
                        delay(1000L * retryCount * retryCount)
                    } else {
                        return Result.failure(
                            Data.Builder()
                                .putString("error", error.message ?: "Failed to prefetch content")
                                .putInt("retry_count", retryCount)
                                .build()
                        )
                    }
                }
            }
            
            return Result.failure(
                Data.Builder()
                    .putString("error", "Failed to prefetch content after $maxRetries attempts")
                    .build()
            )
        } catch (e: Exception) {
            Result.failure(
                Data.Builder()
                    .putString("error", e.message ?: "Unknown error during prefetch")
                    .build()
            )
        }
    }
}
