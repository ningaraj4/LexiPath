package com.example.lexipath.workers

import android.content.Context
import androidx.work.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerScheduler @Inject constructor(
    private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleDailyPrefetch() {
        val dailyPrefetchRequest = PeriodicWorkRequestBuilder<DailyPrefetchWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .setInitialDelay(calculateInitialDelay(7, 0), TimeUnit.MILLISECONDS)
            .addTag("daily_prefetch")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_prefetch",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyPrefetchRequest
        )
    }

    fun scheduleWeeklyPlanner() {
        val weeklyPlannerRequest = PeriodicWorkRequestBuilder<WeeklyPlannerWorker>(
            repeatInterval = 7,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .setInitialDelay(calculateInitialDelayForSunday(7, 5), TimeUnit.MILLISECONDS)
            .addTag("weekly_planner")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "weekly_planner",
            ExistingPeriodicWorkPolicy.KEEP,
            weeklyPlannerRequest
        )
    }

    fun cancelAllWork() {
        workManager.cancelAllWorkByTag("daily_prefetch")
        workManager.cancelAllWorkByTag("weekly_planner")
    }

    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(hour, minute)
        var targetDateTime = now.toLocalDate().atTime(targetTime)
        
        // If target time has passed today, schedule for tomorrow
        if (targetDateTime.isBefore(now)) {
            targetDateTime = targetDateTime.plusDays(1)
        }
        
        return Duration.between(now, targetDateTime).toMillis()
    }

    private fun calculateInitialDelayForSunday(hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        val targetTime = LocalTime.of(hour, minute)
        var targetDateTime = now.toLocalDate().atTime(targetTime)
        
        // Find next Sunday
        val daysUntilSunday = (7 - now.dayOfWeek.value) % 7
        if (daysUntilSunday == 0 && targetDateTime.isBefore(now)) {
            // If it's Sunday but time has passed, schedule for next Sunday
            targetDateTime = targetDateTime.plusDays(7)
        } else {
            targetDateTime = targetDateTime.plusDays(daysUntilSunday.toLong())
        }
        
        return Duration.between(now, targetDateTime).toMillis()
    }
}
