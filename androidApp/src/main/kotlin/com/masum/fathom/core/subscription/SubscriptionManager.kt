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

import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SubscriptionManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("fathom_subscription_prefs", Context.MODE_PRIVATE)
    val licenseEngine = LicenseEngine()
    private val scope = CoroutineScope(Dispatchers.Main)

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
        val expiresAt = prefs.getLong("subscription_expires_at", 0L)
        val isExpired = expiresAt > 0 && System.currentTimeMillis() > expiresAt
        return SubscriptionState(
            tier = if (isExpired) SubscriptionTier.FREE else tier,
            isPro = isPatron && tier != SubscriptionTier.FREE && !isExpired
        )
    }

    fun getDeviceId(): String {
        var id = prefs.getString("fathom_device_id", null)
        if (id.isNullOrBlank()) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString("fathom_device_id", id).apply()
        }
        return id
    }

    fun getSavedLicenseKey(): String? {
        return prefs.getString("fathom_license_key", null)
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
        if (uri.scheme == "fathom" && (uri.host == "billing" || uri.host == "activate")) {
            val licenseKey = uri.getQueryParameter("license_key") ?: uri.getQueryParameter("key")
            val tierParam = uri.getQueryParameter("tier")
            val tier = if (!tierParam.isNullOrBlank()) {
                runCatching { SubscriptionTier.valueOf(tierParam) }.getOrDefault(SubscriptionTier.LIFETIME)
            } else {
                SubscriptionTier.LIFETIME
            }

            if (!licenseKey.isNullOrBlank()) {
                scope.launch {
                    activateWithKey(licenseKey, null)
                }
                return true
            }

            val sessionId = uri.getQueryParameter("session_id") ?: ""
            consecratePatron(tier, sessionId)
            return true
        }
        return false
    }

    suspend fun activateWithKey(licenseKey: String, email: String?): LicenseValidationResult {
        val devId = getDeviceId()
        val result = licenseEngine.activateLicenseRemote(
            licenseToken = licenseKey,
            email = email,
            deviceId = devId
        )

        if (result.isValid) {
            prefs.edit()
                .putBoolean("is_patron", true)
                .putString("subscription_tier", result.tier.name)
                .putString("fathom_license_key", licenseKey.trim().uppercase())
                .putLong("subscription_expires_at", result.expiresAtEpochMs)
                .putString("signed_token", result.signedToken ?: "")
                .putString("token_signature", result.tokenSignature ?: "")
                .apply()

            _subscriptionState.value = SubscriptionState(
                tier = result.tier,
                isPro = true
            )
        }
        return result
    }

    suspend fun fetchActiveDevices(): List<ActiveDeviceInfo> {
        val key = getSavedLicenseKey() ?: return emptyList()
        return licenseEngine.fetchActiveDevicesRemote(key, getDeviceId())
    }

    suspend fun revokeDevice(deviceId: String): Boolean {
        val key = getSavedLicenseKey() ?: return false
        val success = licenseEngine.revokeDeviceRemote(key, deviceId)
        if (success && deviceId == getDeviceId()) {
            resetToFree()
        }
        return success
    }

    suspend fun deactivateCurrentDevice(): Boolean {
        val key = getSavedLicenseKey()
        if (key != null) {
            licenseEngine.revokeDeviceRemote(key, getDeviceId())
        }
        resetToFree()
        return true
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

    fun restorePurchases(): Boolean {
        val state = loadState()
        _subscriptionState.value = state
        return state.isPro
    }

    fun resetToFree() {
        prefs.edit()
            .putBoolean("is_patron", false)
            .putString("subscription_tier", SubscriptionTier.FREE.name)
            .remove("fathom_license_key")
            .remove("signed_token")
            .remove("token_signature")
            .apply()
        _subscriptionState.value = SubscriptionState(
            tier = SubscriptionTier.FREE,
            isPro = false
        )
    }

    companion object {
        const val DODO_MONTHLY_URL = "https://checkout.dodopayments.com/buy/pdt_0NpSdYmrmyFzfotEi1iID?quantity=1"
        const val DODO_ANNUAL_URL = "https://checkout.dodopayments.com/buy/pdt_0NpSfCIOCfkSq1U8dv2NN?quantity=1"
        const val DODO_LIFETIME_URL = "https://checkout.dodopayments.com/buy/pdt_0NpSfeVfdB1dXRI9yHTUe?quantity=1"
    }
}
