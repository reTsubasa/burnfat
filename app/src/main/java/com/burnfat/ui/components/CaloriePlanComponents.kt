package com.burnfat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.burnfat.domain.model.ActivityLevel

/**
 * 热量计划数据
 */
data class CaloriePlanData(
    val dailyDeficit: Int,
    val tdee: Int,
    val targetIntake: Int,
    val weeklyWeightLoss: Float,
    val isHealthy: Boolean
)

/**
 * 计算热量计划
 */
fun calculateCaloriePlan(
    bmr: Int,
    currentWeight: Int,
    targetWeight: Int,
    activityLevel: ActivityLevel,
    targetDate: Long
): CaloriePlanData {
    val weightToLose = currentWeight - targetWeight
    val totalDeficit = (weightToLose * 7700).toInt()
    val daysToTarget = ((targetDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
    val dailyDeficit = (totalDeficit / daysToTarget).coerceIn(0, 1000)

    val tdee = (bmr * activityLevel.multiplier).toInt()
    val targetIntake = (tdee - dailyDeficit).coerceAtLeast(1000)

    val weeklyWeightLoss = dailyDeficit / 7700f * 7

    val isHealthy = dailyDeficit <= 500 && weeklyWeightLoss <= 1f

    return CaloriePlanData(
        dailyDeficit = dailyDeficit,
        tdee = tdee,
        targetIntake = targetIntake,
        weeklyWeightLoss = weeklyWeightLoss,
        isHealthy = isHealthy
    )
}

/**
 * 热量计划预览卡片
 */
@Composable
fun CaloriePlanPreview(
    plan: CaloriePlanData,
    showTitle: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (plan.isHealthy)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (showTitle) {
                Text(
                    "你的热量计划",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            CalorieBarChart(
                tdee = plan.tdee,
                targetIntake = plan.targetIntake,
                dailyDeficit = plan.dailyDeficit,
                isHealthy = plan.isHealthy
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 预计每周减重
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TrendingDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (plan.isHealthy)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("预计每周减重: ", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${String.format("%.1f", plan.weeklyWeightLoss)} kg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (plan.isHealthy)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }

            if (!plan.isHealthy) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "热量缺口过大，建议延长目标时间",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * 分段柱状图
 */
@Composable
fun CalorieBarChart(
    tdee: Int,
    targetIntake: Int,
    dailyDeficit: Int,
    isHealthy: Boolean
) {
    val intakeColor = MaterialTheme.colorScheme.primary
    val deficitColor = if (isHealthy) Color(0xFFFF9800) else MaterialTheme.colorScheme.error

    Column(modifier = Modifier.fillMaxWidth()) {
        // 标签行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("每日消耗 (TDEE)", style = MaterialTheme.typography.labelSmall)
            Text("${tdee} kcal", style = MaterialTheme.typography.labelSmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 单柱分段图
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 热量缺口段（上部）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight((dailyDeficit.toFloat() / tdee.toFloat()).coerceIn(0f, 1f))
                        .background(deficitColor)
                )

                // 每日摄入段（下部）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight((targetIntake.toFloat() / tdee.toFloat()).coerceIn(0f, 1f))
                        .background(intakeColor)
                )
            }

            // 热量缺口标签（显示在缺口区域）
            if (dailyDeficit > 0) {
                val deficitPercent = ((dailyDeficit.toFloat() / tdee.toFloat()) * 100).toInt()
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "热量缺口",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                    Text(
                        "${dailyDeficit} kcal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "(${deficitPercent}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // 每日摄入标签（显示在摄入区域）
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "建议摄入",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
                Text(
                    "${targetIntake} kcal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 图例
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 摄入图例
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(intakeColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("摄入", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.width(16.dp))

            // 缺口图例
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(deficitColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("缺口", style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * 简化版热量计划摘要（用于当前计划页面）
 */
@Composable
fun CaloriePlanSummary(plan: CaloriePlanData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "热量计划",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 三列数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalorieSummaryItem("每日消耗", "${plan.tdee}", MaterialTheme.colorScheme.onSurface)
                CalorieSummaryItem("建议摄入", "${plan.targetIntake}", MaterialTheme.colorScheme.primary)
                CalorieSummaryItem("热量缺口", "${plan.dailyDeficit}", if (plan.isHealthy) Color(0xFFFF9800) else MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 预计每周减重
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TrendingDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (plan.isHealthy)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("预计每周减重: ", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${String.format("%.1f", plan.weeklyWeightLoss)} kg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (plan.isHealthy)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CalorieSummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text("kcal", style = MaterialTheme.typography.labelSmall)
    }
}