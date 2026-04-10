package com.burnfat.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnfat.data.repository.DailyRecordRepository
import com.burnfat.data.repository.FoodEntryRepository
import com.burnfat.data.repository.ExerciseEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class TimeRange(val label: String) {
    WEEK("本周"),
    MONTH("本月"),
    ALL("全部")
}

data class DailyRecordDisplay(
    val date: String,
    val intake: Int,
    val target: Int,
    val exercise: Int,
    val achieved: Boolean,
    val isOverBudget: Boolean,
    val foodItems: List<FoodRecordItem>,
    val exerciseItems: List<ExerciseRecordItem>
)

data class FoodRecordItem(
    val id: Long,
    val name: String,
    val calories: Int,
    val mealType: String,
    val photoPath: String?,
    val sourceType: String,  // AI_PHOTO or MANUAL
    val portion: String?     // 份量描述
)

data class ExerciseRecordItem(
    val id: Long,
    val name: String,
    val calories: Int,
    val duration: Int
)

data class HistoryState(
    val selectedRange: TimeRange = TimeRange.WEEK,
    val averageIntake: Int = 0,
    val achievementRate: Float = 0f,
    val achievedDays: Int = 0,
    val totalDays: Int = 0,
    val totalExercise: Int = 0,
    val records: List<DailyRecordDisplay> = emptyList(),
    // 详情弹窗状态
    val selectedFoodItem: FoodRecordItem? = null,
    val selectedExerciseItem: ExerciseRecordItem? = null,
    val showFoodDetailDialog: Boolean = false,
    val showExerciseDetailDialog: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dailyRecordRepository: DailyRecordRepository,
    private val foodEntryRepository: FoodEntryRepository,
    private val exerciseEntryRepository: ExerciseEntryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun setTimeRange(range: TimeRange) {
        _state.update { it.copy(selectedRange = range) }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val (startDate, endDate) = when (_state.value.selectedRange) {
                TimeRange.WEEK -> today.minusDays(7) to today
                TimeRange.MONTH -> today.minusMonths(1) to today
                TimeRange.ALL -> LocalDate.of(2020, 1, 1) to today
            }

            val startEpoch = startDate.toEpochDay()
            val endEpoch = endDate.toEpochDay()

            val dailyRecords = dailyRecordRepository.getByDateRange(startEpoch, endEpoch)

            val displays = dailyRecords.map { entity ->
                // 计算是否超标
                val totalBudget = entity.targetCalories + entity.exerciseCalories
                val isOverBudget = entity.intakeCalories > totalBudget

                // 获取该日的食物记录
                val foodEntries = foodEntryRepository.getByDate(entity.date)
                val foodItems = foodEntries.map { food ->
                    FoodRecordItem(
                        id = food.id,
                        name = food.foodName,
                        calories = food.calories,
                        mealType = food.mealType,
                        photoPath = food.photoPath,
                        sourceType = food.sourceType,
                        portion = food.portion
                    )
                }

                // 获取该日的运动记录
                val exerciseEntries = exerciseEntryRepository.getByDate(entity.date)
                val exerciseItems = exerciseEntries.map { exercise ->
                    ExerciseRecordItem(
                        id = exercise.id,
                        name = exercise.exerciseType,
                        calories = exercise.caloriesBurned,
                        duration = exercise.durationMinutes
                    )
                }

                DailyRecordDisplay(
                    date = LocalDate.ofEpochDay(entity.date).format(DateTimeFormatter.ofPattern("MM月dd日")),
                    intake = entity.intakeCalories,
                    target = entity.targetCalories,
                    exercise = entity.exerciseCalories,
                    achieved = entity.achieved,
                    isOverBudget = isOverBudget,
                    foodItems = foodItems,
                    exerciseItems = exerciseItems
                )
            }

            val averageIntake = if (dailyRecords.isNotEmpty()) {
                dailyRecords.sumOf { it.intakeCalories.toLong() } / dailyRecords.size
            } else 0

            val totalDays = dailyRecords.size
            val achievedCount = dailyRecordRepository.countAchievedDays(startEpoch, endEpoch)
            val achievementRate = if (totalDays > 0) achievedCount.toFloat() / totalDays else 0f

            val totalExercise = dailyRecords.sumOf { it.exerciseCalories }

            _state.update {
                it.copy(
                    averageIntake = averageIntake.toInt(),
                    achievementRate = achievementRate,
                    achievedDays = achievedCount,
                    totalDays = totalDays,
                    totalExercise = totalExercise,
                    records = displays
                )
            }
        }
    }

    fun deleteFoodEntry(id: Long) {
        viewModelScope.launch {
            foodEntryRepository.deleteById(id)
            loadData()
        }
    }

    fun deleteExerciseEntry(id: Long) {
        viewModelScope.launch {
            exerciseEntryRepository.deleteById(id)
            loadData()
        }
    }

    // 食物记录详情弹窗
    fun showFoodDetail(item: FoodRecordItem) {
        _state.update { it.copy(
            selectedFoodItem = item,
            showFoodDetailDialog = true
        )}
    }

    fun hideFoodDetail() {
        _state.update { it.copy(
            showFoodDetailDialog = false,
            selectedFoodItem = null
        )}
    }

    // 运动记录详情弹窗
    fun showExerciseDetail(item: ExerciseRecordItem) {
        _state.update { it.copy(
            selectedExerciseItem = item,
            showExerciseDetailDialog = true
        )}
    }

    fun hideExerciseDetail() {
        _state.update { it.copy(
            showExerciseDetailDialog = false,
            selectedExerciseItem = null
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

    fun updateFoodMealType(id: Long, mealType: com.burnfat.domain.model.MealType) {
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