package com.masum.fathom.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

enum class ReadingTheme {
    OBSIDIAN,
    SEPIA,
    SLATE
}

object FathomTheme {
    val colors: FathomColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFathomColors.current

    val typography: FathomTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalFathomTypography.current
}

@Composable
fun FathomTheme(
    readingTheme: ReadingTheme = ReadingTheme.OBSIDIAN,
    typography: FathomTypography = FathomTypography(),
    content: @Composable () -> Unit
) {
    val targetColors = when (readingTheme) {
        ReadingTheme.OBSIDIAN -> ObsidianColors
        ReadingTheme.SEPIA -> SepiaColors
        ReadingTheme.SLATE -> SlateColors
    }

    val animSpec = tween<Color>(durationMillis = 350)

    val background by animateColorAsState(targetColors.background, animSpec, label = "th_bg")
    val surface by animateColorAsState(targetColors.surface, animSpec, label = "th_surface")
    val surfaceElevated by animateColorAsState(targetColors.surfaceElevated, animSpec, label = "th_surfaceElevated")
    val textPrimary by animateColorAsState(targetColors.textPrimary, animSpec, label = "th_textPrimary")
    val textSecondary by animateColorAsState(targetColors.textSecondary, animSpec, label = "th_textSecondary")
    val textTertiary by animateColorAsState(targetColors.textTertiary, animSpec, label = "th_textTertiary")
    val accentGold by animateColorAsState(targetColors.accentGold, animSpec, label = "th_accentGold")
    val borderSubtle by animateColorAsState(targetColors.borderSubtle, animSpec, label = "th_borderSubtle")

    val animatedColors = FathomColors(
        background = background,
        surface = surface,
        surfaceElevated = surfaceElevated,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textTertiary = textTertiary,
        accentGold = accentGold,
        borderSubtle = borderSubtle,
        isDark = targetColors.isDark
    )


    CompositionLocalProvider(
        LocalFathomColors provides animatedColors,
        LocalFathomTypography provides typography,
        content = content
    )
}
