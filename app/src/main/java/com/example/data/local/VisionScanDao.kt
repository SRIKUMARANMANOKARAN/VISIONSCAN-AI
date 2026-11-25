package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles ORDER BY createdAt DESC")
    fun observeAllProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun observeProfile(id: Long): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE isDemoMode = 1 LIMIT 1")
    suspend fun getDemoProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)
}

@Dao
interface ScreeningSessionDao {
    @Query("SELECT * FROM screening_sessions WHERE userId = :userId ORDER BY updatedAt DESC")
    fun observeSessionsForUser(userId: Long): Flow<List<ScreeningSessionEntity>>

    @Query("SELECT * FROM screening_sessions WHERE id = :sessionId LIMIT 1")
    fun observeSessionById(sessionId: Long): Flow<ScreeningSessionEntity?>

    @Query("SELECT * FROM screening_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): ScreeningSessionEntity?

    @Query("SELECT * FROM screening_sessions WHERE userId = :userId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestSession(userId: Long): ScreeningSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ScreeningSessionEntity): Long

    @Update
    suspend fun updateSession(session: ScreeningSessionEntity)

    @Query("DELETE FROM screening_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("DELETE FROM screening_sessions WHERE userId = :userId")
    suspend fun deleteAllSessionsForUser(userId: Long)
}
