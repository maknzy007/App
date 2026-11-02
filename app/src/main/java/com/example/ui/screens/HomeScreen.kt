package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.components.AppIconView
import com.example.ui.components.AppItemRow
import com.example.ui.components.HeroBanner
import com.example.ui.theme.StarYellow
import com.example.ui.viewmodel.StoreTab

import com.example.data.model.AdConfig
import com.example.ui.components.AdBannerCard

@Composable
fun HomeScreen(
    currentTab: StoreTab,
    apps: List<AppItem>,
    searchQuery: String,
    selectedCategory: String,
    adConfig: AdConfig = AdConfig(),
    onAppClick: (AppItem) -> Unit,
    onInstallClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter apps based on active tab and category
    val tabFilteredApps = apps.filter { app ->
        when (currentTab) {
            StoreTab.GAMES -> app.category == "الألعاب"
            StoreTab.APPS -> app.category != "الألعاب"
            else -> true
        }
    }

    val categoryFilteredApps = if (selectedCategory == "الكل") {
        tabFilteredApps
    } else {
        tabFilteredApps.filter { it.category == selectedCategory }
    }

    val finalApps = if (searchQuery.isBlank()) {
        categoryFilteredApps
    } else {
        apps.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.developer.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    val featuredApp = finalApps.firstOrNull { it.isFeatured } ?: finalApps.firstOrNull()
    val topChartApps = finalApps.sortedByDescending { it.rating }
    val remainingApps = finalApps.filter { it.id != featuredApp?.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        if (searchQuery.isNotBlank()) {
            item {
                Text(
                    text = "نتائج البحث عن \"$searchQuery\" (${finalApps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
            if (finalApps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🔍", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "لم يتم العثور على نتائج",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "جرب البحث باسم تطبيق آخر أو كلمة مفتاحية.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(finalApps) { app ->
                    AppItemRow(
                        app = app,
                        onAppClick = { onAppClick(app) },
                        onInstallClick = { onInstallClick(app) }
                    )
                }
            }
        } else {
            // Hero Spotlight Banner
            if (featuredApp != null) {
                item {
                    HeroBanner(
                        app = featuredApp,
                        onAppClick = { onAppClick(featuredApp) },
                        onInstallClick = { onInstallClick(featuredApp) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            // Sponsored Ad Banner
            item {
                AdBannerCard(
                    adConfig = adConfig,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Recommended horizontal row
            if (finalApps.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = if (currentTab == StoreTab.GAMES) "ألعاب مقترحة لك" else "تطبيقات مقترحة لك",
                        subtitle = "بناءً على نشاطك واهتماماتك"
                    )
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(finalApps.take(6)) { app ->
                            CompactAppCard(
                                app = app,
                                onAppClick = { onAppClick(app) }
                            )
                        }
                    }
                }
            }

            // Top Charts section
            if (topChartApps.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "الأعلى تقييماً وشعبية",
                        subtitle = "التطبيقات الأكثر تنزيلاً وإعجاباً من المستخدمين"
                    )
                }

                itemsIndexed(topChartApps.take(5)) { index, app ->
                    AppItemRow(
                        app = app,
                        showRank = index + 1,
                        onAppClick = { onAppClick(app) },
                        onInstallClick = { onInstallClick(app) }
                    )
                }
            }

            // More Apps list
            if (remainingApps.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "اكتشف المزيد",
                        subtitle = "تطبيقات مميزة لتجربتها اليوم"
                    )
                }

                items(remainingApps) { app ->
                    AppItemRow(
                        app = app,
                        onAppClick = { onAppClick(app) },
                        onInstallClick = { onInstallClick(app) }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CompactAppCard(
    app: AppItem,
    onAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .width(118.dp)
            .clickable { onAppClick() }
            .testTag("compact_app_${app.id}")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppIconView(
                emoji = app.iconEmoji,
                backgroundColorHex = app.iconColorHex,
                size = 56
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = app.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${app.sizeMb} م.ب",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "${app.rating}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = StarYellow,
                    modifier = Modifier.size(10.dp).padding(start = 1.dp)
                )
            }
        }
    }
}
