package com.burnfat.data.repository

import com.burnfat.data.local.dao.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataClearRepository @Inject constructor(
    private val dailyRecordDao: DailyRecordDao,
    private val foodEntryDao: FoodEntryDao,
    private val exerciseEntryDao: ExerciseEntryDao,
    private val weightHistoryDao: WeightHistoryDao,
    private val bmrHistoryDao: BmrHistoryDao,
    private val achievementDao: AchievementDao
) {
    suspend fun clearAllData() {
        // 按照外键依赖顺序删除
        foodEntryDao.deleteAll()
        exerciseEntryDao.deleteAll()
        achievementDao.deleteAll()
        weightHistoryDao.deleteAll()
        bmrHistoryDao.deleteAll()
        dailyRecordDao.deleteAll()
    }
}