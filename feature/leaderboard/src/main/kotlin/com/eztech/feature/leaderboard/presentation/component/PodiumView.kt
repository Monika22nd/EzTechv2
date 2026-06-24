package com.eztech.feature.leaderboard.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eztech.core.domain.model.LeaderboardEntry
import com.eztech.core.ui.theme.EzTechDimens

private val GoldColor = Color(0xFFFFD700)
private val SilverColor = Color(0xFFC0C0C0)
private val BronzeColor = Color(0xFFCD7F32)

@Composable
fun PodiumView(top3: List<LeaderboardEntry>, modifier: Modifier = Modifier) {
    if (top3.isEmpty()) return
    val first = top3.getOrNull(0)
    val second = top3.getOrNull(1)
    val third = top3.getOrNull(2)

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = EzTechDimens.SpaceLarge),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (second != null) PodiumColumn(second, "🥈", 70.dp, SilverColor, Modifier.weight(1f))
        else Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        if (first != null) PodiumColumn(first, "🥇", 100.dp, GoldColor, Modifier.weight(1f))
        else Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        if (third != null) PodiumColumn(third, "🥉", 50.dp, BronzeColor, Modifier.weight(1f))
        else Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun PodiumColumn(
    entry: LeaderboardEntry, medal: String, podiumHeight: Dp, podiumColor: Color, modifier: Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
        Text(medal, fontSize = 24.sp)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.size(48.dp).clip(CircleShape).background(podiumColor.copy(alpha = 0.3f)), Alignment.Center) {
            Text(entry.displayName.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = podiumColor)
        }
        Spacer(Modifier.height(4.dp))
        Text(entry.displayName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
        Text("${entry.totalExp} EXP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(podiumHeight).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(podiumColor.copy(alpha = 0.85f)), Alignment.Center) {
            Text("#${entry.rank}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
