package com.example.lexipath.data.local.dao

import androidx.room.*
import com.example.lexipath.data.local.entities.DailyContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyContentDao {
    
    @Query("SELECT * FROM daily_content WHERE userId = :userId AND date = :date")
    suspend fun getContentByDate(userId: String, date: String): DailyContentEntity?
    
    @Query("SELECT * FROM daily_content WHERE date = :date LIMIT 1")
    fun getContentByDate(date: String): Flow<DailyContentEntity?>
    
    @Query("SELECT * FROM daily_content WHERE userId = :userId ORDER BY date DESC LIMIT :limit OFFSET :offset")
    suspend fun getContentHistory(userId: String, limit: Int, offset: Int): List<DailyContentEntity>
    
    @Query("SELECT * FROM daily_content WHERE userId = :userId ORDER BY date DESC LIMIT 14")
    fun getRecentContent(userId: String): Flow<List<DailyContentEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: DailyContentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contents: List<DailyContentEntity>)
    
    @Query("DELETE FROM daily_content WHERE cachedAt < :cutoffTime")
    suspend fun deleteOldCache(cutoffTime: Long)
    
    @Query("DELETE FROM daily_content WHERE userId = :userId")
    suspend fun deleteUserContent(userId: String)
}
