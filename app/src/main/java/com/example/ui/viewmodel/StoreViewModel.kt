package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AppItem
import com.example.data.model.StoreNotification
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class StoreTab(val titleAr: String, val titleEn: String) {
    GAMES("الألعاب", "Games"),
    APPS("تطبيقات", "Apps"),
    UPDATES("التحديثات", "Updates"),
    ADMIN("لوحة الإدارة", "Admin")
}

enum class AdminTab(val titleAr: String) {
    ADD_APP("إضافة تطبيق"),
    MANAGE_APPS("إدارة التطبيقات"),
    ADS_MANAGER("إدارة الإعلانات"),
    ANALYTICS("إحصائيات المتجر")
}

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StoreRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StoreRepository(db.appDao(), application)
    }

    private val _currentTab = MutableStateFlow(StoreTab.APPS)
    val currentTab: StateFlow<StoreTab> = _currentTab.asStateFlow()

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedApp = MutableStateFlow<AppItem?>(null)
    val selectedApp: StateFlow<AppItem?> = _selectedApp.asStateFlow()

    private val _isNotificationSheetOpen = MutableStateFlow(false)
    val isNotificationSheetOpen: StateFlow<Boolean> = _isNotificationSheetOpen.asStateFlow()

    private val _autoInstallEnabled = MutableStateFlow(true)
    val autoInstallEnabled: StateFlow<Boolean> = _autoInstallEnabled.asStateFlow()

    private val _autoUpdateEnabled = MutableStateFlow(false)
    val autoUpdateEnabled: StateFlow<Boolean> = _autoUpdateEnabled.asStateFlow()

    private val _isPopUnderVisible = MutableStateFlow(false)
    val isPopUnderVisible: StateFlow<Boolean> = _isPopUnderVisible.asStateFlow()

    val adConfig: StateFlow<com.example.data.model.AdConfig> = repository.observeAdConfig()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.model.AdConfig())

    private val _adminTab = MutableStateFlow(AdminTab.ADD_APP)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    private val _appToUpdate = MutableStateFlow<AppItem?>(null)
    val appToUpdate: StateFlow<AppItem?> = _appToUpdate.asStateFlow()

    val allApps: StateFlow<List<AppItem>> = repository.getAllApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<StoreNotification>> = repository.getNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = notifications
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.count { !it.isRead }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingUpdatesCount: StateFlow<Int> = allApps
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.count { it.hasUpdateAvailable }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun selectTab(tab: StoreTab) {
        _currentTab.value = tab
    }

    fun selectAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun openAppDetail(app: AppItem) {
        _selectedApp.value = app
    }

    fun closeAppDetail() {
        _selectedApp.value = null
    }

    fun toggleNotificationSheet(open: Boolean) {
        _isNotificationSheetOpen.value = open
    }

    fun setAutoInstall(enabled: Boolean) {
        _autoInstallEnabled.value = enabled
    }

    fun setAutoUpdate(enabled: Boolean) {
        _autoUpdateEnabled.value = enabled
    }

    fun setAppToUpdate(app: AppItem?) {
        _appToUpdate.value = app
    }

    fun loginAdmin(user: String, pass: String): Boolean {
        val success = (user.trim() == "admin" && pass == "Lesen20@@")
        if (success) {
            _isAdminAuthenticated.value = true
        }
        return success
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
        _currentTab.value = StoreTab.APPS
    }

    fun saveAdConfig(config: com.example.data.model.AdConfig) {
        viewModelScope.launch {
            repository.saveAdConfig(config)
        }
    }

    fun dismissPopUnder() {
        _isPopUnderVisible.value = false
    }

    fun installOrUpdateApp(app: AppItem) {
        viewModelScope.launch {
            if (adConfig.value.isPopUnderEnabled) {
                _isPopUnderVisible.value = true
            }
            repository.downloadAndInstallApp(app, autoInstall = _autoInstallEnabled.value)
            // Update selectedApp reference if currently opened
            if (_selectedApp.value?.id == app.id) {
                val updated = repository.getAppById(app.id)
                _selectedApp.value = updated
            }
        }
    }

    fun updateAllPendingApps() {
        viewModelScope.launch {
            val toUpdate = allApps.value.filter { it.hasUpdateAvailable }
            for (app in toUpdate) {
                repository.downloadAndInstallApp(app, autoInstall = _autoInstallEnabled.value)
            }
        }
    }

    fun uninstallApp(appId: String) {
        viewModelScope.launch {
            repository.uninstallApp(appId)
            if (_selectedApp.value?.id == appId) {
                _selectedApp.value = repository.getAppById(appId)
            }
        }
    }

    fun addNewApp(
        title: String,
        packageName: String,
        category: String,
        developer: String,
        version: String,
        versionCode: Int,
        sizeMb: Double,
        rating: Float,
        description: String,
        whatsNew: String,
        iconEmoji: String,
        iconColorHex: Long,
        apkUrl: String,
        isFeatured: Boolean,
        notifyUsers: Boolean
    ) {
        viewModelScope.launch {
            val newApp = AppItem(
                id = "app_${UUID.randomUUID().toString().take(8)}",
                title = title.trim(),
                packageName = packageName.trim().ifEmpty { "com.store.${System.currentTimeMillis()}" },
                category = category,
                developer = developer.trim().ifEmpty { "مطور مستقل" },
                version = version.trim().ifEmpty { "1.0.0" },
                versionCode = versionCode,
                sizeMb = if (sizeMb > 0) sizeMb else 25.0,
                rating = if (rating in 1.0f..5.0f) rating else 4.8f,
                reviewsCount = "1K",
                downloadsCount = "10K+",
                description = description.trim().ifEmpty { "تطبيق جديد رائع ومميز يلبي احتياجاتك." },
                whatsNew = whatsNew.trim().ifEmpty { "الإصدار الأول الأولي." },
                iconEmoji = iconEmoji.ifEmpty { "📦" },
                iconColorHex = iconColorHex,
                bannerColorHex = iconColorHex,
                apkUrl = apkUrl.trim(),
                isFeatured = isFeatured,
                isTopChart = false,
                isInstalled = false
            )
            repository.addCustomApp(newApp, notifyUsers = notifyUsers)
        }
    }

    fun pushUpdateForApp(
        appId: String,
        newVersion: String,
        newVersionCode: Int,
        whatsNew: String,
        notifyUsers: Boolean
    ) {
        viewModelScope.launch {
            repository.pushAppUpdate(appId, newVersion, newVersionCode, whatsNew, notifyUsers)
            _appToUpdate.value = null
        }
    }

    fun deleteApp(app: AppItem) {
        viewModelScope.launch {
            repository.deleteApp(app)
            if (_selectedApp.value?.id == app.id) {
                _selectedApp.value = null
            }
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }
}
