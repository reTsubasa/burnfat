package com.burnfat.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.burnfat.data.remote.model.*
import com.burnfat.domain.model.MealType
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 食物分析服务
 * 支持多种AI模型进行食物图片识别
 */
@Singleton
class FoodAnalysisService @Inject constructor(
    private val aiApiService: UnifiedAIApiService,
    private val apiKeyProvider: ApiKeyProvider
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * 分析食物图片
     */
    suspend fun analyzeFoodPhoto(
        imagePath: String,
        mealType: MealType
    ): FoodAnalysisResult {
        // 1. 加载并压缩图片为Base64
        val base64Image = try {
            loadAndCompressToBase64(imagePath)
        } catch (e: FileNotFoundException) {
            throw FoodAnalysisException("图片文件不存在或已被删除")
        } catch (e: Exception) {
            val errorMsg = e.message?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
            throw FoodAnalysisException("图片加载失败: $errorMsg")
        }

        // 2. 获取当前提供商
        val provider = apiKeyProvider.getSelectedProvider()

        // 3. 构建提示词
        val systemPrompt = buildSystemPrompt(provider)
        val analysisPrompt = buildAnalysisPrompt(mealType)

        // 4. 构建消息列表
        val messages = listOf(
            MessageBuilder.textMessage("system", systemPrompt),
            MessageBuilder.imageMessage("user", base64Image, analysisPrompt)
        )

        // 5. 构建请求 - 根据供应商使用不同参数
        val request = ChatCompletionRequest(
            model = provider.visionModel,
            messages = messages,
            maxTokens = if (provider.supportsMaxCompletionTokens) null else provider.defaultMaxTokens,
            maxCompletionTokens = if (provider.supportsMaxCompletionTokens) provider.defaultMaxTokens else null,
            temperature = 0.3f,
            topP = 0.95f,
            stream = false
        )

        // 6. 调用API
        val response = try {
            aiApiService.chatCompletion(request)
        } catch (e: ApiException) {
            throw FoodAnalysisException(e.message ?: "AI服务调用失败")
        }

        // 7. 解析结果
        return parseResponse(response)
    }

    /**
     * 构建系统提示词
     */
    private fun buildSystemPrompt(provider: AIProvider): String {
        val currentDate = java.time.LocalDate.now()
        val weekDay = currentDate.dayOfWeek.let {
            when (it) {
                java.time.DayOfWeek.MONDAY -> "星期一"
                java.time.DayOfWeek.TUESDAY -> "星期二"
                java.time.DayOfWeek.WEDNESDAY -> "星期三"
                java.time.DayOfWeek.THURSDAY -> "星期四"
                java.time.DayOfWeek.FRIDAY -> "星期五"
                java.time.DayOfWeek.SATURDAY -> "星期六"
                java.time.DayOfWeek.SUNDAY -> "星期日"
            }
        }

        return when (provider) {
            AIProvider.MIMO -> "你是MiMo（中文名称也是MiMo），是小米公司研发的AI智能助手。\n今天的日期：${currentDate} ${weekDay}，你的知识截止日期是2024年12月。"
            AIProvider.ALIYUN_QWEN -> "你是通义千问，是阿里云研发的大规模语言模型。\n今天的日期：${currentDate} ${weekDay}。"
        }
    }

    /**
     * 构建食物分析提示词
     */
    private fun buildAnalysisPrompt(mealType: MealType): String {
        return """
你是一位专业的食物热量分析大师，擅长识别各种食物种类并精确分析热量。

这是一张${mealType.displayName}的照片。

【重要说明】
这是一顿餐食的照片，可能有多个不同的菜品和主食。
请仔细扫描整个餐盘/碗，识别图片中所有可见的食物和饮料。

【识别要求】
1. 全面识别：找出餐盘中所有食物，包括主食、荤菜、素菜、汤、饮料
2. 分别估算：每种食物单独列出名称、份量和热量
3. 份量估算：根据图片中的实际大小估算克数或毫升数
4. 热量计算：根据中国食物热量表估算每项的热量(千卡)

【常见份量参考】
- 米饭一碗约150-200g，热量约195-260kcal
- 馒头一个约100g，热量约220kcal
- 炒菜一份约150-200g
- 荤菜热量通常200-400kcal/份
- 素菜热量通常50-150kcal/份

请严格返回以下JSON格式(不要有任何额外文字):
{
    "foods": [
        {
            "name": "具体菜品名称",
            "portion": "份量描述",
            "estimatedGrams": 估算克数,
            "calories": 热量
        }
    ],
    "totalCalories": 总热量,
    "confidence": 置信度(0.0-1.0),
    "mealCategory": "${mealType.displayName}",
    "suggestions": "健康饮食建议"
}
""".trimIndent()
    }

    /**
     * 加载图片并压缩为Base64
     */
    private fun loadAndCompressToBase64(
        imagePath: String,
        targetWidth: Int = 512,
        targetHeight: Int = 512
    ): String {
        val file = File(imagePath)
        if (!file.exists()) {
            throw FileNotFoundException("图片文件不存在: $imagePath")
        }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(imagePath, options)

        var sampleSize = 1
        while (options.outWidth / sampleSize > targetWidth * 2 ||
               options.outHeight / sampleSize > targetHeight * 2) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val bitmap = BitmapFactory.decodeFile(imagePath, decodeOptions)
            ?: throw FoodAnalysisException("无法解码图片，请选择其他图片")

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)

        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * 解析API响应
     */
    private fun parseResponse(response: ChatCompletionResponse): FoodAnalysisResult {
        val content = response.choices.firstOrNull()?.message?.content
            ?: throw FoodAnalysisException("AI响应为空")

        return try {
            val jsonStr = extractJson(content)
            val analysisJson = json.decodeFromString<FoodAnalysisJson>(jsonStr)

            FoodAnalysisResult(
                foods = analysisJson.foods.map { item ->
                    FoodItem(
                        name = item.name,
                        portion = item.portion,
                        estimatedGrams = item.estimatedGrams,
                        calories = item.calories
                    )
                },
                totalCalories = analysisJson.totalCalories,
                confidence = analysisJson.confidence,
                suggestions = analysisJson.suggestions,
                mealCategory = analysisJson.mealCategory
            )
        } catch (e: Exception) {
            parseFallback(content)
        }
    }

    private fun extractJson(content: String): String {
        val jsonStart = content.indexOf("{")
        val jsonEnd = content.lastIndexOf("}") + 1
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return content.substring(jsonStart, jsonEnd)
        }
        throw FoodAnalysisException("响应中未找到JSON格式数据")
    }

    private fun parseFallback(content: String): FoodAnalysisResult {
        val caloriesPattern = Regex("(\\d+)\\s*(kcal|千卡|卡)")
        val caloriesMatch = caloriesPattern.find(content)
        val calories = caloriesMatch?.groupValues?.get(1)?.toInt() ?: 0

        return FoodAnalysisResult(
            foods = listOf(
                FoodItem(
                    name = "未知食物",
                    portion = null,
                    estimatedGrams = null,
                    calories = calories
                )
            ),
            totalCalories = calories,
            confidence = 0.3f,
            suggestions = content,
            mealCategory = null
        )
    }
}