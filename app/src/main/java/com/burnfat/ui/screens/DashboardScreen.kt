package com.burnfat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burnfat.domain.model.MealType
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToFoodLog: () -> Unit,
    onNavigateToExerciseLog: () -> Unit,
    onNavigateToPhotoCapture: (MealType) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToWeightCurve: () -> Unit,
    onNavigateToAchievementWall: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // FAB menu state
    var fabExpanded by remember { mutableStateOf(false) }

    // Refresh data when screen becomes active
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("燃脂君") },
                actions = {
                    IconButton(onClick = onNavigateToAchievementWall) {
                        Icon(Icons.Default.EmojiEvents, "达标墙")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            // FAB with expanding menu
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Menu items
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 拍照识别
                        FabMenuItem(
                            icon = Icons.Default.CameraAlt,
                            label = "拍照识别",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = {
                                fabExpanded = false
                                onNavigateToPhotoCapture(MealType.fromHour(java.time.LocalTime.now().hour))
                            }
                        )

                        // 手动添加餐食
                        FabMenuItem(
                            icon = Icons.Default.Restaurant,
                            label = "手动添加餐食",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            onClick = {
                                fabExpanded = false
                                onNavigateToFoodLog()
                            }
                        )

                        // 添加运动
                        FabMenuItem(
                            icon = Icons.Default.FitnessCenter,
                            label = "添加运动",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            onClick = {
                                fabExpanded = false
                                onNavigateToExerciseLog()
                            }
                        )
                    }
                }

                // Main FAB
                val rotation by animateFloatAsState(
                    targetValue = if (fabExpanded) 45f else 0f,
                    label = "fab_rotation"
                )
                FloatingActionButton(
                    onClick = { fabExpanded = !fabExpanded },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = if (fabExpanded) "关闭菜单" else "添加记录",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // 今日进度卡片
            item {
                TodayProgressCard(
                    intake = state.todayIntake,
                    target = state.todayTarget,
                    exercise = state.todayExercise,
                    streak = state.currentStreak,
                    isOverBudget = state.isOverBudget
                )
            }

            // 快捷入口
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickEntryCard(
                        title = "当前进度",
                        icon = Icons.AutoMirrored.Filled.ShowChart,
                        onClick = onNavigateToWeightCurve,
                        modifier = Modifier.weight(1f)
                    )

                    QuickEntryCard(
                        title = "历史",
                        icon = Icons.Default.History,
                        onClick = onNavigateToHistory,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 今日记录标题
            item {
                var showConversionTip by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日记录",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(
                        onClick = { showConversionTip = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "热量换算提示",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showConversionTip) {
                    AlertDialog(
                        onDismissRequest = { showConversionTip = false },
                        title = { Text("热量单位换算") },
                        text = {
                            Column {
                                Text("1 千卡 (kcal) = 4.184 千焦 (kJ)")
                                Text(
                                    "示例：2000 kcal ≈ 8368 kJ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showConversionTip = false }) {
                                Text("知道了")
                            }
                        }
                    )
                }
            }

            // 食物记录列表
            items(state.todayFoodEntries) { entry ->
                FoodEntryItem(
                    entry = entry,
                    onClick = { viewModel.showFoodDetail(entry) },
                    onDelete = { viewModel.deleteFoodEntry(entry.id) }
                )
            }

            // 运动记录列表
            items(state.todayExerciseEntries) { entry ->
                ExerciseEntryItem(
                    entry = entry,
                    onClick = { viewModel.showExerciseDetail(entry) },
                    onDelete = { viewModel.deleteExerciseEntry(entry.id) }
                )
            }

            // 底部空白
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
            }
        }
    }

    // 食物记录详情弹窗
    if (state.showFoodDetailDialog && state.selectedFoodEntry != null) {
        com.burnfat.ui.components.FoodEntryDetailDialog(
            id = state.selectedFoodEntry!!.id,
            foodName = state.selectedFoodEntry!!.foodName,
            mealType = state.selectedFoodEntry!!.mealType,
            calories = state.selectedFoodEntry!!.calories,
            portion = state.selectedFoodEntry!!.portion,
            photoPath = state.selectedFoodEntry!!.photoPath,
            sourceType = state.selectedFoodEntry!!.sourceType,
            onDismiss = { viewModel.hideFoodDetail() },
            onUpdateFoodName = { name -> viewModel.updateFoodName(state.selectedFoodEntry!!.id, name) },
            onUpdatePortion = { portion -> viewModel.updateFoodPortion(state.selectedFoodEntry!!.id, portion) },
            onUpdateMealType = { mealType -> viewModel.updateFoodMealType(state.selectedFoodEntry!!.id, mealType) },
            onUpdateCalories = { calories -> viewModel.updateFoodCalories(state.selectedFoodEntry!!.id, calories) },
            onDelete = { viewModel.deleteFoodEntry(state.selectedFoodEntry!!.id) }
        )
    }

    // 运动记录详情弹窗
    if (state.showExerciseDetailDialog && state.selectedExerciseEntry != null) {
        com.burnfat.ui.components.ExerciseEntryDetailDialog(
            id = state.selectedExerciseEntry!!.id,
            exerciseType = state.selectedExerciseEntry!!.exerciseType,
            calories = state.selectedExerciseEntry!!.calories,
            durationMinutes = state.selectedExerciseEntry!!.durationMinutes,
            notes = state.selectedExerciseEntry!!.notes,
            onDismiss = { viewModel.hideExerciseDetail() },
            onUpdateCalories = { calories -> viewModel.updateExerciseCalories(state.selectedExerciseEntry!!.id, calories) },
            onDelete = { viewModel.deleteExerciseEntry(state.selectedExerciseEntry!!.id) }
        )
    }
}

