package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdConfig
import com.example.data.model.AppItem
import com.example.ui.components.AdBannerCard
import com.example.ui.components.AppIconView
import com.example.ui.theme.PlayBlue
import com.example.ui.theme.PlayGreen
import com.example.ui.viewmodel.AdminTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    isAuthenticated: Boolean,
    onLogin: (String, String) -> Boolean,
    onLogout: () -> Unit,
    currentAdminTab: AdminTab,
    onAdminTabSelect: (AdminTab) -> Unit,
    apps: List<AppItem>,
    adConfig: AdConfig,
    onSaveAdConfig: (AdConfig) -> Unit,
    onAddNewApp: (
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
    ) -> Unit,
    onPushUpdate: (
        appId: String,
        newVersion: String,
        newVersionCode: Int,
        whatsNew: String,
        notifyUsers: Boolean
    ) -> Unit,
    onDeleteApp: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isAuthenticated) {
        AdminLoginScreen(
            onLogin = onLogin,
            onCancel = onLogout,
            modifier = modifier
        )
        return
    }

    val context = LocalContext.current
    var editingAppForUpdate by remember { mutableStateOf<AppItem?>(null) }
    var appToDelete by remember { mutableStateOf<AppItem?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_screen_container")
    ) {
        // Admin Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PlayBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "لوحة تحكم المشرف (Admin Console)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مسجل باسم: admin",
                            style = MaterialTheme.typography.bodySmall,
                            color = PlayGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Logout Button
                    IconButton(
                        onClick = {
                            onLogout()
                            Toast.makeText(context, "تم تسجيل الخروج من لوحة التحكم.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "تسجيل الخروج",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Admin Sub-tabs
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AdminTab.values().forEachIndexed { index, tab ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = AdminTab.values().size),
                            onClick = { onAdminTabSelect(tab) },
                            selected = currentAdminTab == tab
                        ) {
                            Text(text = tab.titleAr, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Sub-tab contents
        when (currentAdminTab) {
            AdminTab.ADD_APP -> {
                AddAppForm(
                    onPublish = { title, pkg, cat, dev, ver, verCode, size, rating, desc, whatsNew, emoji, color, apk, featured, notify ->
                        onAddNewApp(title, pkg, cat, dev, ver, verCode, size, rating, desc, whatsNew, emoji, color, apk, featured, notify)
                        Toast.makeText(context, "تم نشر تطبيق \"$title\" وإرسال الإشعار بنجاح! 🚀", Toast.LENGTH_LONG).show()
                        onAdminTabSelect(AdminTab.MANAGE_APPS)
                    }
                )
            }
            AdminTab.MANAGE_APPS -> {
                ManageAppsList(
                    apps = apps,
                    onUpdateClick = { editingAppForUpdate = it },
                    onDeleteClick = { appToDelete = it }
                )
            }
            AdminTab.ADS_MANAGER -> {
                AdsManagerForm(
                    currentConfig = adConfig,
                    onSave = { updatedConfig ->
                        onSaveAdConfig(updatedConfig)
                        Toast.makeText(context, "تم حفظ وتفعيل إعدادات الإعلانات بنجاح! 💰", Toast.LENGTH_LONG).show()
                    }
                )
            }
            AdminTab.ANALYTICS -> {
                StoreAnalyticsView(apps = apps)
            }
        }
    }

    // Push Update Dialog
    editingAppForUpdate?.let { app ->
        PushUpdateDialog(
            app = app,
            onDismiss = { editingAppForUpdate = null },
            onConfirmUpdate = { newVersion, newVersionCode, whatsNew, notify ->
                onPushUpdate(app.id, newVersion, newVersionCode, whatsNew, notify)
                editingAppForUpdate = null
                Toast.makeText(context, "تم إصدار تحديث $newVersion بنجاح وإشعار المستخدمين! ⚡", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Delete App Confirmation Dialog
    appToDelete?.let { app ->
        AlertDialog(
            onDismissRequest = { appToDelete = null },
            title = { Text("تأكيد حذف التطبيق") },
            text = { Text("هل أنت متأكد من رغبتك في حذف تطبيق \"${app.title}\" من المتجر؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteApp(app)
                        appToDelete = null
                        Toast.makeText(context, "تم حذف التطبيق بنجاح.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { appToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdsManagerForm(
    currentConfig: AdConfig,
    onSave: (AdConfig) -> Unit
) {
    var isBannerEnabled by remember { mutableStateOf(currentConfig.isBannerEnabled) }
    var isPopUnderEnabled by remember { mutableStateOf(currentConfig.isPopUnderEnabled) }
    var isAdMobEnabled by remember { mutableStateOf(currentConfig.isAdMobEnabled) }

    var admobAppId by remember { mutableStateOf(currentConfig.admobAppId) }
    var admobBannerUnitId by remember { mutableStateOf(currentConfig.admobBannerUnitId) }
    var admobInterstitialUnitId by remember { mutableStateOf(currentConfig.admobInterstitialUnitId) }

    var customBannerTitle by remember { mutableStateOf(currentConfig.customBannerTitle) }
    var customBannerSubtitle by remember { mutableStateOf(currentConfig.customBannerSubtitle) }
    var customBannerClickUrl by remember { mutableStateOf(currentConfig.customBannerClickUrl) }

    var popUnderTitle by remember { mutableStateOf(currentConfig.popUnderTitle) }
    var popUnderClickUrl by remember { mutableStateOf(currentConfig.popUnderClickUrl) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ads_manager_form"),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "إدارة شبكات وإعلانات المتجر (Ad Network Manager)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = "تحكم في إعلانات البانر، البوب آندر (Pop-Under)، وشبكة Google AdMob في جميع شاشات المتجر.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Switch Toggles Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تفعيل إعلانات البانر في المتجر",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "تظهر البانرات في الصفحة الرئيسية وصفحة تفاصيل التطبيق",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isBannerEnabled,
                            onCheckedChange = { isBannerEnabled = it },
                            modifier = Modifier.testTag("toggle_banner_ad")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تفعيل إعلانات البوب آندر (Pop-Under / Interstitial)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "تظهر نافذة إعلان بوب آندر منبثقة عند بدء تنزيل أي تطبيق",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isPopUnderEnabled,
                            onCheckedChange = { isPopUnderEnabled = it },
                            modifier = Modifier.testTag("toggle_popunder_ad")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تفعيل شبكة Google AdMob الرسمية",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PlayBlue
                            )
                            Text(
                                text = "استخدام معرّفات وحدات AdMob الإعلانية",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAdMobEnabled,
                            onCheckedChange = { isAdMobEnabled = it },
                            modifier = Modifier.testTag("toggle_admob")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AdMob Section
            Text(
                text = "معرّفات Google AdMob",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = admobAppId,
                onValueChange = { admobAppId = it },
                label = { Text("معرّف تطبيق AdMob (App ID)") },
                placeholder = { Text("ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_admob_app_id")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = admobBannerUnitId,
                onValueChange = { admobBannerUnitId = it },
                label = { Text("معرّف وحدة إعلان البانر (Banner Ad Unit ID)") },
                placeholder = { Text("ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_admob_banner_id")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = admobInterstitialUnitId,
                onValueChange = { admobInterstitialUnitId = it },
                label = { Text("معرّف وحدة إعلان البوب آندر / البيني (Interstitial Unit ID)") },
                placeholder = { Text("ca-app-pub-xxxxxxxxxxxxxxxx/iiiiiiiiii") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_admob_interstitial_id")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Banners & Pop-under Section
            Text(
                text = "تخصيص نصوص وروابط الإعلانات (Custom Ads & Pop-under)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customBannerTitle,
                onValueChange = { customBannerTitle = it },
                label = { Text("عنوان إعلان البانر (Banner Headline)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = customBannerSubtitle,
                onValueChange = { customBannerSubtitle = it },
                label = { Text("وصف إعلان البانر (Banner Subtitle)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = customBannerClickUrl,
                onValueChange = { customBannerClickUrl = it },
                label = { Text("رابط النقر للبانر (Click URL)") },
                placeholder = { Text("https://example.com/promo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = popUnderTitle,
                onValueChange = { popUnderTitle = it },
                label = { Text("عنوان إعلان البوب آندر (Pop-Under Title)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = popUnderClickUrl,
                onValueChange = { popUnderClickUrl = it },
                label = { Text("رابط التوجيه لإعلان البوب آندر (Pop-Under Destination URL)") },
                placeholder = { Text("https://example.com/offer") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Live Banner Preview
            Text(
                text = "معاينة حية لإعلان البانر (Live Preview):",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            AdBannerCard(
                adConfig = AdConfig(
                    isBannerEnabled = isBannerEnabled,
                    isAdMobEnabled = isAdMobEnabled,
                    admobBannerUnitId = admobBannerUnitId,
                    customBannerTitle = customBannerTitle,
                    customBannerSubtitle = customBannerSubtitle,
                    customBannerClickUrl = customBannerClickUrl
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    val updated = currentConfig.copy(
                        isBannerEnabled = isBannerEnabled,
                        isPopUnderEnabled = isPopUnderEnabled,
                        isAdMobEnabled = isAdMobEnabled,
                        admobAppId = admobAppId.trim(),
                        admobBannerUnitId = admobBannerUnitId.trim(),
                        admobInterstitialUnitId = admobInterstitialUnitId.trim(),
                        customBannerTitle = customBannerTitle.trim(),
                        customBannerSubtitle = customBannerSubtitle.trim(),
                        customBannerClickUrl = customBannerClickUrl.trim(),
                        popUnderTitle = popUnderTitle.trim(),
                        popUnderClickUrl = popUnderClickUrl.trim()
                    )
                    onSave(updated)
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PlayGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_ad_settings_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ إعدادات وتطبيق الإعلانات فوراً 💾", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppForm(
    onPublish: (
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
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("تطبيقات") }
    var developer by remember { mutableStateOf("") }
    var version by remember { mutableStateOf("1.0.0") }
    var versionCode by remember { mutableStateOf("100") }
    var sizeMb by remember { mutableStateOf("25.0") }
    var rating by remember { mutableStateOf("4.8") }
    var description by remember { mutableStateOf("") }
    var whatsNew by remember { mutableStateOf("الإصدار الأولي الممتع للتطبيق.") }
    var selectedEmoji by remember { mutableStateOf("🚀") }
    var selectedColorHex by remember { mutableStateOf(0xFF0B57D0) }
    var apkUrl by remember { mutableStateOf("") }
    var isFeatured by remember { mutableStateOf(true) }
    var notifyUsers by remember { mutableStateOf(true) }

    val categories = listOf("تطبيقات", "الألعاب", "تواصل", "أدوات", "إنتاجية", "ترفيه")
    val emojis = listOf("🚀", "🎮", "💬", "🎨", "📁", "🎬", "📖", "⚡", "🛡️", "🎵", "📷", "🍔")
    val colors = listOf(
        0xFF0B57D0 to "أزرق",
        0xFF00875F to "أخضر",
        0xFF9C27B0 to "بنفسجي",
        0xFFFF5722 to "برتقالي",
        0xFFE91E63 to "وردي",
        0xFF00796B to "تركوازي"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("add_app_form"),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "بيانات التطبيق الجديد",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Preview icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                AppIconView(
                    emoji = selectedEmoji,
                    backgroundColorHex = selectedColorHex,
                    size = 64
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title.ifEmpty { "اسم التطبيق يظهر هنا" },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${developer.ifEmpty { "اسم المطور" }} · $selectedCategory · v$version",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("اسم التطبيق *") },
                placeholder = { Text("مثال: سناب شوت برو") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_app_title")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Package Name
            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text("معرّف الحزمة (Package Name) *") },
                placeholder = { Text("مثال: com.company.app") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_app_pkg")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Developer
            OutlinedTextField(
                value = developer,
                onValueChange = { developer = it },
                label = { Text("اسم المطور / الشركة") },
                placeholder = { Text("مثال: Digital Studio Inc.") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Chips
            Text(
                text = "التصنيف:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Icon Emoji Picker
            Text(
                text = "رمز الأيقونة (Emoji):",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(emojis) { emoji ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(46.dp)
                            .clickable { selectedEmoji = emoji }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Color Picker
            Text(
                text = "لون الأيقونة والخلفية:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(colors) { (hex, _) ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(hex))
                            .clickable { selectedColorHex = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColorHex == hex) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Version and Size
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = version,
                    onValueChange = { version = it },
                    label = { Text("رقم الإصدار (Version)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = sizeMb,
                    onValueChange = { sizeMb = it },
                    label = { Text("الحجم (ميجابايت)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("وصف التطبيق") },
                placeholder = { Text("اكتب وصفاً جذاباً لميزات التطبيق...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth().testTag("input_app_desc")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // What's New
            OutlinedTextField(
                value = whatsNew,
                onValueChange = { whatsNew = it },
                label = { Text("ما الجديد في هذا الإصدار") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Featured Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "عرض في البانر المميز أعلى المتجر",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
            }

            // Notification Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "إرسال إشعار فوري لجميع المستخدمين 🔔",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PlayBlue
                    )
                    Text(
                        text = "سيصل تنبيه فوري في شريط الإشعارات لتنزيل التطبيق",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = notifyUsers, onCheckedChange = { notifyUsers = it })
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Submit Button
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val parsedSize = sizeMb.toDoubleOrNull() ?: 20.0
                    val parsedRating = rating.toFloatOrNull() ?: 4.8f
                    val parsedCode = versionCode.toIntOrNull() ?: 100
                    onPublish(
                        title, packageName, selectedCategory, developer,
                        version, parsedCode, parsedSize, parsedRating,
                        description, whatsNew, selectedEmoji, selectedColorHex,
                        apkUrl, isFeatured, notifyUsers
                    )
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PlayBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("publish_app_button")
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("نشر التطبيق الآن في المتجر 🚀", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
fun ManageAppsList(
    apps: List<AppItem>,
    onUpdateClick: (AppItem) -> Unit,
    onDeleteClick: (AppItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("manage_apps_list"),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "التطبيقات المنشورة (${apps.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(apps) { app ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIconView(
                            emoji = app.iconEmoji,
                            backgroundColorHex = app.iconColorHex,
                            size = 50
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "الإصدار الحالي: v${app.version} (الكود: ${app.versionCode})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${app.category} · ${app.downloadsCount} تنزيل",
                                style = MaterialTheme.typography.labelSmall,
                                color = PlayBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { onDeleteClick(app) },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onUpdateClick(app) },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PlayGreen),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("push_update_btn_${app.id}")
                        ) {
                            Icon(Icons.Filled.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إصدار تحديث جديد ⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
fun PushUpdateDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onConfirmUpdate: (newVersion: String, newVersionCode: Int, whatsNew: String, notifyUsers: Boolean) -> Unit
) {
    var newVersion by remember { mutableStateOf("2.0.0") }
    var newVersionCode by remember { mutableStateOf("${app.versionCode + 1}") }
    var whatsNew by remember { mutableStateOf("ميزات جديدة، تحسينات فائقة في السرعة وتصميم محدث كلياً!") }
    var notifyUsers by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIconView(emoji = app.iconEmoji, backgroundColorHex = app.iconColorHex, size = 36)
                Spacer(modifier = Modifier.width(10.dp))
                Text("إصدار تحديث لـ ${app.title}")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "الإصدار الحالي في المتجر: v${app.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newVersion,
                    onValueChange = { newVersion = it },
                    label = { Text("رقم الإصدار الجديد (Version Name)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_update_version")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = whatsNew,
                    onValueChange = { whatsNew = it },
                    label = { Text("ما الجديد في هذا التحديث (Changelog)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("input_update_changelog")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = notifyUsers,
                        onCheckedChange = { notifyUsers = it }
                    )
                    Text(
                        text = "إرسال إشعار فوري لجميع المستخدمين بوجود التحديث ⚡",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PlayGreen
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val code = newVersionCode.toIntOrNull() ?: (app.versionCode + 1)
                    onConfirmUpdate(newVersion, code, whatsNew, notifyUsers)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PlayGreen),
                modifier = Modifier.testTag("confirm_publish_update_button")
            ) {
                Text("نشر التحديث الآن", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun StoreAnalyticsView(apps: List<AppItem>) {
    val totalApps = apps.size
    val installedApps = apps.count { it.isInstalled }
    val pendingUpdates = apps.count { it.hasUpdateAvailable }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_view"),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "نظرة عامة على المتجر",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnalyticsCard(
                    title = "إجمالي التطبيقات",
                    value = "$totalApps",
                    icon = Icons.Default.Apps,
                    color = PlayBlue,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsCard(
                    title = "المثبتة على الجهاز",
                    value = "$installedApps",
                    icon = Icons.Default.DownloadDone,
                    color = PlayGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnalyticsCard(
                    title = "تحديثات جاهزة",
                    value = "$pendingUpdates",
                    icon = Icons.Default.Sync,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                AnalyticsCard(
                    title = "المستخدمين النشطين",
                    value = "1.2M+",
                    icon = Icons.Default.Group,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "توزيع التطبيقات حسب الفئة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val categoryCounts = apps.groupBy { it.category }
            categoryCounts.forEach { (cat, list) ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${list.size} تطبيق",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PlayBlue
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
