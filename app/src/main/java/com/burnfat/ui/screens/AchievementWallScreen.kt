package com.burnfat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burnfat.domain.model.BadgeRarity
import com.burnfat.domain.model.BadgeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementWallScreen(
    onNavigateBack: () -> Unit,
    viewModel: AchievementWallViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("达标墙") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 统计概览
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AchievementStat("累计达标", "${state.totalDays}天", "✅")
                        AchievementStat("完美达标", "${state.perfectDays}天", "🌟")
                        AchievementStat("当前连续", "${state.currentStreak}天", "🔥")
                        AchievementStat("最长连续", "${state.maxStreak}天", "👑")
                    }
                }
            }

            // 徽章展示
            item {
                Text("获得的徽章", style = MaterialTheme.typography.titleMedium)
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.earnedBadges) { badge ->
                        BadgeItem(
                            badge = badge,
                            earned = true,
                            modifier = Modifier.size(100.dp)
                        )
                    }

                    // 未获得徽章(显示为锁定状态)
                    items(state.unearnedBadges) { badge ->
                        BadgeItem(
                            badge = badge,
                            earned = false,
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }
            }

            // 达标日历标题
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("达标日历", style = MaterialTheme.typography.titleMedium)
            }

            // 达标日历
            item {
                AchievementCalendar(
                    achievements = state.monthAchievements,
                    currentMonth = state.selectedMonth
                )
            }
        }
    }
}

@Composable
fun AchievementStat(label: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, style = MaterialTheme.typography.headlineMedium)
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun BadgeItem(
    badge: BadgeType,
    earned: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (earned) {
        Color(android.graphics.Color.parseColor(badge.rarity.colorHex))
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (earned) badge.rarity.emoji else "🔒",
                style = MaterialTheme.typography.headlineMedium,
                color = if (earned) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                badge.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (earned) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AchievementCalendar(
    achievements: List<AchievementDisplay>,
    currentMonth: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(currentMonth, style = MaterialTheme.typography.titleSmall)

            Spacer(modifier = Modifier.height(12.dp))

            // 星期标题
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEach { day ->
                    Text(
                        day,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 日期格子 (简化版，实际需要根据月份计算)
            for (week in 0..4) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (day in 0..6) {
                        val dayNum = week * 7 + day + 1
                        val achievement = achievements.find { it.day == dayNum }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    when (achievement?.type) {
                                        "PERFECT" -> MaterialTheme.colorScheme.primary
                                        "GOOD" -> Color(0xFF8BC34A)
                                        "ACCEPTABLE" -> Color(0xFFFFEB3B)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNum <= 31) {
                                Text(
                                    "$dayNum",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (achievement != null) Color.White
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class AchievementDisplay(
    val day: Int,
    val type: String?
)