package com.burnfat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burnfat.domain.model.MealType
import com.burnfat.ui.components.OutlinedNumberField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: FoodLogViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSaveSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(showSaveSuccess) {
        if (showSaveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加食物记录") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 当前时段提示
            val currentMealType = MealType.fromHour(java.time.LocalTime.now().hour)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentMealType.icon,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "当前时段: ${currentMealType.displayName} (${currentMealType.typicalTimeRange})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // 餐次选择 - 使用 SegmentedButton
            Text(
                "选择餐次",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 早午晚餐 - 第一行
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = 0,
                        count = 3
                    ),
                    onClick = { viewModel.setMealType(MealType.BREAKFAST) },
                    selected = state.selectedMealType == MealType.BREAKFAST,
                    icon = {
                        if (state.selectedMealType == MealType.BREAKFAST) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    },
                    label = { Text("🌅 早餐") }
                )
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = 1,
                        count = 3
                    ),
                    onClick = { viewModel.setMealType(MealType.LUNCH) },
                    selected = state.selectedMealType == MealType.LUNCH,
                    icon = {
                        if (state.selectedMealType == MealType.LUNCH) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    },
                    label = { Text("☀️ 午餐") }
                )
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = 2,
                        count = 3
                    ),
                    onClick = { viewModel.setMealType(MealType.DINNER) },
                    selected = state.selectedMealType == MealType.DINNER,
                    icon = {
                        if (state.selectedMealType == MealType.DINNER) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    },
                    label = { Text("🌙 晚餐") }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 加餐 - 第二行
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = 0,
                        count = 1
                    ),
                    onClick = { viewModel.setMealType(MealType.SNACK) },
                    selected = state.selectedMealType == MealType.SNACK,
                    icon = {
                        if (state.selectedMealType == MealType.SNACK) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    },
                    label = { Text("🍎 加餐") }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 食物名称
            OutlinedTextField(
                value = state.foodName,
                onValueChange = { viewModel.setFoodName(it) },
                label = { Text("食物名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("例如：米饭、苹果") },
                leadingIcon = {
                    Icon(Icons.Default.Fastfood, contentDescription = null)
                }
            )

            // 热量
            OutlinedNumberField(
                value = state.calories,
                onValueChange = { viewModel.setCalories(it) },
                label = "热量",
                suffix = "kcal",
                modifier = Modifier.fillMaxWidth(),
                supportingText = "请参考食品包装或营养App"
            )

            // 份量描述
            OutlinedTextField(
                value = state.portion,
                onValueChange = { viewModel.setPortion(it) },
                label = { Text("份量描述 (可选)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("例如：一碗、100g") },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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
                        "可以使用首页的「拍照识别」功能自动识别食物热量",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = {
                    viewModel.saveFoodEntry()
                    showSaveSuccess = true
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