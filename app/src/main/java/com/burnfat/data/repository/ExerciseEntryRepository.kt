package com.burnfat.data.repository

import com.burnfat.data.local.dao.ExerciseEntryDao
import com.burnfat.data.local.entity.ExerciseEntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseEntryRepository @Inject constructor(
    private val exerciseEntryDao: ExerciseEntryDao
) {
    suspend fun getById(id: Long): ExerciseEntryEntity? {
        return exerciseEntryDao.getById(id)
    }

    suspend fun getByDate(date: Long): List<ExerciseEntryEntity> {
        return exerciseEntryDao.getByDate(date)
    }

    suspend fun getByDateRange(startDate: Long, endDate: Long): List<ExerciseEntryEntity> {
        return exerciseEntryDao.getByDateRange(startDate, endDate)
    }

    fun getAllFlow(): Flow<List<ExerciseEntryEntity>> {
        return exerciseEntryDao.getAllFlow()
    }

    suspend fun insert(entry: ExerciseEntryEntity): Long {
        return exerciseEntryDao.insert(entry)
    }

    suspend fun update(entry: ExerciseEntryEntity) {
        exerciseEntryDao.update(entry)
    }

    suspend fun deleteById(id: Long) {
        exerciseEntryDao.deleteById(id)
    }

    suspend fun getTotalCaloriesByDate(date: Long): Int {
        return exerciseEntryDao.getTotalCaloriesByDate(date) ?: 0
    }

    suspend fun getDistinctTypes(): List<String> {
        return exerciseEntryDao.getDistinctTypes()
    }
}