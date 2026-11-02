package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentRed
import com.example.ui.viewmodel.StoreTab

@Composable
fun StoreBottomNavigation(
    currentTab: StoreTab,
    onTabSelected: (StoreTab) -> Unit,
    pendingUpdatesCount: Int,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier.testTag("store_bottom_navigation")
    ) {
        // Games Tab
        NavigationBarItem(
            selected = currentTab == StoreTab.GAMES,
            onClick = { onTabSelected(StoreTab.GAMES) },
            icon = {
                Icon(
                    imageVector = if (currentTab == StoreTab.GAMES) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports,
                    contentDescription = StoreTab.GAMES.titleAr
                )
            },
            label = {
                Text(
                    text = StoreTab.GAMES.titleAr,
                    fontWeight = if (currentTab == StoreTab.GAMES) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            },
            modifier = Modifier.testTag("tab_games")
        )

        // Apps Tab
        NavigationBarItem(
            selected = currentTab == StoreTab.APPS,
            onClick = { onTabSelected(StoreTab.APPS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == StoreTab.APPS) Icons.Filled.Apps else Icons.Outlined.Apps,
                    contentDescription = StoreTab.APPS.titleAr
                )
            },
            label = {
                Text(
                    text = StoreTab.APPS.titleAr,
                    fontWeight = if (currentTab == StoreTab.APPS) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            },
            modifier = Modifier.testTag("tab_apps")
        )

        // Updates Tab
        NavigationBarItem(
            selected = currentTab == StoreTab.UPDATES,
            onClick = { onTabSelected(StoreTab.UPDATES) },
            icon = {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = if (currentTab == StoreTab.UPDATES) Icons.Filled.SystemUpdate else Icons.Outlined.SystemUpdate,
                        contentDescription = StoreTab.UPDATES.titleAr
                    )
                    if (pendingUpdatesCount > 0) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(AccentRed)
                        ) {
                            Text(
                                text = "$pendingUpdatesCount",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            label = {
                Text(
                    text = StoreTab.UPDATES.titleAr,
                    fontWeight = if (currentTab == StoreTab.UPDATES) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            },
            modifier = Modifier.testTag("tab_updates")
        )

        // Admin Tab
        NavigationBarItem(
            selected = currentTab == StoreTab.ADMIN,
            onClick = { onTabSelected(StoreTab.ADMIN) },
            icon = {
                Icon(
                    imageVector = if (currentTab == StoreTab.ADMIN) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                    contentDescription = StoreTab.ADMIN.titleAr
                )
            },
            label = {
                Text(
                    text = StoreTab.ADMIN.titleAr,
                    fontWeight = if (currentTab == StoreTab.ADMIN) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            },
            modifier = Modifier.testTag("tab_admin")
        )
    }
}
