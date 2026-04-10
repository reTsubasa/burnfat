package com.burnfat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burnfat.domain.model.ActivityLevel
import com.burnfat.ui.components.NumberInputCard
import com.burnfat.ui.components.ActivityLevelSelector
import com.burnfat.ui.components.TargetDateSelector
import com.burnfat.ui.components.CaloriePlanPreview
import com.burnfat.ui.components.calculateCaloriePlan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: PlanManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("计划管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showCreateDialog() }) {
                        Icon(Icons.Default.Add, "新建计划")
                    }
                }
            )
        }
    ) { padding ->
        if (state.plans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Description,
                        "无计划",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "暂无减脂计划",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.showCreateDialog() }) {
                        Text("创建第一个计划")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.plans.forEach { plan ->
                    PlanCard(
                        plan = plan,
                        isActive = plan.id == state.activePlanId,
                        dateFormat = dateFormat,
                        onActivate = { viewModel.activatePlan(plan.id) },
                        onEdit = { viewModel.showEditDialog(plan) },
                        onDelete = { viewModel.showDeleteDialog(plan) }
                    )
                }
            }
        }
    }

    // 创建/编辑计划全屏对话框
    if (state.showCreateDialog || state.showEditDialog) {
        PlanFormScreen(
            title = if (state.showCreateDialog) "新建计划" else "编辑计划",
            state = state,
            dateFormat = dateFormat,
            onDismiss = { viewModel.hideDialogs() },
            onConfirm = {
                if (state.showCreateDialog) viewModel.createPlan()
                else viewModel.updatePlan()
            },
            onNameChange = { viewModel.setPlanName(it) },
            onStartWeightChange = { viewModel.setStartWeight(it) },
            onCurrentWeightChange = { viewModel.setCurrentWeight(it) },
            onTargetWeightChange = { viewModel.setTargetWeight(it) },
            onBmrChange = { viewModel.setBmr(it) },
            onActivityLevelChange = { viewModel.setActivityLevel(it) },
            onTargetDateChange = { viewModel.setTargetDate(it) },
            onEvaluateDesign = { viewModel.evaluatePlanDesign() },
            onHideEvaluation = { viewModel.hideEvaluationDialog() }
        )
    }

    // 删除确认对话框
    if (state.showDeleteDialog && state.selectedPlan != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDialogs() },
            title = { Text("确认删除") },
            text = { Text("确定要删除计划 \"${state.selectedPlan!!.name}\" 吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deletePlan() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDialogs() }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun PlanCard(
    plan: com.burnfat.data.local.entity.PlanEntity,
    isActive: Boolean,
    dateFormat: SimpleDateFormat,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColor = if (isActive) {
        Color(0xFF4CAF50).copy(alpha = 0.15f)  // 绿色背景突出当前计划
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isActive) {
                    Modifier.background(
                        color = cardColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isActive) {
                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "当前进度",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 体重信息 - 统一字体
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text("起始体重", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${plan.startWeight} kg",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text("当前体重", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${plan.currentWeight} kg",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text("目标体重", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${plan.targetWeight} kg",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 基础代谢和目标日期 - 统一字体
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text("基础代谢", style = MaterialTheme.typography.labelMedium)
                    Text(
                        "${plan.bmr.toInt()} kcal/天",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text("目标日期", style = MaterialTheme.typography.labelMedium)
                    Text(
                        dateFormat.format(Date(plan.targetDate)),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isActive) {
                    Button(
                        onClick = onActivate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("激活")
                    }
                }
                OutlinedButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "编辑", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("编辑")
                }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, "删除", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanFormScreen(
    title: String,
    state: PlanManagementState,
    dateFormat: SimpleDateFormat,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onNameChange: (String) -> Unit,
    onStartWeightChange: (String) -> Unit,
    onCurrentWeightChange: (String) -> Unit,
    onTargetWeightChange: (String) -> Unit,
    onBmrChange: (String) -> Unit,
    onActivityLevelChange: (String) -> Unit,
    onTargetDateChange: (Long) -> Unit,
    onEvaluateDesign: () -> Unit = {},
    onHideEvaluation: () -> Unit = {}
) {
    // 内部状态用于数字输入
    var startWeight by remember { mutableStateOf(state.startWeight.toFloatOrNull()?.toInt() ?: 70) }
    var currentWeight by remember { mutableStateOf(state.currentWeight.toFloatOrNull()?.toInt() ?: 70) }
    var targetWeight by remember { mutableStateOf(state.targetWeight.toFloatOrNull()?.toInt() ?: 65) }
    var bmr by remember { mutableStateOf(state.bmr.toFloatOrNull()?.toInt() ?: 1500) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onConfirm,
                        enabled = state.isFormValid
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 计划名称
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = state.planName,
                        onValueChange = onNameChange,
                        label = { Text("计划名称") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // 起始体重
            NumberInputCard(
                title = "起始体重",
                value = startWeight,
                range = 30..200,
                step = 1,
                unit = "kg",
                onValueChange = {
                    startWeight = it
                    onStartWeightChange(it.toString())
                }
            )

            // 当前体重
            NumberInputCard(
                title = "当前体重",
                value = currentWeight,
                range = 30..200,
                step = 1,
                unit = "kg",
                onValueChange = {
                    currentWeight = it
                    onCurrentWeightChange(it.toString())
                }
            )

            // 目标体重
            NumberInputCard(
                title = "目标体重",
                value = targetWeight,
                range = 30..200,
                step = 1,
                unit = "kg",
                onValueChange = {
                    targetWeight = it
                    onTargetWeightChange(it.toString())
                }
            )

            // 基础代谢率
            NumberInputCard(
                title = "基础代谢率 (BMR)",
                value = bmr,
                range = 800..3000,
                step = 50,
                unit = "kcal",
                onValueChange = {
                    bmr = it
                    onBmrChange(it.toString())
                },
                hint = "建议通过体脂秤或专业设备测量"
            )

            // 活动等级
            ActivityLevelSelector(
                selectedLevel = ActivityLevel.fromName(state.activityLevel),
                onLevelChange = { onActivityLevelChange(it.name) }
            )

            // 目标日期
            TargetDateSelector(
                targetDate = state.targetDate,
                onDateChange = onTargetDateChange
            )

            // 热量计划预览
            val bmrValue = bmr
            val currentWeightValue = currentWeight
            val targetWeightValue = targetWeight
            val activityLevelValue = ActivityLevel.fromName(state.activityLevel)

            val caloriePlan = remember(bmrValue, currentWeightValue, targetWeightValue, activityLevelValue, state.targetDate) {
                calculateCaloriePlan(
                    bmr = bmrValue,
                    currentWeight = currentWeightValue,
                    targetWeight = targetWeightValue,
                    activityLevel = activityLevelValue,
                    targetDate = state.targetDate
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            CaloriePlanPreview(plan = caloriePlan)

            // AI评估按钮
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onEvaluateDesign,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoadingEvaluation && state.isFormValid
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
                    Text("AI评估计划合理性")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // AI评估对话框
    state.designEvaluation?.let { evaluation ->
        if (state.showEvaluationDialog) {
            PlanDesignEvaluationDialog(
                evaluation = evaluation,
                onDismiss = onHideEvaluation
            )
        }
    }

    // AI评估错误对话框
    if (state.evaluationError != null) {
        AlertDialog(
            onDismissRequest = onHideEvaluation,
            icon = {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("AI评估失败") },
            text = {
                Text(
                    state.evaluationError!!,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = onHideEvaluation) {
                    Text("知道了")
                }
            }
        )
    }
}

@Composable
fun PlanDesignEvaluationDialog(
    evaluation: com.burnfat.data.remote.PlanDesignEvaluationResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = when {
                    evaluation.overallScore >= 70 -> Color(0xFF4CAF50)
                    evaluation.overallScore >= 50 -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.error
                }
            )
        },
        title = {
            Text("AI计划评估")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 综合评分
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        progress = { evaluation.overallScore / 100f },
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 8.dp,
                        color = when {
                            evaluation.overallScore >= 70 -> Color(0xFF4CAF50)
                            evaluation.overallScore >= 50 -> Color(0xFFFF9800)
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
                Text(
                    "${evaluation.overallScore}分",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    evaluation.status,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider()

                // 详细评分
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${evaluation.feasibilityScore}",
                            style = MaterialTheme.typography.titleLarge,
                            color = when {
                                evaluation.feasibilityScore >= 70 -> Color(0xFF4CAF50)
                                evaluation.feasibilityScore >= 50 -> Color(0xFFFF9800)
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                        Text("可行性", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${evaluation.healthScore}",
                            style = MaterialTheme.typography.titleLarge,
                            color = when {
                                evaluation.healthScore >= 70 -> Color(0xFF4CAF50)
                                evaluation.healthScore >= 50 -> Color(0xFFFF9800)
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                        Text("健康安全", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // 存在的问题
                if (evaluation.issues.isNotEmpty()) {
                    Text("注意事项", style = MaterialTheme.typography.labelLarge)
                    evaluation.issues.forEach { issue ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text("• ", color = MaterialTheme.colorScheme.error)
                            Text(issue, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // 建议
                Text("改进建议", style = MaterialTheme.typography.labelLarge)
                Text(evaluation.suggestions, style = MaterialTheme.typography.bodyMedium)

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