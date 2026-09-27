package com.masum.fathom.ui.subscription

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.BookOpen
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.Headphones
import com.adamglin.phosphoricons.regular.NotePencil
import com.adamglin.phosphoricons.regular.PaintBrush
import com.adamglin.phosphoricons.regular.ShieldCheck
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.X
import com.masum.fathom.core.model.user.SubscriptionTier
import com.masum.fathom.core.subscription.LocalSubscriptionManager
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily
import com.masum.fathom.ui.theme.SyneFontFamily

private data class PaywallFeature(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanctuaryPaywallSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = com.masum.fathom.core.haptics.LocalFathomHaptics.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val subscriptionManager = LocalSubscriptionManager.current
    val context = LocalContext.current

    var selectedTier by remember { mutableStateOf(SubscriptionTier.ANNUAL) }

    val features = remember {
        listOf(
            PaywallFeature(
                icon = PhosphorIcons.Regular.PaintBrush,
                title = "4 Sacred Reading Themes",
                description = "Deep Obsidian, Ancient Parchment, Midnight Sapphire, and Imperial Emerald."
            ),
            PaywallFeature(
                icon = PhosphorIcons.Regular.Headphones,
                title = "Endless Sacred Audio & Soundscapes",
                description = "Continuous recitations, Tibetan singing bowls, and 432Hz ambient harmonic drones."
            ),
            PaywallFeature(
                icon = PhosphorIcons.Regular.BookOpen,
                title = "Full Pathways Scripture Mastery",
                description = "Study scriptures step-by-step across all chapters with reflections, quizzes, and certificates."
            ),
            PaywallFeature(
                icon = PhosphorIcons.Regular.NotePencil,
                title = "Unlimited Notes & Deep Insights",
                description = "Unlimited personal notes, tag indexing, and deep conversational passage explanations."
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
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        haptics.light()
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FathomTheme.colors.surfaceElevated)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.X,
                        contentDescription = "Close",
                        tint = FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                TextButton(
                    onClick = {
                        haptics.light()
                        val restored = subscriptionManager.restorePurchases()
                        if (restored) {
                            Toast.makeText(context, "Sanctuary+ Patronage Restored", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "No active Sanctuary+ subscription found", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(
                        text = "RESTORE",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = FathomTheme.colors.textSecondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    FathomTheme.colors.accentGold.copy(alpha = 0.28f),
                                    FathomTheme.colors.surfaceElevated
                                )
                            )
                        )
                        .border(1.5.dp, FathomTheme.colors.accentGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Crown,
                        contentDescription = null,
                        tint = FathomTheme.colors.accentGold,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text = "fathom sanctuary",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        letterSpacing = (-0.5).sp,
                        color = FathomTheme.colors.textPrimary
                    )
                    Text(
                        text = "+",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = FathomTheme.colors.accentGold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Ascend into Deep Contemplation",
                    fontFamily = JakartaFontFamily,
                    fontSize = 13.5.sp,
                    color = FathomTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                features.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FathomTheme.colors.surfaceElevated)
                            .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(FathomTheme.colors.accentGold.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = feature.icon,
                                contentDescription = null,
                                tint = FathomTheme.colors.accentGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = feature.title,
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = FathomTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = feature.description,
                                fontFamily = JakartaFontFamily,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = FathomTheme.colors.textTertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SubscriptionTierCard(
                    tier = SubscriptionTier.ANNUAL,
                    isSelected = selectedTier == SubscriptionTier.ANNUAL,
                    onClick = {
                        haptics.light()
                        selectedTier = SubscriptionTier.ANNUAL
                    }
                )

                SubscriptionTierCard(
                    tier = SubscriptionTier.SIX_MONTH,
                    isSelected = selectedTier == SubscriptionTier.SIX_MONTH,
                    onClick = {
                        haptics.light()
                        selectedTier = SubscriptionTier.SIX_MONTH
                    }
                )

                SubscriptionTierCard(
                    tier = SubscriptionTier.MONTHLY,
                    isSelected = selectedTier == SubscriptionTier.MONTHLY,
                    onClick = {
                        haptics.light()
                        selectedTier = SubscriptionTier.MONTHLY
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val buttonText = when (selectedTier) {
                    SubscriptionTier.ANNUAL -> "START 7-DAY FREE TRIAL"
                    SubscriptionTier.SIX_MONTH -> "UNLOCK 6-MONTH PASS ($19.99)"
                    SubscriptionTier.MONTHLY -> "UNLOCK MONTHLY PASS ($4.99)"
                    SubscriptionTier.FREE -> "CONTINUE WITH FREE"
                }

                Button(
                    onClick = {
                        haptics.heavy()
                        subscriptionManager.launchDodoCheckout(selectedTier)
                        subscriptionManager.purchaseTier(selectedTier)
                        Toast.makeText(context, "Welcome to Sanctuary+", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FathomTheme.colors.accentGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Sparkle,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = buttonText,
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.ShieldCheck,
                        contentDescription = null,
                        tint = FathomTheme.colors.textTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Secured via Dodo Payments · Instant Access · Cancel Anytime",
                        fontFamily = JakartaFontFamily,
                        fontSize = 11.sp,
                        color = FathomTheme.colors.textTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionTierCard(
    tier: SubscriptionTier,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
        animationSpec = tween(200),
        label = "border_color"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.12f) else FathomTheme.colors.surfaceElevated,
        animationSpec = tween(200),
        label = "bg_color"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = tier.title,
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textPrimary
                    )

                    tier.badgeLabel?.let { badge ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(FathomTheme.colors.accentGold)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 0.4.sp,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = tier.billingPeriod,
                    fontFamily = JakartaFontFamily,
                    fontSize = 12.sp,
                    color = FathomTheme.colors.textTertiary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = tier.priceDisplay,
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textPrimary
                )
                Text(
                    text = tier.equivalentMonthlyPrice,
                    fontFamily = JakartaFontFamily,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 11.sp,
                    color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                )
            }
        }
    }
}
