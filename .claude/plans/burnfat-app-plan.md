# 减肥热量管理 Android App 实施计划

## 项目概述

**应用名称**: 燃脂君 (BurnFat)
**核心定位**: 单机版热量管理 App，所有数据本地存储，支持完整历史查询
**AI服务**: 小米 MiMo-V2-Omni (api.xiaomimimo.com)
**特色功能**: 体重曲线、达标墙、餐次分类、定期提醒更新BMR

---

## 核心信息要素定义

### 用户档案
| 字段 | 类型 | 说明 | 更新频率 |
|------|------|------|----------|
| BMR | Float | 基础代谢率 | 每2周提醒更新 |
| 目标体重 | Float | 目标 | 固定 |
| 当前体重 | Float | 定期更新 | 每周提醒记录 |
| 目标日期 | Date | 预计达成 | 固定 |
| 活动等级 | Enum | 日常活动量 | 可调整 |

### 每日记录
| 字段 | 类型 | 说明 |
|------|------|------|
| 日期 | Date | YYYY-MM-DD |
| 摄入热量 | Int | 各餐次总和 |
| 运动消耗 | Int | 运动记录总和 |
| 目标热量 | Int | 当日建议摄入 |
| 是否达标 | Boolean | 热量缺口达成 |
| 早餐热量 | Int | 早餐摄入 |
| 午餐热量 | Int | 午餐摄入 |
| 晚餐热量 | Int | 晚餐摄入 |
| 加餐热量 | Int | 加餐/零食摄入 |
| 当日体重 | Float? | 可选记录 |

### 食物记录 (带餐次)
| 字段 | 类型 | 说明 |
|------|------|------|
| 餐次 | Enum | BREAKFAST/LUNCH/DINNER/SNACK |
| 图片路径 | String? | 本地存储 |
| 食物名称 | String | AI识别/手动 |
| 热量 | Int | kcal |
| AI置信度 | Float? | 识别置信度 |
| 时间 | DateTime | 创建时间 |

### 运动记录
| 字段 | 类型 | 说明 |
|------|------|------|
| 运动类型 | String | 如: 跑步、游泳 |
| 持续时间 | Int | 分钟 |
| 消耗热量 | Int | 手动输入 |
| 时间 | DateTime | 创建时间 |

### 达标墙记录
| 字段 | 类型 | 说明 |
|------|------|------|
| 日期 | Date | 达标日期 |
| 达标类型 | Enum | PERFECT/GOOD/ACCEPTABLE |
| 连续天数 | Int | 连续达标次数 |
| 获得徽章 | String? | 成就徽章 |

---

## Phase 0: 小米 MiMo API 确认信息

**API 文档来源**: platform.xiaomimimo.com

### API 端点
| 格式 | Base URL |
|------|----------|
| OpenAI兼容 | `https://api.xiaomimimo.com/v1` |
| Anthropic兼容 | `https://api.xiaomimimo.com/anthropic` |

### 认证方式
```
Header: api-key: YOUR_API_KEY
```
**注意**: 不是 `Authorization: Bearer`，而是直接使用 `api-key` header

### 模型名称
- `mimo-v2-pro` (纯文本模型)
- `mimo-v2-omni` (Vision多模态模型 - **用于图片识别**)

### 系统提示词 (必须使用)
**中文版本**:
```
你是MiMo（中文名称也是MiMo），是小米公司研发的AI智能助手。
今天的日期：{date} {week}，你的知识截止日期是2024年12月。
```

**英文版本**:
```
You are MiMo, an AI assistant developed by Xiaomi.
Today's date: {date} {week}. Your knowledge cutoff date is December 2024.
```

### Vision API 图片输入格式 (已确认)

**支持的图片格式**: JPEG, PNG, GIF, WebP, BMP
**大小限制**: 单张图片 Base64 不超过 10MB

#### Base64 编码传入 (推荐用于App)
```json
{
    "type": "image_url",
    "image_url": {
        "url": "data:image/jpeg;base64,{base64_data}"
    }
}
```
**注意**: Base64 字符串前需携带前缀 `data:{MIME_TYPE};base64,`

