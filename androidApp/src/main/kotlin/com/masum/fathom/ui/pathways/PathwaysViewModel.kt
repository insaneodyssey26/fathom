package com.masum.fathom.ui.pathways

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.common.result.asSuccessOrNull
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.user.ReadingRecord
import com.masum.fathom.core.repository.PassageRepository
import com.masum.fathom.core.repository.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PathwaysViewModel(
    private val passageRepository: PassageRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PathwaysUiState>(PathwaysUiState.Loading)
    val uiState: StateFlow<PathwaysUiState> = _uiState.asStateFlow()

    private var allBooks: List<Book> = emptyList()
    private var selectedBookId: String = "bhagavad_gita"
    private var readingRecords: Map<String, ReadingRecord> = emptyMap()

    init {
        observeReadingRecords()
        loadInitialData()
    }

    fun onEvent(event: PathwaysUiEvent) {
        when (event) {
            is PathwaysUiEvent.OnSelectBook -> selectBook(event.bookId)
            is PathwaysUiEvent.OnOpenChapter -> openChapter(event.chapterNumber)
            is PathwaysUiEvent.OnCloseChapter -> closeChapter()
            is PathwaysUiEvent.OnCompleteChapter -> completeChapter(event.chapterNumber)
            is PathwaysUiEvent.OnOpenPassageContext -> openPassageContext(event.passage)
            is PathwaysUiEvent.OnClosePassageContext -> closePassageContext()
            is PathwaysUiEvent.OnDismissReward -> dismissReward()
        }
    }

    private fun openPassageContext(passage: Passage) {
        _uiState.update { state ->
            if (state is PathwaysUiState.Success) {
                state.copy(selectedPassageForContext = passage)
            } else state
        }
    }

    private fun closePassageContext() {
        _uiState.update { state ->
            if (state is PathwaysUiState.Success) {
                state.copy(selectedPassageForContext = null)
            } else state
        }
    }

    private fun observeReadingRecords() {
        userDataRepository.observeReadingHistory()
            .onEach { records ->
                readingRecords = records.associateBy { it.passageId }
                val currentState = _uiState.value
                if (currentState is PathwaysUiState.Success) {
                    val updatedPathway = buildPathwayForBook(currentState.selectedBook)
                    _uiState.update { currentState.copy(currentPathway = updatedPathway) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            when (val result = passageRepository.getAllBooks()) {
                is FathomResult.Success -> {
                    allBooks = result.data.sortedBy { b ->
                        when (b.id) {
                            "bhagavad_gita" -> 0
                            "meditations" -> 1
                            "tao_te_ching" -> 2
                            "dhammapada" -> 3
                            "enchiridion" -> 4
                            "proverbs" -> 5
                            "ecclesiastes" -> 6
                            "katha_upanishad" -> 7
                            "the_holy_quran" -> 8
                            "psalms" -> 9
                            else -> 10
                        }
                    }
                    val initialBook = allBooks.find { it.id == selectedBookId } ?: allBooks.firstOrNull()
                    if (initialBook != null) {
                        val pathway = buildPathwayForBook(initialBook)
                        _uiState.value = PathwaysUiState.Success(
                            availableBooks = allBooks,
                            selectedBook = initialBook,
                            currentPathway = pathway
                        )
                    } else {
                        _uiState.value = PathwaysUiState.Error("No sacred scriptures available.")
                    }
                }
                is FathomResult.Failure -> {
                    _uiState.value = PathwaysUiState.Error("Failed to load scripture pathways.")
                }
            }
        }
    }

    private fun selectBook(bookId: String) {
        selectedBookId = bookId
        val targetBook = allBooks.find { it.id == bookId } ?: return
        viewModelScope.launch {
            val pathway = buildPathwayForBook(targetBook)
            _uiState.update { state ->
                if (state is PathwaysUiState.Success) {
                    state.copy(
                        selectedBook = targetBook,
                        currentPathway = pathway,
                        activeChapterForReading = null,
                        chapterPassages = emptyList()
                    )
                } else state
            }
        }
    }

    private suspend fun buildPathwayForBook(book: Book): BookPathway {
        val chaptersList = passageRepository.getChaptersForBook(book.id).asSuccessOrNull() ?: emptyList()
        val allBookPassages = passageRepository.getPassagesForBook(book.id, offset = 0, limit = 10000).asSuccessOrNull() ?: emptyList()

        val passagesByChapter = allBookPassages.groupBy { it.chapter ?: 1 }
        val chapterProgressList = mutableListOf<ChapterProgress>()

        var foundCurrent = false
        var completedChaptersCount = 0
        var totalReadVerses = 0

        val sortedChapters = if (chaptersList.isNotEmpty()) {
            chaptersList.sorted()
        } else {
            listOf(1)
        }

        for (index in sortedChapters.indices) {
            val chapterNum = sortedChapters[index]
            val passagesInChapter = passagesByChapter[chapterNum] ?: emptyList()
            val totalVerses = passagesInChapter.size.coerceAtLeast(1)

            val readCount = passagesInChapter.count { p ->
                readingRecords.containsKey(p.id)
            }
            totalReadVerses += readCount

            val isCompleted = (readCount == totalVerses && totalVerses > 0)
            if (isCompleted) {
                completedChaptersCount++
            }

            val isCurrent = !isCompleted && !foundCurrent
            if (isCurrent) {
                foundCurrent = true
            }

            val isLocked = !isCompleted && !isCurrent && index > 0 && !(chapterProgressList.getOrNull(index - 1)?.isCompleted ?: false)

            val nodeType = when {
                index == 0 -> MilestoneNodeType.GATEWAY
                index == sortedChapters.size - 1 -> MilestoneNodeType.TEMPLE
                (index + 1) % 5 == 0 -> MilestoneNodeType.SANCTUARY
                else -> MilestoneNodeType.DISCIPLE
            }

            chapterProgressList.add(
                ChapterProgress(
                    chapterNumber = chapterNum,
                    title = getChapterTitle(book.id, chapterNum),
                    subtitle = getChapterSubtitle(book.id, chapterNum),
                    nodeType = nodeType,
                    totalVerses = totalVerses,
                    completedVerses = readCount,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent || (!foundCurrent && index == 0),
                    isLocked = false
                )
            )
        }

        val totalVersesCount = allBookPassages.size.coerceAtLeast(1)
        val progress = (totalReadVerses.toFloat() / totalVersesCount.toFloat()).coerceIn(0f, 1f)
        val earnedXp = totalReadVerses * 15 + (completedChaptersCount * 100)

        return BookPathway(
            book = book,
            chapters = chapterProgressList,
            totalChapters = sortedChapters.size,
            completedChapters = completedChaptersCount,
            progressPercent = progress,
            totalVerses = totalVersesCount,
            readVerses = totalReadVerses,
            currentStreak = 1,
            wisdomXp = earnedXp
        )
    }

    private fun openChapter(chapterNumber: Int) {
        val currentState = _uiState.value as? PathwaysUiState.Success ?: return
        _uiState.update { currentState.copy(activeChapterForReading = chapterNumber, isLoadingChapter = true) }

        viewModelScope.launch {
            val result = passageRepository.getPassagesForBookChapter(currentState.selectedBook.id, chapterNumber)
            val passages = when (result) {
                is FathomResult.Success -> {
                    if (result.data.isNotEmpty()) {
                        result.data
                    } else {
                        passageRepository.getPassagesForBook(currentState.selectedBook.id, offset = 0, limit = 50).asSuccessOrNull() ?: emptyList()
                    }
                }
                is FathomResult.Failure -> {
                    passageRepository.getPassagesForBook(currentState.selectedBook.id, offset = 0, limit = 50).asSuccessOrNull() ?: emptyList()
                }
            }
            _uiState.update { state ->
                if (state is PathwaysUiState.Success) {
                    state.copy(chapterPassages = passages, isLoadingChapter = false)
                } else state
            }
        }
    }

    private fun closeChapter() {
        _uiState.update { state ->
            if (state is PathwaysUiState.Success) {
                state.copy(activeChapterForReading = null, chapterPassages = emptyList())
            } else state
        }
    }

    private fun completeChapter(chapterNumber: Int) {
        val currentState = _uiState.value as? PathwaysUiState.Success ?: return
        viewModelScope.launch {
            currentState.chapterPassages.forEach { passage ->
                userDataRepository.recordReading(passage.id, currentState.selectedBook.id)
            }
            val updatedPathway = buildPathwayForBook(currentState.selectedBook)
            val chapterObj = updatedPathway.chapters.find { it.chapterNumber == chapterNumber }
            val keyInsight = currentState.chapterPassages.firstOrNull()?.translation ?: ""
            val reward = CompletionReward(
                chapterNumber = chapterNumber,
                chapterTitle = chapterObj?.title ?: "Chapter $chapterNumber",
                scriptureTitle = currentState.selectedBook.title,
                gainedXp = 100 + (currentState.chapterPassages.size * 15),
                totalCompletedChapters = updatedPathway.completedChapters,
                keyInsight = keyInsight,
                passageCount = currentState.chapterPassages.size
            )
            _uiState.update { state ->
                if (state is PathwaysUiState.Success) {
                    state.copy(
                        currentPathway = updatedPathway,
                        activeChapterForReading = null,
                        chapterPassages = emptyList(),
                        completionReward = reward
                    )
                } else state
            }
        }
    }

    private fun dismissReward() {
        _uiState.update { state ->
            if (state is PathwaysUiState.Success) {
                state.copy(completionReward = null)
            } else state
        }
    }

    private fun getChapterTitle(bookId: String, chapter: Int): String {
        return when (bookId) {
            "bhagavad_gita" -> when (chapter) {
                1 -> "Arjuna Vishada Yoga"
                2 -> "Sankhya Yoga"
                3 -> "Karma Yoga"
                4 -> "Jnana Karma Sanyasa Yoga"
                5 -> "Karma Sanyasa Yoga"
                6 -> "Dhyana Yoga"
                7 -> "Jnana Vijnana Yoga"
                8 -> "Aksara Brahma Yoga"
                9 -> "Raja Vidya Raja Guhya Yoga"
                10 -> "Vibhuti Yoga"
                11 -> "Vishwarupa Darshana Yoga"
                12 -> "Bhakti Yoga"
                13 -> "Kshetra Kshetrajna Vibhaga Yoga"
                14 -> "Gunatraya Vibhaga Yoga"
                15 -> "Purushottama Yoga"
                16 -> "Daivasura Sampad Vibhaga Yoga"
                17 -> "Shraddhatraya Vibhaga Yoga"
                18 -> "Moksha Sanyasa Yoga"
                else -> "Chapter $chapter"
            }
            "meditations" -> when (chapter) {
                1 -> "Book I"
                2 -> "Book II"
                3 -> "Book III"
                4 -> "Book IV"
                5 -> "Book V"
                6 -> "Book VI"
                7 -> "Book VII"
                8 -> "Book VIII"
                9 -> "Book IX"
                10 -> "Book X"
                11 -> "Book XI"
                12 -> "Book XII"
                else -> "Book $chapter"
            }
            "dhammapada" -> when (chapter) {
                1 -> "Yamakavagga"
                2 -> "Appamadavagga"
                3 -> "Cittavagga"
                4 -> "Pupphavagga"
                5 -> "Balavagga"
                6 -> "Panditavagga"
                7 -> "Arahantavagga"
                8 -> "Sahassavagga"
                9 -> "Papavagga"
                10 -> "Dandavagga"
                11 -> "Jaravagga"
                12 -> "Attavagga"
                13 -> "Lokavagga"
                14 -> "Buddhavagga"
                15 -> "Sukhavagga"
                16 -> "Piyavagga"
                17 -> "Kodhavagga"
                18 -> "Malavagga"
                19 -> "Dhammatthavagga"
                20 -> "Maggavagga"
                21 -> "Pakinnakavagga"
                22 -> "Nirayavagga"
                23 -> "Nagavagga"
                24 -> "Tanhavagga"
                25 -> "Bhikkhuvagga"
                26 -> "Brahmanavagga"
                else -> "Vagga $chapter"
            }
            "tao_te_ching" -> "Chapter $chapter"
            "enchiridion" -> "Section $chapter"
            "proverbs" -> "Proverbs $chapter"
            "ecclesiastes" -> "Ecclesiastes $chapter"
            else -> "Chapter $chapter"
        }
    }

    private fun getChapterSubtitle(bookId: String, chapter: Int): String {
        return when (bookId) {
            "bhagavad_gita" -> when (chapter) {
                1 -> "The Dilemma of the Soul"
                2 -> "The Eternal Nature of Self"
                3 -> "The Path of Selfless Action"
                4 -> "Wisdom in Action & Sacrifice"
                5 -> "The Art of Renunciation"
                6 -> "Mastery of the Still Mind"
                7 -> "Ultimate Knowledge & Reality"
                8 -> "The Eternal & Imperishable"
                9 -> "The Sovereign Secret"
                10 -> "The Divine Manifestations"
                11 -> "The Cosmic Vision"
                12 -> "The Way of Divine Love"
                13 -> "The Knower & The Known"
                14 -> "The Three Modes of Nature"
                15 -> "The Supreme Supreme Being"
                16 -> "The Divine & Demonic Qualities"
                17 -> "The Threefold Faith"
                18 -> "Liberation Through Surrender"
                else -> ""
            }
            "meditations" -> when (chapter) {
                1 -> "Debts and Lessons of Gratitude"
                2 -> "On the River Gran: The Inner Citadel"
                3 -> "At Carnuntum: Ruling Reason"
                4 -> "The Universe is Flux, Life is Opinion"
                5 -> "Rising to the Duty of Man"
                6 -> "The Web of Coherence"
                7 -> "Tranquility in Change"
                8 -> "The Nature of Pain and Soul"
                9 -> "Injustice Against the Whole"
                10 -> "Simplicity and Sacred Restraint"
                11 -> "Kindness Without Guile"
                12 -> "Depart with Grace and Peace"
                else -> ""
            }
            "dhammapada" -> when (chapter) {
                1 -> "The Twin Verses"
                2 -> "Heedfulness and Awareness"
                3 -> "The Nature of Thought"
                4 -> "The Flowers of Virtue"
                5 -> "The Immature Mind"
                6 -> "The Wise and Learned"
                7 -> "The Enlightened One"
                8 -> "The Thousands of Truth"
                9 -> "Evil and Impulse"
                10 -> "Violence and Compassion"
                11 -> "Old Age and Impermanence"
                12 -> "The Mastery of Self"
                13 -> "The Illusory World"
                14 -> "The Awakened Teacher"
                15 -> "Unconditioned Joy"
                16 -> "Affection and Attachment"
                17 -> "Overcoming Anger"
                18 -> "Impurities and Blemishes"
                19 -> "The Righteous Path"
                20 -> "The Noble Path"
                21 -> "Miscellaneous Insights"
                22 -> "The State of Suffering"
                23 -> "The Disciplined Elephant"
                24 -> "Extinguishing Craving"
                25 -> "The Wandering Mendicant"
                26 -> "The Holy Noble Soul"
                else -> ""
            }
            "tao_te_ching" -> "The Way & Its Virtue"
            else -> ""
        }
    }
}