@Composable
fun FabMenuItem(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Label
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.small,
            shadowElevation = 2.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Mini FAB
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = contentColor
        ) {
            Icon(icon, contentDescription = label)
        }
    }
}

@Composable
fun TodayProgressCard(
    intake: Int,
    target: Int,
    exercise: Int,
    streak: Int,
    isOverBudget: Boolean
) {
    val totalBudget = target + exercise  // 总额度 = 建议摄入 + 运动消耗
    val remaining = totalBudget - intake  // 剩余 = 总额度 - 已摄入

    val cardColor = if (isOverBudget) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 进度环图
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CaloriePoolChart(
                    totalBudget = totalBudget,
                    intake = intake,
                    exercise = exercise,
                    isOverBudget = isOverBudget
                )

                // 中心文字
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isOverBudget) {
                        // 超标状态
                        Text(
                            "已超标",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "${kotlin.math.abs(remaining)} kcal",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        // 正常状态
                        Text(
                            "$remaining",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "剩余热量",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 图例
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    label = "已摄入",
                    value = "$intake"
                )
                LegendItem(
                    color = Color(0xFFFF9800),
                    label = "运动消耗",
                    value = "+$exercise"
                )
            }

            // 总额度提示
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "总额度: $totalBudget kcal (建议摄入 $target + 运动 $exercise)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 连续达标
            if (streak > 0 && !isOverBudget) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "🔥 连续达标 $streak 天",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

/**
 * 热量池进度图 - 单环设计
 * 外环底色表示总额度，填充部分表示已摄入
 */
@Composable
fun CaloriePoolChart(
    totalBudget: Int,
    intake: Int,
    exercise: Int,
    isOverBudget: Boolean
) {
    val backgroundColor = Color(0xFFE0E0E0)  // 底色 - 浅灰
    val intakeColor = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val exerciseColor = Color(0xFFFF9800)  // 橙色 - 运动部分

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val strokeWidth = 24f
        val radius = (size.minDimension / 2) - strokeWidth

        // 底环 - 总额度
        drawCircle(
            color = backgroundColor,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = strokeWidth)
        )

        if (totalBudget > 0) {
            // 计算比例
            val exerciseRatio = (exercise.toFloat() / totalBudget).coerceIn(0f, 1f)
            val intakeRatio = (intake.toFloat() / totalBudget).coerceIn(0f, 1f)

            // 绘制运动部分（从顶部开始，在摄入之后）
            if (exercise > 0) {
                drawArc(
                    color = exerciseColor,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    startAngle = -90f + intakeRatio * 360f,
                    sweepAngle = exerciseRatio * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 绘制已摄入部分（从顶部开始）
            if (intake > 0) {
                drawArc(
                    color = intakeColor,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2),
                    startAngle = -90f,
                    sweepAngle = intakeRatio * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, MaterialTheme.shapes.small)
        )
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "$value kcal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuickEntryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FoodEntryItem(
    entry: FoodEntryDisplay,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteDialog = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 摄入标签
            val intakeColor = Color(0xFF4CAF50)  // 绿色，与sunburst一致
            Surface(
                color = intakeColor,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    "摄入",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.foodName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${entry.mealType} · ${entry.calories} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除记录") },
            text = { Text("确定要删除 \"${entry.foodName}\" 这条记录吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseEntryItem(
    entry: ExerciseEntryDisplay,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteDialog = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 运动标签
            val exerciseColor = Color(0xFFFF9800)  // 橙色，与sunburst一致
            Surface(
                color = exerciseColor,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    "运动",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.exerciseType,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${entry.calories} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除记录") },
            text = { Text("确定要删除 \"${entry.exerciseType}\" 这条运动记录吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}