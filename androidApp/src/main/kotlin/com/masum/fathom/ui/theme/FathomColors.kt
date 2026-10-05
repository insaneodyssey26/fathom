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
    background = Color(0xFF0B0A09),
    surface = Color(0xFF151311),
    surfaceElevated = Color(0xFF1F1C18),
    textPrimary = Color(0xFFF1EADD),
    textSecondary = Color(0xFFA59C8D),
    textTertiary = Color(0xFF70685C),
    accent = Color(0xFFDCC291),
    onAccent = Color(0xFF17120B),
    hairline = Color(0xFF2C2824),
    isDark = true
)

val VellumColors = FathomColors(
    background = Color(0xFFF3EDE0),
    surface = Color(0xFFEAE3D3),
    surfaceElevated = Color(0xFFE0D7C4),
    textPrimary = Color(0xFF1E1A15),
    textSecondary = Color(0xFF5B5347),
    textTertiary = Color(0xFF8A806F),
    accent = Color(0xFF8A5527),
    onAccent = Color(0xFFFBF6EA),
    hairline = Color(0xFFD6CBB5),
    isDark = false
)

val AbyssColors = FathomColors(
    background = Color(0xFF070C13),
    surface = Color(0xFF0F1722),
    surfaceElevated = Color(0xFF172231),
    textPrimary = Color(0xFFE8EEF6),
    textSecondary = Color(0xFF92A2B7),
    textTertiary = Color(0xFF5C6C82),
    accent = Color(0xFF9CBFE3),
    onAccent = Color(0xFF08111B),
    hairline = Color(0xFF212F42),
    isDark = true
)

val MossColors = FathomColors(
    background = Color(0xFF090E0A),
    surface = Color(0xFF111913),
    surfaceElevated = Color(0xFF19241C),
    textPrimary = Color(0xFFE9F0E5),
    textSecondary = Color(0xFF9AAF9B),
    textTertiary = Color(0xFF64786A),
    accent = Color(0xFFB3D296),
    onAccent = Color(0xFF0B1209),
    hairline = Color(0xFF233327),
    isDark = true
)

val LocalFathomColors = staticCompositionLocalOf { NocturneColors }
