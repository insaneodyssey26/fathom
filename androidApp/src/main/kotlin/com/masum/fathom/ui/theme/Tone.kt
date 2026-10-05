package com.masum.fathom.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.masum.fathom.core.model.TraditionId

@Immutable
data class Tone(
    val hue: Color,
    val glowPrimary: Color,
    val glowSecondary: Color,
    val onHue: Color
)

private data class ToneSeed(val hue: Color, val shift: Color)

private val NeutralSeed = ToneSeed(Color(0xFFD9BE92), Color(0xFF8C6A3F))

private fun seedFor(id: TraditionId?): ToneSeed = when (id) {
    TraditionId.HINDUISM -> ToneSeed(Color(0xFFF07A45), Color(0xFFB3261E))
    TraditionId.BUDDHISM -> ToneSeed(Color(0xFFE2AE48), Color(0xFF9C4A1D))
    TraditionId.STOICISM -> ToneSeed(Color(0xFF9DB0CC), Color(0xFF4B5E7A))
    TraditionId.TAOISM -> ToneSeed(Color(0xFF86C99A), Color(0xFF2F7A66))
    TraditionId.ISLAM -> ToneSeed(Color(0xFF45BFB8), Color(0xFF1F4FA3))
    TraditionId.CHRISTIANITY -> ToneSeed(Color(0xFFD9546B), Color(0xFF6E1B3A))
    TraditionId.JUDAISM -> ToneSeed(Color(0xFF9585F0), Color(0xFF2F3E9E))
    null -> NeutralSeed
}

fun toneFor(id: TraditionId?, colors: FathomColors): Tone {
    val seed = seedFor(id)
    val bg = colors.background
    val mute = if (id == null) 0.42f else 1f
    return if (colors.isDark) {
        Tone(
            hue = seed.hue,
            glowPrimary = lerp(bg, seed.hue, 0.34f * mute),
            glowSecondary = lerp(bg, seed.shift, 0.46f * mute),
            onHue = Color(0xFF0B0A09)
        )
    } else {
        Tone(
            hue = lerp(seed.hue, Color(0xFF1A140C), 0.38f),
            glowPrimary = lerp(bg, seed.hue, 0.22f * mute),
            glowSecondary = lerp(bg, seed.shift, 0.14f * mute),
            onHue = Color(0xFFFBF6EA)
        )
    }
}

val LocalTone = compositionLocalOf {
    Tone(NeutralSeed.hue, NocturneColors.background, NocturneColors.background, Color.Black)
}

@Composable
fun rememberTone(id: TraditionId?): Tone {
    val colors = FathomTheme.colors
    val target = remember(id, colors.isDark, colors.background) { toneFor(id, colors) }
    val hue by animateColorAsState(target.hue, FathomMotion.tone(), label = "tone_hue")
    val primary by animateColorAsState(target.glowPrimary, FathomMotion.tone(), label = "tone_a")
    val secondary by animateColorAsState(target.glowSecondary, FathomMotion.tone(), label = "tone_b")
    val onHue by animateColorAsState(target.onHue, FathomMotion.tone(), label = "tone_on")
    return Tone(hue, primary, secondary, onHue)
}
