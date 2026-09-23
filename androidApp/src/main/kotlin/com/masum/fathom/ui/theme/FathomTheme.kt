package com.masum.fathom.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

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
    val colors = when (readingTheme) {
        ReadingTheme.OBSIDIAN -> ObsidianColors
        ReadingTheme.SEPIA -> SepiaColors
        ReadingTheme.SLATE -> SlateColors
    }

    CompositionLocalProvider(
        LocalFathomColors provides colors,
        LocalFathomTypography provides typography,
        content = content
    )
}
