package com.masum.fathom.ui.feed

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.BookBookmark
import com.adamglin.phosphoricons.regular.Compass
import com.adamglin.phosphoricons.regular.Pause
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.Quotes
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.SpeakerHigh
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.audio.SacredAudioState
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.components.TraditionBadge
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassageContextSheet(
    passage: Passage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val exegesis = ScriptureExegesisEngine.generateExegesis(passage)
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.82f).dp
    val bookBackground = getScriptureBackground(passage.bookId, passage.traditionId)
    val audioEngine = LocalSacredAudioEngine.current
    val playbackState by audioEngine.playbackState.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FathomTheme.colors.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = FathomTheme.colors.borderSubtle,
                height = 4.dp,
                width = 36.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TraditionBadge(traditionId = passage.traditionId)

                Text(
                    text = bookBackground.period,
                    style = FathomTheme.typography.caption.copy(
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = FathomTheme.colors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = passage.canonicalReference,
                        style = FathomTheme.typography.brandTitle.copy(
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            lineHeight = 30.sp,
                            letterSpacing = (-0.3).sp
                        ),
                        color = FathomTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = bookBackground.title,
                        style = FathomTheme.typography.caption.copy(
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp
                        ),
                        color = FathomTheme.colors.textTertiary
                    )
                }

                val isSpeakingThis = (playbackState as? SacredAudioState.Playing)?.passage?.id == passage.id ||
                    (playbackState as? SacredAudioState.Paused)?.passage?.id == passage.id
                val isPlaying = playbackState is SacredAudioState.Playing && (playbackState as SacredAudioState.Playing).passage.id == passage.id

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSpeakingThis) FathomTheme.colors.accentGold else FathomTheme.colors.surfaceElevated)
                        .border(
                            width = 1.dp,
                            color = if (isSpeakingThis) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { audioEngine.toggle(passage) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) PhosphorIcons.Regular.Pause else PhosphorIcons.Regular.Play,
                            contentDescription = if (isPlaying) "Pause Recitation" else "Listen to Recitation",
                            tint = if (isSpeakingThis) Color.Black else FathomTheme.colors.accentGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isPlaying) "PAUSE" else "LISTEN",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            letterSpacing = 0.5.sp,
                            color = if (isSpeakingThis) Color.Black else FathomTheme.colors.accentGold
                        )
                    }
                }
            }

            val currentAudioState = playbackState
            if ((currentAudioState is SacredAudioState.Playing && currentAudioState.passage.id == passage.id) ||
                (currentAudioState is SacredAudioState.Paused && currentAudioState.passage.id == passage.id)
            ) {
                val isOriginal = (currentAudioState as? SacredAudioState.Playing)?.isOriginalPart ?: (currentAudioState as? SacredAudioState.Paused)?.isOriginalPart ?: false
                val currentMode = (currentAudioState as? SacredAudioState.Playing)?.mode ?: (currentAudioState as? SacredAudioState.Paused)?.mode ?: com.masum.fathom.core.audio.RecitationMode.TRANSLATION_ONLY
                val currentProg = (currentAudioState as? SacredAudioState.Playing)?.progress ?: (currentAudioState as? SacredAudioState.Paused)?.progress ?: 0f
                val currentSpd = (currentAudioState as? SacredAudioState.Playing)?.speed ?: (currentAudioState as? SacredAudioState.Paused)?.speed ?: 1.0f

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FathomTheme.colors.surfaceElevated)
                        .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FathomTheme.colors.accentGold.copy(alpha = 0.15f))
                                        .clickable {
                                            val nextMode = when (currentMode) {
                                                com.masum.fathom.core.audio.RecitationMode.TRANSLATION_ONLY -> com.masum.fathom.core.audio.RecitationMode.ORIGINAL_ONLY
                                                com.masum.fathom.core.audio.RecitationMode.ORIGINAL_ONLY -> com.masum.fathom.core.audio.RecitationMode.FULL
                                                com.masum.fathom.core.audio.RecitationMode.FULL -> com.masum.fathom.core.audio.RecitationMode.TRANSLATION_ONLY
                                            }
                                            audioEngine.setMode(nextMode)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isOriginal) "ORIGINAL RECITATION" else "ENGLISH RECITATION",
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.4.sp,
                                        color = FathomTheme.colors.accentGold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(FathomTheme.colors.surface)
                                    .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(6.dp))
                                    .clickable {
                                        val nextSpeed = when {
                                            currentSpd <= 0.85f -> 1.0f
                                            currentSpd <= 1.05f -> 1.2f
                                            currentSpd <= 1.25f -> 1.5f
                                            else -> 0.8f
                                        }
                                        audioEngine.setSpeed(nextSpeed)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.1f", currentSpd)}×",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    color = FathomTheme.colors.accentGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { currentProg },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = FathomTheme.colors.accentGold,
                            trackColor = FathomTheme.colors.surface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (!passage.originalText.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FathomTheme.colors.surfaceElevated.copy(alpha = 0.6f))
                        .border(
                            width = 1.dp,
                            color = FathomTheme.colors.borderSubtle.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(42.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(FathomTheme.colors.accentGold)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ORIGINAL SCRIPTURE",
                            style = FathomTheme.typography.caption.copy(
                                fontSize = 10.sp,
                                letterSpacing = 1.3.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = FathomTheme.colors.accentGold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = passage.originalText ?: "",
                            style = FathomTheme.typography.verseOriginal.copy(
                                fontSize = 17.sp,
                                lineHeight = 26.sp
                            ),
                            color = FathomTheme.colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SCRIPTURE INSIGHTS & CONTEXT",
                    style = FathomTheme.typography.caption.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = FathomTheme.colors.accentGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = exegesis.title,
                    style = FathomTheme.typography.commentaryTitle.copy(
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 23.sp
                    ),
                    color = FathomTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = exegesis.contextSummary,
                    style = FathomTheme.typography.commentaryBody.copy(
                        fontSize = 15.sp,
                        lineHeight = 24.sp
                    ),
                    color = FathomTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .border(
                        width = 1.dp,
                        color = FathomTheme.colors.accentGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Quotes,
                        contentDescription = null,
                        tint = FathomTheme.colors.accentGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "PRACTICAL CONTEMPLATION",
                        style = FathomTheme.typography.caption.copy(
                            fontSize = 10.5.sp,
                            letterSpacing = 1.3.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = FathomTheme.colors.accentGold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = exegesis.spiritualInsight,
                    style = FathomTheme.typography.commentaryBody.copy(
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.5.sp,
                        lineHeight = 23.sp
                    ),
                    color = FathomTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "— ${exegesis.sourceAttribution}",
                    style = FathomTheme.typography.caption.copy(
                        fontSize = 11.sp,
                        letterSpacing = 0.3.sp
                    ),
                    color = FathomTheme.colors.textTertiary
                )
            }

            if (passage.explanations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                passage.explanations.forEach { explanation ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(FathomTheme.colors.surfaceElevated.copy(alpha = 0.5f))
                            .border(
                                width = 1.dp,
                                color = FathomTheme.colors.borderSubtle.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "CANONICAL COMMENTARY",
                            style = FathomTheme.typography.caption.copy(
                                fontSize = 10.5.sp,
                                letterSpacing = 1.3.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = FathomTheme.colors.accentGold
                        )

                        if (!explanation.title.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = explanation.title ?: "",
                                style = FathomTheme.typography.commentaryTitle.copy(
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                ),
                                color = FathomTheme.colors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = explanation.body,
                            style = FathomTheme.typography.commentaryBody.copy(
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            ),
                            color = FathomTheme.colors.textSecondary
                        )

                        if (!explanation.author.isNullOrBlank() || !explanation.source.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val attribution = listOfNotNull(explanation.author, explanation.source).joinToString(" • ")
                            Text(
                                text = "— $attribution",
                                style = FathomTheme.typography.caption.copy(fontSize = 11.sp),
                                color = FathomTheme.colors.textTertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .border(
                        width = 1.dp,
                        color = FathomTheme.colors.borderSubtle,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        val searchQuery = "${passage.canonicalReference} commentary exegesis"
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/search?q=${Uri.encode(searchQuery)}")
                        )
                        context.startActivity(intent)
                    }
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Sparkle,
                    contentDescription = null,
                    tint = FathomTheme.colors.accentGold,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Explore Complete Exegesis Online",
                    style = FathomTheme.typography.caption.copy(
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp
                    ),
                    color = FathomTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SacredDivider(modifier = Modifier.padding(bottom = 16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Compass,
                        contentDescription = null,
                        tint = FathomTheme.colors.textTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "HISTORICAL CONTEXT",
                        style = FathomTheme.typography.caption.copy(
                            fontSize = 10.5.sp,
                            letterSpacing = 1.3.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = FathomTheme.colors.textTertiary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = bookBackground.summary,
                    style = FathomTheme.typography.caption.copy(
                        fontFamily = JakartaFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    ),
                    color = FathomTheme.colors.textTertiary
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
