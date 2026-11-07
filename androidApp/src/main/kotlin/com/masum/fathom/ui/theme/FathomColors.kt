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
    background = Color(0xFF040810),
    surface = Color(0xFF0C1420),
    surfaceElevated = Color(0xFF142032),
    textPrimary = Color(0xFFEAF2FB),
    textSecondary = Color(0xFF8BA5C4),
    textTertiary = Color(0xFF506886),
    accent = Color(0xFF4A98EB),
    onAccent = Color(0xFF02070E),
    hairline = Color(0xFF18283E),
    isDark = true
)

val MossColors = FathomColors(
    background = Color(0xFF040C07),
    surface = Color(0xFF0B1910),
    surfaceElevated = Color(0xFF13281A),
    textPrimary = Color(0xFFEAF5EB),
    textSecondary = Color(0xFF91BE9A),
    textTertiary = Color(0xFF527D5B),
    accent = Color(0xFF42B768),
    onAccent = Color(0xFF020904),
    hairline = Color(0xFF173622),
    isDark = true
)

val SaharaColors = FathomColors(
    background = Color(0xFF0B0705),
    surface = Color(0xFF18100B),
    surfaceElevated = Color(0xFF271A12),
    textPrimary = Color(0xFFFBF0E6),
    textSecondary = Color(0xFFC49F8B),
    textTertiary = Color(0xFF7E5D4B),
    accent = Color(0xFFE66E30),
    onAccent = Color(0xFF0E0402),
    hairline = Color(0xFF352217),
    isDark = true
)

val TwilightColors = FathomColors(
    background = Color(0xFF080410),
    surface = Color(0xFF130B22),
    surfaceElevated = Color(0xFF201336),
    textPrimary = Color(0xFFF4EDFB),
    textSecondary = Color(0xFFB197DD),
    textTertiary = Color(0xFF6F569B),
    accent = Color(0xFFA965F2),
    onAccent = Color(0xFF0A0216),
    hairline = Color(0xFF2C194B),
    isDark = true
)

val CinnabarColors = FathomColors(
    background = Color(0xFF0B0405),
    surface = Color(0xFF1A0A0C),
    surfaceElevated = Color(0xFF2A1114),
    textPrimary = Color(0xFFFBE9EB),
    textSecondary = Color(0xFFD2939A),
    textTertiary = Color(0xFF884F56),
    accent = Color(0xFFEA3854),
    onAccent = Color(0xFF140204),
    hairline = Color(0xFF3B161B),
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

val AuroraColors = FathomColors(
    background = Color(0xFF07131A),
    surface = Color(0xFF0E232F),
    surfaceElevated = Color(0xFF163445),
    textPrimary = Color(0xFFE8F7F9),
    textSecondary = Color(0xFF88CBD8),
    textTertiary = Color(0xFF4A8B99),
    accent = Color(0xFF2DD4BF),
    onAccent = Color(0xFF04181B),
    hairline = Color(0xFF1B3F52),
    isDark = true
)

val CelestialColors = FathomColors(
    background = Color(0xFF090D1A),
    surface = Color(0xFF121B30),
    surfaceElevated = Color(0xFF1C2947),
    textPrimary = Color(0xFFEEF3FF),
    textSecondary = Color(0xFF98AEE0),
    textTertiary = Color(0xFF576E9E),
    accent = Color(0xFF6366F1),
    onAccent = Color(0xFF080C1E),
    hairline = Color(0xFF233257),
    isDark = true
)

val LocalFathomColors = staticCompositionLocalOf { NocturneColors }
