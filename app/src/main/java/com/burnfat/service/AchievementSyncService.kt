package com.burnfat.service

import android.content.Context
import com.burnfat.data.local.entity.AchievementEntity
import com.burnfat.data.repository.AchievementRepository
import com.burnfat.data.repository.DailyRecordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 达标记录同步服务
 * 负责检查历史日期的达标情况，并将达标记录同步到达标墙
 */
@Singleton
class AchievementSyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dailyRecordRepository: DailyRecordRepository,
    private val achievementRepository: AchievementRepository
) {

    /**
     * 同步所有未同步的达标记录
     * 在应用启动时调用
     */
    suspend fun syncUnsyncedAchievements() = withContext(Dispatchers.IO) {
        try {
            val unsyncedRecords = dailyRecordRepository.getUnsyncedAchievedRecords()

            if (unsyncedRecords.isEmpty()) {
                return@withContext
            }

            unsyncedRecords.forEach { record ->
                try {
                    val totalBudget = record.targetCalories + record.exerciseCalories
                    val caloriesDeficit = totalBudget - record.intakeCalories
                    val achievementType = determineAchievementType(record)

                    val achievement = AchievementEntity(
                        date = record.date,
                        type = achievementType,
                        streakDays = 0,
                        badgeEarned = null,
                        caloriesDeficit = caloriesDeficit,
                        createdAt = System.currentTimeMillis()
                    )

                    achievementRepository.insert(achievement)
                    dailyRecordRepository.markAsSynced(record.date)
                } catch (e: Exception) {
                    // 忽略单条记录错误，继续处理其他记录
                }
            }
        } catch (e: Exception) {
            // 同步失败，下次启动时会重试
        }
    }

    /**
     * 重新计算并更新所有历史日期的达标状态
     */
    suspend fun recalculateAllAchievementStatus() = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now().toEpochDay()
            val records = dailyRecordRepository.getByDateRange(0, today)

            records.forEach { record ->
                try {
                    val totalBudget = record.targetCalories + record.exerciseCalories
                    val isAchieved = record.intakeCalories <= totalBudget

                    if (record.achieved != isAchieved) {
                        val updatedRecord = record.copy(
                            achieved = isAchieved,
                            achievementType = if (isAchieved) determineAchievementType(record) else null,
                            updatedAt = System.currentTimeMillis()
                        )
                        dailyRecordRepository.update(updatedRecord)
                    }
                } catch (e: Exception) {
                    // 忽略单条记录错误
                }
            }

            syncUnsyncedAchievements()
        } catch (e: Exception) {
            // 计算失败，下次启动时会重试
        }
    }

    private fun determineAchievementType(record: com.burnfat.data.local.entity.DailyRecordEntity): String {
        val totalBudget = record.targetCalories + record.exerciseCalories
        val deficit = totalBudget - record.intakeCalories

        return when {
            record.exerciseCalories > 200 && deficit > 300 -> "PERFECT"
            deficit > 100 -> "GOOD"
            else -> "ACCEPTABLE"
        }
    }
}