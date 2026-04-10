package com.burnfat.data.repository

import com.burnfat.data.local.dao.UserProfileDao
import com.burnfat.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val userProfileDao: UserProfileDao
) {
    suspend fun getProfile(): UserProfileEntity? {
        return userProfileDao.getProfile()
    }

    fun getProfileFlow(): Flow<UserProfileEntity?> {
        return userProfileDao.getProfileFlow()
    }

    suspend fun hasProfile(): Boolean {
        return userProfileDao.hasProfile()
    }

    suspend fun saveProfile(
        age: Int,
        gender: String,
        hasCompletedOnboarding: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val profile = UserProfileEntity(
            id = 0,
            age = age,
            gender = gender,
            hasCompletedOnboarding = hasCompletedOnboarding,
            createdAt = now,
            updatedAt = now
        )
        userProfileDao.insert(profile)
    }

    suspend fun updateAge(age: Int) {
        userProfileDao.updateAge(age, System.currentTimeMillis())
    }

    suspend fun updateGender(gender: String) {
        userProfileDao.updateGender(gender, System.currentTimeMillis())
    }

    suspend fun completeOnboarding() {
        userProfileDao.updateOnboardingStatus(true, System.currentTimeMillis())
    }

    suspend fun hasCompletedOnboarding(): Boolean {
        return userProfileDao.getProfile()?.hasCompletedOnboarding ?: false
    }
}