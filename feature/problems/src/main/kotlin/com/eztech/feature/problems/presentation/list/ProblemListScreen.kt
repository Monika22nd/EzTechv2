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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.problems.presentation.component.ProblemCard
import com.eztech.feature.problems.presentation.component.ProblemFilterRow

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
                title = { Text("Python problems") },
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
                    message = state.errorMessage.orEmpty(),
                    modifier = Modifier.padding(EzTechDimens.ScreenPadding),
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
                    ProblemFilterRow(
                        selectedDifficulty = state.selectedDifficulty,
                        onDifficultySelected = viewModel::selectDifficulty,
                    )
                }
                if (state.problems.isEmpty()) {
                    item {
                        EzTechEmptyState(
                            title = "No matching problems",
                            message = "Choose another difficulty.",
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
