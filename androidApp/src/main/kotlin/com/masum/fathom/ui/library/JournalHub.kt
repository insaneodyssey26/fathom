package com.masum.fathom.ui.library

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val greeting = remember {
        when (LocalTime.now().hour) {
            in 5..11 -> "Good morning,"
            in 12..16 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }
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
            item(key = "greeting") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MonoLabel(text = today, color = colors.textTertiary)
                    Text(text = "$greeting\n${state.userName}.", style = typography.display, color = colors.textPrimary)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        MonoLabel(text = if (subscription.isPro) "Sanctuary+" else "Sacred seeker", color = tone.hue)
                        state.userTradition?.let { MonoLabel(text = it, color = colors.textSecondary) }
                        Row(
                            modifier = Modifier.tappable(scale = 0.95f, onClick = onEditProfile),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(PhosphorIcons.Light.PencilSimpleLine, null, tint = colors.textTertiary, modifier = Modifier.size(15.dp))
                            MonoLabel(text = "Edit", color = colors.textTertiary)
                        }
                    }
                }
            }

            item(key = "streak") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 44.dp),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = state.currentStreakDays.toString(), style = typography.displayLarge.copy(fontSize = 84.sp, lineHeight = 84.sp), color = colors.textPrimary)
                        MonoLabel(text = if (state.currentStreakDays == 1) "Day streak" else "Days streak", color = tone.hue)
                    }
                    TideChart(days = state.weeklyHeatmap, modifier = Modifier.weight(1f))
                }
            }

            item(key = "ledger") {
                Column(Modifier.padding(top = 36.dp)) {
                    Hairline()
                    Row(Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
                        LedgerCell(value = state.totalReadCount.toString(), label = "Readings", modifier = Modifier.weight(1f))
                        Box(Modifier.width(0.75.dp).height(44.dp).background(colors.hairline))
                        LedgerCell(value = state.history.size.toString(), label = "Verses read", modifier = Modifier.weight(1f).padding(start = 24.dp))
                    }
                    Hairline()
                }
            }

            item(key = "saved") {
                NavRow(title = "Saved passages", count = state.bookmarks.size, onClick = onOpenSaved)
            }
            item(key = "reflections") {
                NavRow(title = "Reflections", count = state.notes.size, onClick = onOpenReflections)
            }

            if (!subscription.isPro) {
                item(key = "upgrade") {
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
                            MonoLabel(text = "Sanctuary+", color = tone.hue)
                            Text(text = "Unlimited reflections, narration and themes", style = typography.title, color = colors.textPrimary)
                        }
                        Icon(PhosphorIcons.Light.ArrowRight, null, tint = tone.hue, modifier = Modifier.size(22.dp))
                    }
                }
            }

            item(key = "roads_heading") {
                SectionHeading(overline = "Traditions", title = "The seven roads", modifier = Modifier.padding(top = 52.dp, bottom = 8.dp))
            }
            items(TraditionId.entries.toList(), key = { "road_${it.key}" }) { id ->
                RoadRow(id = id, count = perTradition[id] ?: 0, peak = busiest, onClick = { onOpenCanon(id) })
            }

            if (state.milestoneBadges.isNotEmpty()) {
                item(key = "badges_heading") {
                    SectionHeading(
                        overline = "Milestones",
                        title = "Marks of the journey",
                        trailing = "${state.milestoneBadges.count { it.isUnlocked }} / ${state.milestoneBadges.size}",
                        modifier = Modifier.padding(top = 52.dp, bottom = 20.dp)
                    )
                }
                item(key = "badges") {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(end = 24.dp)
                    ) {
                        items(state.milestoneBadges, key = { it.id }) { badge -> BadgeCard(badge) }
                    }
                }
            }

            item(key = "recent_heading") {
                SectionHeading(
                    overline = "Recently read",
                    title = "Where you have been",
                    trailing = if (state.history.isNotEmpty()) "All ${state.history.size}" else null,
                    modifier = Modifier.padding(top = 52.dp, bottom = 6.dp).tappable(enabled = state.history.isNotEmpty(), scale = 0.99f, onClick = onOpenTimeline)
                )
            }
            if (state.history.isEmpty()) {
                item(key = "recent_empty") {
                    Text(
                        text = "Verses you read will gather here, in order.",
                        style = typography.body,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }
            } else {
                items(state.history.take(6), key = { "recent_${it.record.passageId}" }) { item ->
                    HistoryRow(item = item, onClick = { item.passage?.let(onOpenPassage) })
                }
            }

            item(key = "footer") {
                val openLegal = com.masum.fathom.ui.shell.LocalOpenLegal.current
                Column(Modifier.padding(top = 56.dp)) {
                    FooterRow(title = "What's new", caption = "fathom $versionName · closed beta", onClick = onOpenWhatsNew)
                    FooterRow(title = "Privacy Policy", caption = "How your data is handled") { openLegal(com.masum.fathom.ui.shell.LegalDoc.PRIVACY) }
                    FooterRow(title = "Terms of Use", caption = "The ground rules") { openLegal(com.masum.fathom.ui.shell.LegalDoc.TERMS) }
                    Hairline()
                }
            }
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
private fun TideChart(days: List<HeatmapDay>, modifier: Modifier = Modifier) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val peak = (days.maxOfOrNull { it.readCount } ?: 0).coerceAtLeast(1)
    val trackHeight = 92.dp
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        days.forEachIndexed { index, day ->
            val fill = remember(days) { Animatable(0f) }
            LaunchedEffect(day.readCount, days.size) {
                fill.animateTo(day.readCount.toFloat() / peak, tween(900, index * 70, FathomMotion.Emphasized))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(trackHeight)
                        .clip(RoundedCornerShape(7.dp))
                        .background(colors.hairline),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height((trackHeight - 14.dp) * fill.value + if (day.readCount > 0) 14.dp else 0.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (day.isToday) tone.hue else colors.textSecondary.copy(alpha = 0.55f))
                    )
                }
                MonoLabel(text = day.dayOfWeekLabel.take(1), color = if (day.isToday) tone.hue else colors.textTertiary)
            }
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
private fun NavRow(title: String, count: Int, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().tappable(scale = 0.99f, onClick = onClick).padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = FathomTheme.typography.headline.copy(fontSize = 26.sp, lineHeight = 30.sp), color = colors.textPrimary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(text = count.toString(), style = FathomTheme.typography.headline, color = FathomTheme.tone.hue)
                Icon(PhosphorIcons.Light.ArrowRight, null, tint = colors.textTertiary, modifier = Modifier.size(20.dp))
            }
        }
        Hairline()
    }
}

@Composable
private fun RoadRow(id: TraditionId, count: Int, peak: Int, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    val tone = remember(id, colors.isDark, colors.background) { toneFor(id, colors) }
    val fraction = if (peak > 0) count.toFloat() / peak else 0f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().tappable(scale = 0.99f, onClick = onClick).padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TraditionEmblem(traditionId = id, color = if (count > 0) tone.hue else colors.textTertiary, size = 26.dp, filled = count > 0)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(text = id.displayName, style = FathomTheme.typography.title, color = if (count > 0) colors.textPrimary else colors.textSecondary)
                    MonoLabel(text = if (count > 0) "$count read" else "Untouched", color = if (count > 0) tone.hue else colors.textTertiary)
                }
                Box(Modifier.fillMaxWidth().height(2.dp).background(colors.hairline)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(tone.hue))
                }
            }
        }
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
