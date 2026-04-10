package com.burnfat.data.repository

import com.burnfat.data.local.dao.FoodEntryDao
import com.burnfat.data.local.entity.FoodEntryEntity
import com.burnfat.data.local.dao.MealCaloriesSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodEntryRepository @Inject constructor(
    private val foodEntryDao: FoodEntryDao
) {
    suspend fun getById(id: Long): FoodEntryEntity? {
        return foodEntryDao.getById(id)
    }

    suspend fun getByDate(date: Long): List<FoodEntryEntity> {
        return foodEntryDao.getByDate(date)
    }

    suspend fun getByDateAndMealType(date: Long, mealType: String): List<FoodEntryEntity> {
        return foodEntryDao.getByDateAndMealType(date, mealType)
    }

    suspend fun getByDateRange(startDate: Long, endDate: Long): List<FoodEntryEntity> {
        return foodEntryDao.getByDateRange(startDate, endDate)
    }

    fun getAllFlow(): Flow<List<FoodEntryEntity>> {
        return foodEntryDao.getAllFlow()
    }

    suspend fun insert(entry: FoodEntryEntity): Long {
        return foodEntryDao.insert(entry)
    }

    suspend fun update(entry: FoodEntryEntity) {
        foodEntryDao.update(entry)
    }

    suspend fun deleteById(id: Long) {
        foodEntryDao.deleteById(id)
    }

    suspend fun getTotalCaloriesByDate(date: Long): Int {
        return foodEntryDao.getTotalCaloriesByDate(date) ?: 0
    }

    suspend fun getCaloriesByMealType(date: Long): List<MealCaloriesSummary> {
        return foodEntryDao.getCaloriesByMealType(date)
    }
}