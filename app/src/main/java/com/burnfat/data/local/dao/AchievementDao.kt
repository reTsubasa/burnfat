package com.burnfat.data.local.dao

import androidx.room.*
import com.burnfat.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievement ORDER BY date DESC")
    suspend fun getAll(): List<AchievementEntity>

    @Query("SELECT * FROM achievement ORDER BY date DESC")
    fun getAllFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievement WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getByDateRange(startDate: Long, endDate: Long): List<AchievementEntity>

    @Query("SELECT COUNT(*) FROM achievement WHERE type = 'PERFECT'")
    suspend fun countPerfectDays(): Int

    @Query("SELECT COUNT(*) FROM achievement WHERE type IN ('PERFECT', 'GOOD', 'ACCEPTABLE')")
    suspend fun countAchievedDays(): Int

    @Query("SELECT MAX(streakDays) FROM achievement")
    suspend fun getMaxStreak(): Int?

    @Insert
    suspend fun insert(entity: AchievementEntity): Long

    @Query("""
        SELECT * FROM achievement
        WHERE streakDays > 0
        ORDER BY streakDays DESC
        LIMIT 10
    """)
    suspend fun getTopStreaks(): List<AchievementEntity>

    @Query("DELETE FROM achievement")
    suspend fun deleteAll()
}