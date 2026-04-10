package com.burnfat.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnfat.data.local.entity.FoodEntryEntity
import com.burnfat.data.repository.DailyRecordRepository
import com.burnfat.data.repository.FoodEntryRepository
import com.burnfat.data.repository.PlanRepository
import com.burnfat.domain.calculator.CalorieCalculator
import com.burnfat.domain.model.ActivityLevel
import com.burnfat.domain.model.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class FoodLogState(
    val selectedMealType: MealType = MealType.fromHour(java.time.LocalTime.now().hour),
    val foodName: String = "",
    val calories: String = "",
    val portion: String = ""
) {
    val isValid: Boolean
        get() = foodName.isNotBlank() && calories.toIntOrNull()?.let { it > 0 } ?: false
}

@HiltViewModel
class FoodLogViewModel @Inject constructor(
    private val foodEntryRepository: FoodEntryRepository,
    private val dailyRecordRepository: DailyRecordRepository,
    private val planRepository: PlanRepository,
    private val calorieCalculator: CalorieCalculator
) : ViewModel() {

    private val _state = MutableStateFlow(FoodLogState())
    val state: StateFlow<FoodLogState> = _state.asStateFlow()

    fun setMealType(type: MealType) {
        _state.update { it.copy(selectedMealType = type) }
    }

    fun setFoodName(name: String) {
        _state.update { it.copy(foodName = name) }
    }

    fun setCalories(calories: String) {
        _state.update { it.copy(calories = calories) }
    }

    fun setPortion(portion: String) {
        _state.update { it.copy(portion = portion) }
    }

    fun saveFoodEntry() {
        viewModelScope.launch {
            val currentState = _state.value
            val calories = currentState.calories.toIntOrNull() ?: return@launch

            val today = LocalDate.now().toEpochDay()

            // 获取当前计划计算目标热量
            val activePlan = planRepository.getActivePlan()
            val targetCalories = activePlan?.let { plan ->
                val daysToTarget = ((plan.targetDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
                val dailyDeficit = calorieCalculator.calculateDailyDeficitTarget(
                    plan.currentWeight, plan.targetWeight, daysToTarget.coerceAtLeast(1)
                )
                calorieCalculator.calculateDailyTargetIntake(
                    plan.bmr,
                    ActivityLevel.fromName(plan.activityLevel),
                    dailyDeficit
                )
            } ?: 2000

            // 确保今日记录存在
            dailyRecordRepository.getOrCreateToday(today, targetCalories)

            // 保存食物记录
            val entry = FoodEntryEntity(
                recordDate = today,
                mealType = currentState.selectedMealType.name,
                photoPath = null,
                foodName = currentState.foodName,
                portion = currentState.portion.ifEmpty { null },
                calories = calories,
                sourceType = "MANUAL",
                aiConfidence = null,
                aiSuggestions = null,
                createdAt = System.currentTimeMillis()
            )

            foodEntryRepository.insert(entry)

            // 更新每日记录的热量
            dailyRecordRepository.addFoodCalories(today, calories, currentState.selectedMealType.name)
        }
    }
}