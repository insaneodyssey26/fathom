package com.masum.fathom.ui.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.subscription.LocalSubscriptionManager
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.theme.FathomTheme

private data class PatronPerk(val number: String, val title: String, val body: String)

private val PatronPerks = listOf(
    PatronPerk("01", "Eight reading themes", "Nocturne, Vellum, Abyss, Moss, Sahara, Twilight, Cinnabar, and Dawn."),
    PatronPerk("02", "Guided pathway reading", "Chapter-by-chapter progression across all 15 texts with progress tracking."),
    PatronPerk("03", "Unlimited reflections & vault", "Write personal verse notes and export your private encrypted backup file."),
    PatronPerk("04", "Home screen streak widget", "Track your daily reading streak right from your Android home screen."),
    PatronPerk("05", "Daily morning reminders", "Gentle notifications with daily verses from your chosen tradition."),
    PatronPerk("06", "All ambient soundscapes & timers", "Background audio soundscapes with custom sleep and meditation timers."),
    PatronPerk("07", "Patron badge & zero ads forever", "Patron emblem on your profile with zero tracking or ads.")
)

@Composable
fun SanctuaryPaywallSheet(onDismiss: () -> Unit) {
    val haptics = LocalFathomHaptics.current
    val manager = LocalSubscriptionManager.current
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val subscriptionState by manager.subscriptionState.collectAsState()
    val isPro = subscriptionState.isPro

    FathomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())
        ) {
            Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoLabel(text = if (isPro) "PATRON ACTIVE" else "FATHOM PATRON", color = tone.hue)
                Text(
                    text = if (isPro) "Patron Access Active." else "Support Fathom Patron.",
                    style = typography.display,
                    color = colors.textPrimary
                )
                Text(
                    text = if (isPro) {
                        "Your Patron membership is active. All sacred pathways, themes, reflections, and soundscapes are unlocked."
                    } else {
                        "Support independent development and unlock all themes, pathways, notes, and ambient soundscapes."
                    },
                    style = typography.commentaryBody,
                    color = colors.textSecondary
                )
            }

            Column(Modifier.padding(top = 28.dp)) {
                Hairline()
                PatronPerks.forEach { perk ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        MonoLabel(text = perk.number, color = tone.hue, modifier = Modifier.padding(top = 4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(text = perk.title, style = typography.title, color = colors.textPrimary)
                            Text(text = perk.body, style = typography.body, color = colors.textSecondary)
                        }
                    }
                    Hairline()
                }
            }

            Spacer(Modifier.height(28.dp))

            if (!isPro) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(colors.surfaceElevated)
                        .border(1.dp, tone.hue.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(tone.hue)
                            )
                            MonoLabel(
                                text = "Patron Sanctuary",
                                color = tone.hue
                            )
                        }
                        Text(
                            text = "Patron membership will open soon. Enjoy exploring all texts, soundscapes, and features freely.",
                            style = FathomTheme.typography.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
                            color = colors.textPrimary
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                FathomButton(
                    text = "Understood",
                    onClick = {
                        haptics.light()
                        onDismiss()
                    },
                    style = ButtonStyle.Tone,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                FathomButton(
                    text = "Close",
                    onClick = {
                        haptics.light()
                        onDismiss()
                    },
                    style = ButtonStyle.Tone,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
