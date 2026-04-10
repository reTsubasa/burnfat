package com.burnfat.data.remote

import com.burnfat.data.remote.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 减脂计划建议服务
 * 使用AI模型根据用户数据生成减脂建议
 */
@Singleton
class PlanRecommendationService @Inject constructor(
    private val aiApiService: UnifiedAIApiService,
    private val apiKeyProvider: ApiKeyProvider
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * 获取减脂计划建议
     */
    suspend fun getPlanRecommendation(
        bmr: Float,
        currentWeight: Float,
        targetWeight: Float,
        age: Int,
        gender: String,
        activityLevel: String
    ): PlanRecommendationResult {
        val systemPrompt = buildSystemPrompt()
        val userPrompt = buildUserPrompt(bmr, currentWeight, targetWeight, age, gender, activityLevel)
        val provider = apiKeyProvider.getSelectedProvider()

        val request = ChatCompletionRequest(
            model = provider.textModel,
            messages = listOf(
                MessageBuilder.textMessage("system", systemPrompt),
                MessageBuilder.textMessage("user", userPrompt)
            ),
            maxTokens = if (provider.supportsMaxCompletionTokens) null else provider.defaultMaxTokens,
            maxCompletionTokens = if (provider.supportsMaxCompletionTokens) provider.defaultMaxTokens else null,
            temperature = 0.7f,
            topP = 0.95f,
            stream = false
        )

        val response = aiApiService.chatCompletion(request)
        return parseResponse(response)
    }

    /**
     * 评估计划执行情况
     */
    suspend fun evaluatePlanProgress(
        planName: String,
        startWeight: Float,
        currentWeight: Float,
        targetWeight: Float,
        bmr: Float,
        daysPassed: Int,
        totalDays: Int,
        recentRecords: String = ""
    ): PlanEvaluationResult {
        val systemPrompt = buildEvaluationSystemPrompt()
        val userPrompt = buildEvaluationUserPrompt(
            planName, startWeight, currentWeight, targetWeight, bmr, daysPassed, totalDays, recentRecords
        )
        val provider = apiKeyProvider.getSelectedProvider()

        val request = ChatCompletionRequest(
            model = provider.textModel,
            messages = listOf(
                MessageBuilder.textMessage("system", systemPrompt),
                MessageBuilder.textMessage("user", userPrompt)
            ),
            maxTokens = if (provider.supportsMaxCompletionTokens) null else provider.defaultMaxTokens,
            maxCompletionTokens = if (provider.supportsMaxCompletionTokens) provider.defaultMaxTokens else null,
            temperature = 0.7f,
            topP = 0.95f,
            stream = false
        )

        val response = aiApiService.chatCompletion(request)
        return parseEvaluationResponse(response)
    }

    /**
     * 评估计划设计的合理性
     */
    suspend fun evaluatePlanDesign(
        startWeight: Float,
        currentWeight: Float,
        targetWeight: Float,
        bmr: Float,
        activityLevel: String,
        targetDate: Long
    ): PlanDesignEvaluationResult {
        val systemPrompt = buildDesignEvaluationSystemPrompt()
        val userPrompt = buildDesignEvaluationUserPrompt(
            startWeight, currentWeight, targetWeight, bmr, activityLevel, targetDate
        )
        val provider = apiKeyProvider.getSelectedProvider()

        val request = ChatCompletionRequest(
            model = provider.textModel,
            messages = listOf(
                MessageBuilder.textMessage("system", systemPrompt),
                MessageBuilder.textMessage("user", userPrompt)
            ),
            maxTokens = if (provider.supportsMaxCompletionTokens) null else provider.defaultMaxTokens,
            maxCompletionTokens = if (provider.supportsMaxCompletionTokens) provider.defaultMaxTokens else null,
            temperature = 0.7f,
            topP = 0.95f,
            stream = false
        )

        val response = aiApiService.chatCompletion(request)
        return parseDesignEvaluationResponse(response)
    }

    private fun buildEvaluationSystemPrompt(): String {
        return """你是一位专业的减脂教练，擅长分析减脂计划的执行情况并给出建议。

你需要返回以下JSON格式数据:
{
    "progressScore": 进度评分(0-100的整数，基于时间进度和体重变化),
    "status": "状态描述(例如：进展顺利、需要加油、稍有落后等)",
    "suggestions": "针对当前情况的具体改进建议，2-3条",
    "encouragement": "鼓励的话语，温暖积极"
}

评分标准:
- 体重下降符合预期：高分(70-100)
- 体重下降略慢：中等分(50-69)
- 体重未变或上升：低分(0-49)
- 同时考虑时间进度和目标完成度

注意:
1. 语言要温暖鼓励，不要太严厉
2. 建议要具体可行
3. 如果进展很好，要给予肯定"""
    }

    private fun buildEvaluationUserPrompt(
        planName: String,
        startWeight: Float,
        currentWeight: Float,
        targetWeight: Float,
        bmr: Float,
        daysPassed: Int,
        totalDays: Int,
        recentRecords: String
    ): String {
        val weightLost = startWeight - currentWeight
        val weightToLose = startWeight - targetWeight
        val progressPercent = if (weightToLose > 0) (weightLost / weightToLose * 100).toInt() else 0
        val timePercent = if (totalDays > 0) (daysPassed.toFloat() / totalDays * 100).toInt() else 0

        return """请评估我的减脂计划执行情况:

计划名称: $planName

体重数据:
- 起始体重: ${startWeight}kg
- 当前体重: ${currentWeight}kg
- 目标体重: ${targetWeight}kg
- 已减重: ${String.format("%.1f", weightLost)}kg
- 目标减重: ${String.format("%.1f", weightToLose)}kg

进度数据:
- 已过天数: ${daysPassed}天
- 计划总天数: ${totalDays}天
- 时间进度: ${timePercent}%
- 减重进度: ${progressPercent}%

基础代谢率: ${bmr.toInt()}kcal

$recentRecords

请给出评估和建议。"""
    }

    private fun parseEvaluationResponse(response: ChatCompletionResponse): PlanEvaluationResult {
        val content = response.choices.firstOrNull()?.message?.content
            ?: throw PlanRecommendationException("AI响应为空")

        return try {
            val jsonStr = extractJson(content)
            val result = json.decodeFromString<PlanEvaluationJson>(jsonStr)

            PlanEvaluationResult(
                progressScore = result.progressScore,
                status = result.status,
                suggestions = result.suggestions,
                encouragement = result.encouragement
            )
        } catch (e: Exception) {
            PlanEvaluationResult(
                progressScore = 50,
                status = "继续加油",
                suggestions = content,
                encouragement = "每一天的努力都有意义！"
            )
        }
    }

    private fun buildSystemPrompt(): String {
        return """你是专业的营养师和健身教练。你的任务是根据用户提供的身体数据，给出科学的减脂计划建议。

你需要返回以下JSON格式数据:
{
    "recommendedWeeks": 推荐的减脂周期(周数，整数),
    "dailyCalorieDeficit": 推荐的每日热量缺口(kcal，整数，一般300-500),
    "suggestions": "针对用户的个性化建议，包括饮食和运动方面的具体建议"
}

注意事项:
1. 健康减脂速度建议每周0.5-1kg
2. 每日热量缺口不应超过500kcal
3. 考虑用户的BMR和活动水平计算TDEE
4. 如果目标不合理，给出调整建议"""
    }

    private fun buildUserPrompt(
        bmr: Float,
        currentWeight: Float,
        targetWeight: Float,
        age: Int,
        gender: String,
        activityLevel: String
    ): String {
        val activityDesc = when (activityLevel) {
            "SEDENTARY" -> "久坐不动"
            "LIGHT" -> "轻度活动"
            "MODERATE" -> "中度活动"
            "ACTIVE" -> "高度活动"
            "VERY_ACTIVE" -> "非常高活动量"
            else -> activityLevel
        }

        val genderDesc = if (gender == "MALE") "男" else "女"

        return """请根据以下数据为我制定减脂计划:

基础信息:
- 性别: $genderDesc
- 年龄: ${age}岁
- 当前体重: ${currentWeight}kg
- 目标体重: ${targetWeight}kg
- 需要减重: ${currentWeight - targetWeight}kg

代谢数据:
- 基础代谢率(BMR): ${bmr.toInt()}kcal
- 活动水平: $activityDesc

请给出推荐的建议周期、每日热量缺口，以及个性化的饮食和运动建议。"""
    }

    private fun parseResponse(response: ChatCompletionResponse): PlanRecommendationResult {
        val content = response.choices.firstOrNull()?.message?.content
            ?: throw PlanRecommendationException("AI响应为空")

        return try {
            val jsonStr = extractJson(content)
            val result = json.decodeFromString<PlanRecommendationJson>(jsonStr)

            PlanRecommendationResult(
                recommendedWeeks = result.recommendedWeeks,
                dailyCalorieDeficit = result.dailyCalorieDeficit,
                suggestions = result.suggestions
            )
        } catch (e: Exception) {
            PlanRecommendationResult(
                recommendedWeeks = 12,
                dailyCalorieDeficit = 400,
                suggestions = content
            )
        }
    }

    private fun extractJson(content: String): String {
        val jsonStart = content.indexOf("{")
        val jsonEnd = content.lastIndexOf("}") + 1
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return content.substring(jsonStart, jsonEnd)
        }
        throw PlanRecommendationException("响应中未找到JSON格式数据")
    }

    private fun buildDesignEvaluationSystemPrompt(): String {
        return """你是一位专业的减脂教练和营养师，擅长评估减脂计划的合理性。

你需要返回以下JSON格式数据:
{
    "feasibilityScore": 可行性评分(0-100的整数),
    "healthScore": 健康安全评分(0-100的整数),
    "overallScore": 综合评分(0-100的整数),
    "status": "评估结论(例如：计划合理、需要调整、目标过高等)",
    "issues": ["存在的问题1", "存在的问题2"],
    "suggestions": "针对性的改进建议",
    "encouragement": "鼓励的话"
}

评估标准:
1. 可行性: 目标体重是否合理、时间是否充足、减重速度是否健康
2. 健康安全: 热量缺口是否过大、基础代谢是否足够、活动水平是否匹配
3. 综合: 整体计划的科学性和可执行性

健康减脂标准:
- 每周减重0.5-1kg为健康范围
- 每日热量缺口建议300-500kcal，不超过700kcal
- 目标体重不应低于健康BMI范围(18.5-24)

注意:
1. 语言要专业但温暖
2. 建议要具体可行
3. 如果计划合理，要给予肯定"""
    }

    private fun buildDesignEvaluationUserPrompt(
        startWeight: Float,
        currentWeight: Float,
        targetWeight: Float,
        bmr: Float,
        activityLevel: String,
        targetDate: Long
    ): String {
        val activityDesc = when (activityLevel) {
            "SEDENTARY" -> "久坐不动(活动系数1.2)"
            "LIGHT" -> "轻度活动(活动系数1.375)"
            "MODERATE" -> "中度活动(活动系数1.55)"
            "ACTIVE" -> "高度活动(活动系数1.725)"
            "VERY_ACTIVE" -> "非常高活动量(活动系数1.9)"
            else -> activityLevel
        }

        val weightToLose = currentWeight - targetWeight
        val daysToTarget = ((targetDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
        val weeksToTarget = daysToTarget / 7
        val weeklyLoss = if (weeksToTarget > 0) weightToLose / weeksToTarget else 0f

        val activityMultiplier = when (activityLevel) {
            "SEDENTARY" -> 1.2f
            "LIGHT" -> 1.375f
            "MODERATE" -> 1.55f
            "ACTIVE" -> 1.725f
            "VERY_ACTIVE" -> 1.9f
            else -> 1.55f
        }
        val tdee = bmr * activityMultiplier

        return """请评估我的减脂计划设计:

体重目标:
- 起始体重: ${startWeight}kg
- 当前体重: ${currentWeight}kg
- 目标体重: ${targetWeight}kg
- 需要减重: ${String.format("%.1f", weightToLose)}kg

时间安排:
- 目标日期: ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(targetDate))}
- 剩余天数: ${daysToTarget}天 (约${weeksToTarget}周)
- 预计每周减重: ${String.format("%.2f", weeklyLoss)}kg

代谢数据:
- 基础代谢率(BMR): ${bmr.toInt()}kcal
- 活动水平: $activityDesc
- 估算TDEE: ${tdee.toInt()}kcal

请评估这个计划是否科学合理，并给出建议。"""
    }

    private fun parseDesignEvaluationResponse(response: ChatCompletionResponse): PlanDesignEvaluationResult {
        val content = response.choices.firstOrNull()?.message?.content
            ?: throw PlanRecommendationException("AI响应为空")

        return try {
            val jsonStr = extractJson(content)
            val result = json.decodeFromString<PlanDesignEvaluationJson>(jsonStr)

            PlanDesignEvaluationResult(
                feasibilityScore = result.feasibilityScore,
                healthScore = result.healthScore,
                overallScore = result.overallScore,
                status = result.status,
                issues = result.issues,
                suggestions = result.suggestions,
                encouragement = result.encouragement
            )
        } catch (e: Exception) {
            PlanDesignEvaluationResult(
                feasibilityScore = 70,
                healthScore = 70,
                overallScore = 70,
                status = "继续完善",
                issues = listOf("无法解析详细评估"),
                suggestions = content,
                encouragement = "加油！"
            )
        }
    }
}

@Serializable
data class PlanRecommendationJson(
    @SerialName("recommendedWeeks")
    val recommendedWeeks: Int,
    @SerialName("dailyCalorieDeficit")
    val dailyCalorieDeficit: Int,
    val suggestions: String
)

@Serializable
data class PlanEvaluationJson(
    @SerialName("progressScore")
    val progressScore: Int,
    val status: String,
    val suggestions: String,
    val encouragement: String
)

data class PlanRecommendationResult(
    val recommendedWeeks: Int,
    val dailyCalorieDeficit: Int,
    val suggestions: String
)

data class PlanEvaluationResult(
    val progressScore: Int,
    val status: String,
    val suggestions: String,
    val encouragement: String
)

@Serializable
data class PlanDesignEvaluationJson(
    @SerialName("feasibilityScore")
    val feasibilityScore: Int,
    @SerialName("healthScore")
    val healthScore: Int,
    @SerialName("overallScore")
    val overallScore: Int,
    val status: String,
    val issues: List<String>,
    val suggestions: String,
    val encouragement: String
)

data class PlanDesignEvaluationResult(
    val feasibilityScore: Int,
    val healthScore: Int,
    val overallScore: Int,
    val status: String,
    val issues: List<String>,
    val suggestions: String,
    val encouragement: String
)

class PlanRecommendationException(message: String) : Exception(message)