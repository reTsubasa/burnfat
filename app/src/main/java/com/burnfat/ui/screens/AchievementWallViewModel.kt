package com.burnfat.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnfat.data.repository.AchievementRepository
import com.burnfat.data.repository.DailyRecordRepository
import com.burnfat.domain.model.BadgeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AchievementWallState(
    val totalDays: Int = 0,
    val perfectDays: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val earnedBadges: List<BadgeType> = emptyList(),
    val unearnedBadges: List<BadgeType> = BadgeType.entries.toList(),
    val monthAchievements: List<AchievementDisplay> = emptyList(),
    val selectedMonth: String = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy年M月"))
)

@HiltViewModel
class AchievementWallViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val dailyRecordRepository: DailyRecordRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AchievementWallState())
    val state: StateFlow<AchievementWallState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val totalDays = achievementRepository.countAchievedDays()
            val perfectDays = achievementRepository.countPerfectDays()
            val maxStreak = achievementRepository.getMaxStreak()

            // 获取本月成就
            val currentMonth = YearMonth.now()
            val startOfMonth = currentMonth.atDay(1).toEpochDay()
            val endOfMonth = currentMonth.atEndOfMonth().toEpochDay()

            val monthAchievements = achievementRepository.getByDateRange(startOfMonth, endOfMonth)
            val displays = monthAchievements.map { entity ->
                AchievementDisplay(
                    day = LocalDate.ofEpochDay(entity.date).dayOfMonth,
                    type = entity.type
                )
            }

            // 简单徽章计算
            val earnedBadges = mutableListOf<BadgeType>()
            if (totalDays >= 1) earnedBadges.add(BadgeType.FIRST_DAY)
            if (maxStreak >= 7) earnedBadges.add(BadgeType.WEEK_STREAK)
            if (maxStreak >= 14) earnedBadges.add(BadgeType.TWO_WEEK_STREAK)
            if (maxStreak >= 30) earnedBadges.add(BadgeType.MONTH_STREAK)

            val unearnedBadges = BadgeType.entries.filter { it !in earnedBadges }

            _state.update {
                it.copy(
                    totalDays = totalDays,
                    perfectDays = perfectDays,
                    maxStreak = maxStreak,
                    currentStreak = maxStreak, // 简化处理
                    earnedBadges = earnedBadges,
                    unearnedBadges = unearnedBadges,
                    monthAchievements = displays
                )
            }
        }
    }
}