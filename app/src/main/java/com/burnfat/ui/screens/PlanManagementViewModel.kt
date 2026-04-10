package com.burnfat.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnfat.data.local.entity.PlanEntity
import com.burnfat.data.remote.ApiException
import com.burnfat.data.remote.PlanDesignEvaluationResult
import com.burnfat.data.remote.PlanRecommendationService
import com.burnfat.data.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlanManagementState(
    val plans: List<PlanEntity> = emptyList(),
    val activePlanId: Long? = null,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedPlan: PlanEntity? = null,
    // 新建/编辑表单字段
    val planName: String = "",
    val startWeight: String = "",
    val currentWeight: String = "",
    val targetWeight: String = "",
    val bmr: String = "",
    val activityLevel: String = "MODERATE",
    val targetDate: Long = System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000, // 默认90天后
    // AI评估相关
    val isLoadingEvaluation: Boolean = false,
    val designEvaluation: PlanDesignEvaluationResult? = null,
    val showEvaluationDialog: Boolean = false,
    val evaluationError: String? = null
) {
    val isFormValid: Boolean
        get() = planName.isNotBlank() &&
                startWeight.toFloatOrNull()?.let { it > 0 } ?: false &&
                currentWeight.toFloatOrNull()?.let { it > 0 } ?: false &&
                targetWeight.toFloatOrNull()?.let { it > 0 } ?: false &&
                bmr.toFloatOrNull()?.let { it > 0 } ?: false
}

@HiltViewModel
class PlanManagementViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val planRecommendationService: PlanRecommendationService
) : ViewModel() {

    private val _state = MutableStateFlow(PlanManagementState())
    val state: StateFlow<PlanManagementState> = _state.asStateFlow()

    init {
        loadPlans()
    }

    fun loadPlans() {
        viewModelScope.launch {
            val plans = planRepository.getAllPlans()
            val activePlan = planRepository.getActivePlan()
            _state.update {
                it.copy(
                    plans = plans,
                    activePlanId = activePlan?.id
                )
            }
        }
    }

    fun showCreateDialog() {
        _state.update {
            it.copy(
                showCreateDialog = true,
                planName = "",
                startWeight = "",
                currentWeight = "",
                targetWeight = "",
                bmr = "",
                activityLevel = "MODERATE",
                selectedPlan = null
            )
        }
    }

    fun showEditDialog(plan: PlanEntity) {
        _state.update {
            it.copy(
                showEditDialog = true,
                selectedPlan = plan,
                planName = plan.name,
                startWeight = plan.startWeight.toString(),
                currentWeight = plan.currentWeight.toString(),
                targetWeight = plan.targetWeight.toString(),
                bmr = plan.bmr.toString(),
                activityLevel = plan.activityLevel,
                targetDate = plan.targetDate
            )
        }
    }

    fun showDeleteDialog(plan: PlanEntity) {
        _state.update {
            it.copy(
                showDeleteDialog = true,
                selectedPlan = plan
            )
        }
    }

    fun hideDialogs() {
        _state.update {
            it.copy(
                showCreateDialog = false,
                showEditDialog = false,
                showDeleteDialog = false,
                selectedPlan = null
            )
        }
    }

    fun setPlanName(value: String) {
        _state.update { it.copy(planName = value) }
    }

    fun setStartWeight(value: String) {
        _state.update { it.copy(startWeight = value) }
    }

    fun setCurrentWeight(value: String) {
        _state.update { it.copy(currentWeight = value) }
    }

    fun setTargetWeight(value: String) {
        _state.update { it.copy(targetWeight = value) }
    }

    fun setBmr(value: String) {
        _state.update { it.copy(bmr = value) }
    }

    fun setActivityLevel(value: String) {
        _state.update { it.copy(activityLevel = value) }
    }

    fun setTargetDate(value: Long) {
        _state.update { it.copy(targetDate = value) }
    }

    fun createPlan() {
        viewModelScope.launch {
            val currentState = _state.value
            if (!currentState.isFormValid) return@launch

            val plan = PlanEntity(
                name = currentState.planName,
                startWeight = currentState.startWeight.toFloat(),
                currentWeight = currentState.currentWeight.toFloat(),
                targetWeight = currentState.targetWeight.toFloat(),
                bmr = currentState.bmr.toFloat(),
                activityLevel = currentState.activityLevel,
                targetDate = currentState.targetDate,
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            planRepository.createPlan(plan)
            hideDialogs()
            loadPlans()
        }
    }

    fun updatePlan() {
        viewModelScope.launch {
            val currentState = _state.value
            val selectedPlan = currentState.selectedPlan ?: return@launch
            if (!currentState.isFormValid) return@launch

            planRepository.updatePlan(
                id = selectedPlan.id,
                name = currentState.planName,
                startWeight = currentState.startWeight.toFloat(),
                currentWeight = currentState.currentWeight.toFloat(),
                targetWeight = currentState.targetWeight.toFloat(),
                bmr = currentState.bmr.toFloat(),
                activityLevel = currentState.activityLevel,
                targetDate = currentState.targetDate
            )

            hideDialogs()
            loadPlans()
        }
    }

    fun activatePlan(planId: Long) {
        viewModelScope.launch {
            planRepository.activatePlan(planId)
            loadPlans()
        }
    }

    fun deletePlan() {
        viewModelScope.launch {
            val selectedPlan = _state.value.selectedPlan ?: return@launch
            planRepository.deletePlan(selectedPlan.id)
            hideDialogs()
            loadPlans()
        }
    }

    fun evaluatePlanDesign() {
        viewModelScope.launch {
            val currentState = _state.value
            if (!currentState.isFormValid) return@launch

            _state.update { it.copy(isLoadingEvaluation = true, evaluationError = null) }

            try {
                val result = planRecommendationService.evaluatePlanDesign(
                    startWeight = currentState.startWeight.toFloat(),
                    currentWeight = currentState.currentWeight.toFloat(),
                    targetWeight = currentState.targetWeight.toFloat(),
                    bmr = currentState.bmr.toFloat(),
                    activityLevel = currentState.activityLevel,
                    targetDate = currentState.targetDate
                )

                _state.update {
                    it.copy(
                        isLoadingEvaluation = false,
                        designEvaluation = result,
                        showEvaluationDialog = true
                    )
                }
            } catch (e: ApiException) {
                _state.update {
                    it.copy(
                        isLoadingEvaluation = false,
                        evaluationError = e.message ?: "AI服务调用失败"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingEvaluation = false,
                        evaluationError = "评估失败: ${e.message ?: "请稍后重试"}"
                    )
                }
            }
        }
    }

    fun hideEvaluationDialog() {
        _state.update { it.copy(showEvaluationDialog = false, evaluationError = null) }
    }
}