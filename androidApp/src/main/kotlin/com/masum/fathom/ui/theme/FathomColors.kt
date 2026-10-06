package com.masum.fathom.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class FathomColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val onAccent: Color,
    val hairline: Color,
    val isDark: Boolean
) {
    val scrim: Color get() = if (isDark) Color(0xCC000000) else Color(0x991A140C)
}

val NocturneColors = FathomColors(
    background = Color(0xFF0D0B0A),
    surface = Color(0xFF171412),
    surfaceElevated = Color(0xFF231F1C),
    textPrimary = Color(0xFFF3EEE6),
    textSecondary = Color(0xFFA79E90),
    textTertiary = Color(0xFF6E6558),
    accent = Color(0xFFD8B97E),
    onAccent = Color(0xFF161109),
    hairline = Color(0xFF2C2620),
    isDark = true
)

val VellumColors = FathomColors(
    background = Color(0xFFF5EFE3),
    surface = Color(0xFFECE3D2),
    surfaceElevated = Color(0xFFE2D7C2),
    textPrimary = Color(0xFF1E1914),
    textSecondary = Color(0xFF5C5245),
    textTertiary = Color(0xFF8C7F6E),
    accent = Color(0xFF8B5628),
    onAccent = Color(0xFFFBF6EA),
    hairline = Color(0xFFD6C9B0),
    isDark = false
)

val AbyssColors = FathomColors(
    background = Color(0xFF0B131E),
    surface = Color(0xFF121D2C),
    surfaceElevated = Color(0xFF1B293C),
    textPrimary = Color(0xFFEAF1F9),
    textSecondary = Color(0xFF94A7BF),
    textTertiary = Color(0xFF576E8A),
    accent = Color(0xFF7EB0DE),
    onAccent = Color(0xFF08111B),
    hairline = Color(0xFF22354E),
    isDark = true
)

val MossColors = FathomColors(
    background = Color(0xFF0C1610),
    surface = Color(0xFF14221A),
    surfaceElevated = Color(0xFF1D3025),
    textPrimary = Color(0xFFEBF3EA),
    textSecondary = Color(0xFF9CB6A0),
    textTertiary = Color(0xFF5D7B64),
    accent = Color(0xFF8ECA92),
    onAccent = Color(0xFF0B1209),
    hairline = Color(0xFF253E30),
    isDark = true
)

val LocalFathomColors = staticCompositionLocalOf { NocturneColors }
