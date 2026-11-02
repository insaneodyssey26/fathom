package com.masum.fathom.core.subscription

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import com.masum.fathom.core.model.user.SubscriptionTier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

sealed interface BillingConnectionStatus {
    data object Disconnected : BillingConnectionStatus
    data object Connecting : BillingConnectionStatus
    data object Connected : BillingConnectionStatus
    data class Failure(val responseCode: Int, val debugMessage: String) : BillingConnectionStatus
}

sealed interface BillingPurchaseEvent {
    data class Purchased(val purchases: List<Purchase>) : BillingPurchaseEvent
    data object UserCancelled : BillingPurchaseEvent
    data class Error(val responseCode: Int, val debugMessage: String) : BillingPurchaseEvent
}

data class ProductTierPricing(
    val tier: SubscriptionTier,
    val formattedPrice: String,
    val billingPeriod: String,
    val productDetails: ProductDetails,
    val selectedOfferToken: String?
)

class PlayBillingClientWrapper(
    context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : PurchasesUpdatedListener, BillingClientStateListener {

    private val applicationContext = context.applicationContext

    private val _connectionStatus = MutableStateFlow<BillingConnectionStatus>(BillingConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<BillingConnectionStatus> = _connectionStatus.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<BillingPurchaseEvent>(extraBufferCapacity = 8)
    val purchaseEvents: SharedFlow<BillingPurchaseEvent> = _purchaseEvents.asSharedFlow()

    private val _productPricing = MutableStateFlow<Map<SubscriptionTier, ProductTierPricing>>(emptyMap())
    val productPricing: StateFlow<Map<SubscriptionTier, ProductTierPricing>> = _productPricing.asStateFlow()

    private val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .enablePrepaidPlans()
        .build()

    private val billingClient: BillingClient = BillingClient.newBuilder(applicationContext)
        .setListener(this)
        .enablePendingPurchases(pendingPurchasesParams)
        .enableAutoServiceReconnection()
        .build()

    init {
        startConnection()
    }

    fun startConnection() {
        if (_connectionStatus.value == BillingConnectionStatus.Connecting ||
            _connectionStatus.value == BillingConnectionStatus.Connected
        ) {
            return
        }
        _connectionStatus.value = BillingConnectionStatus.Connecting
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _connectionStatus.value = BillingConnectionStatus.Connected
            externalScope.launch {
                queryAllProductDetails()
            }
        } else {
            _connectionStatus.value = BillingConnectionStatus.Failure(
                responseCode = billingResult.responseCode,
                debugMessage = billingResult.debugMessage
            )
        }
    }

    override fun onBillingServiceDisconnected() {
        _connectionStatus.value = BillingConnectionStatus.Disconnected
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let {
                    _purchaseEvents.tryEmit(BillingPurchaseEvent.Purchased(it))
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseEvents.tryEmit(BillingPurchaseEvent.UserCancelled)
            }
            else -> {
                _purchaseEvents.tryEmit(
                    BillingPurchaseEvent.Error(
                        responseCode = billingResult.responseCode,
                        debugMessage = billingResult.debugMessage
                    )
                )
            }
        }
    }

    suspend fun queryAllProductDetails() {
        if (!billingClient.isReady) {
            return
        }

        val subsProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SubscriptionTier.MONTHLY.id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SubscriptionTier.ANNUAL.id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val inAppProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SubscriptionTier.LIFETIME.id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val fetchedPricing = mutableMapOf<SubscriptionTier, ProductTierPricing>()

        val subsDetails = queryProductDetailsInternal(subsProductList)
        for (details in subsDetails) {
            val tier = when (details.productId) {
                SubscriptionTier.MONTHLY.id -> SubscriptionTier.MONTHLY
                SubscriptionTier.ANNUAL.id -> SubscriptionTier.ANNUAL
                else -> null
            } ?: continue

            val offer = details.subscriptionOfferDetails?.firstOrNull() ?: continue
            val pricingPhase = offer.pricingPhases.pricingPhaseList.firstOrNull() ?: continue

            fetchedPricing[tier] = ProductTierPricing(
                tier = tier,
                formattedPrice = pricingPhase.formattedPrice,
                billingPeriod = tier.billingPeriod,
                productDetails = details,
                selectedOfferToken = offer.offerToken
            )
        }

        val inAppDetails = queryProductDetailsInternal(inAppProductList)
        for (details in inAppDetails) {
            if (details.productId == SubscriptionTier.LIFETIME.id) {
                val offer = details.oneTimePurchaseOfferDetails ?: continue
                fetchedPricing[SubscriptionTier.LIFETIME] = ProductTierPricing(
                    tier = SubscriptionTier.LIFETIME,
                    formattedPrice = offer.formattedPrice,
                    billingPeriod = SubscriptionTier.LIFETIME.billingPeriod,
                    productDetails = details,
                    selectedOfferToken = null
                )
            }
        }

        if (fetchedPricing.isNotEmpty()) {
            _productPricing.value = fetchedPricing
        }
    }

    private suspend fun queryProductDetailsInternal(
        productList: List<QueryProductDetailsParams.Product>
    ): List<ProductDetails> = suspendCoroutine { continuation ->
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params, object : ProductDetailsResponseListener {
            override fun onProductDetailsResponse(
                billingResult: BillingResult,
                queryProductDetailsResult: QueryProductDetailsResult
            ) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(queryProductDetailsResult.productDetailsList)
                } else {
                    continuation.resume(emptyList())
                }
            }
        })
    }

    fun launchBillingFlow(activity: Activity, pricing: ProductTierPricing): BillingResult {
        if (!billingClient.isReady) {
            return BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED)
                .setDebugMessage("BillingClient is not ready")
                .build()
        }

        val productDetailsParamsList = when (pricing.tier) {
            SubscriptionTier.MONTHLY, SubscriptionTier.ANNUAL -> {
                val token = pricing.selectedOfferToken ?: return BillingResult.newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.DEVELOPER_ERROR)
                    .setDebugMessage("Missing offer token")
                    .build()

                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(pricing.productDetails)
                        .setOfferToken(token)
                        .build()
                )
            }
            SubscriptionTier.LIFETIME -> {
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(pricing.productDetails)
                        .build()
                )
            }
            SubscriptionTier.FREE -> emptyList()
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        return billingClient.launchBillingFlow(activity, flowParams)
    }

    suspend fun queryActivePurchases(): List<Purchase> {
        if (!billingClient.isReady) {
            return emptyList()
        }

        val activeList = mutableListOf<Purchase>()

        val subsResult = queryPurchasesForType(BillingClient.ProductType.SUBS)
        activeList.addAll(subsResult.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED })

        val inAppResult = queryPurchasesForType(BillingClient.ProductType.INAPP)
        activeList.addAll(inAppResult.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED })

        return activeList
    }

    private suspend fun queryPurchasesForType(productType: String): List<Purchase> = suspendCoroutine { continuation ->
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(productType)
            .build()

        billingClient.queryPurchasesAsync(params, object : PurchasesResponseListener {
            override fun onQueryPurchasesResponse(billingResult: BillingResult, purchases: List<Purchase>) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(purchases)
                } else {
                    continuation.resume(emptyList())
                }
            }
        })
    }

    suspend fun acknowledgePurchase(purchaseToken: String): Boolean = suspendCoroutine { continuation ->
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            continuation.resume(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
        }
    }

    fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
