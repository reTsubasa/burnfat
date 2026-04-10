package com.burnfat.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnfat.data.repository.*
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

data class FoodEntryDisplay(
    val id: Long,
    val foodName: String,
    val mealType: String,
    val calories: Int,
    val portion: String? = null,
    val photoPath: String? = null,
    val sourceType: String = "MANUAL"
)

data class ExerciseEntryDisplay(
    val id: Long,
    val exerciseType: String,
    val calories: Int,
    val durationMinutes: Int = 0,
    val notes: String? = null
)

data class DashboardState(
    val todayIntake: Int = 0,
    val todayTarget: Int = 2000,
    val todayExercise: Int = 0,
    val currentStreak: Int = 0,
    val todayFoodEntries: List<FoodEntryDisplay> = emptyList(),
    val todayExerciseEntries: List<ExerciseEntryDisplay> = emptyList(),
    val remainingCalories: Int = 2000,
    val hasActivePlan: Boolean = false,
    val isOverBudget: Boolean = false,
    // 详情弹窗状态
    val selectedFoodEntry: FoodEntryDisplay? = null,
    val selectedExerciseEntry: ExerciseEntryDisplay? = null,
    val showFoodDetailDialog: Boolean = false,
    val showExerciseDetailDialog: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val dailyRecordRepository: DailyRecordRepository,
    private val foodEntryRepository: FoodEntryRepository,
    private val exerciseEntryRepository: ExerciseEntryRepository,
    private val achievementRepository: AchievementRepository,
    private val calorieCalculator: CalorieCalculator
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val today = LocalDate.now().toEpochDay()
            val activePlan = planRepository.getActivePlan()

            if (activePlan == null) {
                _state.update { it.copy(hasActivePlan = false) }
                return@launch
            }

            // 计算目标热量
            val daysToTarget = ((activePlan.targetDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
            val dailyDeficit = calorieCalculator.calculateDailyDeficitTarget(
                activePlan.currentWeight, activePlan.targetWeight, daysToTarget.coerceAtLeast(1)
            )
            val targetIntake = calorieCalculator.calculateDailyTargetIntake(
                activePlan.bmr,
                ActivityLevel.fromName(activePlan.activityLevel),
                dailyDeficit
            )

            // 获取或创建今日记录
            val todayRecord = dailyRecordRepository.getOrCreateToday(today, targetIntake)

            // 获取今日食物记录
            val foodEntries = foodEntryRepository.getByDate(today)
            val foodDisplays = foodEntries.map { entity ->
                FoodEntryDisplay(
                    id = entity.id,
                    foodName = entity.foodName,
                    mealType = MealType.fromName(entity.mealType).displayName,
                    calories = entity.calories,
                    portion = entity.portion,
                    photoPath = entity.photoPath,
                    sourceType = entity.sourceType
                )
            }

            // 获取今日运动记录
            val exerciseEntries = exerciseEntryRepository.getByDate(today)
            val exerciseDisplays = exerciseEntries.map { entity ->
                ExerciseEntryDisplay(
                    id = entity.id,
                    exerciseType = entity.exerciseType,
                    calories = entity.caloriesBurned,
                    durationMinutes = entity.durationMinutes,
                    notes = entity.notes
                )
            }

            // 获取连续达标天数
            val maxStreak = achievementRepository.getMaxStreak()

            // 计算热量状态
            val totalBudget = todayRecord.targetCalories + todayRecord.exerciseCalories
            val remaining = totalBudget - todayRecord.intakeCalories
            val isOverBudget = todayRecord.intakeCalories > totalBudget

            _state.update {
                it.copy(
                    hasActivePlan = true,
                    todayIntake = todayRecord.intakeCalories,
                    todayTarget = todayRecord.targetCalories,
                    todayExercise = todayRecord.exerciseCalories,
                    currentStreak = maxStreak,
                    todayFoodEntries = foodDisplays,
                    todayExerciseEntries = exerciseDisplays,
                    remainingCalories = remaining,
                    isOverBudget = isOverBudget
                )
            }
        }
    }

    fun deleteFoodEntry(id: Long) {
        viewModelScope.launch {
            // 获取食物记录详情
            val entry = foodEntryRepository.getById(id)
            if (entry != null) {
                // 从每日记录中减去热量
                dailyRecordRepository.subtractFoodCalories(
                    date = entry.recordDate,
                    calories = entry.calories,
                    mealType = entry.mealType
                )
                // 删除食物记录
                foodEntryRepository.deleteById(id)
            }
            loadData()
        }
    }

    fun deleteExerciseEntry(id: Long) {
        viewModelScope.launch {
            // 获取运动记录详情
            val entry = exerciseEntryRepository.getById(id)
            if (entry != null) {
                // 从每日记录中减去热量
                dailyRecordRepository.subtractExerciseCalories(
                    date = entry.recordDate,
                    calories = entry.caloriesBurned
                )
                // 删除运动记录
                exerciseEntryRepository.deleteById(id)
            }
            loadData()
        }
    }

    // 食物记录详情弹窗
    fun showFoodDetail(entry: FoodEntryDisplay) {
        _state.update { it.copy(
            selectedFoodEntry = entry,
            showFoodDetailDialog = true
        )}
    }

    fun hideFoodDetail() {
        _state.update { it.copy(
            showFoodDetailDialog = false,
            selectedFoodEntry = null
        )}
    }

    // 运动记录详情弹窗
    fun showExerciseDetail(entry: ExerciseEntryDisplay) {
        _state.update { it.copy(
            selectedExerciseEntry = entry,
            showExerciseDetailDialog = true
        )}
    }

    fun hideExerciseDetail() {
        _state.update { it.copy(
            showExerciseDetailDialog = false,
            selectedExerciseEntry = null
        )}
    }

    // 更新食物记录
    fun updateFoodName(id: Long, newName: String) {
        viewModelScope.launch {
            val entry = foodEntryRepository.getById(id) ?: return@launch
            foodEntryRepository.update(entry.copy(foodName = newName))
            loadData()
        }
    }

    fun updateFoodPortion(id: Long, newPortion: String) {
        viewModelScope.launch {
            val entry = foodEntryRepository.getById(id) ?: return@launch
            foodEntryRepository.update(entry.copy(portion = newPortion.ifBlank { null }))
            loadData()
        }
    }

    fun updateFoodMealType(id: Long, mealType: MealType) {
        viewModelScope.launch {
            val entry = foodEntryRepository.getById(id) ?: return@launch
            foodEntryRepository.update(
                entry.copy(
                    mealType = mealType.name,
                    createdAt = System.currentTimeMillis()
                )
            )
            loadData()
        }
    }

    fun updateFoodCalories(id: Long, newCalories: Int) {
        viewModelScope.launch {
            val entry = foodEntryRepository.getById(id) ?: return@launch
            val oldCalories = entry.calories
            // 更新每日记录中的热量
            dailyRecordRepository.subtractFoodCalories(entry.recordDate, oldCalories, entry.mealType)
            dailyRecordRepository.addFoodCalories(entry.recordDate, newCalories, entry.mealType)
            // 更新食物记录
            foodEntryRepository.update(entry.copy(calories = newCalories))
            loadData()
        }
    }

    // 更新运动记录
    fun updateExerciseCalories(id: Long, newCalories: Int) {
        viewModelScope.launch {
            val entry = exerciseEntryRepository.getById(id) ?: return@launch
            val oldCalories = entry.caloriesBurned
            // 更新每日记录中的热量
            dailyRecordRepository.subtractExerciseCalories(entry.recordDate, oldCalories)
            dailyRecordRepository.addExerciseCalories(entry.recordDate, newCalories)
            // 更新运动记录
            exerciseEntryRepository.update(entry.copy(caloriesBurned = newCalories))
            loadData()
        }
    }
}