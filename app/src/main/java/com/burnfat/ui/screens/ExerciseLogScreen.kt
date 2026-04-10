package com.burnfat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burnfat.ui.components.OutlinedNumberField

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExerciseLogViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加运动记录") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 运动类型选择
            Text("选择运动类型", style = MaterialTheme.typography.labelLarge)

            // 常见运动类型网格选择
            ExerciseTypeGrid(
                selectedType = state.exerciseType,
                onTypeSelected = { viewModel.setExerciseType(it) }
            )

            HorizontalDivider()

            // 消耗热量
            OutlinedNumberField(
                value = state.calories,
                onValueChange = { viewModel.setCalories(it) },
                label = "消耗热量",
                suffix = "kcal",
                modifier = Modifier.fillMaxWidth(),
                supportingText = "请根据运动手环、跑步机或App记录填写"
            )

            // 提示信息
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        "提示",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "热量消耗因人而异，建议参考专业设备数据",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = {
                    viewModel.saveExerciseEntry()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isValid
            ) {
                Icon(Icons.Default.Check, "保存")
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseTypeGrid(
    selectedType: String?,
    onTypeSelected: (String) -> Unit
) {
    val exerciseTypes = listOf(
        "跑步" to Icons.AutoMirrored.Filled.DirectionsRun,
        "快走" to Icons.Default.Hiking,
        "骑行" to Icons.Default.BikeScooter,
        "游泳" to Icons.Default.Pool,
        "跳绳" to Icons.Default.SportsGymnastics,
        "瑜伽" to Icons.Default.SelfImprovement,
        "力量训练" to Icons.Default.FitnessCenter,
        "篮球" to Icons.Default.SportsBasketball,
        "羽毛球" to Icons.Default.SportsTennis,
        "足球" to Icons.Default.SportsSoccer,
        "乒乓球" to Icons.Default.SportsTennis,
        "登山" to Icons.Default.Terrain,
        "舞蹈" to Icons.Default.MusicNote,
        "其他" to Icons.Default.MoreHoriz
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        exerciseTypes.forEach { (name, icon) ->
            FilterChip(
                selected = selectedType == name,
                onClick = { onTypeSelected(name) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = name,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(name)
                    }
                }
            )
        }
    }
}