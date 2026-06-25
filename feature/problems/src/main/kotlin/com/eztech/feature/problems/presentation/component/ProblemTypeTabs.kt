package com.eztech.feature.problems.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eztech.feature.problems.presentation.model.ProblemTypeFilter

@Composable
fun ProblemTypeTabs(
    types: List<ProblemTypeFilter>,
    selectedType: String?,
    totalCount: Int,
    onTypeSelected: (String?) -> Unit,
) {
    val selectedIndex = types.indexOfFirst { type -> type.key == selectedType }
        .takeIf { index -> index >= 0 }
        ?.plus(1)
        ?: 0

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 0.dp,
    ) {
        ProblemTypeTab(
            selected = selectedType == null,
            label = "All",
            count = totalCount,
            onClick = { onTypeSelected(null) },
        )
        types.forEach { type ->
            ProblemTypeTab(
                selected = selectedType == type.key,
                label = type.label.compactProblemTypeLabel(),
                count = type.count,
                onClick = { onTypeSelected(type.key) },
            )
        }
    }
}

@Composable
private fun ProblemTypeTab(
    selected: Boolean,
    label: String,
    count: Int,
    onClick: () -> Unit,
) {
    Tab(
        selected = selected,
        onClick = onClick,
        selectedContentColor = MaterialTheme.colorScheme.primary,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        text = {
            Column {
                Text(
                    text = label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        },
    )
}

private fun String.compactProblemTypeLabel(): String = when (this) {
    "Syntax basics" -> "Syntax"
    "Variables and operators" -> "Operators"
    "For and while loops" -> "Loops"
    "Tuples, sets, and dictionaries" -> "Collections"
    "Functions practice" -> "Functions"
    else -> this
}
