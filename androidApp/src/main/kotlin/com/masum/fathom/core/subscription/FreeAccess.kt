package com.masum.fathom.core.subscription

object FreeAccess {
    const val ChapterLimit = 2

    fun isChapterLocked(chapterNumber: Int, isPro: Boolean): Boolean = !isPro && chapterNumber > ChapterLimit
}
