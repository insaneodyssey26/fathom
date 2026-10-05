package com.masum.fathom.ui.pathways

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowRight
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.Glyph
import com.masum.fathom.ui.design.GlyphKind
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.ScriptureFamily
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private data class Spark(val angle: Float, val reach: Float, val size: Float, val delay: Float)

@Composable
fun ChapterCelebrationModal(
    reward: CompletionReward,
    onDismiss: () -> Unit
) {
    val haptics = LocalFathomHaptics.current
    val tone = FathomTheme.tone
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography

    val burst = remember { Animatable(0f) }
    val rise = remember { Animatable(0f) }
    val sparks = remember {
        List(34) { i ->
            val seed = (i * 7919 % 97) / 97f
            Spark(
                angle = (i / 34f) * 6.2831855f + seed,
                reach = 0.55f + (i * 31 % 53) / 53f * 0.9f,
                size = 1.4f + (i * 13 % 17) / 17f * 3f,
                delay = (i * 11 % 23) / 23f * 0.25f
            )
        }
    }

    LaunchedEffect(Unit) {
        haptics.success()
        launch { burst.animateTo(1f, tween(1900, easing = FathomMotion.Emphasized)) }
        rise.animateTo(1f, tween(900, 250, FathomMotion.Emphasized))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false, dismissOnClickOutside = false)
    ) {
        Atmosphere(tone = tone, modifier = Modifier.fillMaxSize(), strength = 1.5f) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height * 0.3f)
                val maxR = size.minDimension * 0.62f
                val p = burst.value
                for (ring in 0 until 3) {
                    val local = ((p - ring * 0.14f) / 0.86f).coerceIn(0f, 1f)
                    if (local > 0f && local < 1f) {
                        drawCircle(
                            color = tone.hue.copy(alpha = (1f - local) * 0.5f),
                            radius = maxR * local,
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }
                sparks.forEach { spark ->
                    val local = ((p - spark.delay) / (1f - spark.delay)).coerceIn(0f, 1f)
                    if (local > 0f && local < 1f) {
                        val r = maxR * spark.reach * local
                        drawCircle(
                            color = tone.hue.copy(alpha = (1f - local) * 0.9f),
                            radius = spark.size.dp.toPx() * (1f - local * 0.5f),
                            center = Offset(center.x + cos(spark.angle) * r, center.y + sin(spark.angle) * r)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))
                Box(
                    modifier = Modifier.graphicsLayer {
                        val s = 0.6f + 0.4f * burst.value.coerceAtMost(1f)
                        scaleX = s
                        scaleY = s
                        alpha = burst.value.coerceAtMost(0.4f) / 0.4f
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Glyph(kind = GlyphKind.Star, color = tone.hue, size = 96.dp, filled = true)
                    Text(
                        text = reward.totalCompletedChapters.toString(),
                        style = typography.numeral.copy(fontSize = 34.sp, lineHeight = 34.sp),
                        color = tone.onHue
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(top = 44.dp)
                        .widthIn(max = 520.dp)
                        .graphicsLayer {
                            alpha = rise.value
                            translationY = (1f - rise.value) * 28.dp.toPx()
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MonoLabel(text = "Chapter complete", color = tone.hue)
                    Text(
                        text = reward.chapterTitle,
                        style = typography.display.copy(fontSize = 38.sp, lineHeight = 42.sp),
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    MonoLabel(text = reward.scriptureTitle, color = colors.textSecondary)

                    if (reward.keyInsight.isNotBlank()) {
                        Spacer(Modifier.height(14.dp))
                        Hairline(modifier = Modifier.widthIn(max = 48.dp), color = tone.hue.copy(alpha = 0.7f))
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = reward.keyInsight,
                            style = typography.verseDisplay.copy(
                                fontFamily = ScriptureFamily,
                                fontStyle = FontStyle.Italic,
                                fontSize = 21.sp,
                                lineHeight = 30.sp
                            ),
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                        Tally(value = "+${reward.gainedXp}", label = "Wisdom")
                        Tally(value = reward.passageCount.toString(), label = "Verses")
                        Tally(value = reward.totalCompletedChapters.toString(), label = "Chapters")
                    }
                }

                Spacer(Modifier.height(40.dp))
                FathomButton(
                    text = "Continue the path",
                    onClick = {
                        haptics.light()
                        onDismiss()
                    },
                    trailing = PhosphorIcons.Light.ArrowRight,
                    style = ButtonStyle.Tone,
                    modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)
                )
            }
        }
    }
}

@Composable
private fun Tally(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = value, style = FathomTheme.typography.headline, color = FathomTheme.colors.textPrimary)
        MonoLabel(text = label)
    }
}
