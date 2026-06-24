package com.eztech.feature.leaderboard.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eztech.core.domain.model.LeaderboardEntry
import com.eztech.core.ui.theme.EzTechDimens

@Composable
fun LeaderboardItem(entry: LeaderboardEntry, modifier: Modifier = Modifier) {
    val bg = if (entry.isCurrentUser) MaterialTheme.colorScheme.primaryContainer
             else MaterialTheme.colorScheme.surface
    val baseMod = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(bg)
        .then(
            if (entry.isCurrentUser) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
            else Modifier
        )
        .padding(horizontal = EzTechDimens.SpaceMedium, vertical = EzTechDimens.SpaceSmall)

    Row(modifier = baseMod, verticalAlignment = Alignment.CenterVertically) {
        Text(
            "#${entry.rank}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (entry.isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(36.dp),
        )
        Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), Alignment.Center) {
            Text(entry.displayName.take(1).uppercase(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(Modifier.width(EzTechDimens.SpaceMedium))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                if (entry.isCurrentUser) {
                    Spacer(Modifier.width(6.dp))
                    Text("You", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
            Text("Lv.${entry.level} · ${entry.solvedCount} solved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${entry.totalExp}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("EXP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
