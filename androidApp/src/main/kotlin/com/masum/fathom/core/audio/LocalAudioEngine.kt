package com.masum.fathom.core.audio

import androidx.compose.runtime.staticCompositionLocalOf

val LocalSacredAudioEngine = staticCompositionLocalOf<SacredAudioEngine> {
    error("SacredAudioEngine not provided")
}
