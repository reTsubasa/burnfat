package com.burnfat.di

import android.content.Context
import androidx.room.Room
import com.burnfat.data.local.database.BurnFatDatabase
import com.burnfat.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BurnFatDatabase {
        return Room.databaseBuilder(
            context,
            BurnFatDatabase::class.java,
            "burnfat_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserProfileDao(database: BurnFatDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    fun providePlanDao(database: BurnFatDatabase): PlanDao {
        return database.planDao()
    }

    @Provides
    fun provideDailyRecordDao(database: BurnFatDatabase): DailyRecordDao {
        return database.dailyRecordDao()
    }

    @Provides
    fun provideFoodEntryDao(database: BurnFatDatabase): FoodEntryDao {
        return database.foodEntryDao()
    }

    @Provides
    fun provideExerciseEntryDao(database: BurnFatDatabase): ExerciseEntryDao {
        return database.exerciseEntryDao()
    }

    @Provides
    fun provideWeightHistoryDao(database: BurnFatDatabase): WeightHistoryDao {
        return database.weightHistoryDao()
    }

    @Provides
    fun provideBmrHistoryDao(database: BurnFatDatabase): BmrHistoryDao {
        return database.bmrHistoryDao()
    }

    @Provides
    fun provideAchievementDao(database: BurnFatDatabase): AchievementDao {
        return database.achievementDao()
    }
}