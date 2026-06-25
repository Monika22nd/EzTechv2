package com.eztech.feature.problems.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eztech.core.domain.model.ProblemCompletion
import com.eztech.core.ui.theme.EzTechDimens

@Composable
fun SubmissionSuccessDialog(
    completion: ProblemCompletion,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text("Problem accepted") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium)) {
                ConfettiStrip()
                Text(
                    text = if (completion.firstSolve) {
                        "+${completion.awardedExp} EXP earned"
                    } else {
                        "Already solved. No additional EXP this time."
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Level ${completion.progress.level} | " +
                        "${completion.progress.solvedCount} problems solved",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (completion.newlyUnlockedBadges.isNotEmpty()) {
                    Text(
                        text = "Badges unlocked: " + completion.newlyUnlockedBadges
                            .joinToString { badge -> badge.name },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue")
            }
        },
    )
}

@Composable
private fun ConfettiStrip(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "confettiProgress",
    )
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        Color(0xFF22C55E),
        Color(0xFFF59E0B),
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
    ) {
        val widthStep = size.width / 9f
        repeat(9) { index ->
            val x = widthStep * index + widthStep * 0.5f
            val phase = (progress + index * 0.13f) % 1f
            val y = phase * size.height
            val color = colors[index % colors.size]
            if (index % 2 == 0) {
                drawCircle(
                    color = color,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y),
                    alpha = 0.85f,
                )
            } else {
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(7.dp.toPx(), 10.dp.toPx()),
                    alpha = 0.85f,
                )
            }
        }
    }
}
