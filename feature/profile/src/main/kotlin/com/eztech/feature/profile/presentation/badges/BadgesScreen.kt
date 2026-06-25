package com.eztech.feature.profile.presentation.badges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.BadgeRequirementType
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.component.EzTechTopBar
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.profile.presentation.component.BadgeItem

@Composable
fun BadgesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BadgesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

    selectedBadge?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            onDismiss = { selectedBadge = null },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            EzTechTopBar(
                title = "Badges",
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            state.errorMessage != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                EzTechEmptyState(
                    title = "Badges unavailable",
                    message = state.errorMessage.orEmpty(),
                    actionLabel = "Try again",
                    onAction = viewModel::retry,
                    modifier = Modifier.padding(EzTechDimens.ScreenPadding),
                )
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            ) {
                BadgeSummary(
                    unlocked = state.unlockedCount,
                    total = state.badges.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EzTechDimens.ScreenPadding),
                )
                BadgeFilters(
                    selectedFilter = state.selectedFilter,
                    onFilterSelected = viewModel::selectFilter,
                )
                if (state.filteredBadges.isEmpty()) {
                    EzTechEmptyState(
                        title = "No badges here",
                        message = "Try a different filter.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(EzTechDimens.ScreenPadding),
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 116.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = EzTechDimens.ScreenPadding,
                            end = EzTechDimens.ScreenPadding,
                            bottom = 96.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
                        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
                    ) {
                        items(state.filteredBadges, key = Badge::id) { badge ->
                            BadgeItem(
                                badge = badge,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { selectedBadge = badge },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeSummary(
    unlocked: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
    ) {
        AssistChip(
            onClick = {},
            label = { Text("$unlocked / $total unlocked") },
        )
        AssistChip(
            onClick = {},
            label = {
                val percent = if (total == 0) 0 else unlocked * 100 / total
                Text("$percent% complete")
            },
        )
    }
}

@Composable
private fun BadgeFilters(
    selectedFilter: BadgeFilter,
    onFilterSelected: (BadgeFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = EzTechDimens.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
    ) {
        items(BadgeFilter.entries) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) },
            )
        }
    }
}

@Composable
private fun BadgeDetailDialog(
    badge: Badge,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(badge.name) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            ) {
                BadgeItem(
                    badge = badge,
                    modifier = Modifier.fillMaxWidth(0.55f),
                )
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = badge.requirementText(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (badge.unlocked) "Unlocked" else "Locked",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (badge.unlocked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}

private fun Badge.requirementText(): String = when (requirement.type) {
    BadgeRequirementType.SOLVE_COUNT -> "Solve ${requirement.value} problems"
    BadgeRequirementType.EXP -> "Earn ${requirement.value} EXP"
    BadgeRequirementType.STREAK -> "Keep a ${requirement.value}-day streak"
    BadgeRequirementType.WATCH_COUNT -> "Complete ${requirement.value} lessons"
    BadgeRequirementType.HARD_COUNT -> "Solve ${requirement.value} hard problems"
    BadgeRequirementType.LEVEL -> "Reach Level ${requirement.value}"
    BadgeRequirementType.SPEED_SOLVE -> "Solve a problem in ${requirement.value} seconds"
}
