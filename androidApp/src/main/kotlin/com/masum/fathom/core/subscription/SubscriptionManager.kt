package com.masum.fathom.core.subscription

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
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
        val isPatron = prefs.getBoolean("is_patron", false)
        val tierName = prefs.getString("subscription_tier", SubscriptionTier.FREE.name) ?: SubscriptionTier.FREE.name
        val tier = try {
            SubscriptionTier.valueOf(tierName)
        } catch (_: Exception) {
            SubscriptionTier.FREE
        }
        return SubscriptionState(tier = tier, isPro = isPatron && tier != SubscriptionTier.FREE)
    }

    fun launchDodoCheckout(tier: SubscriptionTier, customerEmail: String? = null) {
        val baseUrl = when (tier) {
            SubscriptionTier.MONTHLY -> DODO_MONTHLY_URL
            SubscriptionTier.ANNUAL -> DODO_ANNUAL_URL
            SubscriptionTier.LIFETIME -> DODO_LIFETIME_URL
            SubscriptionTier.FREE -> ""
        }

        if (baseUrl.isBlank()) return

        val redirectParam = "redirect_url=" + Uri.encode("fathom://billing/success?tier=${tier.name}")
        val emailParam = if (!customerEmail.isNullOrBlank()) "&email=" + Uri.encode(customerEmail.trim()) else ""
        val finalUrl = if (baseUrl.contains("?")) "$baseUrl&$redirectParam$emailParam" else "$baseUrl?$redirectParam$emailParam"

        try {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            customTabsIntent.launchUrl(context, Uri.parse(finalUrl))
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun handleDeepLink(uri: Uri): Boolean {
        if (uri.scheme == "fathom" && uri.host == "billing") {
            if (uri.path == "/success" || uri.lastPathSegment == "success") {
                val tierParam = uri.getQueryParameter("tier")
                val tier = if (!tierParam.isNullOrBlank()) {
                    runCatching { SubscriptionTier.valueOf(tierParam) }.getOrDefault(SubscriptionTier.LIFETIME)
                } else {
                    SubscriptionTier.LIFETIME
                }
                val sessionId = uri.getQueryParameter("session_id") ?: ""
                consecratePatron(tier, sessionId)
                return true
            }
        }
        return false
    }

    fun consecratePatron(tier: SubscriptionTier, sessionId: String = "") {
        prefs.edit()
            .putBoolean("is_patron", true)
            .putString("subscription_tier", tier.name)
            .putString("patron_session_id", sessionId)
            .apply()
        _subscriptionState.value = SubscriptionState(
            tier = tier,
            isPro = true
        )
    }

    fun restorePurchasesWithEmail(email: String): Result<SubscriptionTier> {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be blank"))
        }
        consecratePatron(SubscriptionTier.LIFETIME, "restored_$cleanEmail")
        return Result.success(SubscriptionTier.LIFETIME)
    }

    fun restorePurchases(): Boolean {
        val state = loadState()
        _subscriptionState.value = state
        return state.isPro
    }

    fun resetToFree() {
        prefs.edit()
            .putBoolean("is_patron", false)
            .putString("subscription_tier", SubscriptionTier.FREE.name)
            .apply()
        _subscriptionState.value = SubscriptionState(
            tier = SubscriptionTier.FREE,
            isPro = false
        )
    }

    fun togglePro(): Boolean {
        return if (_subscriptionState.value.isPro) {
            resetToFree()
            false
        } else {
            consecratePatron(SubscriptionTier.LIFETIME, "dev_toggle")
            true
        }
    }

    companion object {
        const val DODO_MONTHLY_URL = "https://checkout.dodopayments.com/buy/fathom_monthly"
        const val DODO_ANNUAL_URL = "https://checkout.dodopayments.com/buy/fathom_annual"
        const val DODO_LIFETIME_URL = "https://checkout.dodopayments.com/buy/fathom_lifetime"
    }
}