#### URL 方式传入
```json
{
    "type": "image_url",
    "image_url": {
        "url": "https://example.com/image.jpg"
    }
}
```

#### 多图输入支持
可同时传入多张图片，模型能解析并返回贴合语义的回复

#### 图片 Token 计算 (成本控制参考)
图片 Token 用量受分辨率影响，估算公式:
```
num_tokens = (grid_t * grid_h * grid_w) // (SPATIAL_MERGE_SIZE^2)
```
其中:
- `PATCH_SIZE = 16`, `SPATIAL_MERGE_SIZE = 2`
- `IMAGE_MIN_PIXELS = 8192`, `IMAGE_MAX_PIXELS = 8388608`

**建议**: 将图片压缩至 512x512 左右可有效控制 Token 消耗

---

## Phase 1: 项目架构搭建

### 1.1 项目结构

```
app/
├── src/main/java/com/burnfat/
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── DashboardScreen.kt           # 首页概览
│   │   │   ├── FoodLogScreen.kt             # 食物记录(带餐次)
│   │   │   ├── ExerciseLogScreen.kt         # 运动记录
│   │   │   ├── HistoryScreen.kt             # 历史查询
│   │   │   ├── WeightCurveScreen.kt         # 体重曲线图表
│   │   │   ├── AchievementWallScreen.kt     # 达标墙/成就墙
│   │   │   ├── SettingsScreen.kt            # 用户设置
│   │   │   ├── PhotoCaptureScreen.kt        # AI拍照识别
│   │   │   ├── BmrUpdateDialog.kt           # BMR更新提醒弹窗
│   │   │   ├── WeightRecordDialog.kt        # 体重记录弹窗
│   │   │   └── OnboardingScreen.kt          # 新用户引导
│   │   ├── components/
│   │   │   ├── MealTypeSelector.kt          # 餐次选择器
│   │   │   ├── WeightCurveChart.kt          # 体重曲线图
│   │   │   ├── AchievementBadge.kt          # 成就徽章组件
│   │   │   ├── CalorieCard.kt
│   │   │   ├── ProgressChart.kt
│   │   │   └── StreakCounter.kt             # 连续达标计数
│   │   └── theme/
│   │
│   ├── data/
│   │   ├── local/
│   │   │   ├── entity/
│   │   │   │   ├── UserProfileEntity.kt
│   │   │   │   ├── DailyRecordEntity.kt     # 新增餐次字段
│   │   │   │   ├── FoodEntryEntity.kt       # 新增mealType
│   │   │   │   ├── ExerciseEntryEntity.kt
│   │   │   │   ├── WeightHistoryEntity.kt   # 体重历史
│   │   │   │   ├── AchievementEntity.kt     # 达标墙记录
│   │   │   │   └── BmrHistoryEntity.kt      # BMR变更历史
│   │   │   ├── dao/
│   │   │   └── database/
│   │   ├── remote/
│   │   │   ├── MiMoApiService.kt            # 小米MiMo API
│   │   │   └── FoodAnalysisService.kt
│   │   └── repository/
│   │
│   ├── domain/
│   │   ├── model/
│   │   │   ├── MealType.kt                  # 餐次枚举
│   │   │   ├── AchievementType.kt          # 成就类型
│   │   │   └── ReminderRule.kt              # 提醒规则
│   │   ├── usecase/
│   │   │   ├── GetWeightCurveUseCase.kt     # 体重曲线数据
│   │   │   ├── GetAchievementsUseCase.kt    # 达标墙数据
│   │   │   ├── CheckReminderUseCase.kt      # 检查提醒
│   │   │   ├── CalculateNewBmrUseCase.kt    # 基于新体重计算BMR
│   │   │   └── ...
│   │   ├── calculator/
│   │   └── reminder/
│   │       └── ReminderManager.kt           # 提醒管理器
│   │
│   ├── di/
│   ├── util/
│   └── MainActivity.kt
```

---

## Phase 2: 数据模型设计

