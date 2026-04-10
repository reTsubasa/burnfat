package com.burnfat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * BMR历史实体
 */
@Entity(tableName = "bmr_history")
data class BmrHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,                // 更新日期
    val previousBmr: Float,        // 之前的BMR
    val newBmr: Float,             // 新的BMR
    val reason: String,            // 更新原因: MEASURED/CALCULATED/ESTIMATED
    val relatedWeight: Float?,     // 相关体重
    val createdAt: Long
)