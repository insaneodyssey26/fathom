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
import com.masum.fathom.ui.design.SegmentedControl
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

    var activeTextScale by remember(settings.readingTextScale) { mutableFloatStateOf(settings.readingTextScale) }
    var activeLineScale by remember(settings.readingLineHeightScale) { mutableFloatStateOf(settings.readingLineHeightScale) }
    var lastHapticStep by remember { mutableIntStateOf((settings.readingTextScale * 20).roundToInt()) }

    val fonts = com.masum.fathom.core.sanctuary.VerseFontFamily.entries
    val chosenFamily = when (settings.verseFontFamily) {
        com.masum.fathom.core.sanctuary.VerseFontFamily.SERIF -> FathomTheme.typography.verseDisplay.fontFamily
        com.masum.fathom.core.sanctuary.VerseFontFamily.SANS -> com.masum.fathom.ui.theme.InterfaceFamily
    }
    val lineOptions = listOf(0.95f to "Compact", 1.0f to "Balanced", 1.15f to "Airy")
    val lineIndex = lineOptions.indexOfFirst { (activeLineScale - it.first) in -0.04f..0.04f }.coerceAtLeast(0)

    val previewSize = (24 * activeTextScale).toInt()
    val previewLineHeight = (previewSize * 1.28f * activeLineScale).sp

    FathomSheet(onDismiss = onDismiss) {
        SheetHeader(overline = "Reading", title = "Text", onClose = onDismiss)

        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Hairline()
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
                MonoLabel(text = "Psalms 62:2", color = tone.hue)
                Hairline()
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MonoLabel(text = "Typeface", color = colors.textTertiary)
                SegmentedControl(
                    options = fonts.map { it.displayName },
                    selectedIndex = fonts.indexOf(settings.verseFontFamily).coerceAtLeast(0),
                    onSelect = { preferences.updateVerseFontFamily(fonts[it]) }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    MonoLabel(text = "Size", color = colors.textTertiary)
                    MonoLabel(text = "${(activeTextScale * 100).roundToInt()}%", color = tone.hue)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "A", style = FathomTheme.typography.headline.copy(fontSize = 15.sp), color = colors.textTertiary)
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
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "A", style = FathomTheme.typography.headline.copy(fontSize = 28.sp), color = colors.textPrimary)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MonoLabel(text = "Spacing", color = colors.textTertiary)
                SegmentedControl(
                    options = lineOptions.map { it.second },
                    selectedIndex = lineIndex,
                    onSelect = {
                        activeLineScale = lineOptions[it].first
                        preferences.updateReadingComfort(activeTextScale, activeLineScale)
                    }
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