### 2.1 餐次枚举

```kotlin
// domain/model/MealType.kt
enum class MealType(
    val displayName: String,
    val icon: String,
    val typicalTimeRange: String,
    val defaultCalorieRatio: Float  // 占全天热量比例参考
) {
    BREAKFAST(
        displayName = "早餐",
        icon = "🌅",
        typicalTimeRange = "6:00-10:00",
        defaultCalorieRatio = 0.25f   // 25%
    ),
    LUNCH(
        displayName = "午餐",
        icon = "☀️",
        typicalTimeRange = "11:00-14:00",
        defaultCalorieRatio = 0.35f   // 35%
    ),
    DINNER(
        displayName = "晚餐",
        icon = "🌙",
        typicalTimeRange = "17:00-20:00",
        defaultCalorieRatio = 0.30f   // 30%
    ),
    SNACK(
        displayName = "加餐",
        icon = "🍎",
        typicalTimeRange = "任意时间",
        defaultCalorieRatio = 0.10f   // 10%
    );

    companion object {
        fun fromHour(hour: Int): MealType {
            return when (hour) {
                in 6..10 -> BREAKFAST
                in 11..14 -> LUNCH
                in 17..20 -> DINNER
                else -> SNACK
            }
        }
    }
}
```

### 2.2 Room 数据实体

```kotlin
// ============ 用户档案 ============
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 0,
    val bmr: Float,
    val bmrUpdatedAt: Long,
    val targetWeight: Float,
    val currentWeight: Float,
    val weightUpdatedAt: Long,
    val targetDate: Long,
    val activityLevel: String,
    val createdAt: Long,
    val updatedAt: Long
)

// ============ 每日记录 ============
@Entity(tableName = "daily_record")
data class DailyRecordEntity(
    @PrimaryKey val date: Long,
    val intakeCalories: Int,
    val breakfastCalories: Int,
    val lunchCalories: Int,
    val dinnerCalories: Int,
    val snackCalories: Int,
    val exerciseCalories: Int,
    val targetCalories: Int,
    val achieved: Boolean,
    val achievementType: String?,
    val weightRecord: Float?,
    val foodEntryCount: Int,
    val exerciseEntryCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)

// ============ 食物记录 ============
@Entity(tableName = "food_entry")
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordDate: Long,
    val mealType: String,
    val photoPath: String?,
    val foodName: String,
    val portion: String?,
    val calories: Int,
    val sourceType: String,
    val aiConfidence: Float?,
    val aiSuggestions: String?,
    val createdAt: Long
)

// ============ 运动记录 ============
@Entity(tableName = "exercise_entry")
data class ExerciseEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordDate: Long,
    val exerciseType: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val notes: String?,
    val createdAt: Long
)

// ============ 体重历史 ============
@Entity(tableName = "weight_history")
data class WeightHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val weight: Float,
    val changeFromPrevious: Float?,
    val note: String?,
    val createdAt: Long
)

// ============ BMR历史 ============
@Entity(tableName = "bmr_history")
data class BmrHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val previousBmr: Float,
    val newBmr: Float,
    val reason: String,
    val relatedWeight: Float?,
    val createdAt: Long
)

// ============ 达标墙记录 ============
@Entity(tableName = "achievement")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val type: String,
    val streakDays: Int,
    val badgeEarned: String?,
    val caloriesDeficit: Int,
    val createdAt: Long
)
```

---

## Phase 3: 小米 MiMo API 服务实现

### 3.1 API 服务接口

