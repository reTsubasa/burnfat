package com.burnfat.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnfat.data.remote.AIProvider
import com.burnfat.data.remote.ApiKeyProvider
import com.burnfat.data.repository.DataClearRepository
import com.burnfat.data.repository.PlanRepository
import com.burnfat.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SettingsState(
    val age: String = "",
    val gender: String = "MALE",
    val selectedProvider: AIProvider = AIProvider.MIMO,
    val mimoApiKey: String = "",
    val aliyunApiKey: String = "",
    val hasMimoApiKey: Boolean = false,
    val hasAliyunApiKey: Boolean = false,
    val dataClearSuccess: Boolean = false,
    val activePlanName: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyProvider: ApiKeyProvider,
    private val userProfileRepository: UserProfileRepository,
    private val planRepository: PlanRepository,
    private val dataClearRepository: DataClearRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val profile = userProfileRepository.getProfile()
            val activePlan = planRepository.getActivePlan()
            val selectedProvider = apiKeyProvider.getSelectedProvider()

            _state.update {
                it.copy(
                    selectedProvider = selectedProvider,
                    hasMimoApiKey = apiKeyProvider.hasApiKey(AIProvider.MIMO),
                    hasAliyunApiKey = apiKeyProvider.hasApiKey(AIProvider.ALIYUN_QWEN),
                    mimoApiKey = if (apiKeyProvider.hasApiKey(AIProvider.MIMO)) "********" else "",
                    aliyunApiKey = if (apiKeyProvider.hasApiKey(AIProvider.ALIYUN_QWEN)) "********" else "",
                    age = profile?.age?.toString() ?: "",
                    gender = profile?.gender ?: "MALE",
                    activePlanName = activePlan?.name
                )
            }
        }
    }

    fun setAge(value: String) {
        _state.update { it.copy(age = value) }
        viewModelScope.launch {
            value.toIntOrNull()?.let { age ->
                userProfileRepository.updateAge(age)
            }
        }
    }

    fun setGender(value: String) {
        _state.update { it.copy(gender = value) }
        viewModelScope.launch {
            userProfileRepository.updateGender(value)
        }
    }

    fun setSelectedProvider(provider: AIProvider) {
        apiKeyProvider.setSelectedProvider(provider)
        _state.update { it.copy(selectedProvider = provider) }
    }

    fun setMimoApiKey(key: String) {
        _state.update { it.copy(mimoApiKey = key) }
        if (key.isNotBlank() && !key.contains("*")) {
            apiKeyProvider.setApiKey(AIProvider.MIMO, key)
            _state.update { it.copy(hasMimoApiKey = true) }
        }
    }

    fun setAliyunApiKey(key: String) {
        _state.update { it.copy(aliyunApiKey = key) }
        if (key.isNotBlank() && !key.contains("*")) {
            apiKeyProvider.setApiKey(AIProvider.ALIYUN_QWEN, key)
            _state.update { it.copy(hasAliyunApiKey = true) }
        }
    }

    fun exportData(context: Context): File? {
        return try {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "burnfat_export_${dateFormat.format(Date())}.json"
            val file = File(context.cacheDir, fileName)

            val content = """
                {
                    "exportTime": "${Date()}",
                    "version": "1.0.0"
                }
            """.trimIndent()

            file.writeText(content)

            Toast.makeText(context, "数据已导出: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            file
        } catch (e: Exception) {
            Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    fun clearAllData(context: Context) {
        viewModelScope.launch {
            try {
                dataClearRepository.clearAllData()
                _state.update { it.copy(dataClearSuccess = true) }
                Toast.makeText(context, "数据已清除", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "清除失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}