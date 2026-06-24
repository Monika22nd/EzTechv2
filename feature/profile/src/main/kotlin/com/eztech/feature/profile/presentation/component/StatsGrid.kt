package com.eztech.feature.profile.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eztech.core.domain.model.User
import com.eztech.core.ui.theme.EzTechDimens

@Composable
fun StatsGrid(user: User, rank: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EzTechDimens.SpaceSmall),
    ) {
        StatCard(value = "${user.solvedCount}", label = "Solved", modifier = Modifier.weight(1f))
        StatCard(value = "${user.exp}", label = "EXP", modifier = Modifier.weight(1f))
        StatCard(value = "${user.currentStreak}🔥", label = "Streak", modifier = Modifier.weight(1f))
        if (rank > 0) StatCard(value = "#$rank", label = "Rank", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(vertical = EzTechDimens.SpaceMedium, horizontal = EzTechDimens.SpaceSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
    }
}
