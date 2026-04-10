package com.burnfat.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnfat.data.local.entity.FoodEntryEntity
import com.burnfat.data.remote.ApiException
import com.burnfat.data.remote.FoodAnalysisService
import com.burnfat.data.remote.model.FoodAnalysisException
import com.burnfat.data.remote.model.FoodAnalysisResult
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

data class PhotoCaptureState(
    val isLoading: Boolean = false,
    val currentPhotoPath: String? = null,
    val analysisResult: FoodAnalysisResult? = null,
    val showResultDialog: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PhotoCaptureViewModel @Inject constructor(
    private val foodAnalysisService: FoodAnalysisService,
    private val foodEntryRepository: FoodEntryRepository,
    private val dailyRecordRepository: DailyRecordRepository,
    private val planRepository: PlanRepository,
    private val calorieCalculator: CalorieCalculator
) : ViewModel() {

    private val _state = MutableStateFlow(PhotoCaptureState())
    val state: StateFlow<PhotoCaptureState> = _state.asStateFlow()

    fun analyzePhoto(imagePath: String, mealType: MealType) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, currentPhotoPath = imagePath, error = null) }

            try {
                val result = foodAnalysisService.analyzeFoodPhoto(imagePath, mealType)
                _state.update {
                    it.copy(
                        isLoading = false,
                        analysisResult = result,
                        showResultDialog = true
                    )
                }
            } catch (e: ApiException) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "AI服务调用失败"
                    )
                }
            } catch (e: FoodAnalysisException) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "图片分析失败"
                    )
                }
            } catch (e: Exception) {
                val errorMsg = e.message?.takeIf { it.isNotBlank() } ?: "发生错误，请重试"
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "识别失败: $errorMsg"
                    )
                }
            }
        }
    }

    fun dismissResultDialog() {
        _state.update { it.copy(showResultDialog = false) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun saveFoodEntry(mealType: MealType) {
        viewModelScope.launch {
            val result = _state.value.analysisResult ?: return@launch
            val photoPath = _state.value.currentPhotoPath

            val today = LocalDate.now().toEpochDay()

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

            dailyRecordRepository.getOrCreateToday(today, targetCalories)

            result.foods.forEach { food ->
                val entry = FoodEntryEntity(
                    recordDate = today,
                    mealType = mealType.name,
                    photoPath = photoPath,
                    foodName = food.name,
                    portion = food.portion ?: (food.estimatedGrams?.let { "${it}g" }),
                    calories = food.calories,
                    sourceType = "AI_PHOTO",
                    aiConfidence = result.confidence,
                    aiSuggestions = result.suggestions,
                    createdAt = System.currentTimeMillis()
                )

                foodEntryRepository.insert(entry)
                dailyRecordRepository.addFoodCalories(today, food.calories, mealType.name)
            }

            _state.update {
                it.copy(
                    showResultDialog = false,
                    analysisResult = null,
                    currentPhotoPath = null
                )
            }
        }
    }
}