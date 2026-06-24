package com.eztech.feature.profile.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.feature.profile.presentation.component.BadgeItem
import com.eztech.feature.profile.presentation.component.LevelProgressBar
import com.eztech.feature.profile.presentation.component.StatsGrid
import com.eztech.feature.profile.presentation.viewmodel.ProfileViewModel
import com.eztech.core.ui.theme.EzTechDimens

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất") },
            text = { Text("Bạn có chắc muốn đăng xuất không?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                }) { Text("Đăng xuất", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy") }
            },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("👤 Cá nhân", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Rounded.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.user == null -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                    Text("Vui lòng đăng nhập để xem hồ sơ")
                }
            }
            else -> {
                val user = uiState.user!!
                val unlockedBadges = uiState.badges.filter { it.unlocked }
                val allBadges = uiState.badges

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(EzTechDimens.ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceLarge),
                ) {
                    // ── Avatar + Name + Rank ─────────────────────────────
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                Alignment.Center,
                            ) {
                                Text(
                                    user.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Spacer(Modifier.height(EzTechDimens.SpaceSmall))
                            Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                "Level ${user.level}" + if (user.rank > 0) " · #${user.rank} Rank" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // ── Level Progress Bar ────────────────────────────────
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(Modifier.padding(EzTechDimens.SpaceLarge)) {
                                LevelProgressBar(user = user)
                            }
                        }
                    }

                    // ── Stats Grid ────────────────────────────────────────
                    item {
                        StatsGrid(user = user, rank = user.rank)
                    }

                    // ── Badges ─────────────────────────────────────────────
                    item {
                        Text(
                            "🏅 Huy hiệu (${unlockedBadges.size}/${allBadges.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    item {
                        // Fixed height grid for badges (non-scrollable inside lazy)
                        val rows = (allBadges.size + 3) / 4
                        val gridHeight = (rows * 100).dp
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(gridHeight),
                            horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
                            verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
                            userScrollEnabled = false,
                        ) {
                            items(allBadges, key = { it.id }) { badge ->
                                BadgeItem(badge = badge)
                            }
                        }
                    }

                    // Bottom spacer
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}
