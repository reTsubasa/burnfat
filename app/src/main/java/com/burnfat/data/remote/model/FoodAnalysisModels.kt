package com.burnfat.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 食物分析结果 (API响应解析用)
 */
@Serializable
data class FoodAnalysisJson(
    val foods: List<FoodItemJson>,
    @SerialName("totalCalories")
    val totalCalories: Int,
    val confidence: Float,
    val suggestions: String? = null,
    @SerialName("mealCategory")
    val mealCategory: String? = null
)

@Serializable
data class FoodItemJson(
    val name: String,
    val portion: String? = null,
    @SerialName("estimatedGrams")
    val estimatedGrams: Int? = null,
    val calories: Int
)

/**
 * 食物分析结果 (业务用)
 */
data class FoodAnalysisResult(
    val foods: List<FoodItem>,
    val totalCalories: Int,
    val confidence: Float,
    val suggestions: String?,
    val mealCategory: String?
)

data class FoodItem(
    val name: String,
    val portion: String?,
    val estimatedGrams: Int?,
    val calories: Int
)

/**
 * 食物分析异常
 */
class FoodAnalysisException(message: String) : Exception(message)