```kotlin
// data/remote/MiMoApiService.kt
/**
 * 小米 MiMo API 服务 (OpenAI兼容格式)
 * Base URL: https://api.xiaomimimo.com/v1
 * 认证: Header "api-key: YOUR_API_KEY"
 */
interface MiMoApiService {

    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("api-key") apiKey: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

// ============ 请求/响应模型 ============
data class ChatCompletionRequest(
    val model: String = "mimo-v2-omni",  // Vision多模态模型
    val messages: List<Message>,
    val max_completion_tokens: Int = 1024,
    val temperature: Float = 0.3f,
    val top_p: Float = 0.95f,
    val stream: Boolean = false
)

data class Message(
    val role: String,  // system, user, assistant
    val content: List<MessageContent>
)

data class MessageContent(
    val type: String,        // "text" 或 "image_url"
    val text: String? = null,
    val image_url: ImageUrl? = null
)

data class ImageUrl(
    val url: String          // 图片URL 或 "data:image/jpeg;base64,..."
)

data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage?
)

data class Choice(
    val message: ResponseMessage,
    val finish_reason: String
)

data class ResponseMessage(
    val role: String,
    val content: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)
```

### 3.2 食物分析服务

```kotlin
// data/remote/FoodAnalysisService.kt
class FoodAnalysisService @Inject constructor(
    private val mimoApiService: MiMoApiService,
    private val imageProcessor: ImageProcessor,
    private val apiKeyProvider: ApiKeyProvider,
    private val dateProvider: DateProvider
) {

    companion object {
        const val BASE_URL = "https://api.xiaomimimo.com/v1"
        const val MODEL_NAME = "mimo-v2-omni"  // Vision多模态模型
    }

    /**
     * 分析食物图片
     */
    suspend fun analyzeFoodPhoto(
        imagePath: String,
        mealType: MealType
    ): FoodAnalysisResult {
        // 1. 加载并压缩图片
        val base64Image = imageProcessor.loadAndCompressToBase64(
            imagePath,
            targetWidth = 512,
            targetHeight = 512
        )

        // 2. 构建系统提示词 (必须使用官方格式)
        val systemPrompt = buildSystemPrompt()

        // 3. 构建用户消息
        val userContent = listOf(
            MessageContent(
                type = "image_url",
                image_url = ImageUrl(url = "data:image/jpeg;base64,$base64Image")
            ),
            MessageContent(
                type = "text",
                text = buildAnalysisPrompt(mealType)
            )
        )

        // 4. 构建请求
        val request = ChatCompletionRequest(
            model = MODEL_NAME,
            messages = listOf(
                Message(role = "system", content = listOf(
                    MessageContent(type = "text", text = systemPrompt)
                )),
                Message(role = "user", content = userContent)
            ),
            max_completion_tokens = 1024,
            temperature = 0.3f,  // 降低随机性提高准确性
            top_p = 0.95f,
            stream = false
        )

        // 5. 调用API
        val response = mimoApiService.chatCompletion(
            apiKey = apiKeyProvider.getApiKey(),
            request = request
        )

        // 6. 解析结果
        return parseResponse(response)
    }

    /**
     * 构建系统提示词 (官方指定格式)
     */
    private fun buildSystemPrompt(): String {
        val currentDate = dateProvider.getCurrentDate()
        val weekDay = dateProvider.getWeekDay()

        return "你是MiMo（中文名称也是MiMo），是小米公司研发的AI智能助手。\n" +
               "今天的日期：${currentDate} ${weekDay}，你的知识截止日期是2024年12月。"
    }

    /**
     * 构建食物分析提示词
     */
    private fun buildAnalysisPrompt(mealType: MealType): String {
        return """
        这是一张${mealType.displayName}的照片。请分析图片中的食物并估算热量。

        要求:
        1. 识别所有食物及饮料
        2. 估算份量(克/毫升/份)
        3. 估算热量(千卡)，参考常见中国菜肴标准
        4. 考虑${mealType.displayName}的典型热量范围

        请严格按以下JSON格式返回，不要添加其他内容:
        {
            "foods": [
                {
                    "name": "食物名称",
                    "portion": "份量描述",
                    "estimatedGrams": 估算克数,
                    "calories": 热量
                }
            ],
            "totalCalories": 总热量,
            "confidence": 置信度0到1之间的小数,
            "mealCategory": "${mealType.displayName}",
            "suggestions": "健康建议"
        }

        注意:
        - 使用中文回答
        - 热量单位为千卡(kcal)
        - 无法识别的食物标注"未知"并估算相近食物热量
        """.trimIndent()
    }

    private fun parseResponse(response: ChatCompletionResponse): FoodAnalysisResult {
        val content = response.choices.firstOrNull()?.message?.content
            ?: throw FoodAnalysisException("AI响应为空")

        return try {
            val jsonStr = extractJson(content)
            val json = Json.decodeFromString<FoodAnalysisJson>(jsonStr)
            FoodAnalysisResult(
                foods = json.foods.map { FoodItem(it.name, it.portion, it.estimatedGrams, it.calories) },
                totalCalories = json.totalCalories,
                confidence = json.confidence,
                suggestions = json.suggestions,
                mealCategory = json.mealCategory
            )
        } catch (e: Exception) {
            parseFallback(content)
        }
    }

    /**
     * 从响应中提取JSON (处理可能的额外文本)
     */
    private fun extractJson(content: String): String {
        val jsonStart = content.indexOf("{")
        val jsonEnd = content.lastIndexOf("}") + 1
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return content.substring(jsonStart, jsonEnd)
        }
        throw FoodAnalysisException("响应中未找到JSON格式数据")
    }

    private fun parseFallback(content: String): FoodAnalysisResult {
        // 简单文本解析作为后备方案
        // 提取热量数字等关键信息
        val caloriesPattern = Regex("(\\d+)\\s*(kcal|千卡|卡)")
        val caloriesMatch = caloriesPattern.find(content)

        return FoodAnalysisResult(
            foods = listOf(FoodItem("未知食物", null, null, caloriesMatch?.groupValues?.get(1)?.toInt() ?: 0)),
            totalCalories = caloriesMatch?.groupValues?.get(1)?.toInt() ?: 0,
            confidence = 0.3f,
            suggestions = content,
            mealCategory = null
        )
    }
}

data class FoodAnalysisResult(
    val foods: List<FoodItem>,
    val totalCalories: Int,
    val confidence: Float,
    val suggestions: String?,
    val mealCategory: String?
)

data class FoodItem(
    val name: String,
    val portion: String?,
    val estimatedGrams: Int?,
    val calories: Int
)

data class FoodAnalysisJson(
    val foods: List<FoodItemJson>,
    val totalCalories: Int,
    val confidence: Float,
    val suggestions: String?,
    val mealCategory: String?
)

data class FoodItemJson(
    val name: String,
    val portion: String?,
    val estimatedGrams: Int?,
    val calories: Int
)

class FoodAnalysisException(message: String) : Exception(message)
```

