package com.masum.fathom.ui.pathways

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.Flame
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.X
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily
import com.masum.fathom.ui.theme.SyneFontFamily

@Composable
fun ChapterCelebrationModal(
    reward: CompletionReward,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "celebration_scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                FathomTheme.colors.surfaceElevated,
                                FathomTheme.colors.surface
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = FathomTheme.colors.accentGold.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(26.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(pulseScale)
                            .shadow(
                                elevation = 24.dp,
                                shape = CircleShape,
                                ambientColor = FathomTheme.colors.accentGold,
                                spotColor = FathomTheme.colors.accentGold
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        FathomTheme.colors.accentGold,
                                        FathomTheme.colors.accentGold.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Crown,
                            contentDescription = "Chapter Mastered",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "CHAPTER MASTERED",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        color = FathomTheme.colors.accentGold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = reward.chapterTitle,
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = FathomTheme.colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = reward.scriptureTitle,
                        fontFamily = JakartaFontFamily,
                        fontSize = 12.sp,
                        color = FathomTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(FathomTheme.colors.surface)
                            .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Sparkle,
                                    contentDescription = null,
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "+${reward.gainedXp} XP",
                                    fontFamily = SyneFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = FathomTheme.colors.accentGold
                                )
                            }
                            Text(
                                text = "Wisdom XP",
                                fontFamily = JakartaFontFamily,
                                fontSize = 10.5.sp,
                                color = FathomTheme.colors.textTertiary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(FathomTheme.colors.borderSubtle)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.CheckCircle,
                                    contentDescription = null,
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "${reward.passageCount}",
                                    fontFamily = SyneFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = FathomTheme.colors.textPrimary
                                )
                            }
                            Text(
                                text = "Verses Read",
                                fontFamily = JakartaFontFamily,
                                fontSize = 10.5.sp,
                                color = FathomTheme.colors.textTertiary
                            )
                        }
                    }

                    if (reward.keyInsight.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(FathomTheme.colors.accentGold.copy(alpha = 0.08f))
                                .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "SACRED ESSENCE",
                                    fontFamily = SyneFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp,
                                    color = FathomTheme.colors.accentGold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "“${reward.keyInsight}”",
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 12.5.sp,
                                    color = FathomTheme.colors.textPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FathomTheme.colors.accentGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "CLAIM WISDOM & CONTINUE",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
