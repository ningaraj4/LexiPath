package com.example.lexipath.di

import android.content.Context
import com.example.lexipath.data.local.LexiPathDatabase
import com.example.lexipath.data.local.dao.DailyContentDao
import com.example.lexipath.data.local.dao.ProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LexiPathDatabase {
        return LexiPathDatabase.create(context)
    }

    @Provides
    fun provideDailyContentDao(database: LexiPathDatabase): DailyContentDao {
        return database.dailyContentDao()
    }

    @Provides
    fun provideProfileDao(database: LexiPathDatabase): ProfileDao {
        return database.profileDao()
    }
}
