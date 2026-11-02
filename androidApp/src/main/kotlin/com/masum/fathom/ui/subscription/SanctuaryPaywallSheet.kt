package com.masum.fathom.ui.subscription

import android.app.Activity
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.user.SubscriptionTier
import com.masum.fathom.core.subscription.LocalSubscriptionManager
import com.masum.fathom.core.subscription.ProductTierPricing
import com.masum.fathom.core.subscription.RestoreResult
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomTheme
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val subscriptionState by manager.subscriptionState.collectAsState()
    val pricingMap by manager.productPricing.collectAsState()
    val isPro = subscriptionState.isPro

    var selectedTier by remember { mutableStateOf(SubscriptionTier.ANNUAL) }
    var isRestoring by remember { mutableStateOf(false) }

    FathomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())
        ) {
            Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoLabel(text = if (isPro) "PATRON ACTIVE" else "FATHOM PATRON", color = tone.hue)
                Text(
                    text = if (isPro) "Patron Access Active." else "Support Fathom Sanctuary.",
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
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(
                        SubscriptionTier.ANNUAL,
                        SubscriptionTier.MONTHLY,
                        SubscriptionTier.LIFETIME
                    ).forEach { tier ->
                        PatronTierCard(
                            tier = tier,
                            pricing = pricingMap[tier],
                            selected = selectedTier == tier,
                            onClick = {
                                haptics.light()
                                selectedTier = tier
                            }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                val selectedPricing = pricingMap[selectedTier]
                val actionLabel = if (selectedPricing != null) {
                    "Become a Patron · ${selectedPricing.formattedPrice}"
                } else {
                    "Become a Patron · ${selectedTier.priceDisplay}"
                }

                FathomButton(
                    text = actionLabel,
                    onClick = {
                        haptics.medium()
                        if (activity != null) {
                            val launched = manager.launchPurchase(activity, selectedTier)
                            if (!launched) {
                                Toast.makeText(context, "Google Play Store is connecting. Please retry in a moment.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    style = ButtonStyle.Tone,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .tappable(scale = 0.95f) {
                            if (!isRestoring) {
                                isRestoring = true
                                haptics.light()
                                scope.launch {
                                    val result = manager.restorePurchases()
                                    isRestoring = false
                                    when (result) {
                                        is RestoreResult.Success -> {
                                            haptics.success()
                                            Toast.makeText(context, "Welcome back, Patron. Purchases restored.", Toast.LENGTH_LONG).show()
                                            onDismiss()
                                        }
                                        is RestoreResult.NoPurchasesFound -> {
                                            haptics.light()
                                            Toast.makeText(context, "No active subscriptions found for this Google account.", Toast.LENGTH_SHORT).show()
                                        }
                                        is RestoreResult.Error -> {
                                            haptics.error()
                                            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 20.dp)
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            color = tone.hue,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Restore Google Play Purchases",
                            style = typography.caption.copy(fontSize = 13.sp),
                            color = colors.textSecondary
                        )
                    }
                }
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

@Composable
private fun PatronTierCard(
    tier: SubscriptionTier,
    pricing: ProductTierPricing?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val shape = RoundedCornerShape(20.dp)
    val border by animateColorAsState(if (selected) tone.hue else colors.hairline, tween(240), label = "tier_border")
    val fill by animateColorAsState(if (selected) tone.hue.copy(alpha = 0.10f) else colors.surfaceElevated, tween(240), label = "tier_fill")

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
                .size(20.dp)
                .clip(CircleShape)
                .border(1.5.dp, if (selected) tone.hue else colors.textTertiary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(tone.hue)
                )
            }
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = tier.title, style = FathomTheme.typography.title, color = colors.textPrimary)
            MonoLabel(
                text = pricing?.billingPeriod ?: tier.billingPeriod,
                color = colors.textTertiary
            )
            tier.badgeLabel?.let { badge ->
                MonoLabel(
                    text = badge,
                    color = tone.hue,
                    maxLines = 1
                )
            }
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = pricing?.formattedPrice ?: tier.priceDisplay,
                style = FathomTheme.typography.headline.copy(fontSize = 19.sp),
                color = colors.textPrimary
            )
            MonoLabel(
                text = tier.equivalentMonthlyPrice,
                color = colors.textTertiary
            )
        }
    }
}
