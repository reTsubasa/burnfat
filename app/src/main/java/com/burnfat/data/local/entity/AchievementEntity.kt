package com.burnfat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 达标墙记录实体
 */
@Entity(tableName = "achievement")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,                // 达标日期
    val type: String,              // 达标类型: PERFECT/GOOD/ACCEPTABLE
    val streakDays: Int,           // 连续达标天数
    val badgeEarned: String?,      // 获得的徽章名称
    val caloriesDeficit: Int,      // 当日热量缺口
    val createdAt: Long
)