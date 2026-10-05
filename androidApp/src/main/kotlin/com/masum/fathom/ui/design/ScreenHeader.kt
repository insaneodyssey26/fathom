package com.masum.fathom.ui.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun ScreenHeader(
    title: String,
    traditionId: TraditionId?,
    modifier: Modifier = Modifier,
    onTitleClick: (() -> Unit)? = null,
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
            if (!LocalShell.current.usesRail) TraditionEmblem(traditionId = traditionId, color = FathomTheme.tone.hue, size = 26.dp)
            Text(text = title, style = FathomTheme.typography.brandTitle, color = colors.textPrimary)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = trailing
        )
    }
}
