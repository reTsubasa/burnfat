package com.burnfat.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 食物记录实体
 */
@Entity(
    tableName = "food_entry",
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
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordDate: Long,          // 关联的每日记录日期
    val mealType: String,          // 餐次: BREAKFAST/LUNCH/DINNER/SNACK
    val photoPath: String?,        // 本地图片路径
    val foodName: String,          // 食物名称
    val portion: String?,          // 份量描述
    val calories: Int,             // 热量 (kcal)
    val sourceType: String,        // 来源: AI_PHOTO/MANUAL
    val aiConfidence: Float?,      // AI置信度
    val aiSuggestions: String?,    // AI建议
    val createdAt: Long
)