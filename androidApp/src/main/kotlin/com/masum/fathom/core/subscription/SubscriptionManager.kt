package com.masum.fathom.core.subscription

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.android.billingclient.api.Purchase
import com.masum.fathom.core.model.user.SubscriptionState
import com.masum.fathom.core.model.user.SubscriptionTier
import com.masum.fathom.core.security.KeystoreIntegrityChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface RestoreResult {
    data class Success(val tier: SubscriptionTier) : RestoreResult
    data object NoPurchasesFound : RestoreResult
    data class Error(val message: String) : RestoreResult
}

class SubscriptionManager(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    private val prefs: SharedPreferences = getEncryptedPreferences(context)
    val billingClientWrapper: PlayBillingClientWrapper = PlayBillingClientWrapper(context, externalScope)

    val productPricing: StateFlow<Map<SubscriptionTier, ProductTierPricing>> = billingClientWrapper.productPricing

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

    init {
        observeBillingEvents()
    }

    private fun observeBillingEvents() {
        externalScope.launch {
            billingClientWrapper.purchaseEvents.collectLatest { event ->
                when (event) {
                    is BillingPurchaseEvent.Purchased -> {
                        processPurchases(event.purchases)
                    }
                    is BillingPurchaseEvent.UserCancelled -> {}
                    is BillingPurchaseEvent.Error -> {}
                }
            }
        }

        externalScope.launch {
            billingClientWrapper.connectionStatus.collectLatest { status ->
                if (status is BillingConnectionStatus.Connected) {
                    syncPurchasesWithGooglePlay()
                }
            }
        }
    }

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

    private suspend fun processPurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    billingClientWrapper.acknowledgePurchase(purchase.purchaseToken)
                }
                val tier = resolveTierFromProducts(purchase.products)
                if (tier != SubscriptionTier.FREE) {
                    persistPatronState(tier, purchase.purchaseToken)
                    _consecrationEvents.tryEmit(tier)
                    return
                }
            }
        }
    }

    private fun resolveTierFromProducts(products: List<String>): SubscriptionTier {
        return when {
            products.contains(SubscriptionTier.LIFETIME.id) -> SubscriptionTier.LIFETIME
            products.contains(SubscriptionTier.ANNUAL.id) -> SubscriptionTier.ANNUAL
            products.contains(SubscriptionTier.MONTHLY.id) -> SubscriptionTier.MONTHLY
            else -> SubscriptionTier.FREE
        }
    }

    private fun persistPatronState(tier: SubscriptionTier, purchaseToken: String?) {
        val editor = prefs.edit()
            .putBoolean("is_patron", true)
            .putString("subscription_tier", tier.name)

        if (!purchaseToken.isNullOrEmpty()) {
            editor.putString("purchase_token", purchaseToken)
        }

        editor.apply()

        _subscriptionState.value = SubscriptionState(
            tier = tier,
            isPro = true
        )
    }

    suspend fun syncPurchasesWithGooglePlay(): Boolean {
        val activePurchases = billingClientWrapper.queryActivePurchases()
        if (activePurchases.isNotEmpty()) {
            for (purchase in activePurchases) {
                if (!purchase.isAcknowledged) {
                    billingClientWrapper.acknowledgePurchase(purchase.purchaseToken)
                }
            }
            val bestTier = activePurchases
                .map { resolveTierFromProducts(it.products) }
                .maxByOrNull { it.ordinal } ?: SubscriptionTier.FREE

            if (bestTier != SubscriptionTier.FREE) {
                val token = activePurchases.firstOrNull()?.purchaseToken
                persistPatronState(bestTier, token)
                return true
            }
        } else if (_subscriptionState.value.isPro) {
            val hasToken = !prefs.getString("purchase_token", null).isNullOrEmpty()
            if (hasToken) {
                resetToFree()
            }
        }
        return false
    }

    fun launchPurchase(activity: Activity, tier: SubscriptionTier): Boolean {
        val pricing = productPricing.value[tier] ?: return false
        val result = billingClientWrapper.launchBillingFlow(activity, pricing)
        return result.responseCode == com.android.billingclient.api.BillingClient.BillingResponseCode.OK
    }

    suspend fun restorePurchases(): RestoreResult {
        billingClientWrapper.startConnection()
        val activePurchases = billingClientWrapper.queryActivePurchases()
        if (activePurchases.isEmpty()) {
            return RestoreResult.NoPurchasesFound
        }

        for (purchase in activePurchases) {
            if (!purchase.isAcknowledged) {
                billingClientWrapper.acknowledgePurchase(purchase.purchaseToken)
            }
        }

        val restoredTier = activePurchases
            .map { resolveTierFromProducts(it.products) }
            .maxByOrNull { it.ordinal } ?: SubscriptionTier.FREE

        if (restoredTier != SubscriptionTier.FREE) {
            persistPatronState(restoredTier, activePurchases.first().purchaseToken)
            _consecrationEvents.tryEmit(restoredTier)
            return RestoreResult.Success(restoredTier)
        }

        return RestoreResult.NoPurchasesFound
    }

    fun consecratePatron(tier: SubscriptionTier) {
        persistPatronState(tier, null)
        _consecrationEvents.tryEmit(tier)
    }

    fun resetToFree() {
        prefs.edit()
            .putBoolean("is_patron", false)
            .putString("subscription_tier", SubscriptionTier.FREE.name)
            .remove("subscription_expires_at")
            .remove("purchase_token")
            .apply()
        _subscriptionState.value = SubscriptionState(
            tier = SubscriptionTier.FREE,
            isPro = false
        )
    }
}
