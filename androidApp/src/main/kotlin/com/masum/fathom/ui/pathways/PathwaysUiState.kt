package com.masum.fathom.ui.pathways

import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.Passage

enum class MilestoneNodeType {
    GATEWAY,
    DISCIPLE,
    SANCTUARY,
    TEMPLE
}

data class ChapterProgress(
    val chapterNumber: Int,
    val title: String,
    val subtitle: String = "",
    val nodeType: MilestoneNodeType = MilestoneNodeType.DISCIPLE,
    val totalVerses: Int,
    val completedVerses: Int,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
    val isLocked: Boolean
)

data class CompletionReward(
    val chapterNumber: Int,
    val chapterTitle: String,
    val scriptureTitle: String,
    val gainedXp: Int,
    val totalCompletedChapters: Int,
    val keyInsight: String,
    val passageCount: Int
)

data class BookPathway(
    val book: Book,
    val chapters: List<ChapterProgress>,
    val totalChapters: Int,
    val completedChapters: Int,
    val progressPercent: Float,
    val totalVerses: Int,
    val readVerses: Int,
    val currentStreak: Int = 1,
    val wisdomXp: Int = 0
)

sealed interface PathwaysUiState {
    object Loading : PathwaysUiState
    data class Error(val message: String) : PathwaysUiState
    data class Success(
        val availableBooks: List<Book>,
        val selectedBook: Book,
        val currentPathway: BookPathway,
        val activeChapterForReading: Int? = null,
        val chapterPassages: List<Passage> = emptyList(),
        val isLoadingChapter: Boolean = false,
        val selectedPassageForContext: Passage? = null,
        val completionReward: CompletionReward? = null
    ) : PathwaysUiState
}

sealed interface PathwaysUiEvent {
    data class OnSelectBook(val bookId: String) : PathwaysUiEvent
    data class OnOpenChapter(val chapterNumber: Int) : PathwaysUiEvent
    object OnCloseChapter : PathwaysUiEvent
    data class OnCompleteChapter(val chapterNumber: Int) : PathwaysUiEvent
    data class OnOpenPassageContext(val passage: Passage) : PathwaysUiEvent
    object OnClosePassageContext : PathwaysUiEvent
    object OnDismissReward : PathwaysUiEvent
}
