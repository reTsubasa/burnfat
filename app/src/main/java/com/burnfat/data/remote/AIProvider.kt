package com.burnfat.data.remote

/**
 * AI模型提供商配置
 * 定义各供应商的API差异
 */
enum class AIProvider(
    val displayName: String,
    val baseUrl: String,
    val visionModel: String,
    val textModel: String,
    val apiKeyUrl: String,
    val description: String,
    val authHeaderName: String,        // 认证 header 名称
    val authHeaderPrefix: String,      // 认证 header 前缀 (如 "Bearer ")
    val supportsMaxCompletionTokens: Boolean,  // 是否支持 max_completion_tokens
    val defaultMaxTokens: Int          // 默认最大 tokens
) {
    MIMO(
        displayName = "小米 MiMo",
        baseUrl = "https://api.xiaomimimo.com/v1",
        visionModel = "mimo-v2-omni",
        textModel = "mimo-v2-omni",
        apiKeyUrl = "platform.xiaomimimo.com",
        description = "小米自研AI模型，支持视觉识别",
        authHeaderName = "api-key",
        authHeaderPrefix = "",
        supportsMaxCompletionTokens = true,
        defaultMaxTokens = 1024
    ),
    ALIYUN_QWEN(
        displayName = "阿里云 Qwen",
        baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1",
        visionModel = "qwen-vl-plus",
        textModel = "qwen-plus",
        apiKeyUrl = "dashscope.console.aliyun.com",
        description = "阿里通义千问，支持多模态",
        authHeaderName = "Authorization",
        authHeaderPrefix = "Bearer ",
        supportsMaxCompletionTokens = false,
        defaultMaxTokens = 2048
    )
}