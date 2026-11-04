package com.masum.fathom.core.sanctuary

data class VaultRestoreSummary(
    val bookmarksRestored: Int = 0,
    val reflectionsRestored: Int = 0,
    val historyRestored: Int = 0
) {
    val totalRestored: Int get() = bookmarksRestored + reflectionsRestored + historyRestored
}
