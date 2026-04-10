package com.burnfat.domain.model

/**
 * 活动等级枚举
 */
enum class ActivityLevel(
    val multiplier: Float,
    val description: String
) {
    SEDENTARY(1.2f, "久坐不动"),
    LIGHT(1.375f, "轻度活动(每周1-3次运动)"),
    MODERATE(1.55f, "中度活动(每周3-5次运动)"),
    ACTIVE(1.725f, "高度活动(每周6-7次运动)");

    companion object {
        fun fromName(name: String): ActivityLevel {
            return entries.find { it.name == name } ?: SEDENTARY
        }
    }
}