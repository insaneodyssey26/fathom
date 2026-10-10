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

private val NeutralSeed = ToneSeed(Color(0xFF9E845E), Color(0xFF5A4124))

private fun seedFor(id: TraditionId?): ToneSeed = when (id) {
    TraditionId.HINDUISM -> ToneSeed(Color(0xFFB5572B), Color(0xFF782318))
    TraditionId.BUDDHISM -> ToneSeed(Color(0xFFA87A28), Color(0xFF6B3012))
    TraditionId.STOICISM -> ToneSeed(Color(0xFF6B7E99), Color(0xFF2C394A))
    TraditionId.TAOISM -> ToneSeed(Color(0xFF4E8760), Color(0xFF1B4734))
    TraditionId.ISLAM -> ToneSeed(Color(0xFF247A6B), Color(0xFF103352))
    TraditionId.CHRISTIANITY -> ToneSeed(Color(0xFF9E3748), Color(0xFF4A1020))
    TraditionId.JUDAISM -> ToneSeed(Color(0xFF5D52A3), Color(0xFF1B2254))
    null -> NeutralSeed
}

fun toneFor(id: TraditionId?, colors: FathomColors): Tone {
    val seed = if (id != null) seedFor(id) else ToneSeed(colors.accent, lerp(colors.background, colors.accent, 0.45f))
    val bg = colors.background
    val mute = if (id == null) 0.65f else 0.85f
    return if (colors.isDark) {
        Tone(
            hue = seed.hue,
            glowPrimary = lerp(bg, seed.hue, 0.28f * mute),
            glowSecondary = lerp(bg, seed.shift, 0.35f * mute),
            onHue = colors.onAccent
        )
    } else {
        Tone(
            hue = if (id == null) colors.accent else lerp(seed.hue, Color(0xFF1A140C), 0.48f),
            glowPrimary = lerp(bg, seed.hue, 0.18f * mute),
            glowSecondary = lerp(bg, seed.shift, 0.14f * mute),
            onHue = colors.onAccent
        )
    }
}

val LocalTone = compositionLocalOf {
    Tone(NeutralSeed.hue, NocturneColors.background, NocturneColors.background, Color(0xFFF1EADD))
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
