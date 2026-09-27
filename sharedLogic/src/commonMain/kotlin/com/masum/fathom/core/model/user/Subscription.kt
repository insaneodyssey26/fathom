package com.masum.fathom.core.model.user

enum class SubscriptionTier(
    val id: String,
    val title: String,
    val priceDisplay: String,
    val billingPeriod: String,
    val equivalentMonthlyPrice: String,
    val badgeLabel: String? = null
) {
    FREE("free", "Sacred Seeker", "$0.00", "Free Forever", "$0.00"),
    MONTHLY("sanctuary_monthly", "Monthly Pass", "$4.99", "Billed monthly", "$4.99 / mo"),
    SIX_MONTH("sanctuary_six_month", "6-Month Pass", "$19.99", "Billed every 6 months", "$3.33 / mo", "SAVE 33%"),
    ANNUAL("sanctuary_annual", "Annual Sanctuary", "$29.99", "Billed annually ($29.99/yr)", "$2.49 / mo", "BEST VALUE · 7 DAYS FREE")
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
