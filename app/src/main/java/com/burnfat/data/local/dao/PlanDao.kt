package com.burnfat.data.local.dao

import androidx.room.*
import com.burnfat.data.local.entity.PlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plan ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plan ORDER BY createdAt DESC")
    suspend fun getAll(): List<PlanEntity>

    @Query("SELECT * FROM plan WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlan(): PlanEntity?

    @Query("SELECT * FROM plan WHERE isActive = 1 LIMIT 1")
    fun getActivePlanFlow(): Flow<PlanEntity?>

    @Query("SELECT * FROM plan WHERE id = :id")
    suspend fun getById(id: Long): PlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: PlanEntity): Long

    @Update
    suspend fun update(plan: PlanEntity)

    @Delete
    suspend fun delete(plan: PlanEntity)

    @Query("UPDATE plan SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE plan SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: Long)

    @Transaction
    suspend fun activatePlan(id: Long) {
        deactivateAll()
        setActive(id)
    }
}