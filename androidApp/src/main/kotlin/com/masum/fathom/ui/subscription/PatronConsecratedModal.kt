package com.masum.fathom.ui.subscription

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.DeviceMobile
import com.adamglin.phosphoricons.light.Headphones
import com.adamglin.phosphoricons.light.MapTrifold
import com.adamglin.phosphoricons.light.Palette
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.user.SubscriptionTier
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.Glyph
import com.masum.fathom.ui.design.GlyphKind
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun PatronConsecratedModal(
    tier: SubscriptionTier,
    onDismiss: () -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val haptics = LocalFathomHaptics.current

    val entrance = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "halo_transition")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_pulse"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )

    LaunchedEffect(Unit) {
        haptics.success()
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    FathomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(112.dp)
                    .graphicsLayer {
                        scaleX = 0.7f + 0.3f * entrance.value
                        scaleY = 0.7f + 0.3f * entrance.value
                        alpha = entrance.value
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .scale(haloPulse)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    tone.hue.copy(alpha = haloAlpha * 0.45f),
                                    tone.hue.copy(alpha = haloAlpha * 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(tone.hue.copy(alpha = 0.12f))
                        .border(1.5.dp, tone.hue.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Glyph(
                        kind = GlyphKind.Star,
                        color = tone.hue,
                        size = 36.dp,
                        filled = true
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = entrance.value
                        translationY = (1f - entrance.value) * 18.dp.value
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MonoLabel(
                    text = "SANCTUARY CONSECRATED · ${tier.title.uppercase()}",
                    color = tone.hue
                )

                Text(
                    text = "Welcome, Patron of the Deep.",
                    style = typography.display.copy(fontSize = 28.sp, lineHeight = 34.sp),
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Your patronage consecrates independent sacred wisdom and sustains this contemplative space free of tracking and noise. Every sacred pathway, soundscape, theme, and neural voice is now unveiled.",
                    style = typography.body.copy(lineHeight = 22.sp),
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.hairline, RoundedCornerShape(22.dp))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ConsecrationFeatureItem(
                    icon = { Icon(PhosphorIcons.Light.Palette, null, Modifier.size(18.dp), tint = tone.hue) },
                    title = "All 8 Contemplative Themes",
                    description = "Monastery, Desert Father, Zen Paper, Obsidian & more"
                )
                ConsecrationFeatureItem(
                    icon = { Icon(PhosphorIcons.Light.Headphones, null, Modifier.size(18.dp), tint = tone.hue) },
                    title = "Full Sanctuary Soundscapes & Neural Voices",
                    description = "Infinite binaural rain, monastery bells & premium speech synthesis"
                )
                ConsecrationFeatureItem(
                    icon = { Icon(PhosphorIcons.Light.MapTrifold, null, Modifier.size(18.dp), tint = tone.hue) },
                    title = "Complete Pathways & Sanctuary Streak",
                    description = "Sacred milestone rewards, continuous journey tracking & widgets"
                )
                ConsecrationFeatureItem(
                    icon = { Icon(PhosphorIcons.Light.DeviceMobile, null, Modifier.size(18.dp), tint = tone.hue) },
                    title = "Multi-Device Consecration",
                    description = "Sync across up to 2 active devices with remote slot management"
                )
            }

            Spacer(Modifier.height(26.dp))

            FathomButton(
                text = "Enter Sanctuary",
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

@Composable
private fun ConsecrationFeatureItem(
    icon: @Composable () -> Unit,
    title: String,
    description: String
) {
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(colors.surface),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = typography.title.copy(fontSize = 15.sp),
                color = colors.textPrimary
            )
            Text(
                text = description,
                style = typography.commentaryBody.copy(fontSize = 12.5.sp, lineHeight = 16.sp),
                color = colors.textTertiary
            )
        }
    }
}
