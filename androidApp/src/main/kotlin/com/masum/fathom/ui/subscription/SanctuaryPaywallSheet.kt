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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
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
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomTheme

private data class Perk(val title: String, val body: String)

private val Perks = listOf(
    Perk("Four reading themes", "Nocturne, Vellum, Abyss and Moss, each tuned for a different hour."),
    Perk("Endless recitation", "Continuous audio, singing bowls and ambient drones for any length of session."),
    Perk("Every path, in full", "Walk each scripture chapter by chapter, with reflections along the way."),
    Perk("Unlimited reflections", "Write without limits, tag what matters and revisit deep commentary.")
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

    FathomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())
        ) {
            Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoLabel(text = "Sanctuary+", color = tone.hue)
                Text(text = "Go deeper.", style = typography.display, color = colors.textPrimary)
                Text(
                    text = "Everything Fathom offers, with nothing held back.",
                    style = typography.commentaryBody,
                    color = colors.textSecondary
                )
            }

            Column(Modifier.padding(top = 32.dp)) {
                Hairline()
                Perks.forEachIndexed { index, perk ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        MonoLabel(text = (index + 1).toString().padStart(2, '0'), color = tone.hue, modifier = Modifier.padding(top = 6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = perk.title, style = typography.title, color = colors.textPrimary)
                            Text(text = perk.body, style = typography.body, color = colors.textSecondary)
                        }
                    }
                    Hairline()
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(tone.hue.copy(alpha = 0.16f), tone.hue.copy(alpha = 0.04f))))
                    .border(0.75.dp, tone.hue.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                    .padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MonoLabel(text = "Closed testing", color = tone.hue)
                    Text(
                        text = "Pricing is a preview and will change when Fathom launches on Google Play. While you test, everything is unlocked.",
                        style = typography.caption,
                        color = colors.textSecondary
                    )
                }
            }

            Column(Modifier.padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(SubscriptionTier.ANNUAL, SubscriptionTier.SIX_MONTH, SubscriptionTier.MONTHLY).forEach { tier ->
                    TierRow(
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
                text = "Unlock for testing",
                onClick = {
                    haptics.heavy()
                    manager.purchaseTier(selected)
                    Toast.makeText(context, "Sanctuary+ unlocked for testing", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                style = ButtonStyle.Tone,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .tappable(scale = 0.95f) {
                        manager.restorePurchases()
                        Toast.makeText(context, "Full access is active during testing", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                    .padding(vertical = 16.dp, horizontal = 24.dp)
            ) {
                MonoLabel(text = "Restore purchases", color = colors.textTertiary)
            }
        }
    }
}

@Composable
private fun TierRow(tier: SubscriptionTier, selected: Boolean, onClick: () -> Unit) {
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
            Text(text = tier.priceDisplay, style = FathomTheme.typography.headline.copy(fontSize = 24.sp), color = colors.textPrimary)
            MonoLabel(text = tier.equivalentMonthlyPrice, color = colors.textTertiary)
        }
    }
}
