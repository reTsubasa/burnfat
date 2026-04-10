package com.burnfat.data.remote

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API Key 安全存储提供者
 * 使用 EncryptedSharedPreferences 加密存储 API Key
 */
@Singleton
class ApiKeyProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * 获取当前选择的AI提供商
     */
    fun getSelectedProvider(): AIProvider {
        val providerName = encryptedPrefs.getString(KEY_AI_PROVIDER, AIProvider.MIMO.name)
        return AIProvider.entries.find { it.name == providerName } ?: AIProvider.MIMO
    }

    /**
     * 设置AI提供商
     */
    fun setSelectedProvider(provider: AIProvider) {
        encryptedPrefs.edit().putString(KEY_AI_PROVIDER, provider.name).apply()
    }

    /**
     * 获取指定提供商的 API Key
     */
    fun getApiKey(provider: AIProvider): String {
        val key = when (provider) {
            AIProvider.MIMO -> encryptedPrefs.getString(KEY_MIMO_API_KEY, null)
            AIProvider.ALIYUN_QWEN -> encryptedPrefs.getString(KEY_ALIYUN_API_KEY, null)
        }
        return key ?: throw ApiException("${provider.displayName} API Key 未配置，请在设置中输入")
    }

    /**
     * 设置指定提供商的 API Key
     */
    fun setApiKey(provider: AIProvider, key: String) {
        val keyName = when (provider) {
            AIProvider.MIMO -> KEY_MIMO_API_KEY
            AIProvider.ALIYUN_QWEN -> KEY_ALIYUN_API_KEY
        }
        encryptedPrefs.edit().putString(keyName, key).apply()
    }

    /**
     * 检查指定提供商是否已配置 API Key
     */
    fun hasApiKey(provider: AIProvider): Boolean {
        val key = when (provider) {
            AIProvider.MIMO -> encryptedPrefs.getString(KEY_MIMO_API_KEY, null)
            AIProvider.ALIYUN_QWEN -> encryptedPrefs.getString(KEY_ALIYUN_API_KEY, null)
        }
        return !key.isNullOrBlank()
    }

    /**
     * 检查当前选择的提供商是否已配置
     */
    fun hasCurrentApiKey(): Boolean {
        return hasApiKey(getSelectedProvider())
    }

    /**
     * 清除指定提供商的 API Key
     */
    fun clearApiKey(provider: AIProvider) {
        val keyName = when (provider) {
            AIProvider.MIMO -> KEY_MIMO_API_KEY
            AIProvider.ALIYUN_QWEN -> KEY_ALIYUN_API_KEY
        }
        encryptedPrefs.edit().remove(keyName).apply()
    }

    /**
     * 获取当前提供商的 API Key
     */
    fun getCurrentApiKey(): String {
        return getApiKey(getSelectedProvider())
    }

    companion object {
        private const val PREFS_NAME = "burnfat_secure"
        private const val KEY_MIMO_API_KEY = "mimo_api_key"
        private const val KEY_ALIYUN_API_KEY = "aliyun_api_key"
        private const val KEY_AI_PROVIDER = "ai_provider"
    }
}

/**
 * API 异常
 */
class ApiException(message: String) : Exception(message)