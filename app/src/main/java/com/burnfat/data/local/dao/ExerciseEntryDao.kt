package com.burnfat.data.local.dao

import androidx.room.*
import com.burnfat.data.local.entity.ExerciseEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseEntryDao {

    @Query("SELECT * FROM exercise_entry WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntryEntity?

    @Query("SELECT * FROM exercise_entry WHERE recordDate = :date ORDER BY createdAt DESC")
    suspend fun getByDate(date: Long): List<ExerciseEntryEntity>

    @Query("SELECT * FROM exercise_entry WHERE recordDate BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getByDateRange(startDate: Long, endDate: Long): List<ExerciseEntryEntity>

    @Query("SELECT * FROM exercise_entry ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<ExerciseEntryEntity>>

    @Insert
    suspend fun insert(entry: ExerciseEntryEntity): Long

    @Update
    suspend fun update(entry: ExerciseEntryEntity)

    @Delete
    suspend fun delete(entry: ExerciseEntryEntity)

    @Query("DELETE FROM exercise_entry WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT SUM(caloriesBurned) FROM exercise_entry WHERE recordDate = :date")
    suspend fun getTotalCaloriesByDate(date: Long): Int?

    @Query("SELECT DISTINCT exerciseType FROM exercise_entry ORDER BY exerciseType")
    suspend fun getDistinctTypes(): List<String>

    @Query("DELETE FROM exercise_entry")
    suspend fun deleteAll()
}