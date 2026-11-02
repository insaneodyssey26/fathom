package com.masum.fathom.core.subscription

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.masum.fathom.core.model.user.SubscriptionState
import com.masum.fathom.core.model.user.SubscriptionTier
import com.masum.fathom.core.security.KeystoreIntegrityChecker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class SubscriptionManager(private val context: Context) {

    private val prefs: SharedPreferences = getEncryptedPreferences(context)

    companion object {
        fun getEncryptedPreferences(context: Context): SharedPreferences {
            return try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val encrypted = EncryptedSharedPreferences.create(
                    context,
                    "fathom_secure_subscription_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )

                val legacy = context.getSharedPreferences("fathom_subscription_prefs", Context.MODE_PRIVATE)
                if (legacy.contains("is_patron") && !encrypted.contains("is_patron")) {
                    val editor = encrypted.edit()
                    legacy.all.forEach { (k, v) ->
                        when (v) {
                            is Boolean -> editor.putBoolean(k, v)
                            is String -> editor.putString(k, v)
                            is Long -> editor.putLong(k, v)
                            is Int -> editor.putInt(k, v)
                            is Float -> editor.putFloat(k, v)
                        }
                    }
                    editor.apply()
                    legacy.edit().clear().apply()
                }
                encrypted
            } catch (_: Exception) {
                context.getSharedPreferences("fathom_subscription_prefs", Context.MODE_PRIVATE)
            }
        }
    }

    private val _subscriptionState = MutableStateFlow(loadState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _consecrationEvents = MutableSharedFlow<SubscriptionTier>(extraBufferCapacity = 1)
    val consecrationEvents: SharedFlow<SubscriptionTier> = _consecrationEvents.asSharedFlow()

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
        val isTampered = KeystoreIntegrityChecker.isBinaryTampered(context)

        return SubscriptionState(
            tier = if (isExpired || isTampered) SubscriptionTier.FREE else tier,
            isPro = isPatron && tier != SubscriptionTier.FREE && !isExpired && !isTampered
        )
    }

    fun consecratePatron(tier: SubscriptionTier) {
        prefs.edit()
            .putBoolean("is_patron", true)
            .putString("subscription_tier", tier.name)
            .apply()
        _subscriptionState.value = SubscriptionState(
            tier = tier,
            isPro = true
        )
        _consecrationEvents.tryEmit(tier)
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
            .remove("subscription_expires_at")
            .apply()
        _subscriptionState.value = SubscriptionState(
            tier = SubscriptionTier.FREE,
            isPro = false
        )
    }
}
