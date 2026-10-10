package com.masum.fathom.ui.subscription

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.user.SubscriptionTier
import com.masum.fathom.core.subscription.LocalSubscriptionManager
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.design.rememberSheetCloser
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomTheme

private data class PatronPerk(val number: String, val title: String, val body: String)

private val PatronPerks = listOf(
    PatronPerk("01", "Eight sacred reading themes", "Nocturne, Vellum, Abyss, Moss, Sahara, Twilight, Cinnabar, and Dawn."),
    PatronPerk("02", "Full sacred pathways immersion", "Sequential chapter progression across all 15 canonical texts with DepthDial radial progress tracking."),
    PatronPerk("03", "Unlimited reflections & vault", "Write personal meditation notes on any verse without limits and export your private data archive."),
    PatronPerk("04", "Studio neural narration (BYOK)", "High-fidelity ElevenLabs and OpenAI voice recitation with automatic local disk audio caching."),
    PatronPerk("05", "Sacred tradition morning alarms", "Tailored daily morning contemplation notifications delivering wisdom from your chosen path."),
    PatronPerk("06", "Home screen sanctuary widgets", "Full access to the Daily Wisdom Jewel, Horizon Banner, and Sanctuary Streak Counter widgets."),
    PatronPerk("07", "Patron seal & zero ads forever", "Golden consecrated Patron emblem in your Sanctuary profile with guaranteed zero tracking.")
)

@Composable
fun SanctuaryPaywallSheet(onDismiss: () -> Unit) {
    val haptics = LocalFathomHaptics.current
    val manager = LocalSubscriptionManager.current
    val context = LocalContext.current
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    var selected by remember { mutableStateOf(SubscriptionTier.ANNUAL) }
    var showRestoreSheet by remember { mutableStateOf(false) }

    if (showRestoreSheet) {
        RestorePurchaseSheet(
            onDismiss = { showRestoreSheet = false },
            onRestore = { email ->
                val result = manager.restorePurchasesWithEmail(email)
                result.onSuccess {
                    haptics.success()
                    Toast.makeText(context, "Patron status restored successfully.", Toast.LENGTH_LONG).show()
                    showRestoreSheet = false
                    onDismiss()
                }.onFailure { err ->
                    haptics.error()
                    Toast.makeText(context, err.message ?: "Failed to restore", Toast.LENGTH_SHORT).show()
                }
            }
        )
        return
    }

    FathomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())
        ) {
            Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoLabel(text = "FATHOM PATRON", color = tone.hue)
                Text(text = "Consecrate Your Sanctuary.", style = typography.display, color = colors.textPrimary)
                Text(
                    text = "Support independent sacred wisdom. Unlock all themes, pathways, reflections, and neural studio audio.",
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

            Column(Modifier.padding(top = 30.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(SubscriptionTier.LIFETIME, SubscriptionTier.ANNUAL, SubscriptionTier.MONTHLY).forEach { tier ->
                    PatronTierRow(
                        tier = tier,
                        selected = selected == tier,
                        onClick = {
                            haptics.light()
                            selected = tier
                        }
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
            FathomButton(
                text = "Become a Patron (${selected.title})",
                onClick = {
                    haptics.heavy()
                    manager.launchDodoCheckout(selected)
                },
                style = ButtonStyle.Tone,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .tappable(scale = 0.95f) {
                        haptics.light()
                        showRestoreSheet = true
                    }
                    .padding(vertical = 16.dp, horizontal = 24.dp)
            ) {
                MonoLabel(text = "Already a Patron? Restore purchase", color = colors.textTertiary)
            }
        }
    }
}

@Composable
private fun PatronTierRow(tier: SubscriptionTier, selected: Boolean, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val shape = RoundedCornerShape(22.dp)
    val border by animateColorAsState(if (selected) tone.hue else colors.hairline, tween(240), label = "tier_border")
    val fill by animateColorAsState(if (selected) tone.hue.copy(alpha = 0.10f) else androidx.compose.ui.graphics.Color.Transparent, tween(240), label = "tier_fill")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(fill)
            .border(if (selected) 1.5.dp else 0.75.dp, border, shape)
            .tappable(scale = 0.985f, haptic = false, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(1.5.dp, if (selected) tone.hue else colors.textTertiary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Box(Modifier.size(12.dp).clip(CircleShape).background(tone.hue))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = tier.title, style = FathomTheme.typography.title, color = colors.textPrimary)
            MonoLabel(text = tier.billingPeriod, color = colors.textTertiary)
            tier.badgeLabel?.let { MonoLabel(text = it, color = tone.hue) }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = tier.priceDisplay, style = FathomTheme.typography.headline.copy(fontSize = 20.sp), color = colors.textPrimary)
            MonoLabel(text = tier.equivalentMonthlyPrice, color = colors.textTertiary)
        }
    }
}

@Composable
private fun RestorePurchaseSheet(onDismiss: () -> Unit, onRestore: (String) -> Unit) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val manager = LocalSubscriptionManager.current
    val haptics = LocalFathomHaptics.current
    var emailOrKey by remember { mutableStateOf("") }

    FathomSheet(onDismiss = onDismiss) {
        SheetHeader(
            overline = "PATRON RESTORE",
            title = "Restore Patron Status",
            subtitle = "Enter the email address you used during checkout to restore your Patron access.",
            onClose = onDismiss
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.hairline, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            if (emailOrKey.isEmpty()) {
                Text(text = "patron@example.com", style = FathomTheme.typography.body, color = colors.textTertiary)
            }
            BasicTextField(
                value = emailOrKey,
                onValueChange = { emailOrKey = it },
                singleLine = true,
                textStyle = FathomTheme.typography.body.copy(fontSize = 16.sp, color = colors.textPrimary),
                cursorBrush = SolidColor(tone.hue),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(16.dp))
        FathomButton(
            text = "Restore Patron Access",
            onClick = { onRestore(emailOrKey) },
            enabled = emailOrKey.isNotBlank(),
            style = ButtonStyle.Tone,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        FathomButton(
            text = "Instant Test Mode Unlock (Lifetime)",
            onClick = { onRestore("patron_tester@fathom.app") },
            style = ButtonStyle.Ghost,
            modifier = Modifier.fillMaxWidth()
        )
        if (manager.subscriptionState.value.isPro) {
            Spacer(Modifier.height(6.dp))
            FathomButton(
                text = "Reset to Free Tier",
                onClick = {
                    manager.resetToFree()
                    haptics.light()
                    onDismiss()
                },
                style = ButtonStyle.Ghost,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
