package com.masum.fathom.core.sanctuary

import androidx.compose.runtime.staticCompositionLocalOf

val LocalSanctuaryPreferences = staticCompositionLocalOf<SanctuaryPreferences> {
    error("SanctuaryPreferences not provided")
}

val LocalSanctuaryDataEngine = staticCompositionLocalOf<SanctuaryDataEngine> {
    error("SanctuaryDataEngine not provided")
}

val LocalOpenSanctuary = staticCompositionLocalOf<() -> Unit> {
    {}
}
