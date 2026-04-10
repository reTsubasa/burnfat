package com.burnfat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 运动记录实体
 */
@Entity(
    tableName = "exercise_entry",
    foreignKeys = [
        ForeignKey(
            entity = DailyRecordEntity::class,
            parentColumns = ["date"],
            childColumns = ["recordDate"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recordDate")]
)
data class ExerciseEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordDate: Long,          // 关联的每日记录日期
    val exerciseType: String,      // 运动类型
    val durationMinutes: Int,      // 持续时间(分钟)
    val caloriesBurned: Int,       // 消耗热量(手动输入)
    val notes: String?,            // 备注
    val createdAt: Long
)