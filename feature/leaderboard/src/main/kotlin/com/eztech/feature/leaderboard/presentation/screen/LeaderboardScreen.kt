package com.eztech.feature.leaderboard.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.feature.leaderboard.presentation.component.LeaderboardItem
import com.eztech.feature.leaderboard.presentation.component.PodiumView
import com.eztech.feature.leaderboard.presentation.viewmodel.LeaderboardViewModel
import com.eztech.core.ui.theme.EzTechDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onProblemsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LeaderboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "🏆 Bảng xếp hạng",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    TextButton(onClick = onProblemsClick) {
                        Icon(Icons.Rounded.Code, contentDescription = null)
                        Spacer(Modifier.width(EzTechDimens.SpaceSmall))
                        Text("Problems")
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(EzTechDimens.SpaceMedium))
                        Text("Đang tải bảng xếp hạng...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        uiState.error.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            uiState.entries.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Chưa có dữ liệu xếp hạng", style = MaterialTheme.typography.bodyLarge)
                }
            }
            else -> {
                val top3 = uiState.entries.take(3)
                val rest = uiState.entries.drop(3)

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(
                        horizontal = EzTechDimens.SpaceLarge,
                        vertical = EzTechDimens.SpaceMedium,
                    ),
                    verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
                ) {
                    // Podium for top 3
                    item {
                        PodiumView(top3 = top3)
                        Spacer(Modifier.height(EzTechDimens.SpaceLarge))
                        HorizontalDivider()
                        Spacer(Modifier.height(EzTechDimens.SpaceSmall))
                        Text(
                            "Thứ hạng đầy đủ",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(EzTechDimens.SpaceSmall))
                    }

                    // Top 3 as regular rows too
                    items(top3, key = { it.userId }) { entry ->
                        LeaderboardItem(entry = entry)
                    }

                    // Divider
                    if (rest.isNotEmpty()) {
                        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                    }

                    // Remaining entries
                    items(rest, key = { it.userId }) { entry ->
                        LeaderboardItem(entry = entry)
                    }

                    // Bottom padding
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}
