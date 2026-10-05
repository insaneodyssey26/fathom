package com.masum.fathom.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.animation.core.tween

enum class ReadingTheme(val displayName: String) {
    OBSIDIAN("Nocturne"),
    SEPIA("Vellum"),
    SLATE("Abyss"),
    FOREST("Moss")
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

    val tone: Tone
        @Composable
        @ReadOnlyComposable
        get() = LocalTone.current
}

fun ReadingTheme.palette(): FathomColors = when (this) {
    ReadingTheme.OBSIDIAN -> NocturneColors
    ReadingTheme.SEPIA -> VellumColors
    ReadingTheme.SLATE -> AbyssColors
    ReadingTheme.FOREST -> MossColors
}

@Composable
fun FathomTheme(
    readingTheme: ReadingTheme = ReadingTheme.OBSIDIAN,
    typography: FathomTypography = remember { FathomTypography() },
    content: @Composable () -> Unit
) {
    val target = readingTheme.palette()
    val spec = tween<Color>(durationMillis = 420)

    val background by animateColorAsState(target.background, spec, label = "th_bg")
    val surface by animateColorAsState(target.surface, spec, label = "th_surface")
    val surfaceElevated by animateColorAsState(target.surfaceElevated, spec, label = "th_raised")
    val textPrimary by animateColorAsState(target.textPrimary, spec, label = "th_ink")
    val textSecondary by animateColorAsState(target.textSecondary, spec, label = "th_muted")
    val textTertiary by animateColorAsState(target.textTertiary, spec, label = "th_faint")
    val accent by animateColorAsState(target.accent, spec, label = "th_accent")
    val onAccent by animateColorAsState(target.onAccent, spec, label = "th_on_accent")
    val hairline by animateColorAsState(target.hairline, spec, label = "th_hairline")

    val colors = FathomColors(
        background = background,
        surface = surface,
        surfaceElevated = surfaceElevated,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textTertiary = textTertiary,
        accent = accent,
        onAccent = onAccent,
        hairline = hairline,
        isDark = target.isDark
    )

    val neutralTone = remember(colors.isDark, target) { toneFor(null, target) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !colors.isDark
                controller.isAppearanceLightNavigationBars = !colors.isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalFathomColors provides colors,
        LocalFathomTypography provides typography,
        LocalTone provides neutralTone,
        content = content
    )
}
