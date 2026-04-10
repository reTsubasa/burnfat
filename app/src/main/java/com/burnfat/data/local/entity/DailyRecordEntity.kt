package com.burnfat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 每日记录实体
 */
@Entity(tableName = "daily_record")
data class DailyRecordEntity(
    @PrimaryKey val date: Long,            // 日期 (YYYYMMDD格式的Long)
    val intakeCalories: Int,               // 总摄入热量
    val breakfastCalories: Int,            // 早餐热量
    val lunchCalories: Int,                // 午餐热量
    val dinnerCalories: Int,               // 晚餐热量
    val snackCalories: Int,                // 加餐热量
    val exerciseCalories: Int,             // 运动消耗热量
    val targetCalories: Int,               // 当日目标摄入
    val achieved: Boolean,                 // 是否达标
    val achievementType: String?,          // 达标类型
    val weightRecord: Float?,              // 当日体重记录
    val foodEntryCount: Int,               // 食物记录数
    val exerciseEntryCount: Int,           // 运动记录数
    val isSyncedToAchievement: Boolean = false,  // 是否已同步到达标墙
    val createdAt: Long,
    val updatedAt: Long
)