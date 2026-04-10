package com.burnfat.domain.model

/**
 * 餐次枚举
 */
enum class MealType(
    val displayName: String,
    val icon: String,
    val typicalTimeRange: String,
    val defaultCalorieRatio: Float
) {
    BREAKFAST(
        displayName = "早餐",
        icon = "🌅",
        typicalTimeRange = "6:00-10:00",
        defaultCalorieRatio = 0.25f
    ),
    LUNCH(
        displayName = "午餐",
        icon = "☀️",
        typicalTimeRange = "11:00-14:00",
        defaultCalorieRatio = 0.35f
    ),
    DINNER(
        displayName = "晚餐",
        icon = "🌙",
        typicalTimeRange = "17:00-20:00",
        defaultCalorieRatio = 0.30f
    ),
    SNACK(
        displayName = "加餐",
        icon = "🍎",
        typicalTimeRange = "任意时间",
        defaultCalorieRatio = 0.10f
    );

    companion object {
        fun fromHour(hour: Int): MealType {
            return when (hour) {
                in 6..10 -> BREAKFAST
                in 11..14 -> LUNCH
                in 17..20 -> DINNER
                else -> SNACK
            }
        }

        fun fromName(name: String): MealType {
            return entries.find { it.name == name } ?: SNACK
        }
    }
}