package com.eztech.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eztech.core.domain.model.DashboardSummary
import com.eztech.core.domain.model.Lesson
import com.eztech.core.domain.model.LessonContentType
import com.eztech.core.domain.model.Problem
import com.eztech.core.domain.model.Recommendation
import com.eztech.core.domain.model.RecommendationStats
import com.eztech.core.ui.component.EzTechEmptyState
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.home.presentation.component.RecommendationSection

/**
 * Home dashboard route.
 *
 * It observes HomeViewModel and chooses between loading, error, and dashboard content states while
 * delegating navigation actions back to the app-level NavHost.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLearnClick: () -> Unit,
    onIdeClick: () -> Unit,
    onProblemsClick: () -> Unit,
    onRecommendationsClick: () -> Unit,
    onLessonClick: (Lesson) -> Unit,
    onProblemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Dashboard") })
        },
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            state.errorMessage != null && state.summary == null -> ErrorContent(
                message = state.errorMessage.orEmpty(),
                onRetry = viewModel::retry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            state.summary != null -> DashboardContent(
                summary = state.summary!!,
                recommendations = state.recommendations,
                recommendationStats = state.recommendationStats,
                isLoadingRecommendations = state.isLoadingRecommendations,
                onLearnClick = onLearnClick,
                onIdeClick = onIdeClick,
                onProblemsClick = onProblemsClick,
                onRecommendationsClick = onRecommendationsClick,
                onLessonClick = onLessonClick,
                onProblemClick = onProblemClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}

/** Scrollable dashboard body containing progress, recommendations, actions, and next activities. */
@Composable
private fun DashboardContent(
    summary: DashboardSummary,
    recommendations: List<Recommendation>,
    recommendationStats: RecommendationStats?,
    isLoadingRecommendations: Boolean,
    onLearnClick: () -> Unit,
    onIdeClick: () -> Unit,
    onProblemsClick: () -> Unit,
    onRecommendationsClick: () -> Unit,
    onLessonClick: (Lesson) -> Unit,
    onProblemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = EzTechDimens.ScreenPadding,
            top = EzTechDimens.SpaceMedium,
            end = EzTechDimens.ScreenPadding,
            bottom = 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceLarge),
    ) {
        item {
            WelcomeCard(summary = summary)
        }
        item {
            ProgressOverviewCard(summary = summary)
        }
        if (recommendations.isNotEmpty() || isLoadingRecommendations) {
            item {
                RecommendationSection(
                    recommendations = recommendations,
                    stats = recommendationStats,
                    isLoading = isLoadingRecommendations,
                    onViewAllClick = onRecommendationsClick,
                    onRecommendationClick = { recommendation ->
                        recommendation.problem?.let { problem ->
                            onProblemClick(problem.id)
                        } ?: recommendation.lesson?.let(onLessonClick)
                    },
                )
            }
        }
        item {
            QuickActions(
                onLearnClick = onLearnClick,
                onIdeClick = onIdeClick,
                onProblemsClick = onProblemsClick,
            )
        }
        item {
            summary.nextLesson?.let { lesson ->
                ContinueLessonCard(
                    lesson = lesson,
                    onClick = { onLessonClick(lesson) },
                )
            } ?: CompletedCard(
                title = "Lessons complete",
                message = "You have finished every available Python lesson.",
                icon = Icons.Rounded.CheckCircle,
            )
        }
        item {
            summary.nextProblem?.let { problem ->
                NextProblemCard(
                    problem = problem,
                    onClick = { onProblemClick(problem.id) },
                )
            } ?: CompletedCard(
                title = "Problems complete",
                message = "All practice problems are solved.",
                icon = Icons.Rounded.EmojiEvents,
            )
        }
    }
}

/** Greeting card with username, current level, streak, and leaderboard rank. */
@Composable
private fun WelcomeCard(
    summary: DashboardSummary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(EzTechDimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = summary.userName.firstOrNull()
                                ?.uppercaseChar()
                                ?.toString()
                                ?: "E",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome back",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
                    )
                    Text(
                        text = summary.userName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall)) {
                SummaryChip(
                    icon = Icons.Rounded.Timeline,
                    label = "Level ${summary.level}",
                    modifier = Modifier.weight(1f),
                )
                SummaryChip(
                    icon = Icons.Rounded.LocalFireDepartment,
                    label = "${summary.currentStreak} day streak",
                    modifier = Modifier.weight(1f),
                )
                SummaryChip(
                    icon = Icons.Rounded.EmojiEvents,
                    label = if (summary.rank > 0) "#${summary.rank}" else "Unranked",
                    modifier = Modifier.weight(1f),
                )
            }
            ProgressBar(
                progress = summary.expProgressFraction,
                label = "${summary.exp % 500} / 500 EXP",
                trailing = "${summary.expToNextLevel} to next level",
            )
        }
    }
}

