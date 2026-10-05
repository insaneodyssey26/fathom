package com.masum.fathom.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
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
import kotlin.math.abs
import kotlin.math.exp

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

val DockBarHeight = 66.dp
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
    var extent by remember { mutableIntStateOf(0) }
    var dragIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(selectedIndex) {
        marker.animateTo(selectedIndex.toFloat(), spring(dampingRatio = 0.68f, stiffness = 380f))
    }

    val select: (Int) -> Unit = { idx ->
        if (entries[idx].item != selectedItem) onItemSelected(entries[idx].item)
    }

    if (vertical) {
        DockRail(
            selectedIndex = selectedIndex,
            marker = { marker.value },
            hue = tone.hue,
            traditionId = traditionId,
            onSelect = select,
            onExtent = { extent = it },
            extent = extent,
            dragIndex = dragIndex,
            onDrag = { dragIndex = it },
            haptics = { haptics.light() },
            modifier = modifier
        )
    } else {
        DockBar(
            selectedIndex = selectedIndex,
            marker = { marker.value },
            hue = tone.hue,
            onSelect = select,
            onExtent = { extent = it },
            dragIndex = dragIndex,
            onDrag = { dragIndex = it },
            haptics = { haptics.light() },
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
    onExtent: (Int) -> Unit,
    dragIndex: Int,
    onDrag: (Int) -> Unit,
    haptics: () -> Unit,
    modifier: Modifier
) {
    val colors = FathomTheme.colors
    val shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
    val edge = if (colors.isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    val ink = colors.textPrimary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glass(
                shape = shape,
                tint = colors.background.copy(alpha = if (colors.isDark) 0.72f else 0.80f),
                blur = 26.dp,
                outline = false
            )
            .drawBehind {
                val cell = size.width / entries.size
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(hue.copy(alpha = 0.20f), Color.Transparent),
                        center = Offset((marker() + 0.5f) * cell, 0f),
                        radius = cell * 0.95f
                    )
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(DockBarHeight)
                .onSizeChanged { onExtent(it.width) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onDrag(-1) },
                        onDragEnd = { onDrag(-1) },
                        onDragCancel = { onDrag(-1) }
                    ) { change, _ ->
                        val idx = (change.position.x / size.width * entries.size).toInt().coerceIn(0, entries.lastIndex)
                        if (idx != dragIndex) {
                            onDrag(idx)
                            if (idx != selectedIndex) {
                                haptics()
                                onSelect(idx)
                            }
                        }
                    }
                }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val cell = size.width / entries.size
                val x = (marker() + 0.5f) * cell
                drawLine(edge, Offset(26.dp.toPx(), 0.4f), Offset(size.width - 26.dp.toPx(), 0.4f), strokeWidth = 0.75.dp.toPx())
                val pitch = 7.dp.toPx()
                val reach = 46.dp.toPx()
                var i = 0
                var tx = pitch
                while (tx < size.width - pitch) {
                    val d = abs(tx - x) / reach
                    val swell = exp(-(d * d).toDouble()).toFloat()
                    val long = i % 5 == 0
                    val base = if (long) 6.dp.toPx() else 3.dp.toPx()
                    val length = base + swell * 11.dp.toPx()
                    val color = Color(
                        red = ink.red * 0.5f * (1f - swell) + hue.red * swell,
                        green = ink.green * 0.5f * (1f - swell) + hue.green * swell,
                        blue = ink.blue * 0.5f * (1f - swell) + hue.blue * swell,
                        alpha = (0.10f + (if (long) 0.12f else 0f)) * (1f - swell) + swell * 0.95f
                    )
                    drawLine(color, Offset(tx, 1.dp.toPx()), Offset(tx, 1.dp.toPx() + length), strokeWidth = (1f + swell * 1.1f).dp.toPx(), cap = StrokeCap.Round)
                    tx += pitch
                    i++
                }
            }

            Row(Modifier.fillMaxSize().padding(top = 8.dp)) {
                entries.forEachIndexed { index, entry ->
                    DockCell(
                        entry = entry,
                        active = index == selectedIndex,
                        hue = hue,
                        onClick = { onSelect(index) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }
        Box(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun DockRail(
    selectedIndex: Int,
    marker: () -> Float,
    hue: Color,
    traditionId: TraditionId?,
    onSelect: (Int) -> Unit,
    onExtent: (Int) -> Unit,
    extent: Int,
    dragIndex: Int,
    onDrag: (Int) -> Unit,
    haptics: () -> Unit,
    modifier: Modifier
) {
    val colors = FathomTheme.colors
    val density = LocalDensity.current
    val leftInset = WindowInsets.systemBars.union(WindowInsets.displayCutout).asPaddingValues().calculateLeftPadding(LayoutDirection.Ltr)
    val shape = RoundedCornerShape(topEnd = 26.dp, bottomEnd = 26.dp)
    val edge = if (colors.isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    val ink = colors.textPrimary
    val railHeight = with(density) { extent.toDp() }
    val compact = railHeight in 1.dp..479.dp
    val cellHeight = if (compact) 62.dp else 76.dp
    val groupHeight = cellHeight * entries.size

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(DockRailWidth + leftInset)
            .glass(
                shape = shape,
                tint = colors.background.copy(alpha = if (colors.isDark) 0.72f else 0.80f),
                blur = 26.dp,
                outline = false
            )
            .onSizeChanged { onExtent(it.height) }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cell = with(density) { cellHeight.toPx() }
            val top = (size.height - cell * entries.size) / 2f
            val y = top + (marker() + 0.5f) * cell
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(hue.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(size.width, y),
                    radius = cell * 0.95f
                ),
                size = size
            )
            drawLine(edge, Offset(size.width - 0.4f, 26.dp.toPx()), Offset(size.width - 0.4f, size.height - 26.dp.toPx()), strokeWidth = 0.75.dp.toPx())
            val pitch = 7.dp.toPx()
            val reach = 46.dp.toPx()
            var i = 0
            var ty = pitch
            while (ty < size.height - pitch) {
                val d = abs(ty - y) / reach
                val swell = exp(-(d * d).toDouble()).toFloat()
                val long = i % 5 == 0
                val base = if (long) 6.dp.toPx() else 3.dp.toPx()
                val length = base + swell * 11.dp.toPx()
                val color = Color(
                    red = ink.red * 0.5f * (1f - swell) + hue.red * swell,
                    green = ink.green * 0.5f * (1f - swell) + hue.green * swell,
                    blue = ink.blue * 0.5f * (1f - swell) + hue.blue * swell,
                    alpha = (0.10f + (if (long) 0.12f else 0f)) * (1f - swell) + swell * 0.95f
                )
                drawLine(color, Offset(size.width - 1.dp.toPx(), ty), Offset(size.width - 1.dp.toPx() - length, ty), strokeWidth = (1f + swell * 1.1f).dp.toPx(), cap = StrokeCap.Round)
                ty += pitch
                i++
            }
        }

        if (!compact) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 22.dp, start = leftInset)
            ) {
                TraditionEmblem(traditionId = traditionId, color = hue, size = 28.dp)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = leftInset, end = 4.dp)
                .height(groupHeight)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { onDrag(-1) },
                        onDragEnd = { onDrag(-1) },
                        onDragCancel = { onDrag(-1) }
                    ) { change, _ ->
                        val idx = (change.position.y / size.height * entries.size).toInt().coerceIn(0, entries.lastIndex)
                        if (idx != dragIndex) {
                            onDrag(idx)
                            if (idx != selectedIndex) {
                                haptics()
                                onSelect(idx)
                            }
                        }
                    }
                },
            verticalArrangement = Arrangement.Center
        ) {
            entries.forEachIndexed { index, entry ->
                DockCell(
                    entry = entry,
                    active = index == selectedIndex,
                    hue = hue,
                    onClick = { onSelect(index) },
                    modifier = Modifier.fillMaxWidth().height(cellHeight)
                )
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
    modifier: Modifier
) {
    val colors = FathomTheme.colors
    val tint by animateColorAsState(
        if (active) hue else colors.textSecondary,
        spring(stiffness = 500f),
        label = "dock_tint"
    )
    val label by animateColorAsState(
        if (active) colors.textPrimary else colors.textTertiary,
        spring(stiffness = 500f),
        label = "dock_label"
    )
    val lift by animateDpAsState(if (active) (-2).dp else 0.dp, FathomMotion.bounce(), label = "dock_lift")

    Column(
        modifier = modifier.tappable(scale = 0.9f, haptic = !active, role = Role.Tab, onClick = { if (!active) onClick() }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = if (active) entry.iconActive else entry.icon,
            contentDescription = entry.label,
            tint = tint,
            modifier = Modifier.size(24.dp).offset(y = lift)
        )
        Text(
            text = entry.label,
            style = FathomTheme.typography.caption.copy(fontSize = 11.sp),
            color = label
        )
    }
}
