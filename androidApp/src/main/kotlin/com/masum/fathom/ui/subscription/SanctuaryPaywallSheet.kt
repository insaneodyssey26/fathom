package com.masum.fathom.ui.subscription

import com.masum.fathom.ui.design.Whisper
import android.app.Activity
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

    var isRestoring by remember { mutableStateOf(false) }

    if (!isPro) {
        PlusPaywallScreen(
            pricing = pricingMap,
            isRestoring = isRestoring,
            onPurchase = { tier ->
                if (activity != null) {
                    val launched = manager.launchPurchase(activity, tier)
                    if (!launched) Whisper.problem("Connecting to Google Play. Please retry in a moment.")
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
                                Whisper.show("Welcome back. Purchases restored.")
                                onDismiss()
                            }
                            is RestoreResult.NoPurchasesFound -> {
                                haptics.light()
                                Whisper.problem("No active subscriptions found for this Google account.")
                            }
                            is RestoreResult.Error -> {
                                haptics.error()
                                Whisper.problem(result.message)
                            }
                        }
                    }
                }
            },
            onDismiss = onDismiss
        )
        return
    }

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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tone.hue.copy(alpha = 0.16f))
                        .border(1.dp, tone.hue.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isPro) "PLUS ACTIVE" else "FATHOM PLUS",
                        style = typography.caption.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = tone.hue
                    )
                }
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
                                Whisper.problem("Connecting to Google Play. Please retry in a moment.")
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
                                        Whisper.show("Subscription verified with Google Play.")
                                    }
                                    is RestoreResult.NoPurchasesFound -> {
                                        haptics.light()
                                        Whisper.problem("No active subscriptions found for this Google account.")
                                    }
                                    is RestoreResult.Error -> {
                                        haptics.error()
                                        Whisper.problem(result.message)
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
