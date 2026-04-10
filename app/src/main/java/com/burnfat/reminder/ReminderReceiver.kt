package com.burnfat.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.burnfat.MainActivity
import com.burnfat.R
import com.burnfat.domain.reminder.ReminderManager
import com.burnfat.domain.reminder.ReminderType

/**
 * 提醒广播接收器
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "burnfat_reminders"
        const val CHANNEL_NAME = "燃脂君提醒"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "用于提醒您更新BMR和记录体重"
                }

                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderTypeName = intent.getStringExtra(ReminderManager.EXTRA_REMINDER_TYPE)
            ?: return

        val reminderType = try {
            ReminderType.valueOf(reminderTypeName)
        } catch (e: IllegalArgumentException) {
            return
        }

        // 确保通知渠道存在
        createNotificationChannel(context)

        // 发送通知
        showNotification(context, reminderType)
    }

    private fun showNotification(context: Context, type: ReminderType) {
        val (title, message) = when (type) {
            ReminderType.BMR_UPDATE -> {
                "BMR更新提醒" to "您的基础代谢率数据已超过14天未更新，建议重新测量或根据体重变化估算新BMR。"
            }
            ReminderType.WEIGHT_RECORD -> {
                "体重记录提醒" to "已有一周未记录体重了，定期记录有助于追踪减脂进度。"
            }
        }

        // 点击通知打开App
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            type.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(type.ordinal, notification)
    }
}