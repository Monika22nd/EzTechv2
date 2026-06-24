package com.eztech.feature.profile.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eztech.core.domain.model.Badge
import com.eztech.core.domain.model.BadgeRarity

@Composable
fun BadgeItem(badge: Badge, modifier: Modifier = Modifier) {
    val rarityColor = when (badge.rarity) {
        BadgeRarity.COMMON -> Color(0xFF78909C)
        BadgeRarity.RARE -> Color(0xFF1565C0)
        BadgeRarity.EPIC -> Color(0xFF6A1B9A)
        BadgeRarity.LEGENDARY -> Color(0xFFE65100)
    }

    Column(
        modifier = modifier
            .alpha(if (badge.unlocked) 1f else 0.4f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (badge.unlocked) 2.dp else 1.dp,
                color = if (badge.unlocked) rarityColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (badge.unlocked) badge.iconEmoji else "🔒",
            fontSize = 28.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = badge.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = if (badge.unlocked) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            maxLines = 2,
        )
        if (badge.unlocked) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = badge.rarity.label,
                style = MaterialTheme.typography.labelSmall,
                color = rarityColor,
                fontSize = 9.sp,
            )
        }
    }
}
