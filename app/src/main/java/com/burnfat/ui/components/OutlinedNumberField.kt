package com.burnfat.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * 数字输入框组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    prefix: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // 只允许数字和小数点
            val filtered = newValue.filter { it.isDigit() || it == '.' }
            // 确保只有一个小数点
            if (filtered.count { it == '.' } <= 1) {
                onValueChange(filtered)
            }
        },
        label = { Text(label) },
        modifier = modifier,
        suffix = suffix?.let { { Text(it) } },
        prefix = prefix?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        singleLine = true
    )
}