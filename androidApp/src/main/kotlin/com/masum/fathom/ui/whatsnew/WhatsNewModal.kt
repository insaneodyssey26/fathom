package com.masum.fathom.ui.whatsnew

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.Glyph
import com.masum.fathom.ui.design.GlyphKind
import com.masum.fathom.ui.design.Hairline
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

data class FeatureHighlight(
    val title: String,
    val description: String
)

private const val GITHUB_RELEASES_URL = "https://github.com/insaneodyssey26/fathom/releases"

@Composable
fun WhatsNewModal(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current
    val context = LocalContext.current
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography

    val release = remember {
        ReleaseNote(
            version = "v1.7.0",
            title = "Welcome to the First Official Release",
            isCurrent = true,
            highlights = listOf(
                "Reading comfort: Adjust text size and line spacing, and choose between Newsreader (serif) and Geist (sans) font directly from the reader.",
                "Audio controls: Control spoken verses and ambient sounds right from your notification panel, with a seekbar to jump around.",
                "Smooth back gestures: Swipe back naturally from the screen edge to return to previous verses or pages.",
                "Safer backups: Your reading progress, notes, and streak history are now backed up together in your vault file.",
                "Seamless chapter reading: Fixed an issue where finishing a chapter could accidentally reset your place.",
                "Faster performance: Quicker startup, lower battery usage, and smooth scrolling throughout."
            )
        )
    }

    val features = remember {
        listOf(
            FeatureHighlight(
                title = "7 World Traditions, 100% Offline",
                description = "Over 15,000 passages across Stoicism, Taoism, Hinduism, Buddhism, Christianity, Islam, and Judaism."
            ),
            FeatureHighlight(
                title = "Ambient Soundscapes & Timers",
                description = "Continuous background rain, singing bowls, temple bells, and chanting with an auto-fade sleep timer."
            ),
            FeatureHighlight(
                title = "Home Screen Widgets",
                description = "Glanceable daily wisdom and reading streak widgets right on your Android home screen."
            ),
            FeatureHighlight(
                title = "8 Reading Themes",
                description = "Dark and light palettes tuned for night and day reading, including pure black for OLED screens."
            ),
            FeatureHighlight(
                title = "Private & Ad-Free Forever",
                description = "Zero trackers, zero analytics, and no accounts required. All your reflections stay on your device."
            )
        )
    }

    FathomSheet(onDismiss = onDismiss, modifier = modifier) {
        val closeSheet = rememberSheetCloser(onDismiss)
        SheetHeader(overline = "Release notes", title = "What's new", onClose = onDismiss)
        Column(modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            ReleaseEntry(release = release)

            Spacer(Modifier.height(8.dp))
            Hairline()
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Key Features in Fathom",
                style = typography.title,
                color = colors.textPrimary
            )
            Spacer(Modifier.height(14.dp))

            features.forEach { feature ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(tone.hue)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = feature.title,
                            style = typography.label.copy(fontSize = 15.sp),
                            color = colors.textPrimary
                        )
                        Text(
                            text = feature.description,
                            style = typography.body,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(0.75.dp, colors.hairline, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MonoLabel(text = "Previous Beta Releases", color = tone.hue)
                    Text(
                        text = "Curious about changes from our earlier private beta versions (v0.1 through v1.6)? Full commit logs and changelogs are available on GitHub.",
                        style = typography.body,
                        color = colors.textSecondary
                    )
                    FathomButton(
                        text = "View Release History on GitHub ↗",
                        onClick = {
                            haptics.light()
                            val customTabs = CustomTabsIntent.Builder().build()
                            customTabs.launchUrl(context, Uri.parse(GITHUB_RELEASES_URL))
                        },
                        style = ButtonStyle.Ghost,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
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
private fun ReleaseEntry(release: ReleaseNote) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(modifier = Modifier.width(36.dp).fillMaxHeight()) {
            Glyph(
                kind = GlyphKind.Star,
                color = tone.hue,
                size = 20.dp,
                filled = true,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = release.version, style = typography.headline.copy(fontSize = 32.sp, lineHeight = 36.sp), color = colors.textPrimary)
                MonoLabel(text = "Latest", color = tone.hue)
            }
            Text(text = release.title, style = typography.title, color = colors.textPrimary)
            Column(modifier = Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                release.highlights.forEach { highlight ->
                    val split = highlight.indexOf(": ")
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (split > 0) {
                            Text(text = highlight.substring(0, split), style = typography.label.copy(fontSize = 15.sp), color = colors.textPrimary)
                            Text(text = highlight.substring(split + 2), style = typography.body, color = colors.textSecondary)
                        } else {
                            Text(text = highlight, style = typography.body, color = colors.textSecondary)
                        }
                    }
                }
            }
        }
    }
}
