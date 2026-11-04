package com.masum.fathom.ui.subscription

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.light.ArrowRight
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.Minus
import com.adamglin.phosphoricons.regular.Plus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
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

private data class PlusPerkItem(val text: String)

private val PlusCorePerks = listOf(
    PlusPerkItem("All 8 contemplative reading themes & 6 custom app icons"),
    PlusPerkItem("Adaptive tradition atmosphere styling on every passage"),
    PlusPerkItem("Full access to all 15 scripture pathways and study tracks"),
    PlusPerkItem("Unlimited personal reflections & verse notes (Free plan capped at 5)"),
    PlusPerkItem("Obsidian & Notion-compatible Markdown (.md) journal export"),
    PlusPerkItem("Encrypted offline vault backup & one-tap restore"),
    PlusPerkItem("All ambient soundscapes (rain, temple, storm, wind) with sleep timer"),
    PlusPerkItem("Multi-device synchronization across phones and tablets"),
    PlusPerkItem("100% private: offline-first, zero analytics, and zero ads forever")
)

private data class FaqItem(val question: String, val answer: String)

private val SanctuaryFaqs = listOf(
    FaqItem(
        question = "Can I cancel my subscription anytime?",
        answer = "Yes. You can manage or cancel your subscription at any time directly through Google Play Subscriptions with a single tap. You will retain full Plus access until the end of your billing cycle."
    ),
    FaqItem(
        question = "What happens to my reflections if my plan ends?",
        answer = "Your personal reflections, notes, reading streaks, and bookmarks are never deleted. They remain safely stored on your device and inside your encrypted local backup file."
    ),
    FaqItem(
        question = "Does the Lifetime plan include future sacred texts?",
        answer = "Yes. The Lifetime plan gives permanent access to all existing texts, future traditions, audio soundscapes, and updates with zero recurring charges."
    ),
    FaqItem(
        question = "Can I restore my purchase on a new phone?",
        answer = "Yes. Simply log into the same Google account on your new device and tap 'Restore Google Play Purchases'. Your Plus membership will unlock immediately."
    )
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MonoLabel(
                    text = if (isPro) "PLUS ACTIVE" else "FATHOM PLUS",
                    color = tone.hue
                )
                Text(
                    text = if (isPro) "Fathom Plus Active" else "Choose Your Plan",
                    style = typography.display.copy(fontSize = 26.sp),
                    color = colors.textPrimary
                )
                Text(
                    text = if (isPro) {
                        "Your Plus membership is active. All sacred pathways, themes, reflections, and soundscapes are unlocked."
                    } else {
                        "Support independent craftsmanship and unlock all themes, pathways, notes, and ambient soundscapes."
                    },
                    style = typography.commentaryBody,
                    color = colors.textSecondary
                )
            }

            Spacer(Modifier.height(24.dp))

            if (!isPro) {
                PatronTierSelectorTabs(
                    selectedTier = selectedTier,
                    onSelectTier = {
                        haptics.light()
                        selectedTier = it
                    }
                )

                Spacer(Modifier.height(20.dp))

                HeroPlanCard(
                    tier = selectedTier,
                    pricing = pricingMap[selectedTier],
                    onPurchase = {
                        haptics.medium()
                        if (activity != null) {
                            val launched = manager.launchPurchase(activity, selectedTier)
                            if (!launched) {
                                Toast.makeText(context, "Connecting to Google Play. Please retry in a moment.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                Spacer(Modifier.height(28.dp))

                FaqSection(faqs = SanctuaryFaqs)

                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .tappable(scale = 0.96f) {
                            if (!isRestoring) {
                                isRestoring = true
                                haptics.light()
                                scope.launch {
                                    val result = manager.restorePurchases()
                                    isRestoring = false
                                    when (result) {
                                        is RestoreResult.Success -> {
                                            haptics.success()
                                            Toast.makeText(context, "Welcome back. Purchases restored.", Toast.LENGTH_LONG).show()
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
                        .padding(vertical = 12.dp, horizontal = 24.dp)
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
                ActivePatronManagementContent(
                    tier = subscriptionState.tier,
                    purchaseTime = manager.getPurchaseTime(),
                    isAutoRenewing = manager.isAutoRenewing(),
                    orderId = manager.getOrderId(),
                    lifetimePricing = pricingMap[SubscriptionTier.LIFETIME],
                    onManageGooglePlay = {
                        haptics.medium()
                        manager.openManageSubscriptions(context, subscriptionState.tier)
                    },
                    onUpgradeToLifetime = {
                        haptics.medium()
                        if (activity != null) {
                            val launched = manager.launchPurchase(activity, SubscriptionTier.LIFETIME)
                            if (!launched) {
                                Toast.makeText(context, "Connecting to Google Play. Please retry in a moment.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onRestore = {
                        if (!isRestoring) {
                            isRestoring = true
                            haptics.light()
                            scope.launch {
                                val result = manager.restorePurchases()
                                isRestoring = false
                                when (result) {
                                    is RestoreResult.Success -> {
                                        haptics.success()
                                        Toast.makeText(context, "Subscription verified with Google Play.", Toast.LENGTH_SHORT).show()
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
                    },
                    isRestoring = isRestoring
                )
            }
        }
    }
}

@Composable
private fun PatronTierSelectorTabs(
    selectedTier: SubscriptionTier,
    onSelectTier: (SubscriptionTier) -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.hairline, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            SubscriptionTier.ANNUAL to "Annual",
            SubscriptionTier.MONTHLY to "Monthly",
            SubscriptionTier.LIFETIME to "Lifetime"
        ).forEach { (tier, label) ->
            val isSelected = selectedTier == tier
            val bg by animateColorAsState(
                targetValue = if (isSelected) tone.hue else androidx.compose.ui.graphics.Color.Transparent,
                animationSpec = tween(220),
                label = "tab_bg"
            )
            val textCol by animateColorAsState(
                targetValue = if (isSelected) tone.onHue else colors.textSecondary,
                animationSpec = tween(220),
                label = "tab_fg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .tappable(scale = 0.97f) { onSelectTier(tier) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = FathomTheme.typography.caption.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    color = textCol,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun HeroPlanCard(
    tier: SubscriptionTier,
    pricing: ProductTierPricing?,
    onPurchase: () -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val cardShape = RoundedCornerShape(26.dp)

    val formattedPrice = pricing?.formattedPrice ?: tier.priceDisplay
    val periodSuffix = when (tier) {
        SubscriptionTier.ANNUAL -> "/year"
        SubscriptionTier.MONTHLY -> "/month"
        SubscriptionTier.LIFETIME -> "one-time"
        SubscriptionTier.FREE -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(colors.surfaceElevated)
            .border(1.5.dp, tone.hue.copy(alpha = 0.45f), cardShape)
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = formattedPrice,
                        style = typography.headline.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.8).sp
                        ),
                        color = colors.textPrimary
                    )
                    Text(
                        text = periodSuffix,
                        style = typography.caption.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colors.textSecondary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = tier.title,
                        style = typography.title.copy(fontSize = 18.sp),
                        color = colors.textPrimary
                    )
                    tier.badgeLabel?.let { badge ->
                        Text(
                            text = badge,
                            style = typography.caption.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Normal,
                                color = tone.hue
                            )
                        )
                    } ?: run {
                        if (tier.equivalentMonthlyPrice.isNotBlank()) {
                            MonoLabel(
                                text = tier.equivalentMonthlyPrice,
                                color = colors.textTertiary
                            )
                        }
                    }
                }
            }

            Hairline()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                PlusCorePerks.forEach { perk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.CheckCircle,
                            contentDescription = null,
                            tint = tone.hue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = perk.text,
                            style = typography.body.copy(fontSize = 13.5.sp),
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            FathomButton(
                text = "Upgrade to Plus · $formattedPrice",
                onClick = onPurchase,
                style = ButtonStyle.Tone,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FaqSection(faqs: List<FaqItem>) {
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val tone = FathomTheme.tone

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(tone.hue)
            )
            Text(
                text = "Frequently Asked Questions",
                style = typography.title.copy(fontSize = 18.sp),
                color = colors.textPrimary
            )
        }

        faqs.forEach { faq ->
            FaqAccordionItem(faq = faq)
        }
    }
}

@Composable
private fun FaqAccordionItem(faq: FaqItem) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val haptics = LocalFathomHaptics.current
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceElevated)
            .border(0.75.dp, colors.hairline, shape)
            .tappable(scale = 0.99f) {
                haptics.light()
                expanded = !expanded
            }
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = faq.question,
                    style = typography.title.copy(fontSize = 15.sp),
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Icon(
                    imageVector = if (expanded) PhosphorIcons.Regular.Minus else PhosphorIcons.Regular.Plus,
                    contentDescription = null,
                    tint = tone.hue,
                    modifier = Modifier.size(17.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(180)) + expandVertically(
                    animationSpec = tween(200),
                    expandFrom = Alignment.Top
                ),
                exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(
                    animationSpec = tween(180),
                    shrinkTowards = Alignment.Top
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clipToBounds()
                        .padding(top = 6.dp)
                ) {
                    Text(
                        text = faq.answer,
                        style = typography.body.copy(fontSize = 13.5.sp, lineHeight = 20.sp),
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivePatronManagementContent(
    tier: SubscriptionTier,
    purchaseTime: Long,
    isAutoRenewing: Boolean,
    orderId: String?,
    lifetimePricing: ProductTierPricing?,
    onManageGooglePlay: () -> Unit,
    onUpgradeToLifetime: () -> Unit,
    onRestore: () -> Unit,
    isRestoring: Boolean
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val cardShape = RoundedCornerShape(26.dp)

    val isLifetime = tier == SubscriptionTier.LIFETIME
    val purchaseDateFormatted = remember(purchaseTime) {
        if (purchaseTime > 0L) {
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
            Instant.ofEpochMilli(purchaseTime).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
        } else {
            "Active"
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(colors.surfaceElevated)
                .border(1.25.dp, tone.hue.copy(alpha = 0.4f), cardShape)
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(tone.hue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Crown,
                                contentDescription = null,
                                tint = tone.hue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            MonoLabel(
                                text = "MEMBERSHIP STATUS",
                                color = tone.hue
                            )
                            Text(
                                text = tier.title,
                                style = typography.title.copy(fontSize = 17.sp),
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(tone.hue.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isLifetime) "LIFETIME" else if (isAutoRenewing) "RENEWS" else "CANCELLED",
                            style = typography.caption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tone.hue
                            )
                        )
                    }
                }

                Hairline()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Billing Plan",
                            style = typography.caption.copy(fontSize = 13.sp),
                            color = colors.textSecondary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when (tier) {
                                SubscriptionTier.LIFETIME -> "Permanent Access"
                                SubscriptionTier.ANNUAL -> "Annual Plan"
                                SubscriptionTier.MONTHLY -> "Monthly Plan"
                                SubscriptionTier.FREE -> "Free"
                            },
                            style = typography.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isLifetime) "Purchased" else "Subscribed",
                            style = typography.caption.copy(fontSize = 13.sp),
                            color = colors.textSecondary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = purchaseDateFormatted,
                            style = typography.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }

                    if (!isLifetime) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Renewal Status",
                                style = typography.caption.copy(fontSize = 13.sp),
                                color = colors.textSecondary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isAutoRenewing) "Auto-renews" else "Expires soon",
                                style = typography.caption.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                                color = if (isAutoRenewing) tone.hue else colors.textSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    if (!orderId.isNullOrEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Order ID",
                                style = typography.caption.copy(fontSize = 13.sp),
                                color = colors.textSecondary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = orderId.takeLast(14),
                                style = typography.caption.copy(fontSize = 12.sp),
                                color = colors.textTertiary,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (!isLifetime) {
                    Spacer(Modifier.height(4.dp))
                    FathomButton(
                        text = "Manage on Google Play",
                        onClick = onManageGooglePlay,
                        style = ButtonStyle.Tone,
                        trailing = PhosphorIcons.Light.ArrowRight,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (!isLifetime) {
            val lifetimePriceFormatted = lifetimePricing?.formattedPrice ?: SubscriptionTier.LIFETIME.priceDisplay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceElevated)
                    .border(0.75.dp, colors.hairline, RoundedCornerShape(20.dp))
                    .tappable(scale = 0.98f) { onUpgradeToLifetime() }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MonoLabel(
                            text = "ONE-TIME UPGRADE",
                            color = tone.hue
                        )
                        Text(
                            text = "Upgrade to Lifetime Sanctuary",
                            style = typography.title.copy(fontSize = 16.sp),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Pay once · never worry about renewals again ($lifetimePriceFormatted)",
                            style = typography.commentaryBody.copy(fontSize = 12.5.sp),
                            color = colors.textSecondary
                        )
                    }
                    Icon(
                        imageVector = PhosphorIcons.Light.ArrowRight,
                        contentDescription = null,
                        tint = tone.hue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .tappable(scale = 0.96f) { onRestore() }
                .padding(vertical = 12.dp, horizontal = 24.dp)
        ) {
            if (isRestoring) {
                CircularProgressIndicator(
                    color = tone.hue,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Verify Google Play Purchases",
                    style = typography.caption.copy(fontSize = 13.sp),
                    color = colors.textSecondary
                )
            }
        }
    }
}
