package com.example.lexipath.data.local.dao

import androidx.room.*
import com.example.lexipath.data.local.entities.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    
    @Query("SELECT * FROM profiles WHERE userId = :userId")
    suspend fun getProfileByUserId(userId: String): ProfileEntity?
    
    @Query("SELECT * FROM profiles WHERE userId = :userId")
    fun getProfileFlow(userId: String): Flow<ProfileEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)
    
    @Query("DELETE FROM profiles WHERE userId = :userId")
    suspend fun deleteUserProfile(userId: String)
}
