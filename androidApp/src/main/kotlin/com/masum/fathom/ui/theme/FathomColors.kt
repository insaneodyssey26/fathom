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
    val accentGold: Color,
    val borderSubtle: Color,
    val isDark: Boolean
)

val ObsidianColors = FathomColors(
    background = Color(0xFF09090B),
    surface = Color(0xFF121214),
    surfaceElevated = Color(0xFF18181B),
    textPrimary = Color(0xFFFAFAFA),
    textSecondary = Color(0xFFA1A1AA),
    textTertiary = Color(0xFF71717A),
    accentGold = Color(0xFFD4AF37),
    borderSubtle = Color(0xFF27272A),
    isDark = true
)

val SepiaColors = FathomColors(
    background = Color(0xFFFBF7EE),
    surface = Color(0xFFF4EFE6),
    surfaceElevated = Color(0xFFEBE4D8),
    textPrimary = Color(0xFF1C1917),
    textSecondary = Color(0xFF57534E),
    textTertiary = Color(0xFF78716C),
    accentGold = Color(0xFFB45309),
    borderSubtle = Color(0xFFE7E0D3),
    isDark = false
)

val SlateColors = FathomColors(
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceElevated = Color(0xFF334155),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textTertiary = Color(0xFF64748B),
    accentGold = Color(0xFFFBBF24),
    borderSubtle = Color(0xFF334155),
    isDark = true
)

val ForestColors = FathomColors(
    background = Color(0xFF0A120D),
    surface = Color(0xFF111D15),
    surfaceElevated = Color(0xFF1A2A20),
    textPrimary = Color(0xFFF2F8F4),
    textSecondary = Color(0xFF9EBAA8),
    textTertiary = Color(0xFF6B8774),
    accentGold = Color(0xFFD4AF37),
    borderSubtle = Color(0xFF233B2C),
    isDark = true
)

val LocalFathomColors = staticCompositionLocalOf { ObsidianColors }
