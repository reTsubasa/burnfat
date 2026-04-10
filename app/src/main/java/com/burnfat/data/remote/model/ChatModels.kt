package com.burnfat.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 聊天完成请求 - OpenAI兼容格式
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<JsonObject>,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    @SerialName("max_completion_tokens")
    val maxCompletionTokens: Int? = null,
    val temperature: Float? = null,
    @SerialName("top_p")
    val topP: Float? = null,
    val stream: Boolean = false
)

/**
 * 构建消息的辅助函数
 */
object MessageBuilder {
    /**
     * 构建文本消息（content 为字符串）
     */
    fun textMessage(role: String, text: String): JsonObject = buildJsonObject {
        put("role", role)
        put("content", text)
    }

    /**
     * 构建带图片的消息（content 为数组）
     */
    fun imageMessage(role: String, imageBase64: String, text: String): JsonObject = buildJsonObject {
        put("role", role)
        put("content", buildJsonArray {
            // 图片部分
            add(buildJsonObject {
                put("type", "image_url")
                put("image_url", buildJsonObject {
                    put("url", "data:image/jpeg;base64,$imageBase64")
                })
            })
            // 文本部分
            add(buildJsonObject {
                put("type", "text")
                put("text", text)
            })
        })
    }
}

/**
 * 聊天完成响应
 */
@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val choices: List<Choice>,
    val usage: Usage? = null
)

/**
 * 选择项
 */
@Serializable
data class Choice(
    val message: ResponseMessage,
    @SerialName("finish_reason")
    val finishReason: String? = null,
    val index: Int? = null
)

/**
 * 响应消息
 */
@Serializable
data class ResponseMessage(
    val role: String,
    val content: String
)

/**
 * Token使用量
 */
@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int? = null,
    @SerialName("completion_tokens")
    val completionTokens: Int? = null,
    @SerialName("total_tokens")
    val totalTokens: Int? = null
)