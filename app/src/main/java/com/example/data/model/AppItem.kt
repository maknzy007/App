package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppItem(
    @PrimaryKey
    val id: String,
    val title: String,
    val packageName: String,
    val category: String, // "الألعاب", "تطبيقات", "أدوات", "تواصل", "إنتاجية", "ترفيه"
    val developer: String,
    val version: String,
    val versionCode: Int,
    val installedVersion: String? = null,
    val installedVersionCode: Int = 0,
    val sizeMb: Double,
    val rating: Float,
    val reviewsCount: String,
    val downloadsCount: String,
    val description: String,
    val whatsNew: String = "تحسينات في الأداء وإصلاح بعض الأخطاء لتقديم تجربة أفضل.",
    val iconEmoji: String = "📱",
    val iconColorHex: Long = 0xFF0B57D0,
    val bannerColorHex: Long = 0xFF1E88E5,
    val apkUrl: String = "",
    val isFeatured: Boolean = false,
    val isTopChart: Boolean = false,
    val isInstalled: Boolean = false,
    val isUpdating: Boolean = false,
    val downloadProgress: Int = 0, // 0 to 100
    val isDownloading: Boolean = false,
    val releaseDate: Long = System.currentTimeMillis()
) {
    val hasUpdateAvailable: Boolean
        get() = isInstalled && (installedVersionCode < versionCode)
}

@Entity(tableName = "notifications")
data class StoreNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val appId: String? = null,
    val type: String, // "NEW_APP", "UPDATE_AVAILABLE", "INSTALL_SUCCESS"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
