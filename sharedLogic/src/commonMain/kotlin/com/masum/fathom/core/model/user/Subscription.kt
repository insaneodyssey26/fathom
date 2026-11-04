package com.masum.fathom.core.model.user

enum class SubscriptionTier(
    val id: String,
    val title: String,
    val priceDisplay: String,
    val billingPeriod: String,
    val equivalentMonthlyPrice: String,
    val badgeLabel: String? = null
) {
    FREE(
        id = "free",
        title = "Free Plan",
        priceDisplay = "$0.00",
        billingPeriod = "Free Forever",
        equivalentMonthlyPrice = "$0.00"
    ),
    MONTHLY(
        id = "fathom_monthly",
        title = "Plus Monthly",
        priceDisplay = "$4.99",
        billingPeriod = "Billed monthly",
        equivalentMonthlyPrice = ""
    ),
    ANNUAL(
        id = "fathom_annual",
        title = "Plus Annual",
        priceDisplay = "$44.99",
        billingPeriod = "Billed annually ($44.99/yr)",
        equivalentMonthlyPrice = "$3.75 / mo",
        badgeLabel = "Recommended · Save 25%"
    ),
    LIFETIME(
        id = "fathom_lifetime",
        title = "Plus Lifetime",
        priceDisplay = "$79.99",
        billingPeriod = "One-time purchase",
        equivalentMonthlyPrice = "Forever Access",
        badgeLabel = "Lifetime · Pay Once"
    );

    fun localizedPriceDisplay(countryCode: String? = null): String = priceDisplay

    fun localizedBillingPeriod(countryCode: String? = null): String = billingPeriod

    fun localizedEquivalentMonthlyPrice(countryCode: String? = null): String = equivalentMonthlyPrice
}

enum class Entitlement {
    PREMIUM_THEMES,
    UNLIMITED_AUDIO,
    UNLIMITED_REFLECTIONS,
    PATHWAYS_CURRICULUM,
    CONTEMPLATIVE_ANALYTICS,
    AI_EXEGESIS,
    STUDIO_QUOTE_CARDS,
    JOURNAL_EXPORT
}

data class SubscriptionState(
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val isPro: Boolean = false,
    val freeNotesLimit: Int = 5,
    val freeAudioPerDayLimit: Int = 3
) {
    fun hasEntitlement(entitlement: Entitlement): Boolean {
        if (isPro) return true
        return when (entitlement) {
            Entitlement.PREMIUM_THEMES -> false
            Entitlement.UNLIMITED_AUDIO -> false
            Entitlement.UNLIMITED_REFLECTIONS -> false
            Entitlement.PATHWAYS_CURRICULUM -> false
            Entitlement.CONTEMPLATIVE_ANALYTICS -> false
            Entitlement.AI_EXEGESIS -> false
            Entitlement.STUDIO_QUOTE_CARDS -> false
            Entitlement.JOURNAL_EXPORT -> false
        }
    }
}
