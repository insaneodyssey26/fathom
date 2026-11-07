package com.masum.fathom.ui.whatsnew

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.Glyph
import com.masum.fathom.ui.design.GlyphKind
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.design.rememberSheetCloser
import com.masum.fathom.ui.theme.FathomTheme

data class ReleaseNote(
    val version: String,
    val title: String,
    val isCurrent: Boolean,
    val highlights: List<String>
)

private const val GITHUB_RELEASES_URL = "https://github.com/insaneodyssey26/fathom/releases"

@Composable
fun WhatsNewModal(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current
    val context = LocalContext.current

    val releases = remember {
        listOf(
            ReleaseNote(
                version = "v1.8.0",
                title = "Colors, App Icons & Streamlined Sanctuary",
                isCurrent = true,
                highlights = listOf(
                    "Two new reading themes: Northern Teal (Aurora) and Indigo Slate (Celestial), along with a redesigned compact theme picker.",
                    "Custom app icons: Choose from 6 custom app icons to match your home screen style.",
                    "Custom feed gradients (Beta): Design your own contemplative feed atmosphere with an interactive angle wheel and custom colors.",
                    "Easy vault exports: Export your personal reflections and saved notes as clean Markdown files anytime.",
                    "Official Google Play upgrades: Seamless membership activation with instant 1-tap checkout and automatic restore.",
                    "Better small screen support: Cleaner text layout and improved responsiveness across all devices."
                )
            ),
            ReleaseNote(
                version = "v1.7.0",
                title = "Reading Comfort & Audio Upgrades",
                isCurrent = false,
                highlights = listOf(
                    "Reading comfort: Adjust text size and line spacing, and choose between Newsreader (serif) and Geist (sans) font directly from the reader.",
                    "Audio controls: Control spoken verses and ambient sounds right from your notification panel, with a seekbar to jump around.",
                    "Smooth back gestures: Swipe back naturally from the screen edge to return to previous verses or pages.",
                    "Safer backups: Your reading progress, notes, and streak history are now backed up together in your vault file.",
                    "Seamless chapter reading: Fixed an issue where finishing a chapter could accidentally reset your place.",
                    "Faster performance: Quicker startup, lower battery usage, and smooth scrolling throughout."
                )
            )
        )
    }

    FathomSheet(onDismiss = onDismiss, modifier = modifier) {
        val closeSheet = rememberSheetCloser(onDismiss)
        SheetHeader(overline = "Release notes", title = "What's new", onClose = onDismiss)
        Column(modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            releases.forEachIndexed { index, item ->
                ReleaseEntry(release = item, isLast = index == releases.lastIndex)
            }

            Spacer(Modifier.height(8.dp))

            FathomButton(
                text = "View All Releases on GitHub ↗",
                onClick = {
                    haptics.light()
                    val customTabs = CustomTabsIntent.Builder().build()
                    customTabs.launchUrl(context, Uri.parse(GITHUB_RELEASES_URL))
                },
                style = ButtonStyle.Ghost,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            FathomButton(
                text = "Continue",
                onClick = {
                    haptics.light()
                    closeSheet()
                },
                style = ButtonStyle.Tone,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ReleaseEntry(release: ReleaseNote, isLast: Boolean) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val line = if (release.isCurrent) tone.hue.copy(alpha = 0.45f) else colors.hairline

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(modifier = Modifier.width(36.dp).fillMaxHeight()) {
            Canvas(Modifier.fillMaxHeight().width(36.dp)) {
                val x = 10.dp.toPx()
                if (!isLast) {
                    drawLine(
                        color = line,
                        start = Offset(x, 30.dp.toPx()),
                        end = Offset(x, size.height),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
            Glyph(
                kind = GlyphKind.Star,
                color = if (release.isCurrent) tone.hue else colors.textTertiary,
                size = 20.dp,
                filled = release.isCurrent,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = release.version,
                    style = typography.headline.copy(fontSize = 30.sp, lineHeight = 34.sp),
                    color = if (release.isCurrent) colors.textPrimary else colors.textSecondary
                )
                if (release.isCurrent) {
                    MonoLabel(text = "Latest", color = tone.hue)
                }
            }
            Text(
                text = release.title,
                style = typography.title,
                color = if (release.isCurrent) colors.textPrimary else colors.textSecondary
            )
            Column(
                modifier = Modifier.padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                release.highlights.forEach { highlight ->
                    val split = highlight.indexOf(": ")
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (split > 0) {
                            Text(
                                text = highlight.substring(0, split),
                                style = typography.label.copy(fontSize = 15.sp),
                                color = if (release.isCurrent) colors.textPrimary else colors.textSecondary
                            )
                            Text(
                                text = highlight.substring(split + 2),
                                style = typography.body,
                                color = colors.textSecondary
                            )
                        } else {
                            Text(
                                text = highlight,
                                style = typography.body,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
