package com.burnfat.data.local.dao

import androidx.room.*
import com.burnfat.data.local.entity.WeightHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightHistoryDao {

    @Query("SELECT * FROM weight_history ORDER BY date ASC")
    suspend fun getAllOrdered(): List<WeightHistoryEntity>

    @Query("SELECT * FROM weight_history ORDER BY date ASC")
    fun getAllOrderedFlow(): Flow<List<WeightHistoryEntity>>

    @Query("SELECT * FROM weight_history WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getByDateRange(startDate: Long, endDate: Long): List<WeightHistoryEntity>

    @Query("SELECT * FROM weight_history ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): WeightHistoryEntity?

    @Query("SELECT * FROM weight_history ORDER BY date DESC LIMIT 1")
    fun getLatestFlow(): Flow<WeightHistoryEntity?>

    @Insert
    suspend fun insert(entity: WeightHistoryEntity): Long

    @Delete
    suspend fun delete(entity: WeightHistoryEntity)

    @Query("SELECT COUNT(*) FROM weight_history")
    suspend fun getCount(): Int

    @Query("DELETE FROM weight_history")
    suspend fun deleteAll()
}