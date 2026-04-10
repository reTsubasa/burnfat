# BurnFat - 智能热量管理应用

一款基于AI的食物热量识别与饮食计划管理Android应用。

## 功能特性

### 📸 AI食物识别
- **拍照识别**：拍摄食物照片，AI自动识别食物种类并估算热量
- **相册识别**：从相册选择食物照片进行识别
- **多菜品识别**：支持识别一餐中的多种菜品，分别计算热量
- **健康建议**：AI提供饮食健康建议

### 📊 饮食记录
- **每日热量追踪**：记录每餐摄入热量，对比目标值
- **餐食分类**：早餐、午餐、晚餐、加餐分类记录
- **历史记录**：查看历史饮食记录和热量统计

### 🎯 计划管理
- **减脂计划**：设定目标体重和时间，自动计算每日热量目标
- **AI计划评估**：智能评估计划可行性，提供优化建议
- **进度追踪**：可视化展示减脂进度

### 🏆 达标墙
- **达标记录**：记录每日达标情况，积累成就
- **连续达标**：追踪连续达标天数，激励坚持
- **热量缺口统计**：展示累计热量缺口

### 🔧 AI提供商支持
- **小米 MiMo**：小米自研AI模型，支持视觉识别
- **阿里云 Qwen**：阿里通义千问，支持多模态

## 技术架构

- **语言**：Kotlin
- **UI框架**：Jetpack Compose
- **架构模式**：MVVM + Repository
- **依赖注入**：Hilt
- **数据库**：Room
- **网络请求**：OkHttp
- **序列化**：Kotlinx Serialization
- **异步处理**：Kotlin Coroutines + Flow

## 项目结构

```
app/
├── src/main/java/com/burnfat/
│   ├── data/
│   │   ├── local/          # 本地数据库 (Room)
│   │   ├── remote/         # 远程API服务
│   │   └── repository/     # 数据仓库层
│   ├── domain/
│   │   ├── calculator/     # 热量计算逻辑
│   │   └── model/          # 业务模型
│   ├── service/            # 后台服务
│   ├── ui/
│   │   ├── components/     # 可复用UI组件
│   │   ├── screens/        # 各功能页面
│   │   └── theme/          # 主题配置
│   └── MainActivity.kt     # 入口Activity
```

## API配置

应用需要在设置页面配置AI提供商的API Key：

1. **小米 MiMo**
   - 获取地址：[platform.xiaomimimo.com](https://platform.xiaomimimo.com)
   - Header格式：`api-key: <your-api-key>`

2. **阿里云 Qwen**
   - 获取地址：[dashscope.console.aliyun.com](https://dashscope.console.aliyun.com)
   - Header格式：`Authorization: Bearer <your-api-key>`

API Key 使用 EncryptedSharedPreferences 安全存储。

## 构建项目

### 环境要求
- Android Studio Arctic Fox 或更高版本
- JDK 17+
- Android SDK 34

### 构建命令

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

### 输出位置

- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## 热量计算原理

应用基于以下公式计算热量需求：

1. **BMR（基础代谢率）**：使用Mifflin-St Jeor公式
2. **TDEE（每日总消耗）**：BMR × 活动系数
3. **每日目标摄入**：TDEE - 每日热量缺口目标

活动系数：
- 久坐：1.2
- 轻度活动：1.375
- 中度活动：1.55
- 高度活动：1.725
- 极高强度：1.9

## License

MIT License