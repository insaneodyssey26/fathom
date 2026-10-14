package com.masum.fathom.ui.whatsnew

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.rememberSheetCloser
import com.masum.fathom.ui.design.Glyph
import com.masum.fathom.ui.design.GlyphKind
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.theme.FathomTheme

data class ReleaseNote(
    val version: String,
    val title: String,
    val isCurrent: Boolean,
    val highlights: List<String>
)

@Composable
fun WhatsNewModal(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current

    val releases = remember {
        listOf(
            ReleaseNote(
                version = "v1.7.0",
                title = "Refined Back Gestures, Vault History & Smoother Reading",
                isCurrent = true,
                highlights = listOf(
                    "Fluid Predictive Back: Natural curved back gesture with smooth rounded corners as you glide between verses, chapters, and the library.",
                    "Complete Vault History: Reading journey and streak history are now fully preserved inside your private encrypted vault backups.",
                    "Chapter Celebration Polish: Seamless transition when completing sacred pathways chapters without unexpected screen resets.",
                    "Faster & Lighter: Smaller download size, quicker app opening, and butter-smooth scrolling across all contemplative reading feeds."
                )
            ),
            ReleaseNote(
                version = "v1.6.0",
                title = "Sanctuary Patron, Neural Studio Voices & 8 Sacred Themes",
                isCurrent = false,
                highlights = listOf(
                    "Sanctuary Patron Consecration: Support independent sacred wisdom with flexible monthly, annual, and lifetime patron plans. Unlocks all 8 themes, full pathway journeys, and all soundscapes.",
                    "Studio Neural Voice Narration (BYOK): High-fidelity recitation powered by your own ElevenLabs or OpenAI keys with automatic on-device caching for offline listening.",
                    "Eight Sacred Reading Themes: Completely distinct luminance palettes tuned for deep reading—Nocturne (OLED), Vellum (parchment), Abyss (navy), Moss (pine), Sahara (terracotta), Twilight (plum), Cinnabar (burgundy), and Dawn (warm morning).",
                    "Multi-Device License Sync: Activate and manage your Patron license seamlessly on up to 2 personal devices with cryptographic tamper protection.",
                    "Contemplative App Launch: Re-paced tradition emblem animation on launch with serene haptic pacing."
                )
            ),
            ReleaseNote(
                version = "v1.5.0",
                title = "Widgets on your home screen, deeper themes and audio controls",
                isCurrent = false,
                highlights = listOf(
                    "Home screen widgets: Put sacred wisdom and your reading streak right on your home screen. There is a scrollable wisdom card that lets you read the reflection right from your launcher, plus compact jewel and horizon banners.",
                    "Redesigned navigation & Explore: The bottom navigation bar is now a floating glass dock that rests over a soft dark blur. In Explore, searching and filtering now scroll naturally together with the rest of the page.",
                    "8 Sacred Reading Themes: Immerse yourself in handcrafted palettes: Nocturne (OLED gold), Vellum (manuscript parchment), Abyss (midnight sapphire), Moss (forest hermitage), Sahara (terracotta ember), Twilight (mystic lavender), Cinnabar (temple rose), and Dawn (crisp morning sunbeam).",
                    "Lean towards your traditions: When reading All Scriptures, you can now pick traditions to lean your feed towards while discovering wisdom across the entire library. Your choices stay saved when you reopen the app.",
                    "Recitation bar & soundscapes: Recitations now play through a floating audio bar at the bottom with seeking and playback controls. You can also start or pause soundscapes right beside your verse actions.",
                    "Cleaner listening: Multi-line Sanskrit and Arabic verses now flow naturally during narration without reading out raw reference numbers."
                )
            ),
            ReleaseNote(
                version = "v1.4.0",
                title = "A full redesign, a daily verse and update alerts",
                isCurrent = false,
                highlights = listOf(
                    "A new look everywhere: Every screen was redrawn. Scripture is set in a proper reading serif, and each tradition has its own colour that tints the page as you move between them. The four themes are now Nocturne, Vellum, Abyss and Moss.",
                    "Daily verse: One verse a day at the time you pick, from every tradition or just your own. Save or share it straight from the notification, and tapping it opens the verse. It stays hidden on the lock screen unless you choose otherwise.",
                    "Update alerts: Fathom now lets you know when a new version is on Google Play. You can switch this off in Settings.",
                    "Moving through verses: Swipe as before, or push the small knob in the bottom corner of the Read screen. The ruler on the right shows where you are.",
                    "Back gesture: Swiping back now follows your finger. Navigation was rebuilt too, so turning your phone no longer drops you out of a chapter.",
                    "Tablets and landscape: The bottom bar becomes a side rail on wide screens, and every screen was fitted for small phones, tablets and landscape.",
                    "Journal: The You tab is now Journal, with your streak, a week at a glance, progress for each tradition, milestones, saved passages and reflections.",
                    "Share cards: The preview is exactly the image you will save. There are four palettes and three sizes.",
                    "Settings, privacy and terms: Settings is a full page now, and a new More menu on the Read screen gets you to it, plus the Privacy Policy and Terms of Use, which open inside the app.",
                    "Streaks fixed: Reading streaks and the weekly chart were not counting before. They count now, starting with the verses you read from this update."
                )
            ),
            ReleaseNote(
                version = "v1.3.0",
                title = "Ambient Soundscapes, Semantic Topical Explorer & Sacred Road Checkpoints",
                isCurrent = false,
                highlights = listOf(
                    "High-Fidelity Ambient Soundscapes: 6 immersive looping soundscapes including Tibetan Singing Bowl, Himalayan Monks Chanting, Monastery Rain, Temple Bells & Stream, Binaural 432Hz Solfeggio Tone, and Deep Om Drone.",
                    "Contemplative Sleep Timer: Built-in timer with smooth fade-out presets (15m, 30m, 45m, 60m) and custom duration controls.",
                    "Cross-Tradition Semantic Topical Explorer: Explore sacred wisdom categorized across 8 curated life themes (Peace & Anxiety, Courage & Strength, Forgiveness & Grace, Grief & Solace, Wisdom & Purpose, Gratitude & Joy, Surrender & Trust, Stillness & Presence).",
                    "Topical Search Bento Grid: Discover curated existential themes with quick-filter pills, high-density keyword matching across all 7 traditions, and dedicated passage views.",
                    "Refined Sacred Road Checkpoints: Minimal circular milestone nodes with glowing golden accents and responsive lateral labels tailored to any screen width."
                )
            ),
            ReleaseNote(
                version = "v1.2.0",
                title = "Sacred Pathways, Daily Wisdom Widget, Journey Analytics & Tactile Haptics",
                isCurrent = false,
                highlights = listOf(
                    "Sacred Daily Wisdom Widget: Live glancing home screen widget with automatic midnight updates, quick manual refresh, and direct jump to scripture.",
                    "Interactive Sacred Pathways: Guided chapter-by-chapter reading trees with milestone progression and celebration cards.",
                    "Contemplative Reading Analytics: Daily reading streaks, interactive activity heatmap, and sacred milestone achievements in your Sanctuary profile.",
                    "Tactile Haptic Feedback: Subtle, responsive physical vibrations across navigation, scripture bookmarking, and audio controls.",
                    "Personalized Sanctuary Onboarding: Welcome flow to select tradition focus, set reading intentions, and customize your sacred feed.",
                    "Performance & Smoothness: Android Baseline Profiles and AOT compilation for instantaneous launch and fluid 120fps scrolling."
                )
            ),
            ReleaseNote(
                version = "v1.1.0",
                title = "Sanctuary Themes, Audio Scrubbing & Live Updates",
                isCurrent = false,
                highlights = listOf(
                    "4 Sacred Reading Themes: Switch between Obsidian Gold (OLED black & warm gold), Parchment Cream (soft linen & rich umber), Midnight Slate (deep oceanic navy & celestial blue), and Forest Zen (earthy moss & sage).",
                    "Upgraded Sacred Audio Player: Smooth seekbar with responsive timeline scrubbing, granular speed control (+/- 0.1x steps from 0.5x up to 2.5x), and persistent background audio.",
                    "Daily Contemplation Reminders: Gentle morning or evening local notification for daily reflection.",
                    "Google Play In-App Updates: Direct in-app update checks and downloads for closed beta testers.",
                    "Studio Narration (BYOK): Optional on-device integration to plug in your own ElevenLabs or OpenAI keys for studio neural narration.",
                    "Sanctuary Data Vault: Live database stats across all 15,163 verses and easy one-tap JSON export & restore for your bookmarks and reflections."
                )
            ),
            ReleaseNote(
                version = "v0.1.0-alpha",
                title = "Initial Closed Testing Launch",
                isCurrent = false,
                highlights = listOf(
                    "15,163 Canonical Passages: Complete offline library across 7 traditions (Hinduism, Islam, Christianity, Judaism, Buddhism, Taoism, Stoicism).",
                    "Contemplative Sacred Feed: Seamless vertical card paging with direct verse-level navigation.",
                    "Scripture Selector: Hierarchical accordion dropdown to filter by tradition or isolate specific books.",
                    "Verse Exegesis & Meaning: Canonical context, practical reflections, attributions, and direct online exploration shortcuts.",
                    "Themed Launcher Icon: Native support for Nothing Launcher and Material You monochrome icons.",
                    "Local-First Architecture: 100% offline, local on-device SQLite storage with zero tracking."
                )
            )
        )
    }

    FathomSheet(onDismiss = onDismiss, modifier = modifier) {
        val closeSheet = rememberSheetCloser(onDismiss)
        SheetHeader(overline = "Release notes", title = "What's new", onClose = onDismiss)
        Column(modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            releases.forEachIndexed { index, release ->
                ReleaseEntry(release = release, isLast = index == releases.lastIndex)
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
        }
    }
}

@Composable
private fun ReleaseEntry(release: ReleaseNote, isLast: Boolean) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val line = if (release.isCurrent) tone.hue.copy(alpha = 0.55f) else colors.hairline

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(modifier = Modifier.width(36.dp).fillMaxHeight()) {
            Canvas(Modifier.fillMaxHeight().width(36.dp)) {
                val x = 10.dp.toPx()
                if (!isLast) drawLine(line, Offset(x, 30.dp.toPx()), Offset(x, size.height), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            }
            Glyph(
                kind = GlyphKind.Star,
                color = if (release.isCurrent) tone.hue else colors.textTertiary,
                size = 20.dp,
                filled = release.isCurrent,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(bottom = 36.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = release.version, style = typography.headline.copy(fontSize = 32.sp, lineHeight = 36.sp), color = colors.textPrimary)
                if (release.isCurrent) MonoLabel(text = "Latest", color = tone.hue)
            }
            Text(text = release.title, style = typography.title, color = if (release.isCurrent) colors.textPrimary else colors.textSecondary)
            Column(modifier = Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
