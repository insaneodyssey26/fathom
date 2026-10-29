package com.masum.fathom.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.sanctuary.LocalSanctuaryPreferences
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomTheme
import kotlin.math.roundToInt

@Composable
fun ReadingComfortSheet(onDismiss: () -> Unit) {
    val preferences = LocalSanctuaryPreferences.current
    val haptics = LocalFathomHaptics.current
    val settings by preferences.settings.collectAsState()
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone

    var activeTextScale by remember(settings.readingTextScale) {
        mutableFloatStateOf(settings.readingTextScale)
    }
    var activeLineScale by remember(settings.readingLineHeightScale) {
        mutableFloatStateOf(settings.readingLineHeightScale)
    }
    var lastHapticStep by remember {
        mutableIntStateOf((settings.readingTextScale * 20).roundToInt())
    }

    val chosenFamily = when (settings.verseFontFamily) {
        com.masum.fathom.core.sanctuary.VerseFontFamily.SERIF -> FathomTheme.typography.verseDisplay.fontFamily
        com.masum.fathom.core.sanctuary.VerseFontFamily.SANS -> com.masum.fathom.ui.theme.InterfaceFamily
    }

    val sampleBaseSize = 24
    val previewSize = (sampleBaseSize * activeTextScale).toInt()
    val previewLineHeight = (previewSize * 1.28f * activeLineScale).sp

    FathomSheet(onDismiss = onDismiss) {
        SheetHeader(
            overline = "Typography",
            title = "Reading Comfort",
            subtitle = "Adjust verse scale and line balance to suit your eyes.",
            onClose = onDismiss
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, colors.hairline, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MonoLabel(
                        text = "PREVIEW",
                        color = tone.hue
                    )

                    Text(
                        text = "He only is my rock and my salvation; he is my defence; I shall not be greatly moved.",
                        style = FathomTheme.typography.verseDisplay.copy(
                            fontFamily = chosenFamily,
                            fontSize = previewSize.sp,
                            lineHeight = previewLineHeight,
                            color = colors.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Psalms 62:2",
                        style = FathomTheme.typography.caption,
                        color = colors.textTertiary
                    )
                }
            }

            Hairline()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Typeface",
                    style = FathomTheme.typography.body,
                    color = colors.textPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    com.masum.fathom.core.sanctuary.VerseFontFamily.entries.forEach { fontOption ->
                        val isSelected = settings.verseFontFamily == fontOption
                        val pillBg by animateColorAsState(
                            targetValue = if (isSelected) tone.hue.copy(alpha = 0.15f) else colors.surfaceElevated,
                            label = "font_pill_bg"
                        )
                        val textTint by animateColorAsState(
                            targetValue = if (isSelected) tone.hue else colors.textSecondary,
                            label = "font_pill_text"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(pillBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) tone.hue.copy(alpha = 0.45f) else colors.hairline,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .tappable(scale = 0.96f) {
                                    haptics.light()
                                    preferences.updateVerseFontFamily(fontOption)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fontOption.displayName,
                                style = FathomTheme.typography.label.copy(
                                    fontFamily = if (fontOption == com.masum.fathom.core.sanctuary.VerseFontFamily.SERIF) {
                                        FathomTheme.typography.verseDisplay.fontFamily
                                    } else {
                                        com.masum.fathom.ui.theme.InterfaceFamily
                                    },
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                ),
                                color = textTint,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Hairline()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Text Size",
                        style = FathomTheme.typography.body,
                        color = colors.textPrimary
                    )
                    MonoLabel(
                        text = "${(activeTextScale * 100).roundToInt()}%",
                        color = tone.hue
                    )
                }

                Slider(
                    value = activeTextScale,
                    onValueChange = { value ->
                        activeTextScale = value
                        val step = (value * 20).roundToInt()
                        if (step != lastHapticStep) {
                            lastHapticStep = step
                            haptics.light()
                        }
                        preferences.updateReadingComfort(value, activeLineScale)
                    },
                    valueRange = 0.85f..1.35f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = tone.hue,
                        activeTrackColor = tone.hue,
                        inactiveTrackColor = colors.surfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.85f to "Small", 1.0f to "Standard", 1.15f to "Large", 1.30f to "Extra").forEach { (scale, label) ->
                        val isSelected = (activeTextScale - scale) in -0.05f..0.05f
                        val pillBg by animateColorAsState(
                            targetValue = if (isSelected) tone.hue.copy(alpha = 0.15f) else colors.surfaceElevated,
                            label = "scale_pill_bg"
                        )
                        val textTint by animateColorAsState(
                            targetValue = if (isSelected) tone.hue else colors.textTertiary,
                            label = "scale_pill_text"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(pillBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) tone.hue.copy(alpha = 0.45f) else colors.hairline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .tappable(scale = 0.94f) {
                                    haptics.light()
                                    activeTextScale = scale
                                    lastHapticStep = (scale * 20).roundToInt()
                                    preferences.updateReadingComfort(scale, activeLineScale)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = FathomTheme.typography.caption.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                                color = textTint,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Hairline()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Line Height",
                    style = FathomTheme.typography.body,
                    color = colors.textPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.95f to "Compact", 1.0f to "Balanced", 1.15f to "Generous").forEach { (lineScale, title) ->
                        val isSelected = (activeLineScale - lineScale) in -0.04f..0.04f
                        val pillBg by animateColorAsState(
                            targetValue = if (isSelected) tone.hue.copy(alpha = 0.15f) else colors.surfaceElevated,
                            label = "line_pill_bg"
                        )
                        val textTint by animateColorAsState(
                            targetValue = if (isSelected) tone.hue else colors.textSecondary,
                            label = "line_pill_text"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(pillBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) tone.hue.copy(alpha = 0.45f) else colors.hairline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .tappable(scale = 0.96f) {
                                    haptics.light()
                                    activeLineScale = lineScale
                                    preferences.updateReadingComfort(activeTextScale, lineScale)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = FathomTheme.typography.label,
                                color = textTint,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
