package com.masum.fathom.ui.feed

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.platform.LocalConfiguration
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

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                Recitation(passage = passage, state = playback, onToggle = {
                    haptics.medium()
                    audio.toggle(passage)
                }, onMode = audio::setMode, onSpeed = audio::setSpeed)

                passage.originalText?.takeIf { it.isNotBlank() }?.let {
                    Section(label = "In the original") {
                        Text(
                            text = it,
                            style = typography.verseOriginal.copy(textDirection = TextDirection.Content),
                            color = colors.textSecondary
                        )
                    }
                }

                Section(label = "Context") {
                    Text(text = exegesis.title, style = typography.title, color = colors.textPrimary)
                    Text(text = exegesis.contextSummary, style = typography.commentaryBody, color = colors.textSecondary)
                }

                Section(label = "Contemplate") {
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        Box(Modifier.padding(end = 16.dp).width(2.dp).fillMaxHeight().background(tone.hue))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = exegesis.spiritualInsight,
                                style = typography.verseDisplay.copy(fontFamily = ScriptureFamily, fontStyle = FontStyle.Italic, fontSize = 21.sp, lineHeight = 30.sp),
                                color = colors.textPrimary
                            )
                            MonoLabel(text = exegesis.sourceAttribution, color = colors.textTertiary)
                        }
                    }
                }

                if (passage.explanations.isNotEmpty()) {
                    Section(label = "Commentary") {
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

                Section(label = "Background") {
                    Text(text = background.summary, style = typography.body.copy(fontSize = 15.sp, lineHeight = 23.sp), color = colors.textTertiary)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .border(0.75.dp, colors.hairline, RoundedCornerShape(50))
                        .tappable(scale = 0.98f) {
                            val query = "${passage.canonicalReference} commentary exegesis"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")))
                        }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Search the commentary online", style = typography.label, color = colors.textPrimary)
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
private fun Recitation(
    passage: Passage,
    state: SacredAudioState,
    onToggle: () -> Unit,
    onMode: (RecitationMode) -> Unit,
    onSpeed: (Float) -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val haptics = LocalFathomHaptics.current

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
    val original = (state as? SacredAudioState.Playing)?.isOriginalPart ?: (state as? SacredAudioState.Paused)?.isOriginalPart ?: false
    val fill by animateFloatAsState(progress.coerceIn(0f, 1f), tween(200), label = "recitation_progress")

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(if (active) tone.hue else colors.surfaceElevated)
                .tappable(scale = 0.98f, onClick = onToggle)
                .padding(horizontal = 24.dp)
                .height(56.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (playing) PhosphorIcons.Fill.PauseFill else PhosphorIcons.Fill.PlayFill,
                contentDescription = null,
                tint = if (active) tone.onHue else tone.hue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (playing) "Pause recitation" else if (active) "Resume recitation" else "Listen to the recitation",
                style = FathomTheme.typography.label,
                color = if (active) tone.onHue else colors.textPrimary
            )
        }
        if (active) {
            Box(Modifier.fillMaxWidth().height(2.dp).background(colors.hairline)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(fill).background(tone.hue))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                ControlChip(
                    text = if (original) "Original" else "English",
                    onClick = {
                        haptics.light()
                        onMode(
                            when (mode) {
                                RecitationMode.TRANSLATION_ONLY -> RecitationMode.ORIGINAL_ONLY
                                RecitationMode.ORIGINAL_ONLY -> RecitationMode.FULL
                                RecitationMode.FULL -> RecitationMode.TRANSLATION_ONLY
                            }
                        )
                    }
                )
                ControlChip(
                    text = "${String.format(Locale.US, "%.1f", speed)}×",
                    onClick = {
                        haptics.light()
                        onSpeed(
                            when {
                                speed <= 0.85f -> 1.0f
                                speed <= 1.05f -> 1.2f
                                speed <= 1.25f -> 1.5f
                                else -> 0.8f
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ControlChip(text: String, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(0.75.dp, colors.hairline, RoundedCornerShape(50))
            .tappable(scale = 0.94f, haptic = false, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = FathomTheme.typography.label, color = colors.textSecondary)
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
