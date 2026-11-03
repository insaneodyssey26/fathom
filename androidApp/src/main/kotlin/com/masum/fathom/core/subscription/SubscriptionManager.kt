package com.masum.fathom.core.subscription

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
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

    fun getPurchaseTime(): Long = prefs.getLong("purchase_time", 0L)
    fun isAutoRenewing(): Boolean = prefs.getBoolean("is_auto_renewing", true)
    fun getOrderId(): String? = prefs.getString("order_id", null)

    private suspend fun processPurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    billingClientWrapper.acknowledgePurchase(purchase.purchaseToken)
                }
                val tier = resolveTierFromProducts(purchase.products)
                if (tier != SubscriptionTier.FREE) {
                    persistPatronState(
                        tier = tier,
                        purchaseToken = purchase.purchaseToken,
                        purchaseTime = purchase.purchaseTime,
                        autoRenewing = purchase.isAutoRenewing,
                        orderId = purchase.orderId
                    )
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

    private fun persistPatronState(
        tier: SubscriptionTier,
        purchaseToken: String?,
        purchaseTime: Long = 0L,
        autoRenewing: Boolean = true,
        orderId: String? = null
    ) {
        val editor = prefs.edit()
            .putBoolean("is_patron", true)
            .putString("subscription_tier", tier.name)

        if (purchaseTime > 0L) {
            editor.putLong("purchase_time", purchaseTime)
        } else if (!prefs.contains("purchase_time")) {
            editor.putLong("purchase_time", System.currentTimeMillis())
        }

        editor.putBoolean("is_auto_renewing", autoRenewing)

        if (!orderId.isNullOrEmpty()) {
            editor.putString("order_id", orderId)
        }

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
            val bestPurchase = activePurchases.maxByOrNull {
                resolveTierFromProducts(it.products).ordinal
            }
            val bestTier = bestPurchase?.let { resolveTierFromProducts(it.products) } ?: SubscriptionTier.FREE

            if (bestTier != SubscriptionTier.FREE && bestPurchase != null) {
                persistPatronState(
                    tier = bestTier,
                    purchaseToken = bestPurchase.purchaseToken,
                    purchaseTime = bestPurchase.purchaseTime,
                    autoRenewing = bestPurchase.isAutoRenewing,
                    orderId = bestPurchase.orderId
                )
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

        val restoredPurchase = activePurchases.maxByOrNull {
            resolveTierFromProducts(it.products).ordinal
        }
        val restoredTier = restoredPurchase?.let { resolveTierFromProducts(it.products) } ?: SubscriptionTier.FREE

        if (restoredTier != SubscriptionTier.FREE && restoredPurchase != null) {
            persistPatronState(
                tier = restoredTier,
                purchaseToken = restoredPurchase.purchaseToken,
                purchaseTime = restoredPurchase.purchaseTime,
                autoRenewing = restoredPurchase.isAutoRenewing,
                orderId = restoredPurchase.orderId
            )
            _consecrationEvents.tryEmit(restoredTier)
            return RestoreResult.Success(restoredTier)
        }

        return RestoreResult.NoPurchasesFound
    }

    fun openManageSubscriptions(context: Context, tier: SubscriptionTier = _subscriptionState.value.tier) {
        val sku = tier.id
        val packageName = context.packageName
        val intent = if (tier == SubscriptionTier.MONTHLY || tier == SubscriptionTier.ANNUAL) {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/account/subscriptions?sku=$sku&package=$packageName")
            )
        } else {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/account/subscriptions")
            )
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/account/subscriptions")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(fallback)
            } catch (_: Exception) {}
        }
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
