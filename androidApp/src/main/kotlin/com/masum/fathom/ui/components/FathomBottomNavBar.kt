package com.masum.fathom.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.fill.BookOpenText as BookOpenTextFill
import com.adamglin.phosphoricons.fill.MagnifyingGlass as MagnifyingGlassFill
import com.adamglin.phosphoricons.fill.Notebook as NotebookFill
import com.adamglin.phosphoricons.fill.Path as PathFill
import com.adamglin.phosphoricons.light.BookOpenText
import com.adamglin.phosphoricons.light.MagnifyingGlass
import com.adamglin.phosphoricons.light.Notebook
import com.adamglin.phosphoricons.light.Path
import com.masum.fathom.NavigationItem
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.design.TraditionEmblem
import com.masum.fathom.ui.design.glass
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.rememberTone

private data class DockEntry(
    val item: NavigationItem,
    val label: String,
    val icon: ImageVector,
    val iconActive: ImageVector
)

private val entries = listOf(
    DockEntry(NavigationItem.FEED, "Read", PhosphorIcons.Light.BookOpenText, PhosphorIcons.Fill.BookOpenTextFill),
    DockEntry(NavigationItem.PATHWAYS, "Paths", PhosphorIcons.Light.Path, PhosphorIcons.Fill.PathFill),
    DockEntry(NavigationItem.SEARCH, "Explore", PhosphorIcons.Light.MagnifyingGlass, PhosphorIcons.Fill.MagnifyingGlassFill),
    DockEntry(NavigationItem.LIBRARY, "Journal", PhosphorIcons.Light.Notebook, PhosphorIcons.Fill.NotebookFill)
)

val DockBarHeight = 72.dp
val DockRailWidth = 84.dp

@Composable
fun FathomBottomNavBar(
    selectedItem: NavigationItem,
    onItemSelected: (NavigationItem) -> Unit,
    traditionId: TraditionId?,
    modifier: Modifier = Modifier,
    vertical: Boolean = false
) {
    val haptics = LocalFathomHaptics.current
    val tone = rememberTone(traditionId)
    val selectedIndex = entries.indexOfFirst { it.item == selectedItem }.coerceAtLeast(0)
    val marker = remember { Animatable(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) {
        marker.animateTo(
            targetValue = selectedIndex.toFloat(),
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 320f)
        )
    }

    val select: (Int) -> Unit = { idx ->
        if (entries[idx].item != selectedItem) {
            haptics.light()
            onItemSelected(entries[idx].item)
        }
    }

    if (vertical) {
        DockRail(
            selectedIndex = selectedIndex,
            marker = { marker.value },
            hue = tone.hue,
            traditionId = traditionId,
            onSelect = select,
            modifier = modifier
        )
    } else {
        DockBar(
            selectedIndex = selectedIndex,
            marker = { marker.value },
            hue = tone.hue,
            onSelect = select,
            modifier = modifier
        )
    }
}

