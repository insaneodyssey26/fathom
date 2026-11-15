package com.masum.fathom.ui.library

import androidx.compose.foundation.layout.heightIn
import com.adamglin.phosphoricons.light.Check
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.itemsIndexed
import com.masum.fathom.ui.design.StaggerItem
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowRight
import com.adamglin.phosphoricons.light.ArrowUpRight
import com.adamglin.phosphoricons.light.PencilSimpleLine
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.subscription.LocalOpenPaywall
import com.masum.fathom.core.subscription.LocalSubscriptionState
import com.masum.fathom.ui.design.Glyph
import com.masum.fathom.ui.design.GlyphKind
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.LocalShell
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.TraditionEmblem
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.search.SectionHeading
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.ScriptureFamily
import com.masum.fathom.ui.theme.toneFor
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun JournalHub(
    state: LibraryUiState.Success,
    listState: LazyListState,
    topPadding: androidx.compose.ui.unit.Dp,
    onOpenSaved: () -> Unit,
    onOpenReflections: () -> Unit,
    onOpenCanon: (TraditionId) -> Unit,
    onOpenTimeline: () -> Unit,
    onOpenWhatsNew: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenPassage: (com.masum.fathom.core.model.Passage) -> Unit
) {
    val shell = LocalShell.current
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val subscription = LocalSubscriptionState.current
    val openPaywall = LocalOpenPaywall.current
    val context = LocalContext.current

    val today = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE · d MMMM", Locale.getDefault())) }
    val versionName = remember(context) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }
        }.getOrNull().orEmpty()
    }
    val perTradition = remember(state.history) {
        state.history.mapNotNull { it.passage?.traditionId }.groupingBy { it }.eachCount()
    }
    val busiest = perTradition.values.maxOrNull() ?: 1

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxHeight().widthIn(max = shell.contentMaxWidth + 96.dp).padding(start = shell.startInset),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = topPadding + 24.dp, bottom = shell.bottomInset + 40.dp)
        ) {
            item(key = "streak") { StaggerItem(0) {
                val hasAnimatedChart = rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
                var streakTarget by remember { androidx.compose.runtime.mutableIntStateOf(if (hasAnimatedChart.value) state.currentStreakDays else 0) }
                LaunchedEffect(state.currentStreakDays) {
                    streakTarget = state.currentStreakDays
                    hasAnimatedChart.value = true
                }
                val shownStreak by androidx.compose.animation.core.animateIntAsState(streakTarget, tween(600, 80, FathomMotion.Emphasized), label = "streak_count")
                val readToday = state.weeklyHeatmap.lastOrNull { it.isToday }?.readCount?.let { it > 0 } == true
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = today,
                            style = typography.title.copy(fontSize = 17.sp),
                            color = colors.textSecondary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Row(
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .clip(RoundedCornerShape(50))
                                .border(0.75.dp, colors.hairline, RoundedCornerShape(50))
                                .tappable(scale = 0.96f, onClick = onEditProfile)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = state.userName, style = typography.label.copy(fontSize = 15.sp), color = colors.textPrimary, maxLines = 1)
                            Icon(PhosphorIcons.Light.PencilSimpleLine, "Edit profile", tint = tone.hue, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(28.dp))
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = shownStreak.toString(),
                            style = typography.displayLarge.copy(fontSize = 108.sp, lineHeight = 100.sp),
                            color = colors.textPrimary
                        )
                        Column(modifier = Modifier.padding(bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = if (state.currentStreakDays == 1) "day" else "days",
                                style = typography.headline.copy(fontSize = 24.sp, lineHeight = 26.sp),
                                color = colors.textPrimary
                            )
                            MonoLabel(text = "in a row", color = tone.hue)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = when {
                            state.currentStreakDays == 0 -> "Read a verse today to begin your streak."
                            readToday -> "You have read today. Come back tomorrow."
                            else -> "Read a verse today to keep it going."
                        },
                        style = typography.commentaryBody,
                        color = colors.textSecondary
                    )
                    Spacer(Modifier.height(28.dp))
                    WeekDots(days = state.weeklyHeatmap)
                    Spacer(Modifier.height(28.dp))
                    Hairline()
                    Row(Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
                        LedgerCell(value = state.totalReadCount.toString(), label = "Readings", modifier = Modifier.weight(1f))
                        LedgerCell(value = state.history.size.toString(), label = "Verses", modifier = Modifier.weight(1f))
                        LedgerCell(value = state.bookmarks.size.toString(), label = "Saved", modifier = Modifier.weight(1f))
                    }
                    Hairline()
                }
             }}

            item(key = "shelf") { StaggerItem(1) {
                Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ShelfTile(label = "Saved", count = state.bookmarks.size, onClick = onOpenSaved, modifier = Modifier.weight(1f))
                    ShelfTile(label = "Reflections", count = state.notes.size, onClick = onOpenReflections, modifier = Modifier.weight(1f))
                }
             }}

            item(key = "calendar") { StaggerItem(2) {
                ReadingCalendar(history = state.history, modifier = Modifier.padding(top = 36.dp))
             }}

            if (!subscription.isPro) {
                item(key = "upgrade") { StaggerItem(6) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.linearGradient(listOf(tone.hue.copy(alpha = 0.22f), tone.hue.copy(alpha = 0.06f))))
                            .border(0.75.dp, tone.hue.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                            .tappable(scale = 0.98f, onClick = openPaywall)
                            .padding(22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(tone.hue.copy(alpha = 0.18f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "FATHOM PLUS",
                                    style = typography.caption.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = tone.hue
                                )
                            }
                            Text(text = "Unlimited reflections, narration and themes", style = typography.title, color = colors.textPrimary)
                        }
                        Icon(PhosphorIcons.Light.ArrowRight, null, tint = tone.hue, modifier = Modifier.size(22.dp))
                    }
                 }}
            }

            item(key = "roads_heading") { StaggerItem(7) {
                SectionHeading(overline = "Traditions", title = "The seven roads", modifier = Modifier.padding(top = 52.dp, bottom = 8.dp))
             }}
            itemsIndexed(TraditionId.entries.chunked(2), key = { index, _ -> "road_row_$index" }) { _, pair ->
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { id ->
                        RoadCard(id = id, count = perTradition[id] ?: 0, peak = busiest, onClick = { onOpenCanon(id) }, modifier = Modifier.weight(1f))
                    }
                    if (pair.size < 2) Spacer(Modifier.weight(1f))
                }
            }

            if (state.milestoneBadges.isNotEmpty()) {
                item(key = "badges_heading") { StaggerItem(8) {
                    SectionHeading(
                        overline = "Milestones",
                        title = "Marks of the journey",
                        trailing = "${state.milestoneBadges.count { it.isUnlocked }} / ${state.milestoneBadges.size}",
                        modifier = Modifier.padding(top = 52.dp, bottom = 20.dp)
                    )
                 }}
                item(key = "badges") { StaggerItem(9) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .layout { measurable, constraints ->
                                    val horizontalPaddingPx = 24.dp.roundToPx()
                                    val expandedWidth = constraints.maxWidth + (horizontalPaddingPx * 2)
                                    val placeable = measurable.measure(
                                        constraints.copy(
                                            minWidth = expandedWidth,
                                            maxWidth = expandedWidth
                                        )
                                    )
                                    layout(constraints.maxWidth, placeable.height) {
                                        placeable.placeRelative(-horizontalPaddingPx, 0)
                                    }
                                },
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp)
                        ) {
                            items(state.milestoneBadges, key = { it.id }) { badge -> BadgeCard(badge) }
                        }
                    }
                 }}
            }

            item(key = "recent_heading") { StaggerItem(10) {
                SectionHeading(
                    overline = "Recently read",
                    title = "Where you have been",
                    trailing = if (state.history.isNotEmpty()) "All ${state.history.size}" else null,
                    modifier = Modifier.padding(top = 52.dp, bottom = 6.dp).tappable(enabled = state.history.isNotEmpty(), scale = 0.99f, onClick = onOpenTimeline)
                )
             }}
            if (state.history.isEmpty()) {
                item(key = "recent_empty") { StaggerItem(11) {
                    Text(
                        text = "Verses you read will gather here, in order.",
                        style = typography.body,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                 }}
            } else {
                items(state.history.take(6), key = { "recent_${it.record.passageId}" }) { item ->
                    HistoryRow(item = item, onClick = { item.passage?.let(onOpenPassage) })
                }
            }

            item(key = "footer") { StaggerItem(12) {
                val openLegal = com.masum.fathom.ui.shell.LocalOpenLegal.current
                Column(Modifier.padding(top = 56.dp)) {
                    FooterRow(title = "What's new", caption = "fathom v$versionName", onClick = onOpenWhatsNew)
                    FooterRow(title = "Privacy Policy", caption = "How your data is handled") { openLegal(com.masum.fathom.ui.shell.LegalDoc.PRIVACY) }
                    FooterRow(title = "Terms of Use", caption = "The ground rules") { openLegal(com.masum.fathom.ui.shell.LegalDoc.TERMS) }
                    Hairline()
                }
             }}
        }
    }
}

