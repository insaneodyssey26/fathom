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
        title = "Sacred Seeker",
        priceDisplay = "$0.00",
        billingPeriod = "Free Forever",
        equivalentMonthlyPrice = "$0.00"
    ),
    MONTHLY(
        id = "fathom_monthly",
        title = "Monthly Seeker",
        priceDisplay = "$3.99 / ₹99",
        billingPeriod = "Billed monthly",
        equivalentMonthlyPrice = "$3.99 / mo"
    ),
    ANNUAL(
        id = "fathom_annual",
        title = "Annual Sanctuary",
        priceDisplay = "$19.99 / ₹499",
        billingPeriod = "Billed annually ($19.99/yr)",
        equivalentMonthlyPrice = "$1.66 / mo",
        badgeLabel = "BEST VALUE · 60% OFF"
    ),
    LIFETIME(
        id = "fathom_lifetime",
        title = "Patron Consecration",
        priceDisplay = "$39.99 / ₹999",
        billingPeriod = "One-time purchase",
        equivalentMonthlyPrice = "Forever Access",
        badgeLabel = "PATRON'S CHOICE · NO SUBSCRIPTION"
    )
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
