package com.eztech.feature.problems.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eztech.core.domain.model.Difficulty

@Composable
fun DifficultyBadge(
    difficulty: Difficulty,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = when (difficulty) {
        Difficulty.EASY -> Color(0xFFD7F5E4) to Color(0xFF126B3A)
        Difficulty.MEDIUM -> Color(0xFFFFEDC2) to Color(0xFF805500)
        Difficulty.HARD -> Color(0xFFFFDAD6) to Color(0xFF9C2A22)
    }
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = difficulty.name.lowercase().replaceFirstChar(Char::uppercase),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