### 3.3 Retrofit 配置

```kotlin
// di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.xiaomimimo.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideMiMoApiService(retrofit: Retrofit): MiMoApiService {
        return retrofit.create(MiMoApiService::class.java)
    }
}
```

### 3.4 API Key 安全存储

```kotlin
// util/ApiKeyProvider.kt
class ApiKeyProvider @Inject constructor(
    private val context: Context
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        "burnfat_secure",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getApiKey(): String {
        return encryptedPrefs.getString("mimo_api_key", "")
            ?: throw ApiException("API Key未配置，请在设置中输入")
    }

    fun setApiKey(key: String) {
        encryptedPrefs.edit().putString("mimo_api_key", key).apply()
    }

    fun hasApiKey(): Boolean = encryptedPrefs.contains("mimo_api_key")

    fun clearApiKey() {
        encryptedPrefs.edit().remove("mimo_api_key").apply()
    }
}

class ApiException(message: String) : Exception(message)
```

---

## Phase 4: 定期提醒系统

### 4.1 提醒规则

```kotlin
// domain/reminder/ReminderManager.kt
class ReminderManager @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {

    companion object {
        const val BMR_UPDATE_INTERVAL_DAYS = 14      // BMR: 2周
        const val WEIGHT_RECORD_INTERVAL_DAYS = 7    // 体重: 1周
        const val GOAL_CHECK_INTERVAL_DAYS = 30      // 目标检查: 1月
    }

    /**
     * 检查是否需要触发提醒
     */
    suspend fun checkReminders(): List<ReminderAlert> {
        val profile = userProfileRepository.getProfile()
        val alerts = mutableListOf<ReminderAlert>()
        val now = System.currentTimeMillis()

        // BMR更新提醒
        profile?.let { p ->
            val bmrDaysSince = (now - p.bmrUpdatedAt) / (24 * 60 * 60 * 1000)
            if (bmrDaysSince >= BMR_UPDATE_INTERVAL_DAYS) {
                alerts.add(ReminderAlert(
                    type = ReminderType.BMR_UPDATE,
                    message = "您的基础代谢率数据已超过${BMR_UPDATE_INTERVAL_DAYS}天未更新。\n" +
                              "减肥过程中BMR会随体重下降而降低，建议重新测量或根据新体重估算。",
                    daysSinceLastUpdate = bmrDaysSince.toInt(),
                    suggestedAction = "建议前往专业机构测量，或使用公式估算:\n新BMR ≈ 旧BMR × (新体重/旧体重)"
                ))
            }

            // 体重记录提醒
            val weightDaysSince = (now - p.weightUpdatedAt) / (24 * 60 * 60 * 1000)
            if (weightDaysSince >= WEIGHT_RECORD_INTERVAL_DAYS) {
                alerts.add(ReminderAlert(
                    type = ReminderType.WEIGHT_RECORD,
                    message = "已${weightDaysSince.toInt()}天未记录体重。\n" +
                              "定期记录体重有助于追踪减脂进度。",
                    daysSinceLastUpdate = weightDaysSince.toInt(),
                    suggestedAction = "建议每周固定时间测量(如周一早晨空腹)"
                ))
            }
        }

        return alerts
    }

    /**
     * 根据体重变化计算新BMR
     */
    fun calculateNewBmr(
        oldBmr: Float,
        oldWeight: Float,
        newWeight: Float
    ): Float {
        return oldBmr * (newWeight / oldWeight)
    }

    /**
     * 设置系统通知提醒
     */
    fun scheduleSystemReminder(context: Context, reminderType: ReminderType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_type", reminderType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderType.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = when (reminderType) {
            ReminderType.BMR_UPDATE ->
                System.currentTimeMillis() + (BMR_UPDATE_INTERVAL_DAYS * 24 * 60 * 60 * 1000)
            ReminderType.WEIGHT_RECORD ->
                System.currentTimeMillis() + (WEIGHT_RECORD_INTERVAL_DAYS * 24 * 60 * 60 * 1000)
            ReminderType.GOAL_CHECK ->
                System.currentTimeMillis() + (GOAL_CHECK_INTERVAL_DAYS * 24 * 60 * 60 * 1000)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}

data class ReminderAlert(
    val type: ReminderType,
    val message: String,
    val daysSinceLastUpdate: Int,
    val suggestedAction: String
)

enum class ReminderType {
    BMR_UPDATE,
    WEIGHT_RECORD,
    GOAL_CHECK
}
```

