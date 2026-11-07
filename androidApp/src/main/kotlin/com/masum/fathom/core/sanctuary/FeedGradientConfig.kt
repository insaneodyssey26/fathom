package com.masum.fathom.core.sanctuary

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class GradientFlow(val displayName: String, val subtitle: String) {
    TOP_TO_BOTTOM(
        displayName = "Vertical",
        subtitle = "Flows from top to bottom"
    ),
    DIAGONAL(
        displayName = "Diagonal",
        subtitle = "Sweeps from top-left to bottom-right"
    ),
    RADIAL_ORB(
        displayName = "Radial Orb",
        subtitle = "Centered luminous aura"
    ),
    BOTTOM_UP(
        displayName = "Rising",
        subtitle = "Rises from bottom to top"
    ),
    CUSTOM_ANGLE(
        displayName = "Custom Angle",
        subtitle = "Rotated freely with angle dial"
    );

    fun createBrush(
        colors: List<Color>,
        width: Float,
        height: Float,
        angleDegrees: Float = 135f
    ): Brush {
        val safeColors = if (colors.size < 2) {
            listOf(colors.firstOrNull() ?: Color(0xFF0F172A), Color(0xFF1E293B))
        } else {
            colors
        }

        return when (this) {
            TOP_TO_BOTTOM -> Brush.linearGradient(
                colors = safeColors,
                start = Offset(0f, 0f),
                end = Offset(0f, height)
            )
            DIAGONAL -> Brush.linearGradient(
                colors = safeColors,
                start = Offset(0f, 0f),
                end = Offset(width, height)
            )
            RADIAL_ORB -> Brush.radialGradient(
                colors = safeColors,
                center = Offset(width * 0.5f, height * 0.45f),
                radius = maxOf(width, height) * 0.75f
            )
            BOTTOM_UP -> Brush.linearGradient(
                colors = safeColors,
                start = Offset(0f, height),
                end = Offset(0f, 0f)
            )
            CUSTOM_ANGLE -> {
                val rad = Math.toRadians(angleDegrees.toDouble())
                val cx = width * 0.5f
                val cy = height * 0.5f
                val len = Math.hypot(width.toDouble(), height.toDouble()).toFloat() * 0.5f
                val dx = (Math.cos(rad) * len).toFloat()
                val dy = (Math.sin(rad) * len).toFloat()
                Brush.linearGradient(
                    colors = safeColors,
                    start = Offset(cx - dx, cy - dy),
                    end = Offset(cx + dx, cy + dy)
                )
            }
        }
    }
}

data class FeedGradientPreset(
    val id: String,
    val name: String,
    val colorA: String,
    val colorB: String,
    val flow: GradientFlow
)

object FeedGradientPresets {
    val presets = listOf(
        FeedGradientPreset("aurora", "Northern Aurora", "#0B1D28", "#1A3A34", GradientFlow.DIAGONAL),
        FeedGradientPreset("velvet_night", "Velvet Night", "#0D0B18", "#1E1838", GradientFlow.TOP_TO_BOTTOM),
        FeedGradientPreset("desert_twilight", "Desert Dusk", "#1C1117", "#381E28", GradientFlow.RADIAL_ORB),
        FeedGradientPreset("parchment_gold", "Sacred Vellum", "#2B2115", "#14110E", GradientFlow.TOP_TO_BOTTOM),
        FeedGradientPreset("celestial_slate", "Celestial Slate", "#0F172A", "#1E293B", GradientFlow.BOTTOM_UP),
        FeedGradientPreset("forest_sanctuary", "Deep Moss", "#0B1A12", "#182C20", GradientFlow.DIAGONAL)
    )

    fun parseHexColor(hex: String, fallback: Color = Color(0xFF0B1220)): Color {
        val clean = hex.trim().removePrefix("#")
        return when (clean.length) {
            6 -> {
                runCatching {
                    val r = clean.substring(0, 2).toInt(16)
                    val g = clean.substring(2, 4).toInt(16)
                    val b = clean.substring(4, 6).toInt(16)
                    Color(r, g, b, 255)
                }.getOrDefault(fallback)
            }
            8 -> {
                runCatching {
                    val a = clean.substring(0, 2).toInt(16)
                    val r = clean.substring(2, 4).toInt(16)
                    val g = clean.substring(4, 6).toInt(16)
                    val b = clean.substring(6, 8).toInt(16)
                    Color(r, g, b, a)
                }.getOrDefault(fallback)
            }
            3 -> {
                runCatching {
                    val r = clean.substring(0, 1).repeat(2).toInt(16)
                    val g = clean.substring(1, 2).repeat(2).toInt(16)
                    val b = clean.substring(2, 3).repeat(2).toInt(16)
                    Color(r, g, b, 255)
                }.getOrDefault(fallback)
            }
            else -> fallback
        }
    }

    fun calculateLuminance(color: Color): Float {
        return (0.299f * color.red) + (0.587f * color.green) + (0.114f * color.blue)
    }

    fun isDarkGradient(colorA: Color, colorB: Color): Boolean {
        val avgLum = (calculateLuminance(colorA) + calculateLuminance(colorB)) / 2f
        return avgLum < 0.45f
    }

    fun isValidHex(hex: String): Boolean {
        val clean = hex.trim().removePrefix("#")
        return (clean.length == 6 || clean.length == 3 || clean.length == 8) &&
                clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }
}
