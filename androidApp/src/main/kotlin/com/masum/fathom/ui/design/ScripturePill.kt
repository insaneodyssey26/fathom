package com.masum.fathom.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.CaretDown
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun ScripturePill(
    traditionId: TraditionId?,
    overline: String,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .widthIn(max = 190.dp)
            .height(36.dp)
            .glass(shape, tint = colors.background.copy(alpha = 0.42f))
            .tappable(scale = 0.97f, onClick = onClick)
            .padding(start = 14.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = FathomTheme.typography.label.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Icon(PhosphorIcons.Light.CaretDown, "Choose scripture", tint = colors.textSecondary, modifier = Modifier.size(12.dp))
    }
}
