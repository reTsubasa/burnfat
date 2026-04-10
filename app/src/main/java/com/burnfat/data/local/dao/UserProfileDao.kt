package com.burnfat.data.local.dao

import androidx.room.*
import com.burnfat.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 0")
    suspend fun getProfile(): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE id = 0")
    fun getProfileFlow(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity)

    @Update
    suspend fun update(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET age = :age, updatedAt = :updatedAt WHERE id = 0")
    suspend fun updateAge(age: Int, updatedAt: Long)

    @Query("UPDATE user_profile SET gender = :gender, updatedAt = :updatedAt WHERE id = 0")
    suspend fun updateGender(gender: String, updatedAt: Long)

    @Query("UPDATE user_profile SET hasCompletedOnboarding = :completed, updatedAt = :updatedAt WHERE id = 0")
    suspend fun updateOnboardingStatus(completed: Boolean, updatedAt: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM user_profile)")
    suspend fun hasProfile(): Boolean
}