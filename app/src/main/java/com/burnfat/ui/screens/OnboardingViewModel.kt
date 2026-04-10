package com.burnfat.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnfat.data.repository.PlanRepository
import com.burnfat.data.repository.UserProfileRepository
import com.burnfat.domain.model.ActivityLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val age: String = "",
    val gender: String = "MALE",
    val bmr: String = "",
    val currentWeight: String = "",
    val targetWeight: String = "",
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val targetDate: Long = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000), // 3 months default
    val isCompleted: Boolean = false,
    val isLoading: Boolean = true
) {
    val isValid: Boolean
        get() = age.toIntOrNull()?.let { it in 10..100 } ?: false &&
                bmr.toFloatOrNull()?.let { it in 800f..3000f } ?: false &&
                currentWeight.toFloatOrNull()?.let { it > 0 } ?: false &&
                targetWeight.toFloatOrNull()?.let { it > 0 } ?: false
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val planRepository: PlanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        // 检查是否有active plan，如果有则跳过onboarding
        viewModelScope.launch {
            val hasCompleted = userProfileRepository.hasCompletedOnboarding() ||
                              planRepository.getActivePlan() != null
            _state.update { it.copy(isCompleted = hasCompleted, isLoading = false) }
        }
    }

    fun setAge(value: String) {
        _state.update { it.copy(age = value.filter { c -> c.isDigit() }) }
    }

    fun setGender(value: String) {
        _state.update { it.copy(gender = value) }
    }

    fun setBmr(value: String) {
        _state.update { it.copy(bmr = value.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun setCurrentWeight(value: String) {
        _state.update { it.copy(currentWeight = value.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun setTargetWeight(value: String) {
        _state.update { it.copy(targetWeight = value.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun setActivityLevel(level: ActivityLevel) {
        _state.update { it.copy(activityLevel = level) }
    }

    fun setTargetDate(timestamp: Long) {
        _state.update { it.copy(targetDate = timestamp) }
    }

    fun saveAndComplete() {
        viewModelScope.launch {
            val currentState = _state.value

            val age = currentState.age.toIntOrNull() ?: return@launch
            val bmr = currentState.bmr.toFloatOrNull() ?: return@launch
            val currentWeight = currentState.currentWeight.toFloatOrNull() ?: return@launch
            val targetWeight = currentState.targetWeight.toFloatOrNull() ?: return@launch

            // Save user profile
            userProfileRepository.saveProfile(
                age = age,
                gender = currentState.gender,
                hasCompletedOnboarding = true
            )

            // Create plan
            val planCount = planRepository.getAll().size
            planRepository.createPlan(
                name = "计划 #${planCount + 1}",
                startWeight = currentWeight,
                targetWeight = targetWeight,
                bmr = bmr,
                activityLevel = currentState.activityLevel.name,
                targetDate = currentState.targetDate
            )

            _state.update { it.copy(isCompleted = true) }
        }
    }
}