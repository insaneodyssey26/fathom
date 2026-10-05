package com.masum.fathom.ui.shell

import androidx.compose.runtime.staticCompositionLocalOf

val LocalOpenMore = staticCompositionLocalOf<() -> Unit> { {} }

val LocalOpenLegal = staticCompositionLocalOf<(LegalDoc) -> Unit> { {} }
