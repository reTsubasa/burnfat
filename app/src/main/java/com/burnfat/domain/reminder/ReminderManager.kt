package com.burnfat.domain.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.burnfat.data.local.dao.PlanDao
import com.burnfat.reminder.ReminderReceiver
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 提醒管理器
 */
@Singleton
class ReminderManager @Inject constructor(
    private val planDao: PlanDao
) {

    /**
     * 检查是否需要触发提醒
     */
    suspend fun checkReminders(): List<ReminderAlert> {
        val activePlan = planDao.getActivePlan() ?: return emptyList()
        val alerts = mutableListOf<ReminderAlert>()
        val now = System.currentTimeMillis()

        // BMR更新提醒 (每2周)
        val bmrDaysSince = (now - activePlan.updatedAt) / MILLIS_PER_DAY
        if (bmrDaysSince >= BMR_UPDATE_INTERVAL_DAYS) {
            alerts.add(
                ReminderAlert(
                    type = ReminderType.BMR_UPDATE,
                    message = "您的基础代谢率数据已超过${BMR_UPDATE_INTERVAL_DAYS}天未更新。\n" +
                            "减肥过程中BMR会随体重下降而降低，建议重新测量或根据新体重估算。",
                    daysSinceLastUpdate = bmrDaysSince.toInt(),
                    suggestedAction = "建议前往专业机构测量，或使用公式估算:\n新BMR ≈ 旧BMR × (新体重/旧体重)"
                )
            )
        }

        // 体重记录提醒 (每周)
        val weightDaysSince = (now - activePlan.updatedAt) / MILLIS_PER_DAY
        if (weightDaysSince >= WEIGHT_RECORD_INTERVAL_DAYS) {
            alerts.add(
                ReminderAlert(
                    type = ReminderType.WEIGHT_RECORD,
                    message = "已${weightDaysSince.toInt()}天未记录体重。\n" +
                            "定期记录体重有助于追踪减脂进度。",
                    daysSinceLastUpdate = weightDaysSince.toInt(),
                    suggestedAction = "建议每周固定时间测量(如周一早晨空腹)"
                )
            )
        }

        return alerts
    }

    /**
     * 设置系统通知提醒
     */
    fun scheduleSystemReminder(context: Context, reminderType: ReminderType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, reminderType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderType.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = when (reminderType) {
            ReminderType.BMR_UPDATE -> System.currentTimeMillis() + BMR_UPDATE_INTERVAL_DAYS * MILLIS_PER_DAY
            ReminderType.WEIGHT_RECORD -> System.currentTimeMillis() + WEIGHT_RECORD_INTERVAL_DAYS * MILLIS_PER_DAY
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    companion object {
        const val BMR_UPDATE_INTERVAL_DAYS = 14
        const val WEIGHT_RECORD_INTERVAL_DAYS = 7
        const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
        const val EXTRA_REMINDER_TYPE = "reminder_type"
    }
}

/**
 * 提醒类型
 */
enum class ReminderType {
    BMR_UPDATE,
    WEIGHT_RECORD
}

/**
 * 提醒警报数据
 */
data class ReminderAlert(
    val type: ReminderType,
    val message: String,
    val daysSinceLastUpdate: Int,
    val suggestedAction: String
)