package com.burnfat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 减脂计划实体
 */
@Entity(tableName = "plan")
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,               // 计划名称
    val startWeight: Float,         // 起始体重 (kg)
    val currentWeight: Float,       // 当前体重 (kg)
    val targetWeight: Float,        // 目标体重 (kg)
    val bmr: Float,                 // 基础代谢率 (kcal)
    val activityLevel: String,      // 活动等级
    val targetDate: Long,           // 目标日期 (timestamp)
    val isActive: Boolean,          // 是否为当前激活计划
    val createdAt: Long,            // 创建时间
    val updatedAt: Long             // 更新时间
)