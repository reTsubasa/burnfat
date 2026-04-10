package com.burnfat.data.repository

import com.burnfat.data.local.dao.AchievementDao
import com.burnfat.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao
) {
    suspend fun getAll(): List<AchievementEntity> {
        return achievementDao.getAll()
    }

    fun getAllFlow(): Flow<List<AchievementEntity>> {
        return achievementDao.getAllFlow()
    }

    suspend fun getByDateRange(startDate: Long, endDate: Long): List<AchievementEntity> {
        return achievementDao.getByDateRange(startDate, endDate)
    }

    suspend fun countPerfectDays(): Int {
        return achievementDao.countPerfectDays()
    }

    suspend fun countAchievedDays(): Int {
        return achievementDao.countAchievedDays()
    }

    suspend fun getMaxStreak(): Int {
        return achievementDao.getMaxStreak() ?: 0
    }

    suspend fun insert(entity: AchievementEntity): Long {
        return achievementDao.insert(entity)
    }

    suspend fun getTopStreaks(): List<AchievementEntity> {
        return achievementDao.getTopStreaks()
    }
}