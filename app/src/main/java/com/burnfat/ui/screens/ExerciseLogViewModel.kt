package com.burnfat.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnfat.data.local.entity.ExerciseEntryEntity
import com.burnfat.data.repository.DailyRecordRepository
import com.burnfat.data.repository.ExerciseEntryRepository
import com.burnfat.data.repository.PlanRepository
import com.burnfat.domain.calculator.CalorieCalculator
import com.burnfat.domain.model.ActivityLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ExerciseLogState(
    val exerciseType: String = "",
    val calories: String = ""
) {
    val isValid: Boolean
        get() = exerciseType.isNotBlank() &&
                calories.toIntOrNull()?.let { it > 0 } ?: false
}

@HiltViewModel
class ExerciseLogViewModel @Inject constructor(
    private val exerciseEntryRepository: ExerciseEntryRepository,
    private val dailyRecordRepository: DailyRecordRepository,
    private val planRepository: PlanRepository,
    private val calorieCalculator: CalorieCalculator
) : ViewModel() {

    private val _state = MutableStateFlow(ExerciseLogState())
    val state: StateFlow<ExerciseLogState> = _state.asStateFlow()

    fun setExerciseType(type: String) {
        _state.update { it.copy(exerciseType = type) }
    }

    fun setCalories(calories: String) {
        _state.update { it.copy(calories = calories) }
    }

    fun saveExerciseEntry() {
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

            // 保存运动记录
            val entry = ExerciseEntryEntity(
                recordDate = today,
                exerciseType = currentState.exerciseType,
                durationMinutes = 0,
                caloriesBurned = calories,
                notes = null,
                createdAt = System.currentTimeMillis()
            )

            exerciseEntryRepository.insert(entry)

            // 更新每日记录的运动热量
            dailyRecordRepository.addExerciseCalories(today, calories)
        }
    }
}