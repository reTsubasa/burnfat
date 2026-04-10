package com.burnfat

import android.app.Application
import com.burnfat.service.AchievementSyncService
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BurnFatApp : Application() {

    @Inject
    lateinit var achievementSyncService: AchievementSyncService

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // 后台同步历史达标记录
        applicationScope.launch {
            achievementSyncService.syncUnsyncedAchievements()
        }
    }
}