package com.masum.fathom.core.subscription

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.android.billingclient.api.Purchase
import com.masum.fathom.BuildConfig
import com.masum.fathom.core.billing.FathomPurchaseVerifier
import com.masum.fathom.core.model.user.SubscriptionState
import com.masum.fathom.core.model.user.SubscriptionTier
import com.masum.fathom.core.security.FathomKeystoreManager
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

    private fun decodeKey(bytes: ByteArray, salt: Byte = 0x55.toByte()): String {
        val out = ByteArray(bytes.size)
        for (i in bytes.indices) {
            out[i] = (bytes[i].toInt() xor salt.toInt()).toByte()
        }
        return String(out, Charsets.UTF_8)
    }

    private val KEY_IS_PATRON = decodeKey(byteArrayOf(51, 52, 33, 61, 58, 56, 10, 60, 38, 10, 37, 52, 33, 39, 58, 59))
    private val KEY_SUBSCRIPTION_TIER = decodeKey(byteArrayOf(51, 52, 33, 61, 58, 56, 10, 38, 32, 55, 38, 54, 39, 60, 37, 33, 60, 58, 59, 10, 33, 60, 48, 39))
    private val KEY_PURCHASE_TOKEN = decodeKey(byteArrayOf(51, 52, 33, 61, 58, 56, 10, 37, 32, 39, 54, 61, 52, 38, 48, 10, 33, 58, 62, 48, 59))
    private val KEY_ORDER_ID = decodeKey(byteArrayOf(51, 52, 33, 61, 58, 56, 10, 58, 39, 49, 48, 39, 10, 60, 49))
    private val KEY_ENTITLEMENT_HMAC = decodeKey(byteArrayOf(51, 52, 33, 61, 58, 56, 10, 48, 59, 33, 60, 33, 57, 48, 56, 48, 59, 33, 10, 61, 56, 52, 54))
    private val KEY_PURCHASE_TIME = decodeKey(byteArrayOf(51, 52, 33, 61, 58, 56, 10, 37, 32, 39, 54, 61, 52, 38, 48, 10, 33, 60, 56, 48))
    private val KEY_IS_AUTO_RENEWING = decodeKey(byteArrayOf(51, 52, 33, 61, 58, 56, 10, 60, 38, 10, 52, 32, 33, 58, 10, 39, 48, 59, 48, 34, 60, 59, 50))

    private val prefs: SharedPreferences = getEncryptedPreferences(context)
    val billingClientWrapper: PlayBillingClientWrapper = PlayBillingClientWrapper(context, externalScope)

    val productPricing: StateFlow<Map<SubscriptionTier, ProductTierPricing>> = billingClientWrapper.productPricing

    companion object {
        fun getEncryptedPreferences(context: Context): SharedPreferences {
            return try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                EncryptedSharedPreferences.create(
                    context,
                    "fathom_secure_subscription_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
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
        migrateLegacyPreferences()
        observeBillingEvents()
    }

    private fun migrateLegacyPreferences() {
        if (prefs.contains("is_patron")) {
            val legacyIsPatron = prefs.getBoolean("is_patron", false)
            val legacyTier = prefs.getString("subscription_tier", SubscriptionTier.FREE.name) ?: SubscriptionTier.FREE.name
            val legacyToken = prefs.getString("purchase_token", null)
            val legacyOrderId = prefs.getString("order_id", null)
            val legacyTime = prefs.getLong("purchase_time", 0L)
            val legacyAutoRenewing = prefs.getBoolean("is_auto_renewing", true)

            val editor = prefs.edit()
                .putBoolean(KEY_IS_PATRON, legacyIsPatron)
                .putString(KEY_SUBSCRIPTION_TIER, legacyTier)
                .putBoolean(KEY_IS_AUTO_RENEWING, legacyAutoRenewing)

            if (legacyTime > 0L) editor.putLong(KEY_PURCHASE_TIME, legacyTime)
            if (!legacyToken.isNullOrEmpty()) editor.putString(KEY_PURCHASE_TOKEN, legacyToken)
            if (!legacyOrderId.isNullOrEmpty()) editor.putString(KEY_ORDER_ID, legacyOrderId)

            if (legacyIsPatron && !legacyToken.isNullOrEmpty()) {
                val payload = computeEntitlementPayload(legacyTier, legacyToken, legacyOrderId)
                val hmac = FathomKeystoreManager.generateEntitlementHmac(payload)
                if (hmac.isNotBlank()) {
                    editor.putString(KEY_ENTITLEMENT_HMAC, hmac)
                }
            }

            editor.remove("is_patron")
                .remove("subscription_tier")
                .remove("purchase_token")
                .remove("order_id")
                .remove("purchase_time")
                .remove("is_auto_renewing")
                .apply()
        }
    }

    private fun computeEntitlementPayload(tier: String, token: String, orderId: String?): String {
        return "${tier.trim().uppercase()}:${token.trim()}:${(orderId ?: "").trim()}"
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
        val isPatron = prefs.getBoolean(KEY_IS_PATRON, false)
        if (!isPatron) {
            return SubscriptionState(tier = SubscriptionTier.FREE, isPro = false)
        }

        val tierName = prefs.getString(KEY_SUBSCRIPTION_TIER, SubscriptionTier.FREE.name) ?: SubscriptionTier.FREE.name
        val tier = try {
            SubscriptionTier.valueOf(tierName)
        } catch (_: Exception) {
            SubscriptionTier.FREE
        }

        if (tier == SubscriptionTier.FREE) {
            return SubscriptionState(tier = SubscriptionTier.FREE, isPro = false)
        }

        val expiresAt = prefs.getLong("subscription_expires_at", 0L)
        if (expiresAt > 0 && System.currentTimeMillis() > expiresAt) {
            resetToFree()
            return SubscriptionState(tier = SubscriptionTier.FREE, isPro = false)
        }

        if (KeystoreIntegrityChecker.isBinaryTampered(context)) {
            return SubscriptionState(tier = SubscriptionTier.FREE, isPro = false)
        }

        val token = prefs.getString(KEY_PURCHASE_TOKEN, null)
        val orderId = prefs.getString(KEY_ORDER_ID, null)

        if (!token.isNullOrEmpty()) {
            val storedHmac = prefs.getString(KEY_ENTITLEMENT_HMAC, null)
            val payload = computeEntitlementPayload(tier.name, token, orderId)
            if (storedHmac.isNullOrBlank()) {
                val generated = FathomKeystoreManager.generateEntitlementHmac(payload)
                if (generated.isNotBlank()) {
                    prefs.edit().putString(KEY_ENTITLEMENT_HMAC, generated).apply()
                } else {
                    resetToFree()
                    return SubscriptionState(tier = SubscriptionTier.FREE, isPro = false)
                }
            } else {
                val isValid = FathomKeystoreManager.verifyEntitlementHmac(payload, storedHmac)
                if (!isValid) {
                    resetToFree()
                    return SubscriptionState(tier = SubscriptionTier.FREE, isPro = false)
                }
            }
        }

        return SubscriptionState(
            tier = tier,
            isPro = true
        )
    }

    fun getPurchaseTime(): Long = prefs.getLong(KEY_PURCHASE_TIME, 0L)
    fun isAutoRenewing(): Boolean = prefs.getBoolean(KEY_IS_AUTO_RENEWING, true)
    fun getOrderId(): String? = prefs.getString(KEY_ORDER_ID, null)

    private suspend fun processPurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!FathomPurchaseVerifier.verify(purchase)) {
                    continue
                }
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
            .putBoolean(KEY_IS_PATRON, true)
            .putString(KEY_SUBSCRIPTION_TIER, tier.name)

        if (purchaseTime > 0L) {
            editor.putLong(KEY_PURCHASE_TIME, purchaseTime)
        } else if (!prefs.contains(KEY_PURCHASE_TIME)) {
            editor.putLong(KEY_PURCHASE_TIME, System.currentTimeMillis())
        }

        editor.putBoolean(KEY_IS_AUTO_RENEWING, autoRenewing)

        if (!orderId.isNullOrEmpty()) {
            editor.putString(KEY_ORDER_ID, orderId)
        }

        if (!purchaseToken.isNullOrEmpty()) {
            editor.putString(KEY_PURCHASE_TOKEN, purchaseToken)
            val payload = computeEntitlementPayload(tier.name, purchaseToken, orderId)
            val hmac = FathomKeystoreManager.generateEntitlementHmac(payload)
            if (hmac.isNotBlank()) {
                editor.putString(KEY_ENTITLEMENT_HMAC, hmac)
            }
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
            val verifiedPurchases = activePurchases.filter { FathomPurchaseVerifier.verify(it) }
            for (purchase in verifiedPurchases) {
                if (!purchase.isAcknowledged) {
                    billingClientWrapper.acknowledgePurchase(purchase.purchaseToken)
                }
            }
            val bestPurchase = verifiedPurchases.maxByOrNull {
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
            val hasToken = !prefs.getString(KEY_PURCHASE_TOKEN, null).isNullOrEmpty()
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

        val verifiedPurchases = activePurchases.filter { FathomPurchaseVerifier.verify(it) }
        if (verifiedPurchases.isEmpty()) {
            return RestoreResult.NoPurchasesFound
        }

        for (purchase in verifiedPurchases) {
            if (!purchase.isAcknowledged) {
                billingClientWrapper.acknowledgePurchase(purchase.purchaseToken)
            }
        }

        val restoredPurchase = verifiedPurchases.maxByOrNull {
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
        if (BuildConfig.DEBUG) {
            persistPatronState(tier, "debug_patron_token")
            _consecrationEvents.tryEmit(tier)
        }
    }

    fun resetToFree() {
        prefs.edit()
            .putBoolean(KEY_IS_PATRON, false)
            .putString(KEY_SUBSCRIPTION_TIER, SubscriptionTier.FREE.name)
            .remove("subscription_expires_at")
            .remove(KEY_PURCHASE_TOKEN)
            .remove(KEY_ORDER_ID)
            .remove(KEY_ENTITLEMENT_HMAC)
            .apply()
        _subscriptionState.value = SubscriptionState(
            tier = SubscriptionTier.FREE,
            isPro = false
        )
    }
}
