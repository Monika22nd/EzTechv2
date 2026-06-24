package com.eztech.feature.learn.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eztech.core.domain.model.LessonCategory
import com.eztech.core.ui.theme.EzTechDimens

@Composable
internal fun CategoryItem(
    category: LessonCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visual = categoryVisual(category.id)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 184.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp),
                color = visual.containerColor(),
            ) {
                Box(
                    modifier = Modifier.padding(EzTechDimens.SpaceMedium),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = visual.contentColor(),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = EzTechDimens.SpaceMedium,
                        end = EzTechDimens.SpaceMedium,
                        bottom = EzTechDimens.SpaceMedium,
                    ),
                verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${category.lessonCount} lessons",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class CategoryVisual(
    val icon: ImageVector,
    val containerColor: @Composable () -> Color,
    val contentColor: @Composable () -> Color,
)

private fun categoryVisual(categoryId: String): CategoryVisual = when {
    "data_types" in categoryId -> CategoryVisual(
        icon = Icons.Rounded.DataObject,
        containerColor = { MaterialTheme.colorScheme.secondaryContainer },
        contentColor = { MaterialTheme.colorScheme.onSecondaryContainer },
    )

    "functions" in categoryId -> CategoryVisual(
        icon = Icons.Rounded.Functions,
        containerColor = { MaterialTheme.colorScheme.tertiaryContainer },
        contentColor = { MaterialTheme.colorScheme.onTertiaryContainer },
    )

    else -> CategoryVisual(
        icon = Icons.Rounded.RocketLaunch,
        containerColor = { MaterialTheme.colorScheme.primaryContainer },
        contentColor = { MaterialTheme.colorScheme.onPrimaryContainer },
    )
}
