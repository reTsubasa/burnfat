package com.burnfat.data.repository

import com.burnfat.data.local.dao.BmrHistoryDao
import com.burnfat.data.local.entity.BmrHistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BmrHistoryRepository @Inject constructor(
    private val bmrHistoryDao: BmrHistoryDao
) {
    suspend fun getAll(): List<BmrHistoryEntity> {
        return bmrHistoryDao.getAll()
    }

    fun getAllFlow(): Flow<List<BmrHistoryEntity>> {
        return bmrHistoryDao.getAllFlow()
    }

    suspend fun getLatest(): BmrHistoryEntity? {
        return bmrHistoryDao.getLatest()
    }

    suspend fun insert(entity: BmrHistoryEntity) {
        bmrHistoryDao.insert(entity)
    }
}