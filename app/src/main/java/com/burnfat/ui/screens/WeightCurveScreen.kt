package com.burnfat.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burnfat.ui.components.OutlinedNumberField
import com.burnfat.ui.components.CaloriePlanSummary
import com.burnfat.ui.components.calculateCaloriePlan
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightCurveScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlanEdit: ((Long) -> Unit)? = null,
    viewModel: WeightCurveViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("当前进度") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 计划信息卡片
            if (state.planName.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    onClick = { onNavigateToPlanEdit?.invoke(state.planId) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            state.planName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑计划",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // 热量计划摘要图表
            if (state.bmr > 0) {
                val caloriePlan = remember(state.bmr, state.currentWeight, state.targetWeight, state.activityLevel, state.targetDate) {
                    calculateCaloriePlan(
                        bmr = state.bmr.toInt(),
                        currentWeight = state.currentWeight.toInt(),
                        targetWeight = state.targetWeight.toInt(),
                        activityLevel = com.burnfat.domain.model.ActivityLevel.fromName(state.activityLevel),
                        targetDate = state.targetDate
                    )
                }
                CaloriePlanSummary(plan = caloriePlan)
            }

            // 统计卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeightStatItem("起始", "${state.startWeight} kg")
                    WeightStatItem("当前", "${state.currentWeight} kg", highlight = true)
                    WeightStatItem("目标", "${state.targetWeight} kg")
                }
            }

            // 累计减重
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("累计减重")
                    Text(
                        "${String.format("%.1f", state.totalLoss)} kg",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state.totalLoss > 0) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.error
                    )
                }
            }

            // 体重曲线图表
            Card(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("体重变化", style = MaterialTheme.typography.titleMedium)
                        Text(
                            when (state.trend) {
                                WeightTrend.DESCENDING -> "下降中"
                                WeightTrend.STABLE -> "稳定"
                                WeightTrend.ASCENDING -> "上升中"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when (state.trend) {
                                WeightTrend.DESCENDING -> MaterialTheme.colorScheme.primary
                                WeightTrend.ASCENDING -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.points.isNotEmpty()) {
                        WeightCurveChart(
                            actualPoints = state.points,
                            predictedPoints = state.predictedPoints,
                            targetWeight = state.targetWeight,
                            startWeight = state.startWeight,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无体重数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 记录体重按钮
            Button(
                onClick = { viewModel.showWeightDialog() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, "记录")
                Spacer(modifier = Modifier.width(8.dp))
                Text("记录今日体重")
            }

            // AI评估按钮
            OutlinedButton(
                onClick = { viewModel.getAiEvaluation() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoadingEvaluation
            ) {
                if (state.isLoadingEvaluation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("评估中...")
                } else {
                    Icon(Icons.Default.AutoAwesome, "AI评估")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("获取AI评估建议")
                }
            }
        }
    }

    // 体重记录对话框
    if (state.showWeightDialog) {
        WeightRecordDialog(
            currentBmr = state.bmr,
            onDismiss = { viewModel.hideWeightDialog() },
            onConfirm = { weight, newBmr ->
                viewModel.recordWeight(weight, newBmr)
                viewModel.hideWeightDialog()
            }
        )
    }

    // AI评估对话框
    state.evaluation?.let { evaluation ->
        if (state.showEvaluationDialog) {
            EvaluationDialog(
                evaluation = evaluation,
                onDismiss = { viewModel.hideEvaluationDialog() }
            )
        }
    }
}

@Composable
fun WeightStatItem(label: String, value: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = if (highlight) MaterialTheme.typography.titleLarge
                           else MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun WeightCurveChart(
    actualPoints: List<WeightPoint>,
    predictedPoints: List<WeightPoint>,
    targetWeight: Float,
    startWeight: Float,
    modifier: Modifier = Modifier
) {
    if (actualPoints.isEmpty() && predictedPoints.isEmpty()) return

    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd")

    // 计算Y轴范围
    val allWeights = actualPoints.map { it.weight } + predictedPoints.map { it.weight } + listOf(targetWeight, startWeight)
    val minWeight = (allWeights.minOrNull() ?: 0f).let { it - 2 }
    val maxWeight = (allWeights.maxOrNull() ?: 0f).let { it + 2 }
    val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)

    // 计算X轴日期范围
    val allDates = actualPoints.map { it.date } + predictedPoints.map { it.date }
    val minDate = allDates.minOrNull() ?: System.currentTimeMillis()
    val maxDate = allDates.maxOrNull() ?: System.currentTimeMillis()
    val dateRange = (maxDate - minDate).coerceAtLeast(1L)

    // 颜色
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Y轴标签
    val yLabels = listOf(
        String.format("%.0f", maxWeight),
        String.format("%.0f", (maxWeight + minWeight) / 2),
        String.format("%.0f", minWeight)
    )

    // X轴标签
    val xLabels = listOf(
        Instant.ofEpochMilli(minDate).atZone(ZoneId.systemDefault()).toLocalDate(),
        Instant.ofEpochMilli(minDate + dateRange / 2).atZone(ZoneId.systemDefault()).toLocalDate(),
        Instant.ofEpochMilli(maxDate).atZone(ZoneId.systemDefault()).toLocalDate()
    )

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            // Y轴标签
            Column(
                modifier = Modifier.width(32.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                yLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant
                    )
                }
            }

            // 图表区域
            Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // 目标线
                val targetY = ((maxWeight - targetWeight) / weightRange * canvasHeight)
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(0f, targetY),
                    end = Offset(canvasWidth, targetY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )

                // 预测曲线 (虚线)
                if (predictedPoints.isNotEmpty()) {
                    val predictedPathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    var prevX = -1f
                    var prevY = -1f

                    predictedPoints.sortedBy { it.date }.forEach { point ->
                        val x = ((point.date - minDate).toFloat() / dateRange * canvasWidth)
                        val y = ((maxWeight - point.weight) / weightRange * canvasHeight)

                        if (prevX >= 0) {
                            drawLine(
                                color = surfaceVariant,
                                start = Offset(prevX, prevY),
                                end = Offset(x, y),
                                strokeWidth = 3f,
                                pathEffect = predictedPathEffect,
                                cap = StrokeCap.Round
                            )
                        }
                        prevX = x
                        prevY = y
                    }
                }

                // 实际数据曲线 (实线)
                if (actualPoints.isNotEmpty()) {
                    var prevX = -1f
                    var prevY = -1f

                    actualPoints.sortedBy { it.date }.forEach { point ->
                        val x = ((point.date - minDate).toFloat() / dateRange * canvasWidth)
                        val y = ((maxWeight - point.weight) / weightRange * canvasHeight)

                        // 绘制连线
                        if (prevX >= 0) {
                            drawLine(
                                color = primaryColor,
                                start = Offset(prevX, prevY),
                                end = Offset(x, y),
                                strokeWidth = 3f,
                                cap = StrokeCap.Round
                            )
                        }

                        // 绘制数据点
                        drawCircle(
                            color = primaryColor,
                            center = Offset(x, y),
                            radius = 6f
                        )

                        prevX = x
                        prevY = y
                    }
                }
            }
        }

        // X轴标签
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            xLabels.forEach { date ->
                Text(
                    text = date.format(dateFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WeightRecordDialog(
    currentBmr: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float, Float?) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var bmr by remember { mutableStateOf(currentBmr.toString()) }
    var updateBmr by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录体重") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedNumberField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = "体重",
                    suffix = "kg",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = updateBmr,
                        onCheckedChange = { updateBmr = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("同时更新基础代谢率")
                }

                if (updateBmr) {
                    OutlinedNumberField(
                        value = bmr,
                        onValueChange = { bmr = it },
                        label = "基础代谢率",
                        suffix = "kcal/天",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "体重变化后，基础代谢率也会相应变化。建议重新测量或按公式估算：\n新BMR ≈ 当前BMR × (新体重/当前体重)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    "建议在早晨空腹、排便后测量",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    weight.toFloatOrNull()?.let { w ->
                        val newBmr = if (updateBmr) bmr.toFloatOrNull() else null
                        onConfirm(w, newBmr)
                    }
                },
                enabled = weight.isNotBlank() && (!updateBmr || bmr.isNotBlank())
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

enum class WeightTrend {
    DESCENDING, STABLE, ASCENDING
}

data class WeightPoint(
    val date: Long,
    val weight: Float
)

@Composable
fun EvaluationDialog(
    evaluation: com.burnfat.data.remote.PlanEvaluationResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("AI评估报告")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 进度评分
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        progress = { evaluation.progressScore / 100f },
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 8.dp,
                        color = when {
                            evaluation.progressScore >= 70 -> MaterialTheme.colorScheme.primary
                            evaluation.progressScore >= 50 -> Color(0xFFFF9800)
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
                Text(
                    "${evaluation.progressScore}分",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    evaluation.status,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider()

                // 建议
                Text("改进建议", style = MaterialTheme.typography.labelLarge)
                Text(
                    evaluation.suggestions,
                    style = MaterialTheme.typography.bodyMedium
                )

                // 鼓励语
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        evaluation.encouragement,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了")
            }
        }
    )
}