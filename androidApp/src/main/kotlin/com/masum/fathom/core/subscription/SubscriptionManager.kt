package com.masum.fathom.core.subscription

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import com.masum.fathom.core.model.user.SubscriptionState
import com.masum.fathom.core.model.user.SubscriptionTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SubscriptionManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("fathom_subscription_prefs", Context.MODE_PRIVATE)

    private val _subscriptionState = MutableStateFlow(loadState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private fun loadState(): SubscriptionState {
        val tierName = prefs.getString("subscription_tier", SubscriptionTier.FREE.name) ?: SubscriptionTier.FREE.name
        val tier = try {
            SubscriptionTier.valueOf(tierName)
        } catch (_: Exception) {
            SubscriptionTier.FREE
        }
        val isPro = tier != SubscriptionTier.FREE
        return SubscriptionState(tier = tier, isPro = isPro)
    }

    fun launchDodoCheckout(tier: SubscriptionTier) {
        val checkoutUrl = when (tier) {
            SubscriptionTier.MONTHLY -> "https://checkout.dodopayments.com/buy/fathom_monthly"
            SubscriptionTier.SIX_MONTH -> "https://checkout.dodopayments.com/buy/fathom_six_month"
            SubscriptionTier.ANNUAL -> "https://checkout.dodopayments.com/buy/fathom_annual"
            SubscriptionTier.FREE -> ""
        }

        if (checkoutUrl.isNotEmpty()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                purchaseTier(tier)
            }
        }
    }

    fun purchaseTier(tier: SubscriptionTier) {
        prefs.edit().putString("subscription_tier", tier.name).apply()
        _subscriptionState.value = SubscriptionState(
            tier = tier,
            isPro = tier != SubscriptionTier.FREE
        )
    }

    fun restorePurchases(): Boolean {
        val tier = loadState().tier
        _subscriptionState.value = SubscriptionState(
            tier = tier,
            isPro = tier != SubscriptionTier.FREE
        )
        return tier != SubscriptionTier.FREE
    }

    fun resetToFree() {
        prefs.edit().putString("subscription_tier", SubscriptionTier.FREE.name).apply()
        _subscriptionState.value = SubscriptionState(
            tier = SubscriptionTier.FREE,
            isPro = false
        )
    }
}