@Composable
private fun FooterRow(title: String, caption: String, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    Column {
        Hairline()
        Row(
            modifier = Modifier.fillMaxWidth().tappable(scale = 0.99f, onClick = onClick).padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = title, style = FathomTheme.typography.title, color = colors.textPrimary)
                MonoLabel(text = caption, color = colors.textTertiary)
            }
            Icon(PhosphorIcons.Light.ArrowUpRight, null, tint = colors.textTertiary, modifier = Modifier.size(20.dp))
        }
    }
}


@Composable
private fun LedgerCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = value, style = FathomTheme.typography.numeral, color = FathomTheme.colors.textPrimary)
        MonoLabel(text = label)
    }
}



@Composable
private fun BadgeCard(badge: MilestoneBadge) {
    val colors = FathomTheme.colors
    val tone = remember(badge.traditionId, colors.isDark, colors.background) { toneFor(badge.traditionId, colors) }
    val shape = RoundedCornerShape(26.dp)
    val progress = if (badge.requiredCount > 0) (badge.currentCount.toFloat() / badge.requiredCount).coerceIn(0f, 1f) else 0f
    Column(
        modifier = Modifier
            .width(248.dp)
            .clip(shape)
            .background(Brush.linearGradient(listOf(tone.hue.copy(alpha = if (badge.isUnlocked) 0.28f else 0.10f), tone.hue.copy(alpha = 0.04f))))
            .border(0.75.dp, tone.hue.copy(alpha = if (badge.isUnlocked) 0.4f else 0.15f), shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Glyph(kind = GlyphKind.Star, color = if (badge.isUnlocked) tone.hue else colors.textTertiary, size = 28.dp, filled = badge.isUnlocked)
            MonoLabel(text = badge.tierName, color = if (badge.isUnlocked) tone.hue else colors.textTertiary)
        }
        Spacer(Modifier.height(6.dp))
        Text(text = badge.title, style = FathomTheme.typography.title, color = colors.textPrimary)
        Text(
            text = badge.description,
            style = FathomTheme.typography.caption,
            color = colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(1f, fill = false))
        Box(Modifier.fillMaxWidth().height(2.dp).background(colors.hairline)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(progress).background(tone.hue))
        }
        MonoLabel(text = "${badge.currentCount} / ${badge.requiredCount}", color = colors.textTertiary)
    }
}

