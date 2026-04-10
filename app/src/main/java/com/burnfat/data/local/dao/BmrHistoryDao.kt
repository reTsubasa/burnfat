package com.burnfat.data.local.dao

import androidx.room.*
import com.burnfat.data.local.entity.BmrHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BmrHistoryDao {

    @Query("SELECT * FROM bmr_history ORDER BY date DESC")
    suspend fun getAll(): List<BmrHistoryEntity>

    @Query("SELECT * FROM bmr_history ORDER BY date DESC")
    fun getAllFlow(): Flow<List<BmrHistoryEntity>>

    @Query("SELECT * FROM bmr_history ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): BmrHistoryEntity?

    @Insert
    suspend fun insert(entity: BmrHistoryEntity)

    @Query("DELETE FROM bmr_history")
    suspend fun deleteAll()
}