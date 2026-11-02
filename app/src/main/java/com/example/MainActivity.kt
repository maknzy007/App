package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.StoreBottomNavigation
import com.example.ui.components.StoreTopBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StoreTab
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.initNotificationChannels(this)

        setContent {
            MyApplicationTheme {
                StoreApp()
            }
        }
    }
}

@Composable
fun StoreApp(viewModel: StoreViewModel = viewModel()) {
    // Request notification permissions on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* Granted or denied */ }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val allApps by viewModel.allApps.collectAsStateWithLifecycle()
    val selectedApp by viewModel.selectedApp.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val pendingUpdatesCount by viewModel.pendingUpdatesCount.collectAsStateWithLifecycle()
    val isNotificationSheetOpen by viewModel.isNotificationSheetOpen.collectAsStateWithLifecycle()
    val adminTab by viewModel.adminTab.collectAsStateWithLifecycle()
    val autoInstallEnabled by viewModel.autoInstallEnabled.collectAsStateWithLifecycle()
    val autoUpdateEnabled by viewModel.autoUpdateEnabled.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val adConfig by viewModel.adConfig.collectAsStateWithLifecycle()
    val isPopUnderVisible by viewModel.isPopUnderVisible.collectAsStateWithLifecycle()

    val categories = listOf("الكل", "الألعاب", "تطبيقات", "تواصل", "أدوات", "إنتاجية", "ترفيه")

    // Handle back button when not on main screen
    BackHandler(enabled = selectedApp != null || currentTab != StoreTab.APPS) {
        if (selectedApp != null) {
            viewModel.closeAppDetail()
        } else if (currentTab != StoreTab.APPS) {
            viewModel.selectTab(StoreTab.APPS)
        }
    }

    Scaffold(
        topBar = {
            if (selectedApp == null && currentTab != StoreTab.ADMIN) {
                StoreTopBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { viewModel.selectCategory(it) },
                    unreadNotificationCount = unreadNotificationsCount,
                    onNotificationClick = { viewModel.toggleNotificationSheet(true) },
                    onAdminClick = { viewModel.selectTab(StoreTab.ADMIN) },
                    isAdminActive = currentTab == StoreTab.ADMIN
                )
            }
        },
        bottomBar = {
            if (selectedApp == null) {
                StoreBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) },
                    pendingUpdatesCount = pendingUpdatesCount
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedApp != null) {
                AppDetailScreen(
                    app = selectedApp!!,
                    adConfig = adConfig,
                    onBack = { viewModel.closeAppDetail() },
                    onInstallOrUpdate = { viewModel.installOrUpdateApp(selectedApp!!) },
                    onUninstall = { viewModel.uninstallApp(selectedApp!!.id) }
                )
            } else {
                when (currentTab) {
                    StoreTab.GAMES, StoreTab.APPS -> {
                        HomeScreen(
                            currentTab = currentTab,
                            apps = allApps,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            adConfig = adConfig,
                            onAppClick = { viewModel.openAppDetail(it) },
                            onInstallClick = { viewModel.installOrUpdateApp(it) }
                        )
                    }
                    StoreTab.UPDATES -> {
                        UpdatesScreen(
                            apps = allApps,
                            autoInstallEnabled = autoInstallEnabled,
                            onAutoInstallChange = { viewModel.setAutoInstall(it) },
                            autoUpdateEnabled = autoUpdateEnabled,
                            onAutoUpdateChange = { viewModel.setAutoUpdate(it) },
                            onUpdateAll = { viewModel.updateAllPendingApps() },
                            onInstallOrUpdateApp = { viewModel.installOrUpdateApp(it) },
                            onAppClick = { viewModel.openAppDetail(it) },
                            onUninstallApp = { viewModel.uninstallApp(it) }
                        )
                    }
                    StoreTab.ADMIN -> {
                        AdminScreen(
                            isAuthenticated = isAdminAuthenticated,
                            onLogin = { u, p -> viewModel.loginAdmin(u, p) },
                            onLogout = { viewModel.logoutAdmin() },
                            currentAdminTab = adminTab,
                            onAdminTabSelect = { viewModel.selectAdminTab(it) },
                            apps = allApps,
                            adConfig = adConfig,
                            onSaveAdConfig = { viewModel.saveAdConfig(it) },
                            onAddNewApp = { title, pkg, cat, dev, ver, verCode, size, rating, desc, whatsNew, emoji, color, apk, featured, notify ->
                                viewModel.addNewApp(
                                    title = title,
                                    packageName = pkg,
                                    category = cat,
                                    developer = dev,
                                    version = ver,
                                    versionCode = verCode,
                                    sizeMb = size,
                                    rating = rating,
                                    description = desc,
                                    whatsNew = whatsNew,
                                    iconEmoji = emoji,
                                    iconColorHex = color,
                                    apkUrl = apk,
                                    isFeatured = featured,
                                    notifyUsers = notify
                                )
                            },
                            onPushUpdate = { appId, newVersion, newVersionCode, whatsNew, notify ->
                                viewModel.pushUpdateForApp(appId, newVersion, newVersionCode, whatsNew, notify)
                            },
                            onDeleteApp = { viewModel.deleteApp(it) }
                        )
                    }
                }
            }
        }
    }

    // Pop-Under / Interstitial Ad Dialog
    if (isPopUnderVisible) {
        com.example.ui.components.PopUnderAdDialog(
            adConfig = adConfig,
            onDismiss = { viewModel.dismissPopUnder() }
        )
    }

    // In-app Notifications Sheet
    if (isNotificationSheetOpen) {
        NotificationSheet(
            notifications = notifications,
            onDismiss = { viewModel.toggleNotificationSheet(false) },
            onNotificationClick = { notif ->
                viewModel.markNotificationRead(notif.id)
                if (notif.appId != null) {
                    val app = allApps.firstOrNull { it.id == notif.appId }
                    if (app != null) {
                        viewModel.openAppDetail(app)
                        viewModel.toggleNotificationSheet(false)
                    }
                }
            },
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onClearAll = { viewModel.clearAllNotifications() }
        )
    }
}
