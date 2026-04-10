package com.burnfat.data.local.dao

import androidx.room.*
import com.burnfat.data.local.entity.FoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {

    @Query("SELECT * FROM food_entry WHERE id = :id")
    suspend fun getById(id: Long): FoodEntryEntity?

    @Query("SELECT * FROM food_entry WHERE recordDate = :date ORDER BY createdAt DESC")
    suspend fun getByDate(date: Long): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entry WHERE recordDate = :date AND mealType = :mealType ORDER BY createdAt DESC")
    suspend fun getByDateAndMealType(date: Long, mealType: String): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entry WHERE recordDate BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    suspend fun getByDateRange(startDate: Long, endDate: Long): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entry ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entry ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<FoodEntryEntity>>

    @Insert
    suspend fun insert(entry: FoodEntryEntity): Long

    @Update
    suspend fun update(entry: FoodEntryEntity)

    @Delete
    suspend fun delete(entry: FoodEntryEntity)

    @Query("DELETE FROM food_entry WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM food_entry")
    suspend fun deleteAll()

    @Query("SELECT SUM(calories) FROM food_entry WHERE recordDate = :date")
    suspend fun getTotalCaloriesByDate(date: Long): Int?

    @Query("SELECT SUM(calories) FROM food_entry WHERE recordDate = :date AND mealType = :mealType")
    suspend fun getTotalByMealType(date: Long, mealType: String): Int?

    @Query("""
        SELECT mealType, SUM(calories) as total
        FROM food_entry
        WHERE recordDate = :date
        GROUP BY mealType
    """)
    suspend fun getCaloriesByMealType(date: Long): List<MealCaloriesSummary>
}

data class MealCaloriesSummary(
    val mealType: String,
    val total: Int
)