package com.burnfat.data.local.dao

import androidx.room.*
import com.burnfat.data.local.entity.DailyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyRecordDao {

    @Query("SELECT * FROM daily_record WHERE date = :date")
    suspend fun getByDate(date: Long): DailyRecordEntity?

    @Query("SELECT * FROM daily_record WHERE date = :date")
    fun getByDateFlow(date: Long): Flow<DailyRecordEntity?>

    @Query("SELECT * FROM daily_record WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getByDateRange(startDate: Long, endDate: Long): List<DailyRecordEntity>

    @Query("SELECT * FROM daily_record ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentRecords(limit: Int): List<DailyRecordEntity>

    @Query("SELECT * FROM daily_record ORDER BY date DESC")
    fun getAllRecordsFlow(): Flow<List<DailyRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DailyRecordEntity)

    @Update
    suspend fun update(record: DailyRecordEntity)

    @Query("""
        UPDATE daily_record
        SET intakeCalories = intakeCalories + :calories,
            breakfastCalories = breakfastCalories + CASE WHEN :mealType = 'BREAKFAST' THEN :calories ELSE 0 END,
            lunchCalories = lunchCalories + CASE WHEN :mealType = 'LUNCH' THEN :calories ELSE 0 END,
            dinnerCalories = dinnerCalories + CASE WHEN :mealType = 'DINNER' THEN :calories ELSE 0 END,
            snackCalories = snackCalories + CASE WHEN :mealType = 'SNACK' THEN :calories ELSE 0 END,
            foodEntryCount = foodEntryCount + 1,
            updatedAt = :timestamp
        WHERE date = :date
    """)
    suspend fun addFoodCalories(date: Long, calories: Int, mealType: String, timestamp: Long)

    @Query("""
        UPDATE daily_record
        SET exerciseCalories = exerciseCalories + :calories,
            exerciseEntryCount = exerciseEntryCount + 1,
            updatedAt = :timestamp
        WHERE date = :date
    """)
    suspend fun addExerciseCalories(date: Long, calories: Int, timestamp: Long)

    @Query("""
        UPDATE daily_record
        SET intakeCalories = intakeCalories - :calories,
            breakfastCalories = breakfastCalories - CASE WHEN :mealType = 'BREAKFAST' THEN :calories ELSE 0 END,
            lunchCalories = lunchCalories - CASE WHEN :mealType = 'LUNCH' THEN :calories ELSE 0 END,
            dinnerCalories = dinnerCalories - CASE WHEN :mealType = 'DINNER' THEN :calories ELSE 0 END,
            snackCalories = snackCalories - CASE WHEN :mealType = 'SNACK' THEN :calories ELSE 0 END,
            foodEntryCount = foodEntryCount - 1,
            updatedAt = :timestamp
        WHERE date = :date
    """)
    suspend fun subtractFoodCalories(date: Long, calories: Int, mealType: String, timestamp: Long)

    @Query("""
        UPDATE daily_record
        SET exerciseCalories = exerciseCalories - :calories,
            exerciseEntryCount = exerciseEntryCount - 1,
            updatedAt = :timestamp
        WHERE date = :date
    """)
    suspend fun subtractExerciseCalories(date: Long, calories: Int, timestamp: Long)

    @Query("SELECT COUNT(*) FROM daily_record WHERE achieved = 1 AND date BETWEEN :startDate AND :endDate")
    suspend fun countAchievedDays(startDate: Long, endDate: Long): Int

    @Query("SELECT * FROM daily_record WHERE achieved = 1 ORDER BY date DESC LIMIT :limit")
    suspend fun getAchievedRecords(limit: Int): List<DailyRecordEntity>

    @Query("SELECT * FROM daily_record WHERE achieved = 1 AND isSyncedToAchievement = 0 ORDER BY date DESC")
    suspend fun getUnsyncedAchievedRecords(): List<DailyRecordEntity>

    @Query("UPDATE daily_record SET isSyncedToAchievement = 1, updatedAt = :timestamp WHERE date = :date")
    suspend fun markAsSynced(date: Long, timestamp: Long)

    @Transaction
    suspend fun getOrCreateToday(date: Long, targetCalories: Int): DailyRecordEntity {
        return getByDate(date) ?: run {
            val record = DailyRecordEntity(
                date = date,
                intakeCalories = 0,
                breakfastCalories = 0,
                lunchCalories = 0,
                dinnerCalories = 0,
                snackCalories = 0,
                exerciseCalories = 0,
                targetCalories = targetCalories,
                achieved = false,
                achievementType = null,
                weightRecord = null,
                foodEntryCount = 0,
                exerciseEntryCount = 0,
                isSyncedToAchievement = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            insert(record)
            record
        }
    }

    @Query("DELETE FROM daily_record")
    suspend fun deleteAll()
}