---

## Phase 5: 体重曲线图表

### 5.1 曲线数据获取

```kotlin
// domain/usecase/GetWeightCurveUseCase.kt
class GetWeightCurveUseCase @Inject constructor(
    private val weightHistoryRepository: WeightHistoryRepository,
    private val userProfileRepository: UserProfileRepository
) {

    data class WeightCurveData(
        val points: List<WeightPoint>,
        val targetWeight: Float,
        val startWeight: Float,
        val currentWeight: Float,
        val totalLoss: Float,
        val estimatedFinalWeight: Float,
        val trend: WeightTrend
    )

    data class WeightPoint(
        val date: LocalDate,
        val weight: Float,
        val changeFromPrevious: Float?,
        val daysFromStart: Int
    )

    enum class WeightTrend {
        DESCENDING, STABLE, ASCENDING, FLUCTUATING
    }

    suspend fun getWeightCurve(): WeightCurveData {
        val profile = userProfileRepository.getProfile()!!
        val history = weightHistoryRepository.getAllOrdered()

        val points = history.mapIndexed { index, entity ->
            WeightPoint(
                date = LocalDate.ofEpochDay(entity.date),
                weight = entity.weight,
                changeFromPrevious = entity.changeFromPrevious,
                daysFromStart = index
            )
        }

        val trend = calculateTrend(points)
        val estimatedFinal = estimateFinalWeight(points, profile.targetDate)

        return WeightCurveData(
            points = points,
            targetWeight = profile.targetWeight,
            startWeight = points.firstOrNull()?.weight ?: profile.currentWeight,
            currentWeight = points.lastOrNull()?.weight ?: profile.currentWeight,
            totalLoss = (points.firstOrNull()?.weight ?: 0f) - (points.lastOrNull()?.weight ?: 0f),
            estimatedFinalWeight = estimatedFinal,
            trend = trend
        )
    }

    private fun calculateTrend(points: List<WeightPoint>): WeightTrend {
        if (points.size < 3) return WeightTrend.STABLE

        val recent = points.takeLast(7)
        val avgChange = recent.mapNotNull { it.changeFromPrevious }.average()

        return when {
            avgChange < -0.3 -> WeightTrend.DESCENDING
            avgChange > 0.3 -> WeightTrend.ASCENDING
            recent.mapNotNull { it.changeFromPrevious }.any { it > 0.5 } -> WeightTrend.FLUCTUATING
            else -> WeightTrend.STABLE
        }
    }

    private fun estimateFinalWeight(points: List<WeightPoint>, targetDate: Long): Float {
        if (points.size < 2) return points.lastOrNull()?.weight ?: 0f

        val recentAvgLoss = points.takeLast(4)
            .mapNotNull { it.changeFromPrevious }
            .average()

        val daysRemaining = (targetDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)

        return points.last().weight + (recentAvgLoss * daysRemaining / 7).toFloat()
    }
}
```

