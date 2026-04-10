package com.burnfat.domain.model

/**
 * 徽章稀有度
 */
enum class BadgeRarity(
    val colorHex: String,
    val emoji: String
) {
    COMMON("#9E9E9E", "🏅"),
    RARE("#2196F3", "💎"),
    EPIC("#9C27B0", "👑"),
    LEGENDARY("#FFD700", "🌟")
}

/**
 * 徽章要求
 */
data class BadgeRequirement(
    val streak: Int? = null,
    val weightLoss: Float? = null,
    val goalReached: Boolean? = null
)

/**
 * 徽章类型定义
 */
enum class BadgeType(
    val badgeName: String,
    val displayName: String,
    val description: String,
    val requirement: BadgeRequirement,
    val rarity: BadgeRarity
) {
    FIRST_DAY(
        badgeName = "first_day",
        displayName = "起步",
        description = "完成第一个达标日",
        requirement = BadgeRequirement(streak = 1),
        rarity = BadgeRarity.COMMON
    ),
    WEEK_STREAK(
        badgeName = "week_streak",
        displayName = "一周坚持",
        description = "连续7天达标",
        requirement = BadgeRequirement(streak = 7),
        rarity = BadgeRarity.COMMON
    ),
    TWO_WEEK_STREAK(
        badgeName = "two_week_streak",
        displayName = "两周毅力",
        description = "连续14天达标",
        requirement = BadgeRequirement(streak = 14),
        rarity = BadgeRarity.RARE
    ),
    MONTH_STREAK(
        badgeName = "month_streak",
        displayName = "月度冠军",
        description = "连续30天达标",
        requirement = BadgeRequirement(streak = 30),
        rarity = BadgeRarity.EPIC
    ),
    WEIGHT_1KG(
        badgeName = "weight_1kg",
        displayName = "减重1kg",
        description = "累计减重达到1公斤",
        requirement = BadgeRequirement(weightLoss = 1f),
        rarity = BadgeRarity.COMMON
    ),
    WEIGHT_5KG(
        badgeName = "weight_5kg",
        displayName = "减重5kg 🎉",
        description = "累计减重达到5公斤",
        requirement = BadgeRequirement(weightLoss = 5f),
        rarity = BadgeRarity.RARE
    ),
    WEIGHT_10KG(
        badgeName = "weight_10kg",
        displayName = "减重10kg 🏆",
        description = "累计减重达到10公斤",
        requirement = BadgeRequirement(weightLoss = 10f),
        rarity = BadgeRarity.EPIC
    ),
    GOAL_REACHED(
        badgeName = "goal_reached",
        displayName = "目标达成 🌟",
        description = "达成设定的目标体重",
        requirement = BadgeRequirement(goalReached = true),
        rarity = BadgeRarity.LEGENDARY
    );

    companion object {
        fun fromName(name: String): BadgeType? {
            return values().find { it.badgeName == name }
        }
    }
}