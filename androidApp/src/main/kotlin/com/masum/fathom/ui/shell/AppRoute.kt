package com.masum.fathom.ui.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.masum.fathom.NavigationItem
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.library.JournalPage

sealed interface AppRoute {
    data class Tab(val item: NavigationItem) : AppRoute
    data class Chapter(val number: Int) : AppRoute
    data class Book(val id: String) : AppRoute
    data class Journal(val page: JournalPage) : AppRoute
    data object Settings : AppRoute
    data class Legal(val doc: LegalDoc) : AppRoute
}

private fun AppRoute.encode(): String = when (this) {
    is AppRoute.Tab -> "tab:${item.name}"
    is AppRoute.Chapter -> "chapter:$number"
    is AppRoute.Book -> "book:$id"
    is AppRoute.Journal -> when (val p = page) {
        JournalPage.Hub -> "journal:hub"
        JournalPage.Saved -> "journal:saved"
        JournalPage.Reflections -> "journal:reflections"
        JournalPage.Timeline -> "journal:timeline"
        is JournalPage.Canon -> "journal:canon:${p.traditionId.key}"
    }
    AppRoute.Settings -> "settings"
    is AppRoute.Legal -> "legal:${doc.name}"
}

private fun decode(raw: String): AppRoute? {
    val parts = raw.split(":", limit = 3)
    return when (parts[0]) {
        "tab" -> NavigationItem.entries.firstOrNull { it.name == parts.getOrNull(1) }?.let { AppRoute.Tab(it) }
        "chapter" -> parts.getOrNull(1)?.toIntOrNull()?.let { AppRoute.Chapter(it) }
        "book" -> parts.getOrNull(1)?.let { AppRoute.Book(raw.removePrefix("book:")) }
        "journal" -> when (parts.getOrNull(1)) {
            "hub" -> AppRoute.Journal(JournalPage.Hub)
            "saved" -> AppRoute.Journal(JournalPage.Saved)
            "reflections" -> AppRoute.Journal(JournalPage.Reflections)
            "timeline" -> AppRoute.Journal(JournalPage.Timeline)
            "canon" -> parts.getOrNull(2)?.let { TraditionId.fromKeyOrNull(it) }?.let { AppRoute.Journal(JournalPage.Canon(it)) }
            else -> null
        }
        "settings" -> AppRoute.Settings
        "legal" -> LegalDoc.entries.firstOrNull { it.name == parts.getOrNull(1) }?.let { AppRoute.Legal(it) }
        else -> null
    }
}

class AppNavigator(val stack: SnapshotStateList<AppRoute>) {

    val top: AppRoute get() = stack.last()

    val currentTab: NavigationItem
        get() = stack.lastOrNull { it is AppRoute.Tab }.let { (it as? AppRoute.Tab)?.item } ?: NavigationItem.FEED

    fun selectTab(item: NavigationItem) {
        if (stack.size == 1 && top == AppRoute.Tab(item)) return
        if (item == NavigationItem.FEED) {
            stack.clear()
            stack.add(AppRoute.Tab(NavigationItem.FEED))
        } else {
            val target = AppRoute.Tab(item)
            if (stack.size == 2 && stack[1] == target) return
            stack.clear()
            stack.add(AppRoute.Tab(NavigationItem.FEED))
            stack.add(target)
        }
    }

    fun push(route: AppRoute) {
        if (top != route) stack.add(route)
    }

    fun pop() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    fun resetToFeed() {
        stack.clear()
        stack.add(AppRoute.Tab(NavigationItem.FEED))
    }
}

@Composable
fun rememberAppNavigator(): AppNavigator {
    val stack = rememberSaveable(
        saver = listSaver<SnapshotStateList<AppRoute>, String>(
            save = { list -> list.map { it.encode() } },
            restore = { saved -> saved.mapNotNull(::decode).ifEmpty { listOf(AppRoute.Tab(NavigationItem.FEED)) }.toMutableStateList() }
        )
    ) { mutableStateListOf<AppRoute>(AppRoute.Tab(NavigationItem.FEED)) }
    return androidx.compose.runtime.remember(stack) { AppNavigator(stack) }
}
