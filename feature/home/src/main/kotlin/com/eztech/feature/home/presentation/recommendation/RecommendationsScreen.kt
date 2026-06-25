package com.eztech.feature.home.presentation.recommendation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.Recommendation
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.home.presentation.component.RecommendationCard
import com.eztech.feature.home.presentation.component.RecommendationStatsCard

/**
 * Full-page recommendation list.
 *
 * The screen mirrors the Home recommendation cards but gives more vertical space for the stats panel
 * and all ranked recommendations. Tapping a card routes either to a problem solve/detail flow or to
 * a lesson, depending on the card target.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(
    onBackClick: () -> Unit,
    onLessonClick: (Lesson) -> Unit,
    onProblemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecommendationsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Recommendations") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
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
                    title = "Recommendations unavailable",
                    message = state.errorMessage.orEmpty(),
                    modifier = Modifier.padding(EzTechDimens.ScreenPadding),
                    actionLabel = "Try again",
                    onAction = viewModel::retry,
                )
            }

            state.recommendations.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                EzTechEmptyState(
                    title = "No recommendations yet",
                    message = "Solve problems and watch lessons to personalize this page.",
                    modifier = Modifier.padding(EzTechDimens.ScreenPadding),
                )
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = EzTechDimens.ScreenPadding,
                    top = EzTechDimens.SpaceMedium,
                    end = EzTechDimens.ScreenPadding,
                    bottom = 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            ) {
                state.stats?.let { stats ->
                    item {
                        RecommendationStatsCard(stats = stats)
                    }
                }
                items(state.recommendations, key = Recommendation::id) { recommendation ->
                    RecommendationCard(
                        recommendation = recommendation,
                        onClick = {
                            recommendation.problem?.let { problem ->
                                onProblemClick(problem.id)
                            } ?: recommendation.lesson?.let(onLessonClick)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
