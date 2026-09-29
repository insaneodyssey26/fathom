package com.masum.fathom.core.audio

import androidx.compose.runtime.staticCompositionLocalOf

val LocalSacredAudioEngine = staticCompositionLocalOf<SacredAudioEngine> {
    error("SacredAudioEngine not provided")
}

val LocalSanctuaryAmbienceEngine = staticCompositionLocalOf<SanctuaryAmbienceEngine> {
    error("SanctuaryAmbienceEngine not provided")
}

val LocalOpenAmbience = staticCompositionLocalOf<() -> Unit> {
    error("LocalOpenAmbience not provided")
}
