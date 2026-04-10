package com.burnfat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户档案实体
 * 仅存储用户的个人信息，减脂计划相关数据在PlanEntity中
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 0,
    val age: Int,                   // 年龄
    val gender: String,             // 性别: MALE/FEMALE
    val hasCompletedOnboarding: Boolean = false,  // 是否完成首次引导
    val createdAt: Long,
    val updatedAt: Long
)