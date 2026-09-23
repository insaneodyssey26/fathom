package com.masum.fathom.core.model

enum class TraditionId(val key: String, val displayName: String) {
    HINDUISM("hinduism", "Hinduism"),
    BUDDHISM("buddhism", "Buddhism"),
    STOICISM("stoicism", "Stoicism"),
    TAOISM("taoism", "Taoism"),
    ISLAM("islam", "Islam"),
    CHRISTIANITY("christianity", "Christianity"),
    JUDAISM("judaism", "Judaism");

    companion object {
        fun fromKeyOrNull(key: String): TraditionId? {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
        }
    }
}
