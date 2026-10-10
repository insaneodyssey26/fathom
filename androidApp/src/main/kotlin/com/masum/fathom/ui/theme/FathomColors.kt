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
    background = Color(0xFF050505),
    surface = Color(0xFF141210),
    surfaceElevated = Color(0xFF1F1B16),
    textPrimary = Color(0xFFF5EFE6),
    textSecondary = Color(0xFFA99E90),
    textTertiary = Color(0xFF6F6558),
    accent = Color(0xFFDFB86C),
    onAccent = Color(0xFF161109),
    hairline = Color(0xFF2A241C),
    isDark = true
)

val VellumColors = FathomColors(
    background = Color(0xFFF5EFE3),
    surface = Color(0xFFE9DEC9),
    surfaceElevated = Color(0xFFDDCFB5),
    textPrimary = Color(0xFF2B2015),
    textSecondary = Color(0xFF665440),
    textTertiary = Color(0xFF968168),
    accent = Color(0xFF9E5722),
    onAccent = Color(0xFFFFF8EC),
    hairline = Color(0xFFD1C2A3),
    isDark = false
)

val AbyssColors = FathomColors(
    background = Color(0xFF0A1526),
    surface = Color(0xFF132238),
    surfaceElevated = Color(0xFF1D324F),
    textPrimary = Color(0xFFEDF5FD),
    textSecondary = Color(0xFF98B3D3),
    textTertiary = Color(0xFF5B789C),
    accent = Color(0xFF5EA8F0),
    onAccent = Color(0xFF05111E),
    hairline = Color(0xFF233B5C),
    isDark = true
)

val MossColors = FathomColors(
    background = Color(0xFF0B1F14),
    surface = Color(0xFF142F20),
    surfaceElevated = Color(0xFF1E432E),
    textPrimary = Color(0xFFEDF8EE),
    textSecondary = Color(0xFFA2CEAA),
    textTertiary = Color(0xFF5F8B68),
    accent = Color(0xFF52C478),
    onAccent = Color(0xFF06140A),
    hairline = Color(0xFF244F35),
    isDark = true
)

val SaharaColors = FathomColors(
    background = Color(0xFF22140D),
    surface = Color(0xFF331E14),
    surfaceElevated = Color(0xFF462A1D),
    textPrimary = Color(0xFFFDF1E8),
    textSecondary = Color(0xFFCEAA97),
    textTertiary = Color(0xFF8E6B58),
    accent = Color(0xFFF0783A),
    onAccent = Color(0xFF1A0A04),
    hairline = Color(0xFF523324),
    isDark = true
)

val TwilightColors = FathomColors(
    background = Color(0xFF170E2B),
    surface = Color(0xFF251744),
    surfaceElevated = Color(0xFF35225E),
    textPrimary = Color(0xFFF6F0FE),
    textSecondary = Color(0xFFBEA8E7),
    textTertiary = Color(0xFF7D65A8),
    accent = Color(0xFFB878FB),
    onAccent = Color(0xFF120422),
    hairline = Color(0xFF3E286D),
    isDark = true
)

val CinnabarColors = FathomColors(
    background = Color(0xFF240C0E),
    surface = Color(0xFF361317),
    surfaceElevated = Color(0xFF4C1B20),
    textPrimary = Color(0xFFFDECEE),
    textSecondary = Color(0xFFDCA1A7),
    textTertiary = Color(0xFF965C62),
    accent = Color(0xFFF44360),
    onAccent = Color(0xFF1E0306),
    hairline = Color(0xFF582329),
    isDark = true
)

val DawnColors = FathomColors(
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFEDF2F7),
    surfaceElevated = Color(0xFFE0E7EF),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textTertiary = Color(0xFF7F92A8),
    accent = Color(0xFF1D6FDC),
    onAccent = Color(0xFFF8FAFC),
    hairline = Color(0xFFCBD5E1),
    isDark = false
)

val LocalFathomColors = staticCompositionLocalOf { NocturneColors }
