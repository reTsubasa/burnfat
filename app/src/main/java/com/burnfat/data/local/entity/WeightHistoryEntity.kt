package com.burnfat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 体重历史实体
 */
@Entity(tableName = "weight_history")
data class WeightHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,                // 记录日期
    val weight: Float,             // 体重
    val changeFromPrevious: Float?,// 与上次记录的变化
    val note: String?,             // 备注
    val createdAt: Long
)