@Composable
private fun DockBar(
    selectedIndex: Int,
    marker: () -> Float,
    hue: Color,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val density = LocalDensity.current
    val capsuleShape = RoundedCornerShape(32.dp)
    val edgeColor = if (colors.isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    val innerGlow = if (colors.isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.40f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    elevation = if (colors.isDark) 16.dp else 12.dp,
                    shape = capsuleShape,
                    ambientColor = if (colors.isDark) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.12f),
                    spotColor = hue.copy(alpha = 0.20f)
                )
                .glass(
                    shape = capsuleShape,
                    tint = colors.background.copy(alpha = if (colors.isDark) 0.74f else 0.84f),
                    blur = 28.dp,
                    outline = false
                )
                .border(width = 0.75.dp, color = edgeColor, shape = capsuleShape)
                .drawBehind {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(innerGlow, Color.Transparent),
                            startY = 0f,
                            endY = size.height * 0.5f
                        ),
                        cornerRadius = CornerRadius(32.dp.toPx(), 32.dp.toPx())
                    )
                }
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp)
            ) {
                val totalWidth = maxWidth
                val cellWidth = totalWidth / entries.size
                val pillWidth = (cellWidth - 6.dp).coerceAtMost(74.dp)
                val pillHeight = 46.dp
                val pillOffsetX = (cellWidth * marker()) + (cellWidth - pillWidth) / 2
                val pillOffsetY = (maxHeight - pillHeight) / 2

                val pillWidthPx = with(density) { pillWidth.toPx() }
                val pillHeightPx = with(density) { pillHeight.toPx() }

                Box(
                    modifier = Modifier
                        .offset(x = pillOffsetX, y = pillOffsetY)
                        .size(width = pillWidth, height = pillHeight)
                        .clip(RoundedCornerShape(23.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    hue.copy(alpha = if (colors.isDark) 0.18f else 0.14f),
                                    hue.copy(alpha = if (colors.isDark) 0.08f else 0.05f)
                                ),
                                center = Offset(pillWidthPx / 2f, pillHeightPx / 2f),
                                radius = pillWidthPx * 0.8f
                            )
                        )
                        .border(
                            width = 0.75.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    hue.copy(alpha = if (colors.isDark) 0.38f else 0.26f),
                                    hue.copy(alpha = if (colors.isDark) 0.14f else 0.08f)
                                )
                            ),
                            shape = RoundedCornerShape(23.dp)
                        )
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    entries.forEachIndexed { index, entry ->
                        DockCell(
                            entry = entry,
                            active = index == selectedIndex,
                            hue = hue,
                            onClick = { onSelect(index) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DockRail(
    selectedIndex: Int,
    marker: () -> Float,
    hue: Color,
    traditionId: TraditionId?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val density = LocalDensity.current
    val leftInset = WindowInsets.systemBars
        .union(WindowInsets.displayCutout)
        .asPaddingValues()
        .calculateLeftPadding(LayoutDirection.Ltr)
    val railShape = RoundedCornerShape(32.dp)
    val edgeColor = if (colors.isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    val cellHeight = 64.dp
    val groupHeight = cellHeight * entries.size

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(DockRailWidth + leftInset)
            .padding(start = leftInset + 12.dp, top = 20.dp, bottom = 20.dp, end = 12.dp)
            .shadow(
                elevation = 16.dp,
                shape = railShape,
                ambientColor = if (colors.isDark) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.12f),
                spotColor = hue.copy(alpha = 0.20f)
            )
            .glass(
                shape = railShape,
                tint = colors.background.copy(alpha = if (colors.isDark) 0.74f else 0.84f),
                blur = 28.dp,
                outline = false
            )
            .border(width = 0.75.dp, color = edgeColor, shape = railShape)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            TraditionEmblem(traditionId = traditionId, color = hue, size = 26.dp)
        }

        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.Center)
                .height(groupHeight)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            val pillWidth = 52.dp
            val pillHeight = 52.dp
            val pillOffsetY = (cellHeight * marker()) + (cellHeight - pillHeight) / 2
            val pillOffsetX = (maxWidth - pillWidth) / 2

            Box(
                modifier = Modifier
                    .offset(x = pillOffsetX, y = pillOffsetY)
                    .size(width = pillWidth, height = pillHeight)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                hue.copy(alpha = if (colors.isDark) 0.18f else 0.14f),
                                hue.copy(alpha = if (colors.isDark) 0.08f else 0.05f)
                            )
                        )
                    )
                    .border(
                        width = 0.75.dp,
                        color = hue.copy(alpha = if (colors.isDark) 0.38f else 0.26f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                entries.forEachIndexed { index, entry ->
                    DockCell(
                        entry = entry,
                        active = index == selectedIndex,
                        hue = hue,
                        onClick = { onSelect(index) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cellHeight)
                    )
                }
            }
        }
    }
}

@Composable
private fun DockCell(
    entry: DockEntry,
    active: Boolean,
    hue: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val iconTint by animateColorAsState(
        targetValue = if (active) hue else colors.textTertiary.copy(alpha = 0.80f),
        animationSpec = spring(stiffness = 500f),
        label = "dock_icon_tint"
    )
    val labelColor by animateColorAsState(
        targetValue = if (active) colors.textPrimary else colors.textTertiary.copy(alpha = 0.70f),
        animationSpec = spring(stiffness = 500f),
        label = "dock_label_color"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (active) 1.06f else 1.0f,
        animationSpec = FathomMotion.bounce(),
        label = "dock_icon_scale"
    )
    val pipAlpha by animateFloatAsState(
        targetValue = if (active) 1.0f else 0f,
        animationSpec = spring(stiffness = 450f),
        label = "dock_pip_alpha"
    )

    Column(
        modifier = modifier.tappable(
            scale = 0.93f,
            haptic = !active,
            role = Role.Tab,
            onClick = { if (!active) onClick() }
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (active) entry.iconActive else entry.icon,
            contentDescription = entry.label,
            tint = iconTint,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                }
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = entry.label,
            style = FathomTheme.typography.caption.copy(
                fontSize = 10.5.sp,
                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                letterSpacing = 0.3.sp
            ),
            color = labelColor
        )

        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(width = 8.dp, height = 2.dp)
                .graphicsLayer { alpha = pipAlpha }
                .clip(RoundedCornerShape(1.dp))
                .background(hue)
        )
    }
}