---

## Phase 6: 达标墙/成就墙

### 6.1 成就徽章定义

```kotlin
// domain/model/BadgeType.kt
enum class BadgeType(
    val name: String,
    val displayName: String,
    val description: String,
    val requirement: BadgeRequirement,
    val rarity: BadgeRarity
) {
    FIRST_DAY(
        name = "first_day",
        displayName = "起步",
        description = "完成第一个达标日",
        requirement = BadgeRequirement(streak = 1),
        rarity = BadgeRarity.COMMON
    ),
    WEEK_STREAK(
        name = "week_streak",
        displayName = "一周坚持",
        description = "连续7天达标",
        requirement = BadgeRequirement(streak = 7),
        rarity = BadgeRarity.COMMON
    ),
    TWO_WEEK_STREAK(
        name = "two_week_streak",
        displayName = "两周毅力",
        description = "连续14天达标",
        requirement = BadgeRequirement(streak = 14),
        rarity = BadgeRarity.RARE
    ),
    MONTH_STREAK(
        name = "month_streak",
        displayName = "月度冠军",
        description = "连续30天达标",
        requirement = BadgeRequirement(streak = 30),
        rarity = BadgeRarity.EPIC
    ),
    WEIGHT_1KG(
        name = "weight_1kg",
        displayName = "减重1kg",
        description = "累计减重达到1公斤",
        requirement = BadgeRequirement(weightLoss = 1f),
        rarity = BadgeRarity.COMMON
    ),
    WEIGHT_5KG(
        name = "weight_5kg",
        displayName = "减重5kg 🎉",
        description = "累计减重达到5公斤",
        requirement = BadgeRequirement(weightLoss = 5f),
        rarity = BadgeRarity.RARE
    ),
    WEIGHT_10KG(
        name = "weight_10kg",
        displayName = "减重10kg 🏆",
        description = "累计减重达到10公斤",
        requirement = BadgeRequirement(weightLoss = 10f),
        rarity = BadgeRarity.EPIC
    ),
    GOAL_REACHED(
        name = "goal_reached",
        displayName = "目标达成 🌟",
        description = "达成设定的目标体重",
        requirement = BadgeRequirement(goalReached = true),
        rarity = BadgeRarity.LEGENDARY
    )
}

data class BadgeRequirement(
    val streak: Int? = null,
    val weightLoss: Float? = null,
    val goalReached: Boolean? = null
)

enum class BadgeRarity(val colorHex: String, val emoji: String) {
    COMMON("#9E9E9E", "🏅"),
    RARE("#2196F3", "💎"),
    EPIC("#9C27B0", "👑"),
    LEGENDARY("#FFD700", "🌟")
}
```

