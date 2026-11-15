package com.masum.fathom.ui.subscription

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.X
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.user.SubscriptionTier
import com.masum.fathom.core.subscription.ProductTierPricing
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.WhisperHost
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomTheme

private data class Perk(val title: String, val body: String)

private val Perks = listOf(
    Perk("Every theme and icon", "All ten reading themes and six home-screen icons."),
    Perk("Every chapter", "Read past the first two chapters of each scripture, with guided pathways."),
    Perk("Unlimited reflections", "Write freely and export everything to Markdown."),
    Perk("Soundscapes and sleep timer", "Temple rain, river and singing bowl."),
    Perk("Encrypted vault", "Backup, restore and sync across your devices."),
    Perk("Private, always", "No ads, no analytics, no account.")
)

private val PlanOrder = listOf(SubscriptionTier.ANNUAL, SubscriptionTier.MONTHLY, SubscriptionTier.LIFETIME)

@Composable
fun PlusPaywallScreen(
    pricing: Map<SubscriptionTier, ProductTierPricing>,
    isRestoring: Boolean,
    onPurchase: (SubscriptionTier) -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val haptics = LocalFathomHaptics.current
    var selected by remember { mutableStateOf(SubscriptionTier.ANNUAL) }

    fun priceOf(tier: SubscriptionTier) = pricing[tier]?.formattedPrice ?: tier.priceDisplay
    val annualSaving = remember(pricing) { annualSavingPercent(pricing) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Atmosphere(tone = tone, modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 190.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(Modifier.height(72.dp))
                    MonoLabel(text = "Fathom Plus", color = tone.hue)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Read without\nlimits.",
                        style = FathomTheme.typography.displayLarge.copy(fontSize = 44.sp, lineHeight = 46.sp),
                        color = colors.textPrimary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Funded by readers, never by ads.",
                        style = FathomTheme.typography.commentaryBody,
                        color = colors.textSecondary
                    )

                    Spacer(Modifier.height(28.dp))

                    Column(
                        modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PlanOrder.forEach { tier ->
                            PlanRow(
                                tier = tier,
                                price = priceOf(tier),
                                annualSaving = annualSaving,
                                selected = selected == tier,
                                onClick = {
                                    if (selected != tier) {
                                        haptics.light()
                                        selected = tier
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(36.dp))
                    MonoLabel(text = "Everything included", color = colors.textTertiary)
                    Spacer(Modifier.height(8.dp))
                    Column(Modifier.widthIn(max = 560.dp).fillMaxWidth()) {
                        Perks.forEach { perk ->
                            Hairline()
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(text = perk.title, style = FathomTheme.typography.title.copy(fontSize = 16.sp), color = colors.textPrimary)
                                    Text(text = perk.body, style = FathomTheme.typography.caption, color = colors.textTertiary)
                                }
                            }
                        }
                        Hairline()
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 12.dp, end = 16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceElevated.copy(alpha = 0.7f))
                        .tappable(scale = 0.92f) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(PhosphorIcons.Light.X, "Close", tint = colors.textPrimary, modifier = Modifier.size(20.dp))
                }

                WhisperHost(modifier = Modifier.align(Alignment.TopCenter), insideSheet = true)

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.3f to colors.background.copy(alpha = 0.94f),
                                1f to colors.background
                            )
                        )
                        .navigationBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clip(RoundedCornerShape(50))
                            .background(tone.hue)
                            .tappable(scale = 0.98f) { onPurchase(selected) },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = selected,
                            transitionSpec = { fadeIn(tween(120)).togetherWith(fadeOut(tween(90))) },
                            label = "plus_cta"
                        ) { tier ->
                            Text(
                                text = ctaLabel(tier, priceOf(tier)),
                                style = FathomTheme.typography.label.copy(fontSize = 16.sp),
                                color = tone.onHue
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .heightIn(min = 36.dp)
                                .tappable(scale = 0.97f, haptic = false) { onRestore() }
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isRestoring) {
                                CircularProgressIndicator(color = tone.hue, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            } else {
                                MonoLabel(text = "Restore purchase", color = colors.textSecondary)
                            }
                        }
                        Box(Modifier.size(3.dp).clip(CircleShape).background(colors.textTertiary))
                        MonoLabel(text = "Cancel anytime", color = colors.textTertiary)
                    }
                }
            }
        }
    }
}

