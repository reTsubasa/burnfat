package com.burnfat.data.repository

import com.burnfat.data.local.dao.DailyRecordDao
import com.burnfat.data.local.entity.DailyRecordEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyRecordRepository @Inject constructor(
    private val dailyRecordDao: DailyRecordDao
) {
    suspend fun getByDate(date: Long): DailyRecordEntity? {
        return dailyRecordDao.getByDate(date)
    }

    fun getByDateFlow(date: Long): Flow<DailyRecordEntity?> {
        return dailyRecordDao.getByDateFlow(date)
    }

    suspend fun getByDateRange(startDate: Long, endDate: Long): List<DailyRecordEntity> {
        return dailyRecordDao.getByDateRange(startDate, endDate)
    }

    fun getAllRecordsFlow(): Flow<List<DailyRecordEntity>> {
        return dailyRecordDao.getAllRecordsFlow()
    }

    suspend fun getOrCreateToday(date: Long, targetCalories: Int): DailyRecordEntity {
        return dailyRecordDao.getOrCreateToday(date, targetCalories)
    }

    suspend fun addFoodCalories(date: Long, calories: Int, mealType: String) {
        dailyRecordDao.addFoodCalories(date, calories, mealType, System.currentTimeMillis())
    }

    suspend fun addExerciseCalories(date: Long, calories: Int) {
        dailyRecordDao.addExerciseCalories(date, calories, System.currentTimeMillis())
    }

    suspend fun subtractFoodCalories(date: Long, calories: Int, mealType: String) {
        dailyRecordDao.subtractFoodCalories(date, calories, mealType, System.currentTimeMillis())
    }

    suspend fun subtractExerciseCalories(date: Long, calories: Int) {
        dailyRecordDao.subtractExerciseCalories(date, calories, System.currentTimeMillis())
    }

    suspend fun countAchievedDays(startDate: Long, endDate: Long): Int {
        return dailyRecordDao.countAchievedDays(startDate, endDate)
    }

    suspend fun getUnsyncedAchievedRecords(): List<DailyRecordEntity> {
        return dailyRecordDao.getUnsyncedAchievedRecords()
    }

    suspend fun markAsSynced(date: Long) {
        dailyRecordDao.markAsSynced(date, System.currentTimeMillis())
    }

    suspend fun update(record: DailyRecordEntity) {
        dailyRecordDao.update(record)
    }

    companion object {
        fun LocalDate.toEpochDayLong(): Long {
            return this.toEpochDay()
        }
    }
}