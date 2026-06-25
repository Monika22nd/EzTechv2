package com.eztech.feature.problems.presentation.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.ui.theme.EzTechDimens
import com.eztech.feature.problems.presentation.list.ProblemSortOption
import com.eztech.feature.problems.presentation.model.ProblemTypeFilter

@Composable
fun ProblemFilterRow(
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty?) -> Unit,
    availableProblemTypes: List<ProblemTypeFilter>,
    selectedProblemType: String?,
    onProblemTypeSelected: (String?) -> Unit,
    sortOption: ProblemSortOption,
    onSortOptionSelected: (ProblemSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
        ) {
            FilterChip(
                selected = selectedDifficulty == null,
                onClick = { onDifficultySelected(null) },
                label = { Text("All") },
            )
            Difficulty.entries.forEach { difficulty ->
                FilterChip(
                    selected = selectedDifficulty == difficulty,
                    onClick = { onDifficultySelected(difficulty) },
                    label = {
                        Text(difficulty.name.lowercase().replaceFirstChar(Char::uppercase))
                    },
                )
            }
        }

        if (availableProblemTypes.isNotEmpty()) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
            ) {
                FilterChip(
                    selected = selectedProblemType == null,
                    onClick = { onProblemTypeSelected(null) },
                    label = { Text("All types") },
                )
                availableProblemTypes.take(MAX_VISIBLE_TYPES).forEach { type ->
                    FilterChip(
                        selected = selectedProblemType == type.key,
                        onClick = { onProblemTypeSelected(type.key) },
                        label = { Text(type.displayLabel) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
        ) {
            ProblemSortOption.entries.forEach { option ->
                FilterChip(
                    selected = sortOption == option,
                    onClick = { onSortOptionSelected(option) },
                    label = { Text(option.label) },
                )
            }
        }
    }
}

private const val MAX_VISIBLE_TYPES = 16
