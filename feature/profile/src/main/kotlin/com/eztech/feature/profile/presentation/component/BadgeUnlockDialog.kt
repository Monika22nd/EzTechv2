package com.eztech.feature.profile.presentation.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eztech.core.domain.model.Badge
import com.eztech.core.ui.theme.EzTechDimens

@Composable
fun BadgeUnlockDialog(
    badge: Badge,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text("Badge unlocked") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceMedium),
            ) {
                BadgeBurst(iconText = badge.iconEmoji)
                Text(
                    text = badge.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = badge.rarity.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
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
private fun BadgeBurst(
    iconText: String,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "badgeBurst")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Restart,
        ),
        label = "badgePulse",
    )
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier.size(112.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = primary,
                radius = 32.dp.toPx() + pulse * 22.dp.toPx(),
                center = center,
                alpha = 0.12f * (1f - pulse),
            )
            repeat(12) { index ->
                val angle = (index / 12f) * 6.28318f
                val distance = 34.dp.toPx() + pulse * 22.dp.toPx()
                val x = center.x + kotlin.math.cos(angle.toDouble()).toFloat() * distance
                val y = center.y + kotlin.math.sin(angle.toDouble()).toFloat() * distance
                drawCircle(
                    color = if (index % 2 == 0) primary else tertiary,
                    radius = 3.dp.toPx(),
                    center = Offset(x, y),
                    alpha = 0.75f * (1f - pulse * 0.6f),
                )
            }
            drawCircle(
                color = primary,
                radius = 34.dp.toPx(),
                center = center,
                alpha = 0.18f,
            )
        }
        Text(
            text = iconText,
            fontSize = if (iconText.length <= 2) 34.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
    }
}