private fun ctaLabel(tier: SubscriptionTier, price: String): String = when (tier) {
    SubscriptionTier.MONTHLY -> "Continue  ·  $price / month"
    SubscriptionTier.ANNUAL -> "Continue  ·  $price / year"
    SubscriptionTier.LIFETIME -> "Get lifetime  ·  $price"
    SubscriptionTier.FREE -> "Continue"
}

@Composable
private fun PlanRow(tier: SubscriptionTier, price: String, annualSaving: Int, selected: Boolean, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val shape = RoundedCornerShape(20.dp)
    val border by animateColorAsState(if (selected) tone.hue else colors.hairline, tween(150), label = "plan_border")
    val fill by animateColorAsState(if (selected) tone.hue.copy(alpha = 0.10f) else Color.Transparent, tween(150), label = "plan_fill")
    val dot by animateColorAsState(if (selected) tone.hue else Color.Transparent, tween(150), label = "plan_dot")
    val name = when (tier) {
        SubscriptionTier.MONTHLY -> "Monthly"
        SubscriptionTier.ANNUAL -> "Annual"
        SubscriptionTier.LIFETIME -> "Lifetime"
        SubscriptionTier.FREE -> "Free"
    }
    val caption = when (tier) {
        SubscriptionTier.MONTHLY -> "Billed every month"
        SubscriptionTier.ANNUAL -> tier.equivalentMonthlyPrice.ifBlank { "Billed every year" }
        SubscriptionTier.LIFETIME -> "One payment, yours forever"
        SubscriptionTier.FREE -> ""
    }
    val suffix = when (tier) {
        SubscriptionTier.MONTHLY -> "/ month"
        SubscriptionTier.ANNUAL -> "/ year"
        SubscriptionTier.LIFETIME -> "once"
        SubscriptionTier.FREE -> ""
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(fill)
            .border(if (selected) 1.5.dp else 0.75.dp, border, shape)
            .tappable(scale = 0.99f, haptic = false, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(22.dp).clip(CircleShape).border(1.5.dp, if (selected) tone.hue else colors.textTertiary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(dot))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = name, style = FathomTheme.typography.title.copy(fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = colors.textPrimary)
                if (tier == SubscriptionTier.ANNUAL && annualSaving > 0) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(50)).background(tone.hue).padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = "SAVE $annualSaving%", style = FathomTheme.typography.mono.copy(fontSize = 9.sp), color = tone.onHue)
                    }
                }
            }
            Text(text = caption, style = FathomTheme.typography.caption, color = colors.textTertiary, maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(text = price, style = FathomTheme.typography.title.copy(fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = colors.textPrimary, maxLines = 1)
            Text(text = suffix, style = FathomTheme.typography.caption.copy(fontSize = 11.sp), color = colors.textTertiary)
        }
    }
}

private const val FallbackAnnualSaving = 25

private fun recurringMicros(pricing: ProductTierPricing?): Long? = runCatching {
    val details = pricing?.productDetails?.subscriptionOfferDetails ?: return@runCatching null
    val offer = details.firstOrNull { it.offerToken == pricing.selectedOfferToken } ?: details.firstOrNull()
    offer?.pricingPhases?.pricingPhaseList?.lastOrNull()?.priceAmountMicros
}.getOrNull()

private fun annualSavingPercent(pricing: Map<SubscriptionTier, ProductTierPricing>): Int {
    val monthly = recurringMicros(pricing[SubscriptionTier.MONTHLY])
    val annual = recurringMicros(pricing[SubscriptionTier.ANNUAL])
    if (monthly == null || annual == null || monthly <= 0L) return FallbackAnnualSaving
    val yearOfMonthly = monthly * 12.0
    return (((yearOfMonthly - annual) / yearOfMonthly) * 100).let { Math.round(it).toInt() }.coerceAtLeast(0)
}
