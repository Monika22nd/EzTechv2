package com.eztech.feature.problems.presentation.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eztech.core.domain.model.Difficulty
import com.eztech.core.ui.theme.EzTechDimens

@Composable
fun ProblemFilterRow(
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
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
}
