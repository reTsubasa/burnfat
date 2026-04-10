package com.burnfat.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnfat.data.remote.PlanEvaluationResult
import com.burnfat.data.remote.PlanRecommendationService
import com.burnfat.data.repository.PlanRepository
import com.burnfat.data.repository.WeightHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WeightCurveState(
    val startWeight: Float = 0f,
    val currentWeight: Float = 0f,
    val targetWeight: Float = 0f,
    val totalLoss: Float = 0f,
    val trend: WeightTrend = WeightTrend.STABLE,
    val points: List<WeightPoint> = emptyList(),
    val predictedPoints: List<WeightPoint> = emptyList(),
    val startDate: Long = 0,
    val targetDate: Long = 0,
    val showWeightDialog: Boolean = false,
    val planName: String = "",
    val bmr: Float = 0f,
    val planId: Long = 0,
    val activityLevel: String = "MODERATE",
    // AI评估相关
    val isLoadingEvaluation: Boolean = false,
    val evaluation: PlanEvaluationResult? = null,
    val showEvaluationDialog: Boolean = false
)

@HiltViewModel
class WeightCurveViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val weightHistoryRepository: WeightHistoryRepository,
    private val planRecommendationService: PlanRecommendationService
) : ViewModel() {

    private val _state = MutableStateFlow(WeightCurveState())
    val state: StateFlow<WeightCurveState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val activePlan = planRepository.getActivePlan()
            val history = weightHistoryRepository.getAllOrdered()

            if (activePlan == null) return@launch

            val startWeight = activePlan.startWeight
            val targetWeight = activePlan.targetWeight
            val targetDate = activePlan.targetDate
            val createdAt = activePlan.createdAt
            val bmr = activePlan.bmr

            // 生成实际体重数据点
            val actualPoints = history.map { entity ->
                WeightPoint(
                    date = entity.date,
                    weight = entity.weight
                )
            }

            // 如果没有历史记录，使用当前体重作为起点
            val points = if (actualPoints.isEmpty()) {
                listOf(WeightPoint(date = createdAt, weight = startWeight))
            } else {
                actualPoints
            }

            val currentWeight = activePlan.currentWeight
            val totalLoss = startWeight - currentWeight

            // 生成预测体重曲线
            val predictedPoints = generatePredictedCurve(
                startWeight = startWeight,
                targetWeight = targetWeight,
                startDate = points.firstOrNull()?.date ?: createdAt,
                targetDate = targetDate
            )

            // 计算趋势
            val trend = if (points.size >= 2) {
                val recentPoints = points.takeLast(7)
                val changes = recentPoints.zipWithNext { a, b -> b.weight - a.weight }
                val avgChange = changes.average()
                when {
                    avgChange < -0.1 -> WeightTrend.DESCENDING
                    avgChange > 0.1 -> WeightTrend.ASCENDING
                    else -> WeightTrend.STABLE
                }
            } else WeightTrend.STABLE

            _state.update {
                it.copy(
                    startWeight = startWeight,
                    currentWeight = currentWeight,
                    targetWeight = targetWeight,
                    totalLoss = totalLoss,
                    trend = trend,
                    points = points,
                    predictedPoints = predictedPoints,
                    startDate = createdAt,
                    targetDate = targetDate,
                    planName = activePlan.name,
                    bmr = bmr,
                    planId = activePlan.id,
                    activityLevel = activePlan.activityLevel
                )
            }
        }
    }

    private fun generatePredictedCurve(
        startWeight: Float,
        targetWeight: Float,
        startDate: Long,
        targetDate: Long
    ): List<WeightPoint> {
        if (targetDate <= startDate) return emptyList()

        val days = ((targetDate - startDate) / (24 * 60 * 60 * 1000)).toInt()
        if (days <= 0) return emptyList()

        val dailyLoss = (startWeight - targetWeight) / days
        val points = mutableListOf<WeightPoint>()

        for (i in 0..days step 7) {
            val date = startDate + (i * 24 * 60 * 60 * 1000L)
            val weight = startWeight - (dailyLoss * i)
            points.add(WeightPoint(date = date, weight = weight))
        }

        points.add(WeightPoint(date = targetDate, weight = targetWeight))

        return points
    }

    fun showWeightDialog() {
        _state.update { it.copy(showWeightDialog = true) }
    }

    fun hideWeightDialog() {
        _state.update { it.copy(showWeightDialog = false) }
    }

    fun recordWeight(weight: Float, newBmr: Float? = null) {
        viewModelScope.launch {
            weightHistoryRepository.insert(weight)

            // 更新当前计划中的体重和BMR
            val activePlan = planRepository.getActivePlan()
            if (activePlan != null) {
                planRepository.updateWeight(activePlan.id, weight, newBmr)
            }

            loadData()
        }
    }

    fun getAiEvaluation() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingEvaluation = true) }

            try {
                val currentState = _state.value
                val now = System.currentTimeMillis()
                val daysPassed = ((now - currentState.startDate) / (24 * 60 * 60 * 1000)).toInt()
                val totalDays = ((currentState.targetDate - currentState.startDate) / (24 * 60 * 60 * 1000)).toInt()

                val result = planRecommendationService.evaluatePlanProgress(
                    planName = currentState.planName,
                    startWeight = currentState.startWeight,
                    currentWeight = currentState.currentWeight,
                    targetWeight = currentState.targetWeight,
                    bmr = currentState.bmr,
                    daysPassed = daysPassed,
                    totalDays = totalDays
                )

                _state.update {
                    it.copy(
                        isLoadingEvaluation = false,
                        evaluation = result,
                        showEvaluationDialog = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingEvaluation = false,
                        evaluation = PlanEvaluationResult(
                            progressScore = 50,
                            status = "无法获取评估",
                            suggestions = "请检查网络连接或稍后重试",
                            encouragement = "继续加油！"
                        ),
                        showEvaluationDialog = true
                    )
                }
            }
        }
    }

    fun hideEvaluationDialog() {
        _state.update { it.copy(showEvaluationDialog = false) }
    }
}