/** Shows lesson/problem progress and EXP toward the next level. */
@Composable
private fun ProgressOverviewCard(
    summary: DashboardSummary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(EzTechDimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceLarge),
        ) {
            Text(
                text = "Python progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            ProgressBar(
                progress = summary.lessonProgressFraction,
                label = "Lessons",
                trailing = "${summary.completedLessonCount}/${summary.totalLessonCount}",
            )
            ProgressBar(
                progress = summary.problemProgressFraction,
                label = "Problems",
                trailing = "${summary.solvedProblemCount}/${summary.totalProblemCount}",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall)) {
                MetricTile(
                    value = summary.videoLessonCount.toString(),
                    label = "Videos",
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    value = summary.tutorialLessonCount.toString(),
                    label = "Tutorials",
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    value = summary.solvedProblemCount.toString(),
                    label = "Solved",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Three large shortcuts to Learn, IDE, and Problems. */
@Composable
private fun QuickActions(
    onLearnClick: () -> Unit,
    onIdeClick: () -> Unit,
    onProblemsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
    ) {
        Text(
            text = "Quick actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall)) {
            ActionTile(
                label = "Learn",
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                onClick = onLearnClick,
                modifier = Modifier.weight(1f),
            )
            ActionTile(
                label = "IDE",
                icon = Icons.Rounded.Code,
                onClick = onIdeClick,
                modifier = Modifier.weight(1f),
            )
            ActionTile(
                label = "Problems",
                icon = Icons.AutoMirrored.Rounded.Assignment,
                onClick = onProblemsClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Continue-learning card for the next unwatched lesson. */
@Composable
private fun ContinueLessonCard(
    lesson: Lesson,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier.padding(EzTechDimens.SpaceLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (lesson.type == LessonContentType.VIDEO) {
                            Icons.Rounded.PlayArrow
                        } else {
                            Icons.AutoMirrored.Rounded.MenuBook
                        },
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall),
            ) {
                Text(
                    text = "Continue learning",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (lesson.order > 0) {
                        "${lesson.order}. ${lesson.title}"
                    } else {
                        lesson.title
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = lesson.description.ifBlank { lessonTypeLabel(lesson) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Card for the next unsolved problem in curriculum order. */
@Composable
private fun NextProblemCard(
    problem: Problem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(EzTechDimens.SpaceLarge),
            verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Next problem",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (problem.order > 0) {
                            "#${problem.order}  ${problem.title}"
                        } else {
                            problem.title
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = problem.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Code, contentDescription = null)
                Spacer(Modifier.width(EzTechDimens.SpaceSmall))
                Text("Solve now")
            }
        }
    }
}

/** Reusable completion message when all lessons/problems are finished. */
@Composable
private fun CompletedCard(
    title: String,
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier.padding(EzTechDimens.SpaceLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.tertiary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Button-like card used in the QuickActions row. */
@Composable
private fun ActionTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = EzTechDimens.SpaceMedium, horizontal = EzTechDimens.SpaceSmall),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Small statistic tile for dashboard progress numbers. */
@Composable
private fun MetricTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(EzTechDimens.SpaceMedium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Pill used inside the welcome card for level/streak/rank. */
@Composable
private fun SummaryChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = EzTechDimens.SpaceSmall,
                vertical = EzTechDimens.SpaceSmall,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(EzTechDimens.SpaceXSmall))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Linear progress bar with a stable background/foreground shape. */
@Composable
private fun ProgressBar(
    progress: Float,
    label: String,
    trailing: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

/** Centered loading spinner shown before dashboard data is available. */
@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Error state with retry action for blocking dashboard failures. */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(EzTechDimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = EzTechDimens.SpaceLarge,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        EzTechEmptyState(
            title = "Dashboard unavailable",
            message = message,
        )
        OutlinedButton(onClick = onRetry) {
            Text("Try again")
        }
    }
}

/** Human-readable label for tutorial/video lesson cards. */
private fun lessonTypeLabel(lesson: Lesson): String =
    if (lesson.type == LessonContentType.VIDEO) "Video lesson" else "Tutorial lesson"
