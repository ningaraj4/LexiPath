package com.example.lexipath

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.lexipath.workers.WorkManagerScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LexiPathApplication : Application() {
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    @Inject
    lateinit var workManagerScheduler: WorkManagerScheduler
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WorkManager with Hilt
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        )
        
        // Schedule background workers
        workManagerScheduler.scheduleDailyPrefetch()
        workManagerScheduler.scheduleWeeklyPlanner()
    }
}
