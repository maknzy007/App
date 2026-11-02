package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {

    private const val CHANNEL_NEW_APPS = "channel_new_apps"
    private const val CHANNEL_UPDATES = "channel_updates"
    private const val CHANNEL_DOWNLOADS = "channel_downloads"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelNew = NotificationChannel(
                CHANNEL_NEW_APPS,
                "تطبيقات جديدة في المتجر",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات عند توفر تطبيقات وألعاب جديدة في المتجر"
            }

            val channelUpdates = NotificationChannel(
                CHANNEL_UPDATES,
                "تحديثات التطبيقات",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات عند توفر تحديثات جديدة للتطبيقات المثبتة"
            }

            val channelDownloads = NotificationChannel(
                CHANNEL_DOWNLOADS,
                "التنزيل والتثبيت",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تنبيهات اكتمال التنزيل والتثبيت التلقائي"
            }

            notificationManager.createNotificationChannels(listOf(channelNew, channelUpdates, channelDownloads))
        }
    }

    fun notifyNewAppAdded(context: Context, appTitle: String, category: String) {
        initNotificationChannels(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appTitle.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_NEW_APPS)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("🎉 تطبيق جديد متاح الآن!")
            .setContentText("تم نشر \"$appTitle\" ضمن قسم $category. اضغط للاستكشاف والتحميل.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("تم نشر تطبيق \"$appTitle\" بنجاح! متوفر الآن للتحميل المجاني في Play Market."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(appTitle.hashCode(), notification)
        } catch (_: SecurityException) {
            // Handled if permission denied
        }
    }

    fun notifyUpdateAvailable(context: Context, appTitle: String, newVersion: String, whatsNew: String) {
        initNotificationChannels(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appTitle.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_UPDATES)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("⚡ تحديث جديد: $appTitle ($newVersion)")
            .setContentText("تحديث جديد متاح يشتمل على ميزات وتحسينات جديدة.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("تحديث متاح لتطبيق $appTitle الإصدار $newVersion.\nالجديد: $whatsNew"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(appTitle.hashCode() + 1, notification)
        } catch (_: SecurityException) {
            // Handled
        }
    }

    fun notifyInstallComplete(context: Context, appTitle: String) {
        initNotificationChannels(context)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            appTitle.hashCode() + 2,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DOWNLOADS)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("✅ اكتمل التثبيت")
            .setContentText("تم تثبيت تطبيق \"$appTitle\" بنجاح وهو جاهز للاستخدام.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(appTitle.hashCode() + 2, notification)
        } catch (_: SecurityException) {
            // Handled
        }
    }
}
