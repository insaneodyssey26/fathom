package com.masum.fathom.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.BookBookmark
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.Sparkle
import com.masum.fathom.NavigationItem
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun FathomBottomNavBar(
    selectedItem: NavigationItem,
    onItemSelected: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        FathomTheme.colors.background.copy(alpha = 0.20f),
                        FathomTheme.colors.background.copy(alpha = 0.60f),
                        FathomTheme.colors.background.copy(alpha = 0.90f),
                        FathomTheme.colors.background.copy(alpha = 0.98f),
                        FathomTheme.colors.background
                    )
                )
            )
            .padding(top = 40.dp)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .height(52.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(FathomTheme.colors.surface.copy(alpha = 0.92f))
                .border(
                    width = 1.dp,
                    color = FathomTheme.colors.borderSubtle.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(4.dp)
        ) {
            val segmentWidth = maxWidth / 3
            val targetOffset = when (selectedItem) {
                NavigationItem.FEED -> 0.dp
                NavigationItem.SEARCH -> segmentWidth
                NavigationItem.LIBRARY -> segmentWidth * 2
            }

            val indicatorOffset by animateDpAsState(
                targetValue = targetOffset,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "nav_indicator_glide"
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .border(
                        width = 1.dp,
                        color = FathomTheme.colors.accentGold.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(24.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    label = "Discover",
                    icon = PhosphorIcons.Regular.Sparkle,
                    isSelected = selectedItem == NavigationItem.FEED,
                    onClick = { onItemSelected(NavigationItem.FEED) },
                    modifier = Modifier.weight(1f)
                )

                NavBarItem(
                    label = "Explore",
                    icon = PhosphorIcons.Regular.MagnifyingGlass,
                    isSelected = selectedItem == NavigationItem.SEARCH,
                    onClick = { onItemSelected(NavigationItem.SEARCH) },
                    modifier = Modifier.weight(1f)
                )

                NavBarItem(
                    label = "Library",
                    icon = PhosphorIcons.Regular.BookBookmark,
                    isSelected = selectedItem == NavigationItem.LIBRARY,
                    onClick = { onItemSelected(NavigationItem.LIBRARY) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_content_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.94f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "nav_item_scale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.scale(scale),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = label.uppercase(),
                style = FathomTheme.typography.traditionBadge.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = contentColor
            )
        }
    }
}