@Composable
fun HistoryRow(item: HistoryWithPassage, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = FathomTheme.colors
    val passage = item.passage
    val tone = remember(passage?.traditionId, colors.isDark, colors.background) { toneFor(passage?.traditionId, colors) }
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().tappable(scale = 0.99f, haptic = false, onClick = onClick).padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MonoLabel(text = passage?.canonicalReference ?: item.record.passageId, color = tone.hue, modifier = Modifier.weight(1f, fill = false))
                if (item.record.readCount > 1) MonoLabel(text = "×${item.record.readCount}", color = colors.textTertiary)
            }
            passage?.let {
                Text(
                    text = it.translation,
                    style = FathomTheme.typography.commentaryBody.copy(fontFamily = ScriptureFamily, fontSize = 18.sp, lineHeight = 26.sp),
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Hairline()
    }
}

@Composable
private fun WeekDots(days: List<HeatmapDay>) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val peak = (days.maxOfOrNull { it.readCount } ?: 0).coerceAtLeast(1)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (day.readCount > 0) tone.hue.copy(alpha = 0.35f + 0.65f * day.readCount / peak) else Color.Transparent)
                        .border(if (day.isToday) 1.5.dp else 0.75.dp, if (day.isToday || day.readCount > 0) tone.hue else colors.hairline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (day.readCount > 0) Icon(PhosphorIcons.Light.Check, null, tint = tone.onHue, modifier = Modifier.size(15.dp))
                }
                MonoLabel(text = day.dayOfWeekLabel.take(1), color = if (day.isToday) tone.hue else colors.textTertiary)
            }
        }
    }
}

