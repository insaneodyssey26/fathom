package com.masum.fathom.ui.whatsnew

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.X
import com.masum.fathom.R
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily
import com.masum.fathom.ui.theme.SyneFontFamily

data class ReleaseNote(
    val version: String,
    val title: String,
    val isCurrent: Boolean,
    val highlights: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewModal(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val appIconBitmap = remember(context) {
        try {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap.asImageBitmap()
            } else {
                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }

    val releases = remember {
        listOf(
            ReleaseNote(
                version = "v1.2.0",
                title = "Sacred Pathways, Daily Wisdom Widget, Journey Analytics & Tactile Haptics",
                isCurrent = true,
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FathomTheme.colors.background,
        dragHandle = null,
        modifier = modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FathomTheme.colors.surfaceElevated)
                            .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (appIconBitmap != null) {
                            Image(
                                bitmap = appIconBitmap,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp).clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Sparkle,
                                contentDescription = null,
                                tint = FathomTheme.colors.accentGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            Text(
                                text = "fathom",
                                style = FathomTheme.typography.brandTitle.copy(fontSize = 18.sp),
                                color = FathomTheme.colors.textPrimary
                            )
                            Text(
                                text = ".",
                                style = FathomTheme.typography.brandTitle.copy(
                                    fontSize = 18.sp,
                                    color = FathomTheme.colors.accentGold
                                )
                            )
                        }
                        Text(
                            text = "Release Notes & History",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = FathomTheme.colors.textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = {
                        haptics.light()
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(FathomTheme.colors.surfaceElevated)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.X,
                        contentDescription = "Close",
                        tint = FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                releases.forEach { release ->
                    ReleaseCard(release = release)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    haptics.light()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FathomTheme.colors.accentGold,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "GOT IT",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun ReleaseCard(
    release: ReleaseNote,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (release.isCurrent) {
                    Brush.verticalGradient(
                        listOf(
                            FathomTheme.colors.accentGold.copy(alpha = 0.10f),
                            FathomTheme.colors.surfaceElevated
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            FathomTheme.colors.surfaceElevated,
                            FathomTheme.colors.surfaceElevated
                        )
                    )
                }
            )
            .border(
                width = if (release.isCurrent) 1.5.dp else 1.dp,
                color = if (release.isCurrent) FathomTheme.colors.accentGold.copy(alpha = 0.6f) else FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = release.version,
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (release.isCurrent) FathomTheme.colors.accentGold else FathomTheme.colors.textPrimary
                    )

                    if (release.isCurrent) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(FathomTheme.colors.accentGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LATEST",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Text(
                text = release.title,
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = FathomTheme.colors.textPrimary
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                release.highlights.forEach { highlight ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (release.isCurrent) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary)
                        )
                        Text(
                            text = highlight,
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.5.sp,
                            lineHeight = 17.5.sp,
                            color = FathomTheme.colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
