package com.burnfat.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录") },
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
        ) {
            // 时间范围选择
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeRange.entries.forEach { range ->
                    FilterChip(
                        selected = state.selectedRange == range,
                        onClick = { viewModel.setTimeRange(range) },
                        label = { Text(range.label) }
                    )
                }
            }

            // 统计卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("平均摄入", "${state.averageIntake} kcal")
                    StatItem("达标天数", "${state.achievedDays}/${state.totalDays}")
                    StatItem("运动消耗", "${state.totalExercise} kcal")
                }
            }

            // 记录列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.records) { record ->
                    DailyRecordCard(
                        record = record,
                        onClickFood = { item -> viewModel.showFoodDetail(item) },
                        onClickExercise = { item -> viewModel.showExerciseDetail(item) },
                        onDeleteFood = { id -> viewModel.deleteFoodEntry(id) },
                        onDeleteExercise = { id -> viewModel.deleteExerciseEntry(id) }
                    )
                }
            }
        }
    }

    // 食物记录详情弹窗
    if (state.showFoodDetailDialog && state.selectedFoodItem != null) {
        com.burnfat.ui.components.FoodEntryDetailDialog(
            id = state.selectedFoodItem!!.id,
            foodName = state.selectedFoodItem!!.name,
            mealType = state.selectedFoodItem!!.mealType,
            calories = state.selectedFoodItem!!.calories,
            portion = state.selectedFoodItem!!.portion,
            photoPath = state.selectedFoodItem!!.photoPath,
            sourceType = state.selectedFoodItem!!.sourceType,
            onDismiss = { viewModel.hideFoodDetail() },
            onUpdateFoodName = { name -> viewModel.updateFoodName(state.selectedFoodItem!!.id, name) },
            onUpdatePortion = { portion -> viewModel.updateFoodPortion(state.selectedFoodItem!!.id, portion) },
            onUpdateMealType = { mealType -> viewModel.updateFoodMealType(state.selectedFoodItem!!.id, mealType) },
            onUpdateCalories = { calories -> viewModel.updateFoodCalories(state.selectedFoodItem!!.id, calories) },
            onDelete = { viewModel.deleteFoodEntry(state.selectedFoodItem!!.id) }
        )
    }

    // 运动记录详情弹窗
    if (state.showExerciseDetailDialog && state.selectedExerciseItem != null) {
        com.burnfat.ui.components.ExerciseEntryDetailDialog(
            id = state.selectedExerciseItem!!.id,
            exerciseType = state.selectedExerciseItem!!.name,
            calories = state.selectedExerciseItem!!.calories,
            durationMinutes = state.selectedExerciseItem!!.duration,
            notes = null,
            onDismiss = { viewModel.hideExerciseDetail() },
            onUpdateCalories = { calories -> viewModel.updateExerciseCalories(state.selectedExerciseItem!!.id, calories) },
            onDelete = { viewModel.deleteExerciseEntry(state.selectedExerciseItem!!.id) }
        )
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun DailyRecordCard(
    record: DailyRecordDisplay,
    onClickFood: (FoodRecordItem) -> Unit,
    onClickExercise: (ExerciseRecordItem) -> Unit,
    onDeleteFood: (Long) -> Unit,
    onDeleteExercise: (Long) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 日期和达标状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(record.date, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (record.isOverBudget) {
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "超标",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                    if (record.achieved) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                "达标",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // 食物记录
            if (record.foodItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                record.foodItems.forEach { item ->
                    FoodRecordRow(
                        item = item,
                        onClick = { onClickFood(item) },
                        onDelete = { onDeleteFood(item.id) }
                    )
                }
            }

            // 运动记录
            if (record.exerciseItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                record.exerciseItems.forEach { item ->
                    ExerciseRecordRow(
                        item = item,
                        onClick = { onClickExercise(item) },
                        onDelete = { onDeleteExercise(item.id) }
                    )
                }
            }

            // 汇总
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            "总摄入",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${record.intake} kcal", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFFF9800).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            "总消耗",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${record.exercise} kcal", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FoodRecordRow(
    item: FoodRecordItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteDialog = true }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 摄入标签
        val intakeColor = Color(0xFF4CAF50)  // 绿色，与sunburst一致
        Surface(
            color = intakeColor,
            shape = MaterialTheme.shapes.extraSmall
        ) {
            Text(
                "摄入",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        // 图片或图标
        if (item.sourceType == "AI_PHOTO" && item.photoPath != null) {
            // 显示AI识别的照片缩略图
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(item.photoPath))
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
        } else {
            // 手动添加显示餐食图标
            Icon(
                Icons.Default.Restaurant,
                contentDescription = "餐食",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                style = MaterialTheme.typography.bodyMedium
            )
            // 显示份量描述
            if (item.portion != null) {
                Text(
                    item.portion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            "${item.calories} kcal",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除记录") },
            text = { Text("确定要删除 \"${item.name}\" 这条记录吗？") },
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
fun ExerciseRecordRow(
    item: ExerciseRecordItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val exerciseIcon = getExerciseIcon(item.name)
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteDialog = true }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 运动标签
        val exerciseColor = Color(0xFFFF9800)  // 橙色，与sunburst一致
        Surface(
            color = exerciseColor,
            shape = MaterialTheme.shapes.extraSmall
        ) {
            Text(
                "运动",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        // 运动类型图标
        Icon(
            exerciseIcon,
            contentDescription = item.name,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            item.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${item.calories} kcal",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除记录") },
            text = { Text("确定要删除 \"${item.name}\" 这条运动记录吗？") },
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

@Composable
fun getExerciseIcon(exerciseType: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (exerciseType) {
        "跑步" -> Icons.AutoMirrored.Filled.DirectionsRun
        "快走" -> Icons.Default.Hiking
        "骑行" -> Icons.Default.BikeScooter
        "游泳" -> Icons.Default.Pool
        "跳绳" -> Icons.Default.SportsGymnastics
        "瑜伽" -> Icons.Default.SelfImprovement
        "力量训练" -> Icons.Default.FitnessCenter
        "篮球" -> Icons.Default.SportsBasketball
        "羽毛球", "乒乓球" -> Icons.Default.SportsTennis
        "足球" -> Icons.Default.SportsSoccer
        "登山" -> Icons.Default.Terrain
        "舞蹈" -> Icons.Default.MusicNote
        else -> Icons.Default.FitnessCenter
    }
}