package com.masum.fathom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun TraditionBadge(
    traditionId: TraditionId,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(FathomTheme.colors.accentGold.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = FathomTheme.colors.accentGold.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = traditionId.displayName.uppercase(),
            style = FathomTheme.typography.traditionBadge,
            color = FathomTheme.colors.accentGold
        )
    }
}
