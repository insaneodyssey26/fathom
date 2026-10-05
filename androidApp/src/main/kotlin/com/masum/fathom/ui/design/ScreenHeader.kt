package com.masum.fathom.ui.design

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun ScreenHeader(
    title: String,
    traditionId: TraditionId?,
    modifier: Modifier = Modifier,
    onTitleClick: (() -> Unit)? = null,
    badge: String? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    val colors = FathomTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, end = 20.dp, top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = if (onTitleClick != null) Modifier.tappable(scale = 0.97f, onClick = onTitleClick) else Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!LocalShell.current.usesRail && LocalConfiguration.current.screenWidthDp >= 380) TraditionEmblem(traditionId = traditionId, color = FathomTheme.tone.hue, size = 26.dp)
            Text(text = title, style = FathomTheme.typography.brandTitle, color = colors.textPrimary)
            if (badge != null) {
                val tone = FathomTheme.tone
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .border(0.75.dp, tone.hue.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(text = badge, style = FathomTheme.typography.caption, color = tone.hue, maxLines = 1)
                }
            }
        }
        Row(
            modifier = Modifier.weight(1f, fill = false).padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            content = trailing
        )
    }
}
