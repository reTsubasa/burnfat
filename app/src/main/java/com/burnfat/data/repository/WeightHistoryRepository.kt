package com.burnfat.data.repository

import com.burnfat.data.local.dao.WeightHistoryDao
import com.burnfat.data.local.entity.WeightHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightHistoryRepository @Inject constructor(
    private val weightHistoryDao: WeightHistoryDao
) {
    suspend fun getAllOrdered(): List<WeightHistoryEntity> {
        return weightHistoryDao.getAllOrdered()
    }

    fun getAllOrderedFlow(): Flow<List<WeightHistoryEntity>> {
        return weightHistoryDao.getAllOrderedFlow()
    }

    suspend fun getLatest(): WeightHistoryEntity? {
        return weightHistoryDao.getLatest()
    }

    fun getLatestFlow(): Flow<WeightHistoryEntity?> {
        return weightHistoryDao.getLatestFlow()
    }

    suspend fun insert(weight: Float, note: String? = null): Long {
        val now = System.currentTimeMillis()

        // 计算与上次记录的变化
        val latest = weightHistoryDao.getLatest()
        val changeFromPrevious = latest?.let { weight - it.weight }

        val entity = WeightHistoryEntity(
            date = now,
            weight = weight,
            changeFromPrevious = changeFromPrevious,
            note = note,
            createdAt = now
        )

        return weightHistoryDao.insert(entity)
    }

    suspend fun getCount(): Int {
        return weightHistoryDao.getCount()
    }
}