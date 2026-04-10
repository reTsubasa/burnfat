package com.burnfat.data.remote

import com.burnfat.data.remote.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 统一的AI API服务
 * 支持MiMo和阿里云Qwen等多种提供商
 * 根据不同供应商使用不同的认证方式和参数
 */
@Singleton
class UnifiedAIApiService @Inject constructor(
    private val apiKeyProvider: ApiKeyProvider
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * 发送聊天补全请求
     */
    suspend fun chatCompletion(request: ChatCompletionRequest): ChatCompletionResponse = withContext(Dispatchers.IO) {
        val provider = apiKeyProvider.getSelectedProvider()
        val apiKey = apiKeyProvider.getApiKey(provider)

        val requestJson = json.encodeToString(ChatCompletionRequest.serializer(), request)
        val requestBody = requestJson.toRequestBody("application/json".toMediaType())

        val authValue = "${provider.authHeaderPrefix}$apiKey"

        val httpRequest = Request.Builder()
            .url("${provider.baseUrl}/chat/completions")
            .addHeader(provider.authHeaderName, authValue)
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = try {
            client.newCall(httpRequest).execute()
        } catch (e: UnknownHostException) {
            throw ApiException("网络连接失败，请检查网络设置")
        } catch (e: SocketTimeoutException) {
            throw ApiException("请求超时，请稍后重试")
        } catch (e: Exception) {
            val errorMsg = e.message?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
            throw ApiException("网络请求失败: $errorMsg")
        }

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            val friendlyError = when (response.code) {
                401 -> "API Key 无效或已过期，请检查设置"
                403 -> "API 访问被拒绝，请检查 API Key 权限"
                404 -> "API 地址错误或模型不存在"
                429 -> "请求过于频繁，请稍后重试"
                500, 502, 503 -> "AI 服务暂时不可用，请稍后重试"
                else -> "${provider.displayName} API 错误 (${response.code})"
            }
            throw ApiException("$friendlyError\n\n详细信息: $errorBody")
        }

        val responseBody = response.body?.string()
        if (responseBody.isNullOrBlank()) {
            throw ApiException("响应体为空")
        }

        try {
            json.decodeFromString<ChatCompletionResponse>(responseBody)
        } catch (e: Exception) {
            val errorMsg = e.message?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
            throw ApiException("AI 响应格式错误: $errorMsg")
        }
    }

    fun getVisionModel(): String {
        return apiKeyProvider.getSelectedProvider().visionModel
    }

    fun getTextModel(): String {
        return apiKeyProvider.getSelectedProvider().textModel
    }
}