package com.eztech.feature.problems.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.problems.presentation.component.DifficultyBadge
import com.eztech.feature.problems.presentation.component.ProblemDescription
import com.eztech.feature.problems.presentation.component.VisibleTestCaseCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProblemDetailScreen(
    onBackClick: () -> Unit,
    onSolveClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProblemDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val problem = state.problem

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(problem?.title ?: "Problem") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            if (problem != null) {
                Button(
                    onClick = { onSolveClick(problem.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(EzTechDimens.ScreenPadding),
                ) {
                    Icon(Icons.Rounded.Code, contentDescription = null)
                    Text(
                        text = "Solve problem",
                        modifier = Modifier.padding(start = EzTechDimens.SpaceSmall),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            state.errorMessage != null || problem == null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                EzTechEmptyState(
                    title = "Problem unavailable",
                    message = state.errorMessage ?: "This problem could not be loaded.",
                    modifier = Modifier.padding(EzTechDimens.ScreenPadding),
                    actionLabel = "Try again",
                    onAction = viewModel::retry,
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(EzTechDimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
                    ) {
                        DifficultyBadge(problem.difficulty)
                        Text(
                            text = problem.tags.joinToString("  |  "),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                item {
                    ProblemDescription(text = problem.description)
                }
                if (problem.constraints.isNotEmpty()) {
                    item { HorizontalDivider() }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall)) {
                            Text(
                                text = "Constraints",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            problem.constraints.forEach { constraint ->
                                Text(
                                    text = "- $constraint",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                if (state.visibleTestCases.isNotEmpty()) {
                    item {
                        Text(
                            text = "Examples",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    itemsIndexed(
                        items = state.visibleTestCases,
                        key = { _, testCase -> testCase.id },
                    ) { index, testCase ->
                        VisibleTestCaseCard(testCase = testCase, index = index)
                    }
                }
            }
        }
    }
}
