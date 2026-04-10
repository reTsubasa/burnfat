package com.burnfat.domain.calculator

import com.burnfat.domain.model.ActivityLevel
import com.burnfat.domain.model.AchievementType
import kotlin.math.roundToInt

/**
 * 热量计算引擎
 */
class CalorieCalculator {

    /**
     * 计算每日热量缺口目标
     * 公式: (当前体重 - 目标体重) × 7700 ÷ 天数
     *
     * @param currentWeight 当前体重
     * @param targetWeight 目标体重
     * @param daysToTarget 达成目标的天数
     * @return 每日热量缺口
     */
    fun calculateDailyDeficitTarget(
        currentWeight: Float,
        targetWeight: Float,
        daysToTarget: Int
    ): Int {
        if (daysToTarget <= 0) return 0

        val totalDeficitNeeded = (currentWeight - targetWeight) * CALORIES_PER_KG
        return (totalDeficitNeeded / daysToTarget).roundToInt()
    }

    /**
     * 计算每日建议摄入热量
     * 公式: BMR × 活动系数 - 缺口目标
     *
     * @param bmr 基础代谢率
     * @param activityLevel 活动等级
     * @param dailyDeficitTarget 每日缺口目标
     * @return 建议每日摄入热量
     */
    fun calculateDailyTargetIntake(
        bmr: Float,
        activityLevel: ActivityLevel,
        dailyDeficitTarget: Int
    ): Int {
        val maintenanceCalories = bmr * activityLevel.multiplier
        return (maintenanceCalories - dailyDeficitTarget).roundToInt()
    }

    /**
     * 计算实际热量缺口
     * 公式: 目标摄入 - 实际摄入 + 运动消耗
     *
     * @param targetIntake 目标摄入
     * @param actualIntake 实际摄入
     * @param exerciseCalories 运动消耗
     * @return 实际热量缺口
     */
    fun calculateActualDeficit(
        targetIntake: Int,
        actualIntake: Int,
        exerciseCalories: Int
    ): Int {
        return targetIntake - actualIntake + exerciseCalories
    }

    /**
     * 评估达标状态
     *
     * @param actualIntake 实际摄入
     * @param targetIntake 目标摄入
     * @param tolerance 允许误差范围
     * @return 达标类型
     */
    fun evaluateAchievement(
        actualIntake: Int,
        targetIntake: Int,
        tolerance: Int = DEFAULT_TOLERANCE
    ): AchievementType {
        val difference = actualIntake - targetIntake
        return when {
            difference in -PERFECT_TOLERANCE..PERFECT_TOLERANCE -> AchievementType.PERFECT
            difference in -tolerance..tolerance -> AchievementType.GOOD
            difference < tolerance + ACCEPTABLE_MARGIN -> AchievementType.ACCEPTABLE
            else -> AchievementType.NOT_ACHIEVED
        }
    }

    /**
     * 计算预估体重变化
     * 公式: 累计缺口 ÷ 7700
     *
     * @param totalDeficit 累计热量缺口
     * @return 预估体重变化
     */
    fun calculateEstimatedWeightLoss(totalDeficit: Int): Float {
        return totalDeficit / CALORIES_PER_KG
    }

    /**
     * 计算达成目标所需天数
     *
     * @param remainingDeficit 剩余需要的热量缺口
     * @param dailyDeficitAverage 平均每日缺口
     * @return 所需天数，-1表示无法达成
     */
    fun calculateDaysToGoal(
        remainingDeficit: Int,
        dailyDeficitAverage: Float
    ): Int {
        if (dailyDeficitAverage <= 0) return -1
        return (remainingDeficit / dailyDeficitAverage).roundToInt()
    }

    /**
     * 根据体重变化估算新BMR
     * 简单比例法: 新BMR = 旧BMR × (新体重/旧体重)
     */
    fun estimateNewBmr(
        oldBmr: Float,
        oldWeight: Float,
        newWeight: Float
    ): Float {
        return oldBmr * (newWeight / oldWeight)
    }

    /**
     * 计算各餐次建议热量
     *
     * @param totalCalories 全天目标热量
     * @return 各餐次热量分配
     */
    fun calculateMealCalories(totalCalories: Int): MealCalorieDistribution {
        return MealCalorieDistribution(
            breakfast = (totalCalories * 0.25f).roundToInt(),
            lunch = (totalCalories * 0.35f).roundToInt(),
            dinner = (totalCalories * 0.30f).roundToInt(),
            snack = (totalCalories * 0.10f).roundToInt()
        )
    }

    companion object {
        const val CALORIES_PER_KG = 7700f
        const val DEFAULT_TOLERANCE = 100
        const val PERFECT_TOLERANCE = 50
        const val ACCEPTABLE_MARGIN = 200
    }
}

/**
 * 各餐次热量分配
 */
data class MealCalorieDistribution(
    val breakfast: Int,
    val lunch: Int,
    val dinner: Int,
    val snack: Int
)