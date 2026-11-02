package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdConfig
import com.example.data.model.AppItem
import com.example.data.model.StoreNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY releaseDate DESC")
    fun getAllApps(): Flow<List<AppItem>>

    @Query("SELECT * FROM apps WHERE id = :id LIMIT 1")
    suspend fun getAppById(id: String): AppItem?

    @Query("SELECT * FROM apps WHERE id = :id LIMIT 1")
    fun observeAppById(id: String): Flow<AppItem?>

    @Query("SELECT * FROM apps WHERE isFeatured = 1")
    fun getFeaturedApps(): Flow<List<AppItem>>

    @Query("SELECT * FROM apps WHERE category = :category")
    fun getAppsByCategory(category: String): Flow<List<AppItem>>

    @Query("SELECT * FROM apps WHERE isInstalled = 1")
    fun getInstalledApps(): Flow<List<AppItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppItem>)

    @Update
    suspend fun updateApp(app: AppItem)

    @Delete
    suspend fun deleteApp(app: AppItem)

    @Query("DELETE FROM apps WHERE id = :id")
    suspend fun deleteAppById(id: String)

    @Query("UPDATE apps SET isDownloading = :isDownloading, downloadProgress = :progress WHERE id = :id")
    suspend fun updateDownloadProgress(id: String, isDownloading: Boolean, progress: Int)

    @Query("UPDATE apps SET isInstalled = :isInstalled, installedVersion = :version, installedVersionCode = :versionCode, isDownloading = 0, downloadProgress = 100 WHERE id = :id")
    suspend fun setAppInstalled(id: String, isInstalled: Boolean, version: String?, versionCode: Int)

    @Query("UPDATE apps SET version = :version, versionCode = :versionCode, whatsNew = :whatsNew WHERE id = :id")
    suspend fun updateAppVersion(id: String, version: String, versionCode: Int, whatsNew: String)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<StoreNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: StoreNotification)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()

    // Ad Configuration
    @Query("SELECT * FROM ad_config WHERE id = 1 LIMIT 1")
    fun observeAdConfig(): Flow<AdConfig?>

    @Query("SELECT * FROM ad_config WHERE id = 1 LIMIT 1")
    suspend fun getAdConfig(): AdConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdConfig(adConfig: AdConfig)
}
