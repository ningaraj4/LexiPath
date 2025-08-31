package com.example.lexipath.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.lexipath.data.local.dao.DailyContentDao
import com.example.lexipath.data.local.dao.ProfileDao
import com.example.lexipath.data.local.entities.Converters
import com.example.lexipath.data.local.entities.DailyContentEntity
import com.example.lexipath.data.local.entities.ProfileEntity
import com.example.lexipath.data.local.entities.QuizLogEntity
import com.example.lexipath.data.local.entities.WeeklyPlanEntity

@Database(
    entities = [
        DailyContentEntity::class,
        ProfileEntity::class,
        QuizLogEntity::class,
        WeeklyPlanEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LexiPathDatabase : RoomDatabase() {
    
    abstract fun dailyContentDao(): DailyContentDao
    abstract fun profileDao(): ProfileDao
    
    companion object {
        const val DATABASE_NAME = "lexipath_database"
        
        @Volatile
        private var INSTANCE: LexiPathDatabase? = null
        
        fun getDatabase(context: Context): LexiPathDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LexiPathDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        fun create(context: Context): LexiPathDatabase {
            return getDatabase(context)
        }
    }
}
