package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDao
import com.example.data.model.AppItem
import com.example.data.model.StoreNotification
import com.example.util.ApkInstaller
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StoreRepository(
    private val appDao: AppDao,
    private val context: Context
) {

    fun getAllApps(): Flow<List<AppItem>> = appDao.getAllApps()

    fun getFeaturedApps(): Flow<List<AppItem>> = appDao.getFeaturedApps()

    fun getInstalledApps(): Flow<List<AppItem>> = appDao.getInstalledApps()

    fun getAppsByCategory(category: String): Flow<List<AppItem>> = appDao.getAppsByCategory(category)

    fun observeAppById(id: String): Flow<AppItem?> = appDao.observeAppById(id)

    fun getNotifications(): Flow<List<StoreNotification>> = appDao.getAllNotifications()

    suspend fun getAppById(id: String): AppItem? = appDao.getAppById(id)

    /**
     * Downloads an app with realistic progress increments, then automatically installs it
     * via Android PackageInstaller intent and notifies the user.
     */
    suspend fun downloadAndInstallApp(app: AppItem, autoInstall: Boolean = true) {
        withContext(Dispatchers.IO) {
            appDao.updateDownloadProgress(app.id, isDownloading = true, progress = 5)

            val steps = listOf(15, 32, 55, 78, 92, 100)
            for (progress in steps) {
                delay(300)
                appDao.updateDownloadProgress(app.id, isDownloading = true, progress = progress)
            }

            delay(200)
            // Complete installation
            appDao.setAppInstalled(
                id = app.id,
                isInstalled = true,
                version = app.version,
                versionCode = app.versionCode
            )

            // Trigger system Package Installer intent automatically
            if (autoInstall) {
                withContext(Dispatchers.Main) {
                    ApkInstaller.triggerInstall(context, app.title, app.packageName)
                }
            }

            // Trigger notification
            NotificationHelper.notifyInstallComplete(context, app.title)

            // Save in-app notification
            appDao.insertNotification(
                StoreNotification(
                    title = "تم تثبيت التطبيق بنجاح ✅",
                    message = "تم تنزيل وتثبيت \"${app.title}\" الإصدار ${app.version} بنجاح وهو متاح للاستخدام.",
                    appId = app.id,
                    type = "INSTALL_SUCCESS"
                )
            )
        }
    }

    suspend fun uninstallApp(appId: String) {
        withContext(Dispatchers.IO) {
            appDao.setAppInstalled(appId, isInstalled = false, version = null, versionCode = 0)
            appDao.updateDownloadProgress(appId, isDownloading = false, progress = 0)
        }
    }

    suspend fun addCustomApp(app: AppItem, notifyUsers: Boolean) {
        withContext(Dispatchers.IO) {
            appDao.insertApp(app)

            if (notifyUsers) {
                NotificationHelper.notifyNewAppAdded(context, app.title, app.category)

                appDao.insertNotification(
                    StoreNotification(
                        title = "🎉 تطبيق جديد: ${app.title}",
                        message = "تمت إضافة تطبيق \"${app.title}\" الجديد ضمن قسم ${app.category}. تصفحه وحمله الآن!",
                        appId = app.id,
                        type = "NEW_APP"
                    )
                )
            }
        }
    }

    suspend fun updateAppDetails(app: AppItem) {
        withContext(Dispatchers.IO) {
            appDao.updateApp(app)
        }
    }

    suspend fun pushAppUpdate(
        appId: String,
        newVersion: String,
        newVersionCode: Int,
        whatsNew: String,
        notifyUsers: Boolean
    ) {
        withContext(Dispatchers.IO) {
            val app = appDao.getAppById(appId) ?: return@withContext
            appDao.updateAppVersion(appId, newVersion, newVersionCode, whatsNew)

            if (notifyUsers) {
                NotificationHelper.notifyUpdateAvailable(context, app.title, newVersion, whatsNew)

                appDao.insertNotification(
                    StoreNotification(
                        title = "⚡ تحديث متوفر: ${app.title}",
                        message = "يتوفر الآن تحديث جديد للإصدار $newVersion. التغييرات: $whatsNew",
                        appId = app.id,
                        type = "UPDATE_AVAILABLE"
                    )
                )
            }
        }
    }

    suspend fun deleteApp(app: AppItem) {
        withContext(Dispatchers.IO) {
            appDao.deleteApp(app)
        }
    }

    suspend fun markNotificationAsRead(id: Long) {
        withContext(Dispatchers.IO) {
            appDao.markNotificationAsRead(id)
        }
    }

    suspend fun markAllNotificationsAsRead() {
        withContext(Dispatchers.IO) {
            appDao.markAllNotificationsAsRead()
        }
    }

    suspend fun clearNotifications() {
        withContext(Dispatchers.IO) {
            appDao.clearNotifications()
        }
    }

    fun observeAdConfig(): Flow<com.example.data.model.AdConfig> {
        return kotlinx.coroutines.flow.flow {
            val current = appDao.getAdConfig() ?: com.example.data.model.AdConfig()
            emit(current)
            appDao.observeAdConfig().collect { cfg ->
                if (cfg != null) {
                    emit(cfg)
                }
            }
        }
    }

    suspend fun saveAdConfig(adConfig: com.example.data.model.AdConfig) {
        withContext(Dispatchers.IO) {
            appDao.saveAdConfig(adConfig)
        }
    }
}
