package com.burnfat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burnfat.domain.model.ActivityLevel
import com.burnfat.ui.components.NumberInputCard
import com.burnfat.ui.components.ActivityLevelSelector
import com.burnfat.ui.components.TargetDateSelector
import com.burnfat.ui.components.GenderSelector
import com.burnfat.ui.components.CaloriePlanPreview
import com.burnfat.ui.components.calculateCaloriePlan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 表单数据
    var age by remember { mutableStateOf(25) }
    var gender by remember { mutableStateOf("MALE") }
    var bmr by remember { mutableStateOf(1500) }
    var currentWeight by remember { mutableStateOf(70) }
    var targetWeight by remember { mutableStateOf(65) }
    var activityLevel by remember { mutableStateOf(ActivityLevel.MODERATE) }

    // 计算推荐热量
    val caloriePlan = remember(bmr, currentWeight, targetWeight, activityLevel, state.targetDate) {
        calculateCaloriePlan(
            bmr = bmr,
            currentWeight = currentWeight,
            targetWeight = targetWeight,
            activityLevel = activityLevel,
            targetDate = state.targetDate
        )
    }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新建计划") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
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
            // 欢迎信息
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "欢迎使用燃脂君！",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "设置你的基础数据，我们将为你生成专属的热量计划",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // 性别选择
            GenderSelector(
                selectedGender = gender,
                onGenderChange = { gender = it }
            )

            // 年龄
            NumberInputCard(
                title = "年龄",
                value = age,
                range = 10..100,
                step = 1,
                unit = "岁",
                onValueChange = { age = it }
            )

            // 基础代谢率
            NumberInputCard(
                title = "基础代谢率 (BMR)",
                value = bmr,
                range = 800..3000,
                step = 50,
                unit = "kcal",
                onValueChange = { bmr = it },
                hint = "建议通过体脂秤或专业设备测量"
            )

            // 当前体重
            NumberInputCard(
                title = "当前体重",
                value = currentWeight,
                range = 30..200,
                step = 1,
                unit = "kg",
                onValueChange = { currentWeight = it }
            )

            // 目标体重
            NumberInputCard(
                title = "目标体重",
                value = targetWeight,
                range = 30..200,
                step = 1,
                unit = "kg",
                onValueChange = { targetWeight = it }
            )

            // 活动等级
            ActivityLevelSelector(
                selectedLevel = activityLevel,
                onLevelChange = { activityLevel = it }
            )

            // 目标日期
            TargetDateSelector(
                targetDate = state.targetDate,
                onDateChange = { viewModel.setTargetDate(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 热量计划预览
            CaloriePlanPreview(plan = caloriePlan)

            Spacer(modifier = Modifier.height(8.dp))

            // 开始按钮
            Button(
                onClick = {
                    viewModel.setAge(age.toString())
                    viewModel.setGender(gender)
                    viewModel.setBmr(bmr.toString())
                    viewModel.setCurrentWeight(currentWeight.toString())
                    viewModel.setTargetWeight(targetWeight.toString())
                    viewModel.setActivityLevel(activityLevel)
                    viewModel.saveAndComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = targetWeight < currentWeight && bmr >= 800 && age >= 10
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始减脂之旅", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun GenderChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                      else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}