---

## Phase 7: 热量计算引擎

```kotlin
// domain/calculator/CalorieCalculator.kt
class CalorieCalculator {

    /**
     * 每日热量缺口目标
     * 公式: (当前体重 - 目标体重) × 7700 ÷ 天数
     */
    fun calculateDailyDeficitTarget(
        currentWeight: Float,
        targetWeight: Float,
        daysToTarget: Int
    ): Int {
        val totalDeficitNeeded = (currentWeight - targetWeight) * 7700f
        return (totalDeficitNeeded / daysToTarget).roundToInt()
    }

    /**
     * 每日建议摄入热量
     * 公式: BMR × 活动系数 - 缺口目标
     */
    fun calculateDailyTargetIntake(
        bmr: Float,
        activityLevel: ActivityLevel,
        dailyDeficitTarget: Int
    ): Int {
        val maintenanceCalories = bmr * activityLevel.multiplier
        return (maintenanceCalories - dailyDeficitTarget).roundToInt()
    }

    /**
     * 评估达标状态
     */
    fun evaluateAchievement(
        actualIntake: Int,
        targetIntake: Int,
        tolerance: Int = 100
    ): AchievementType {
        val difference = actualIntake - targetIntake
        return when {
            difference in -50..50 -> AchievementType.PERFECT
            difference in -tolerance..tolerance -> AchievementType.GOOD
            difference < tolerance + 200 -> AchievementType.ACCEPTABLE
            else -> AchievementType.NOT_ACHIEVED
        }
    }
}

enum class ActivityLevel(val multiplier: Float, val description: String) {
    SEDENTARY(1.2f, "久坐不动"),
    LIGHT(1.375f, "轻度活动(每周1-3次运动)"),
    MODERATE(1.55f, "中度活动(每周3-5次运动)"),
    ACTIVE(1.725f, "高度活动(每周6-7次运动)")
}

enum class AchievementType {
    PERFECT,    // 完美达标 (误差±50kcal)
    GOOD,       // 良好达标 (误差±100kcal)
    ACCEPTABLE, // 基本达标 (超出<200kcal)
    NOT_ACHIEVED // 未达标
}
```

---

## Phase 8: 项目依赖配置

```kotlin
// build.gradle.kts (app module)
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.burnfat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.burnfat"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // WorkManager (提醒)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

---

## Phase 9: 验证清单

### 功能测试

| 模块 | 测试项 | 预期结果 |
|------|--------|----------|
| 餐次记录 | 早/午/晚/加餐分类 | 正确归属各餐次 |
| AI识别 | MiMo API调用 | 热量误差 < 25% |
| BMR提醒 | 14天未更新触发 | 弹窗正确显示 |
| 体重提醒 | 7天未记录触发 | 弹窗正确显示 |
| 体重曲线 | 多数据点渲染 | 曲线正确绘制 |
| 达标墙 | 徽章获取判定 | 条件判定正确 |

### API 验证

| 项目 | 状态 |
|------|------|
| API端点 | ✅ 已确认 |
| 认证Header | ✅ 已确认 (api-key) |
| 系统提示词 | ✅ 已确认 |
| 图片输入格式 | ✅ 已确认 (Base64/URL) |
| Vision模型 | ✅ 已确认 (mimo-v2-omni) |
| 支持图片格式 | ✅ 已确认 (JPEG/PNG/GIF/WebP/BMP) |

---

## 待确认事项

1. **MiMo API定价**: 需确认调用成本以进行成本控制
2. **食物库预设**: 是否需要内置常见食物热量参考库

---

*计划创建日期: 2026-04-09*
*最后更新: 2026-04-09 (确认Vision API信息)*