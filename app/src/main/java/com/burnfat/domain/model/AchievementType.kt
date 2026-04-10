package com.burnfat.domain.model

/**
 * 达标类型枚举
 */
enum class AchievementType {
    PERFECT,      // 完美达标 (误差±50kcal)
    GOOD,         // 良好达标 (误差±100kcal)
    ACCEPTABLE,   // 基本达标 (超出<200kcal)
    NOT_ACHIEVED; // 未达标

    companion object {
        fun fromName(name: String?): AchievementType {
            return values().find { it.name == name } ?: NOT_ACHIEVED
        }
    }
}