package com.masum.fathom.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.FeedMode
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.ReadingTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraditionFilterSheet(
    selectedTraditions: Set<TraditionId>,
    activeMode: FeedMode,
    activeTheme: ReadingTheme,
    onToggleTradition: (TraditionId) -> Unit,
    onChangeMode: (FeedMode) -> Unit,
    onChangeTheme: (ReadingTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FathomTheme.colors.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "READING TRADITIONS",
                style = FathomTheme.typography.canonicalHeader,
                color = FathomTheme.colors.accentGold
            )
            Spacer(modifier = Modifier.height(12.dp))

            TraditionId.entries.forEach { tradition ->
                val isSelected = tradition in selectedTraditions
                TraditionOptionRow(
                    tradition = tradition,
                    isSelected = isSelected,
                    onToggle = { onToggleTradition(tradition) }
                )
            }

            SacredDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "ATMOSPHERE & THEME",
                style = FathomTheme.typography.canonicalHeader,
                color = FathomTheme.colors.accentGold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReadingTheme.entries.forEach { theme ->
                    val isCurrent = theme == activeTheme
                    ThemeOptionButton(
                        theme = theme,
                        isCurrent = isCurrent,
                        onClick = { onChangeTheme(theme) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TraditionOptionRow(
    tradition: TraditionId,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val rowScale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.985f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "tradition_scale"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) FathomTheme.colors.textPrimary else FathomTheme.colors.textTertiary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tradition_text_color"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(rowScale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle
            )
            .padding(vertical = 11.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = tradition.displayName,
            style = FathomTheme.typography.commentaryBody,
            color = textColor
        )

        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                    scaleIn(
                        initialScale = 0.6f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                    ),
                exit = fadeOut() + scaleOut(targetScale = 0.6f)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = FathomTheme.colors.accentGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionButton(
    theme: ReadingTheme,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isCurrent) 1.02f else 0.97f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "theme_scale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isCurrent) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "theme_border"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                when (theme) {
                    ReadingTheme.OBSIDIAN -> com.masum.fathom.ui.theme.ObsidianColors.background
                    ReadingTheme.SEPIA -> com.masum.fathom.ui.theme.SepiaColors.background
                    ReadingTheme.SLATE -> com.masum.fathom.ui.theme.SlateColors.background
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = theme.name,
            style = FathomTheme.typography.traditionBadge.copy(fontSize = 11.sp),
            color = when (theme) {
                ReadingTheme.OBSIDIAN -> com.masum.fathom.ui.theme.ObsidianColors.textPrimary
                ReadingTheme.SEPIA -> com.masum.fathom.ui.theme.SepiaColors.textPrimary
                ReadingTheme.SLATE -> com.masum.fathom.ui.theme.SlateColors.textPrimary
            }
        )
    }
}
