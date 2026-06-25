package com.eztech.feature.problems.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.problems.presentation.component.ProblemCard
import com.eztech.feature.problems.presentation.component.ProblemFilterRow
import com.eztech.feature.problems.presentation.component.ProblemSearchBar
import com.eztech.feature.problems.presentation.component.ProblemTypeTabs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemListScreen(
    onBackClick: (() -> Unit)? = null,
    onProblemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProblemListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        text = "Python problems",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            state.errorMessage != null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                EzTechEmptyState(
                    title = "Problems unavailable",
                    message = state.errorMessage ?: "Could not load the problem set.",
                    modifier = Modifier.padding(EzTechDimens.ScreenPadding),
                    actionLabel = "Try again",
                    onAction = viewModel::retry,
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(
                    start = EzTechDimens.ScreenPadding,
                    top = EzTechDimens.SpaceMedium,
                    end = EzTechDimens.ScreenPadding,
                    bottom = 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            ) {
                item {
                    ProblemTypeTabs(
                        types = state.availableProblemTypes,
                        selectedType = state.selectedProblemType,
                        totalCount = state.allProblems.size,
                        onTypeSelected = viewModel::selectProblemType,
                    )
                }
                item {
                    ProblemSearchBar(
                        query = state.searchQuery,
                        onQueryChanged = viewModel::onSearchQueryChanged,
                    )
                }
                item {
                    ProblemFilterRow(
                        selectedDifficulty = state.selectedDifficulty,
                        onDifficultySelected = viewModel::selectDifficulty,
                        sortOption = state.sortOption,
                        onSortOptionSelected = viewModel::selectSortOption,
                    )
                }
                if (state.problems.isEmpty()) {
                    item {
                        EzTechEmptyState(
                            title = "No matching problems",
                            message = "Try another search, difficulty, or problem type.",
                            modifier = Modifier.fillMaxSize().padding(EzTechDimens.SpaceXLarge),
                        )
                    }
                } else {
                    items(state.problems, key = { problem -> problem.id }) { problem ->
                        ProblemCard(
                            problem = problem,
                            onClick = { onProblemClick(problem.id) },
                        )
                    }
                }
            }
        }
    }
}