@Composable
private fun ReadingCalendar(history: List<HistoryWithPassage>, modifier: Modifier = Modifier) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val zone = remember { java.time.ZoneId.systemDefault() }
    val today = remember { LocalDate.now() }
    val counts = remember(history) {
        history.groupingBy { java.time.Instant.ofEpochMilli(it.record.lastSeenAtEpochMs).atZone(zone).toLocalDate() }.eachCount()
    }
    val weeks = 5
    val start = remember(today) { today.with(java.time.DayOfWeek.MONDAY).minusWeeks((weeks - 1).toLong()) }
    val inRange = counts.filterKeys { !it.isBefore(start) && !it.isAfter(today) }
    val peak = (inRange.values.maxOrNull() ?: 1).coerceAtLeast(1)
    val letters = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(text = "Last five weeks", style = FathomTheme.typography.headline.copy(fontSize = 22.sp, lineHeight = 26.sp), color = colors.textPrimary)
            MonoLabel(text = "${inRange.size} days read", color = tone.hue)
        }
        Row(Modifier.fillMaxWidth()) {
            letters.forEach { letter ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { MonoLabel(text = letter, color = colors.textTertiary) }
            }
        }
        for (week in 0 until weeks) {
            Row(Modifier.fillMaxWidth()) {
                for (dow in 0 until 7) {
                    val date = start.plusWeeks(week.toLong()).plusDays(dow.toLong())
                    val future = date.isAfter(today)
                    val count = counts[date] ?: 0
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        future -> Color.Transparent
                                        count == 0 -> colors.hairline.copy(alpha = 0.6f)
                                        else -> tone.hue.copy(alpha = 0.3f + 0.7f * count / peak)
                                    }
                                )
                                .then(if (date == today) Modifier.border(1.5.dp, tone.hue, CircleShape) else Modifier)
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendDot(color = tone.hue, label = "Read")
            LegendDot(color = colors.hairline.copy(alpha = 0.6f), label = "None")
            LegendDot(color = Color.Transparent, label = "Today", ring = tone.hue)
        }
        Text(
            text = "Each dot is a day. Brighter means more verses read.",
            style = FathomTheme.typography.caption,
            color = colors.textTertiary
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String, ring: Color? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
                .then(if (ring != null) Modifier.border(1.5.dp, ring, CircleShape) else Modifier)
        )
        MonoLabel(text = label, color = FathomTheme.colors.textTertiary)
    }
}

@Composable
private fun ShelfTile(label: String, count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val shape = RoundedCornerShape(26.dp)
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(shape)
            .background(colors.surfaceElevated)
            .border(0.75.dp, colors.hairline, shape)
            .tappable(scale = 0.98f, onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        MonoLabel(text = label, color = colors.textTertiary)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(text = count.toString(), style = FathomTheme.typography.displayLarge.copy(fontSize = 44.sp, lineHeight = 44.sp), color = tone.hue)
            Icon(PhosphorIcons.Light.ArrowUpRight, null, tint = colors.textTertiary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun RoadCard(id: TraditionId, count: Int, peak: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = FathomTheme.colors
    val tone = remember(id, colors.isDark, colors.background) { toneFor(id, colors) }
    val fraction = if (peak > 0) count.toFloat() / peak else 0f
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .height(140.dp)
            .clip(shape)
            .background(tone.hue.copy(alpha = if (count > 0) 0.14f else 0.05f))
            .border(0.75.dp, tone.hue.copy(alpha = if (count > 0) 0.30f else 0.14f), shape)
            .tappable(scale = 0.98f, onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TraditionEmblem(traditionId = id, color = if (count > 0) tone.hue else colors.textTertiary, size = 26.dp, filled = count > 0)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = id.displayName, style = FathomTheme.typography.title.copy(fontSize = 19.sp), color = if (count > 0) colors.textPrimary else colors.textSecondary, maxLines = 1)
            MonoLabel(text = if (count > 0) "$count read" else "Untouched", color = if (count > 0) tone.hue else colors.textTertiary)
            Box(Modifier.fillMaxWidth().height(2.dp).background(colors.hairline)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(tone.hue))
            }
        }
    }
}
