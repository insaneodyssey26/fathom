package com.masum.fathom.core.subscription

import androidx.compose.runtime.staticCompositionLocalOf
import com.masum.fathom.core.model.user.SubscriptionState

val LocalSubscriptionManager = staticCompositionLocalOf<SubscriptionManager> {
    error("SubscriptionManager not provided")
}

val LocalSubscriptionState = staticCompositionLocalOf<SubscriptionState> {
    SubscriptionState()
}

val LocalOpenPaywall = staticCompositionLocalOf<() -> Unit> {
    {}
}
