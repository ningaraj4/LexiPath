package com.example.lexipath.di

import android.content.Context
import com.example.lexipath.workers.WorkManagerScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    @Singleton
    fun provideWorkManagerScheduler(
        @ApplicationContext context: Context
    ): WorkManagerScheduler {
        return WorkManagerScheduler(context)
    }
}
