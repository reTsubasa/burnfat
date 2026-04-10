package com.burnfat.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.burnfat.data.local.dao.*
import com.burnfat.data.local.entity.*

@Database(
    entities = [
        UserProfileEntity::class,
        PlanEntity::class,
        DailyRecordEntity::class,
        FoodEntryEntity::class,
        ExerciseEntryEntity::class,
        WeightHistoryEntity::class,
        BmrHistoryEntity::class,
        AchievementEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BurnFatDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun planDao(): PlanDao
    abstract fun dailyRecordDao(): DailyRecordDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun exerciseEntryDao(): ExerciseEntryDao
    abstract fun weightHistoryDao(): WeightHistoryDao
    abstract fun bmrHistoryDao(): BmrHistoryDao
    abstract fun achievementDao(): AchievementDao
}