package com.eztech.feature.home.presentation.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eztech.core.domain.model.Recommendation
import com.eztech.core.domain.model.RecommendationMetric
import com.eztech.core.domain.model.RecommendationStats
import com.eztech.core.domain.model.RecommendationType
import com.eztech.core.ui.theme.EzTechDimens

/**
 * Compact recommendation block used on the Home dashboard.
 *
 * It shows the learning stats card first, then a horizontal list of recommendation cards. The same
 * card component is reused by the dedicated Recommendations page for visual consistency.
 */
@Composable
fun RecommendationSection(
    recommendations: List<Recommendation>,
    stats: RecommendationStats?,
    isLoading: Boolean,
    onRecommendationClick: (Recommendation) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
        ) {
            Icon(
                imageVector = Icons.Rounded.Timeline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = "Recommended for you",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onViewAllClick) {
                Text("View all")
            }
        }
        stats?.let { recommendationStats ->
            RecommendationStatsCard(stats = recommendationStats)
        }

        when {
            isLoading -> RecommendationLoadingCard()
            recommendations.isNotEmpty() -> LazyRow(
                horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
            ) {
                items(recommendations, key = Recommendation::id) { recommendation ->
                    RecommendationCard(
                        recommendation = recommendation,
                        onClick = { onRecommendationClick(recommendation) },
                        modifier = Modifier.width(284.dp),
                    )
                }
            }
        }
    }
}

/** Card for one recommended problem or lesson, including explanation text and metric chips. */
@Composable
fun RecommendationCard(
    recommendation: Recommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(EzTechDimens.SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
            ) {
                RecommendationIcon(type = recommendation.type)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recommendation.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = recommendation.subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (recommendation.sequenceLabel.isNotBlank() || recommendation.stageLabel.isNotBlank()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall),
                ) {
                    if (recommendation.sequenceLabel.isNotBlank()) {
                        MetricChip(
                            metric = RecommendationMetric(
                                label = "Order",
                                value = recommendation.sequenceLabel,
                            ),
                        )
                    }
                    if (recommendation.stageLabel.isNotBlank()) {
                        MetricChip(
                            metric = RecommendationMetric(
                                label = "Stage",
                                value = recommendation.stageLabel,
                            ),
                        )
                    }
                }
            }
            Text(
                text = recommendation.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (recommendation.metrics.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall),
                ) {
                    recommendation.metrics.take(3).forEach { metric ->
                        MetricChip(metric = metric)
                    }
                }
            }
        }
    }
}

/** Summary card showing the statistics that drive recommendation decisions. */
@Composable
fun RecommendationStatsCard(
    stats: RecommendationStats,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(EzTechDimens.SpaceMedium),
            verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
        ) {
            Text(
                text = "Learning signal",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall),
            ) {
                MetricChip(RecommendationMetric("Solved", "${stats.solvedProblems}/${stats.totalProblems}"))
                MetricChip(RecommendationMetric("Progress", "${stats.progressPercent}%"))
                MetricChip(RecommendationMetric("Stage", stats.currentStage))
                MetricChip(RecommendationMetric("Stage solved", stats.stageProgressText))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceXSmall),
            ) {
                MetricChip(
                    RecommendationMetric(
                        "Next",
                        stats.nextDifficulty.name.lowercase().replaceFirstChar(Char::uppercase),
                    ),
                )
                MetricChip(RecommendationMetric("Weak area", stats.weakestArea))
                MetricChip(RecommendationMetric("Easy", stats.solvedEasy.toString()))
                MetricChip(RecommendationMetric("Medium", stats.solvedMedium.toString()))
                MetricChip(RecommendationMetric("Hard", stats.solvedHard.toString()))
            }
        }
    }
}

/** Small horizontal metric chip used for solved counts, current stage, and difficulty signals. */
@Composable
private fun MetricChip(
    metric: RecommendationMetric,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = EzTechDimens.SpaceSmall,
                vertical = EzTechDimens.SpaceXSmall,
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

/** Chooses the card icon based on whether the recommendation opens a problem or lesson. */
@Composable
private fun RecommendationIcon(type: RecommendationType) {
    val icon = when (type) {
        RecommendationType.PROBLEM -> Icons.AutoMirrored.Rounded.Assignment
        RecommendationType.LESSON -> Icons.AutoMirrored.Rounded.MenuBook
    }
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/** Placeholder card while the recommendation stream is still loading. */
@Composable
private fun RecommendationLoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier.padding(EzTechDimens.SpaceMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Text(
                text = "Preparing recommendations",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
