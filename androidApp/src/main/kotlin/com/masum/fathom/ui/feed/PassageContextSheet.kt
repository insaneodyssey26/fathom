package com.masum.fathom.ui.feed

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.setValue
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.fill.Pause as PauseFill
import com.adamglin.phosphoricons.fill.Play as PlayFill
import com.adamglin.phosphoricons.light.ArrowUpRight
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.audio.RecitationMode
import com.masum.fathom.core.audio.SacredAudioState
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.LocalTone
import com.masum.fathom.ui.theme.ScriptureFamily
import com.masum.fathom.ui.theme.rememberTone
import androidx.compose.runtime.CompositionLocalProvider
import java.util.Locale

@Composable
fun PassageContextSheet(
    passage: Passage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val haptics = LocalFathomHaptics.current
    val audio = LocalSacredAudioEngine.current
    val playback by audio.playbackState.collectAsState()
    val exegesis = remember(passage.id) { ScriptureExegesisEngine.generateExegesis(passage) }
    val background = remember(passage.bookId) { getScriptureBackground(passage.bookId, passage.traditionId) }
    val tone = rememberTone(passage.traditionId)

    CompositionLocalProvider(LocalTone provides tone) {
        FathomSheet(onDismiss = onDismiss) {
            SheetHeader(
                overline = "${passage.traditionId.displayName} · ${background.period}",
                title = passage.canonicalReference,
                subtitle = background.title,
                onClose = onDismiss
            )

            val tabs = remember(passage.id) {
                buildList {
                    add("Context")
                    add("Reflect")
                    if (passage.explanations.isNotEmpty()) add("Commentary")
                }
            }
            var tab by androidx.compose.runtime.saveable.rememberSaveable(passage.id) { androidx.compose.runtime.mutableIntStateOf(0) }

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                VerseCard(passage = passage, state = playback, onToggle = {
                    haptics.medium()
                    audio.toggle(passage)
                }, onMode = audio::setMode, onSpeed = audio::setSpeed)

                com.masum.fathom.ui.design.SegmentedControl(options = tabs, selectedIndex = tab.coerceIn(0, tabs.lastIndex), onSelect = { tab = it })

                androidx.compose.animation.AnimatedContent(
                    targetState = tabs[tab.coerceIn(0, tabs.lastIndex)],
                    transitionSpec = {
                        androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(160, 40))
                            .togetherWith(androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(100)))
                    },
                    label = "context_tab"
                ) { label ->
                    Column(verticalArrangement = Arrangement.spacedBy(26.dp)) {
                        when (label) {
                            "Context" -> {
                                Section(label = "About this passage") {
                                    Text(text = exegesis.title, style = typography.title, color = colors.textPrimary)
                                    Text(text = exegesis.contextSummary, style = typography.commentaryBody, color = colors.textSecondary)
                                }
                                passage.originalText?.takeIf { it.isNotBlank() }?.let {
                                    Section(label = "In the original") {
                                        Text(
                                            text = it,
                                            style = typography.verseOriginal.copy(textDirection = TextDirection.Content),
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                                Section(label = background.title) {
                                    Text(text = background.summary, style = typography.body.copy(fontSize = 15.sp, lineHeight = 23.sp), color = colors.textTertiary)
                                    MonoLabel(text = background.period, color = colors.textTertiary)
                                }
                            }
                            "Reflect" -> Section(label = "Contemplate") {
                                Text(
                                    text = exegesis.spiritualInsight,
                                    style = typography.verseDisplay.copy(fontFamily = ScriptureFamily, fontStyle = FontStyle.Italic, fontSize = 22.sp, lineHeight = 32.sp),
                                    color = colors.textPrimary
                                )
                                MonoLabel(text = exegesis.sourceAttribution, color = colors.textTertiary)
                            }
                            else -> Section(label = "Commentary") {
                                passage.explanations.forEachIndexed { index, explanation ->
                                    if (index > 0) Hairline(Modifier.padding(vertical = 8.dp))
                                    explanation.title?.takeIf { it.isNotBlank() }?.let {
                                        Text(text = it, style = typography.title, color = colors.textPrimary)
                                    }
                                    Text(text = explanation.body, style = typography.commentaryBody, color = colors.textSecondary)
                                    val credit = listOfNotNull(explanation.author, explanation.source).joinToString(" · ")
                                    if (credit.isNotBlank()) MonoLabel(text = credit, color = colors.textTertiary)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tappable(scale = 0.99f) {
                            val query = "${passage.canonicalReference} commentary exegesis"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")))
                        }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Search the commentary online", style = typography.label, color = colors.textSecondary)
                    Icon(PhosphorIcons.Light.ArrowUpRight, null, tint = tone.hue, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun Section(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MonoLabel(text = label, color = FathomTheme.tone.hue)
        content()
    }
}

@Composable
private fun VerseCard(
    passage: Passage,
    state: SacredAudioState,
    onToggle: () -> Unit,
    onMode: (RecitationMode) -> Unit,
    onSpeed: (Float) -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography

    val active = when (state) {
        is SacredAudioState.Playing -> state.passage.id == passage.id
        is SacredAudioState.Paused -> state.passage.id == passage.id
        SacredAudioState.Idle -> false
    }
    val playing = state is SacredAudioState.Playing && state.passage.id == passage.id
    val progress = when (state) {
        is SacredAudioState.Playing -> state.progress
        is SacredAudioState.Paused -> state.progress
        SacredAudioState.Idle -> 0f
    }
    val mode = when (state) {
        is SacredAudioState.Playing -> state.mode
        is SacredAudioState.Paused -> state.mode
        SacredAudioState.Idle -> RecitationMode.TRANSLATION_ONLY
    }
    val speed = when (state) {
        is SacredAudioState.Playing -> state.speed
        is SacredAudioState.Paused -> state.speed
        SacredAudioState.Idle -> 1f
    }
    val fill by animateFloatAsState(progress.coerceIn(0f, 1f), tween(200), label = "recitation_progress")
    val modes = listOf(RecitationMode.TRANSLATION_ONLY, RecitationMode.ORIGINAL_ONLY, RecitationMode.FULL)
    val speeds = listOf(0.8f, 1.0f, 1.2f, 1.5f)
    val speedIndex = speeds.indices.minByOrNull { kotlin.math.abs(speeds[it] - speed) } ?: 1
    val shape = RoundedCornerShape(28.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(tone.hue.copy(alpha = 0.09f))
            .border(0.75.dp, tone.hue.copy(alpha = 0.22f), shape)
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "\u201C",
            style = typography.display.copy(fontSize = 56.sp, lineHeight = 40.sp),
            color = tone.hue.copy(alpha = 0.6f)
        )
        Text(
            text = passage.translation,
            style = typography.verseDisplay.copy(fontFamily = ScriptureFamily, fontStyle = FontStyle.Italic, fontSize = 21.sp, lineHeight = 30.sp),
            color = colors.textPrimary
        )
        Hairline(color = tone.hue.copy(alpha = 0.2f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(tone.hue)
                    .tappable(scale = 0.92f, haptic = false, onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playing) PhosphorIcons.Fill.PauseFill else PhosphorIcons.Fill.PlayFill,
                    contentDescription = if (playing) "Pause recitation" else "Play recitation",
                    tint = tone.onHue,
                    modifier = Modifier.size(19.dp)
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (playing) "Playing recitation" else if (active) "Paused" else "Listen to the recitation",
                    style = typography.label,
                    color = colors.textPrimary
                )
                Box(Modifier.fillMaxWidth().height(2.dp).background(colors.hairline)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(if (active) fill else 0f).background(tone.hue))
                }
            }
        }
        if (active) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                com.masum.fathom.ui.design.SegmentedControl(
                    options = listOf("English", "Original", "Both"),
                    selectedIndex = modes.indexOf(mode).coerceAtLeast(0),
                    onSelect = { onMode(modes[it]) },
                    height = 38.dp
                )
                com.masum.fathom.ui.design.SegmentedControl(
                    options = speeds.map { String.format(Locale.US, "%.1f\u00D7", it) },
                    selectedIndex = speedIndex,
                    onSelect = { onSpeed(speeds[it]) },
                    height = 38.dp
                )
            }
        }
    }
}

private data class ScriptureBackground(
    val title: String,
    val summary: String,
    val period: String
)

private fun getScriptureBackground(bookId: String, traditionId: TraditionId): ScriptureBackground {
    return when (bookId) {
        "the_holy_quran" -> ScriptureBackground(
            title = "The Holy Quran (Al-Qur'an)",
            summary = "The central religious text of Islam, revealed over 23 years in classical Arabic. It centers upon Tawhid (Divine Unity), moral accountability, social justice, and guiding humanity toward peace and spiritual surrender.",
            period = "7th Century CE • Arabic Revelation"
        )
        "bhagavad_gita" -> ScriptureBackground(
            title = "The Bhagavad Gita (The Song of God)",
            summary = "A foundational spiritual dialogue occurring on the battlefield of Kurukshetra between Prince Arjuna and Sri Krishna. It synthesizes Karma Yoga (selfless action), Jnana Yoga (wisdom), and Bhakti Yoga (devotion).",
            period = "Vedic Antiquity • Sanskrit Classic"
        )
        "gospel_of_matthew", "matthew" -> ScriptureBackground(
            title = "The Gospel According to Saint Matthew",
            summary = "The first of the four canonical Gospels, bridging the Hebrew scriptures with Christ's fulfillment of messianic prophecy. It contains the Sermon on the Mount and foundational kingdom parables.",
            period = "1st Century CE • New Testament"
        )
        "gospel_of_john", "john" -> ScriptureBackground(
            title = "The Gospel According to Saint John",
            summary = "A deeply mystical and theological Gospel presenting Christ as the eternal Logos (Word) through whom all creation came into being, focusing on truth, light, and eternal life.",
            period = "1st Century CE • Johannine Literature"
        )
        "the_book_of_psalms", "psalms" -> ScriptureBackground(
            title = "The Book of Psalms (Tehillim)",
            summary = "A sacred anthology of Hebrew lyrical poetry, prayers, and hymns attributed primarily to King David. It spans the full range of human emotion—lament, trust, thanksgiving, and ecstatic praise.",
            period = "Ancient Israelite Psalter • Hebrew Scripture"
        )
        "genesis" -> ScriptureBackground(
            title = "The Book of Genesis (Bereshit)",
            summary = "The opening book of the Torah and Bible recounting the creation of the cosmos, the inherent dignity of the human soul (Imago Dei), and the ancestral covenants of faith.",
            period = "Ancient Torah • Hebrew Canonical Text"
        )
        "the_dhammapada", "dhammapada" -> ScriptureBackground(
            title = "The Dhammapada (Path of Dhamma)",
            summary = "The most revered verse collection in the Pali Canon, encapsulating the Buddha's teachings on mindfulness, mental mastery, ethical purity, and liberation from suffering.",
            period = "5th Century BCE • Early Buddhist Canon"
        )
        "tao_te_ching" -> ScriptureBackground(
            title = "Tao Te Ching (The Book of the Way)",
            summary = "Attributed to the sage Laozi, this cornerstone text of Taoism teaches harmony with the natural flow of the universe through simplicity, humility, and Wu-Wei (effortless action).",
            period = "4th Century BCE • Classical Chinese Wisdom"
        )
        "meditations_marcus_aurelius", "meditations" -> ScriptureBackground(
            title = "Meditations (To Himself)",
            summary = "The intimate personal journals of Roman Emperor Marcus Aurelius written during military campaigns, exploring Stoic resilience, impermanence, and the tranquility of the governing mind.",
            period = "2nd Century CE • Hellenistic Stoic Philosophy"
        )
        "the_enchiridion", "enchiridion" -> ScriptureBackground(
            title = "The Enchiridion (The Handbook)",
            summary = "Compiled by Arrian from the lectures of the freed slave and philosopher Epictetus, outlining the core Stoic dichotomy of control and practical ethical freedom.",
            period = "2nd Century CE • Classical Stoic Manual"
        )
        "the_book_of_proverbs", "proverbs" -> ScriptureBackground(
            title = "The Book of Proverbs (Mishlei)",
            summary = "Practical wisdom literature attributed to King Solomon and Hebrew sages, instructing the reader in moral discernment, disciplined speech, and the fear of the Lord.",
            period = "Biblical Wisdom Literature • Ancient Near East"
        )
        "ecclesiastes" -> ScriptureBackground(
            title = "Ecclesiastes (Qoheleth)",
            summary = "A poignant philosophical exploration of mortality, the fleeting nature of worldly ambition ('hebel'), and the sacred joy found in simple daily existence and reverence for God.",
            period = "Biblical Wisdom Literature • Hebrew Canon"
        )
        "isha_upanishad", "katha_upanishad", "kena_upanishad" -> ScriptureBackground(
            title = "The Principal Upanishads (Vedanta)",
            summary = "The contemplative philosophical summits of the Vedic tradition, exploring the fundamental oneness of the individual self (Atman) with the ultimate cosmic reality (Brahman).",
            period = "Upanishadic Era • Sanskrit Vedanta"
        )
        else -> ScriptureBackground(
            title = "${traditionId.displayName} Canonical Scripture",
            summary = "Sacred text preserved across generations to cultivate wisdom, virtue, inner stillness, and spiritual discernment in daily life.",
            period = "Canonical Heritage"
        )
    }
}
