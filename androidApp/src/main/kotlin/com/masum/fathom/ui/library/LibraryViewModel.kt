package com.masum.fathom.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.fathom.core.common.result.asSuccessOrNull
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.UserNote
import com.masum.fathom.core.repository.PassageRepository
import com.masum.fathom.core.repository.UserDataRepository
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import com.masum.fathom.core.sanctuary.SanctuarySettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

private data class RawLibraryData(
    val bookmarkItems: List<BookmarkWithPassage>,
    val noteItems: List<NoteWithPassage>,
    val historyItems: List<HistoryWithPassage>,
    val totalReads: Int,
    val currentStreak: Int,
    val weeklyHeatmap: List<HeatmapDay>,
    val badges: List<MilestoneBadge>
)

private data class FilteredLibraryData(
    val bookmarks: List<BookmarkWithPassage>,
    val notes: List<NoteWithPassage>,
    val history: List<HistoryWithPassage>,
    val totalReads: Int,
    val currentStreak: Int,
    val weeklyHeatmap: List<HeatmapDay>,
    val badges: List<MilestoneBadge>,
    val traditionFilter: TraditionId?,
    val tagFilter: String?,
    val searchQuery: String
)

class LibraryViewModel(
    private val userDataRepository: UserDataRepository,
    private val passageRepository: PassageRepository,
    private val sanctuaryPreferences: SanctuaryPreferences? = null
) : ViewModel() {

    private val passageCache = ConcurrentHashMap<String, Passage>()

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(LibraryTab.BOOKMARKS)
    private val _selectedPassageForNote = MutableStateFlow<Passage?>(null)
    private val _selectedPassageForContext = MutableStateFlow<Passage?>(null)
    private val _selectedPassageForShare = MutableStateFlow<Passage?>(null)
    private val _selectedTraditionFilter = MutableStateFlow<TraditionId?>(null)
    private val _selectedTagFilter = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isEditingProfile = MutableStateFlow(false)

    init {
        observeLibraryData()
    }

    private suspend fun getCachedPassage(passageId: String): Passage? {
        val cached = passageCache[passageId]
        if (cached != null) return cached
        val loaded = passageRepository.getPassage(passageId).asSuccessOrNull()
        if (loaded != null) {
            passageCache[passageId] = loaded
        }
        return loaded
    }

    fun onEvent(event: LibraryUiEvent) {
        when (event) {
            is LibraryUiEvent.OnSelectTab -> _selectedTab.value = event.tab
            is LibraryUiEvent.OnRemoveBookmark -> removeBookmark(event.passageId)
            is LibraryUiEvent.OnUpdateBookmarkTags -> updateBookmarkTags(event.passageId, event.tags)
            is LibraryUiEvent.OnOpenContext -> _selectedPassageForContext.value = event.passage
            is LibraryUiEvent.OnCloseContext -> _selectedPassageForContext.value = null
            is LibraryUiEvent.OnOpenShare -> _selectedPassageForShare.value = event.passage
            is LibraryUiEvent.OnCloseShare -> _selectedPassageForShare.value = null
            is LibraryUiEvent.OnOpenNoteDialog -> _selectedPassageForNote.value = event.passage
            is LibraryUiEvent.OnCloseNoteDialog -> _selectedPassageForNote.value = null
            is LibraryUiEvent.OnSaveNote -> saveNote(event.passageId, event.content)
            is LibraryUiEvent.OnDeleteNote -> deleteNote(event.noteId)
            is LibraryUiEvent.OnFilterTradition -> _selectedTraditionFilter.value = event.traditionId
            is LibraryUiEvent.OnFilterTag -> _selectedTagFilter.value = event.tag
            is LibraryUiEvent.OnSearchQueryChanged -> _searchQuery.value = event.query
            is LibraryUiEvent.OnOpenEditProfile -> _isEditingProfile.value = true
            is LibraryUiEvent.OnCloseEditProfile -> _isEditingProfile.value = false
            is LibraryUiEvent.OnSaveProfile -> {
                sanctuaryPreferences?.updateProfile(event.name, event.tradition)
                _isEditingProfile.value = false
            }
        }
    }

    private fun observeLibraryData() {
        val filterFlow = combine(
            _selectedTraditionFilter,
            _selectedTagFilter,
            _searchQuery
        ) { traditionFilter, tagFilter, query ->
            Triple(traditionFilter, tagFilter, query)
        }

        val rawDataFlow: Flow<RawLibraryData> = combine(
            userDataRepository.observeBookmarks(),
            userDataRepository.observeAllNotes(),
            userDataRepository.observeReadingHistory()
        ) { bookmarks, notes, history ->
            val bookmarkItems = bookmarks.map { bookmark ->
                val passage = getCachedPassage(bookmark.passageId)
                BookmarkWithPassage(bookmark, passage)
            }

            val noteItems = notes.map { note ->
                val passage = getCachedPassage(note.passageId)
                NoteWithPassage(note, passage)
            }

            val historyItems = history.map { record ->
                val passage = getCachedPassage(record.passageId)
                HistoryWithPassage(record, passage)
            }

            val totalReads = history.sumOf { it.readCount }
            val (currentStreak, weeklyHeatmap) = calculateStreakAndHeatmap(history)
            val badges = calculateMilestoneBadges(history, totalReads, currentStreak)

            RawLibraryData(
                bookmarkItems = bookmarkItems,
                noteItems = noteItems,
                historyItems = historyItems,
                totalReads = totalReads,
                currentStreak = currentStreak,
                weeklyHeatmap = weeklyHeatmap,
                badges = badges
            )
        }.flowOn(Dispatchers.Default)

        val filteredDataFlow: Flow<FilteredLibraryData> = combine(
            rawDataFlow,
            filterFlow
        ) { rawData, filters ->
            val traditionFilter = filters.first
            val tagFilter = filters.second
            val query = filters.third

            val filteredBookmarks = rawData.bookmarkItems.filter { item ->
                val matchesTradition = traditionFilter == null || item.passage?.traditionId == traditionFilter
                val matchesTag = tagFilter == null || tagFilter in item.bookmark.tags
                val matchesQuery = query.isBlank() || (item.passage?.translation?.contains(query, ignoreCase = true) == true ||
                        item.passage?.canonicalReference?.contains(query, ignoreCase = true) == true)
                matchesTradition && matchesTag && matchesQuery
            }

            val filteredNotes = rawData.noteItems.filter { item ->
                val matchesTradition = traditionFilter == null || item.passage?.traditionId == traditionFilter
                val matchesQuery = query.isBlank() || (item.note.content.contains(query, ignoreCase = true) ||
                        item.passage?.canonicalReference?.contains(query, ignoreCase = true) == true ||
                        item.passage?.translation?.contains(query, ignoreCase = true) == true)
                matchesTradition && matchesQuery
            }

            FilteredLibraryData(
                bookmarks = filteredBookmarks,
                notes = filteredNotes,
                history = rawData.historyItems,
                totalReads = rawData.totalReads,
                currentStreak = rawData.currentStreak,
                weeklyHeatmap = rawData.weeklyHeatmap,
                badges = rawData.badges,
                traditionFilter = traditionFilter,
                tagFilter = tagFilter,
                searchQuery = query
            )
        }.flowOn(Dispatchers.Default)

        val profileFlow = combine(
            _isEditingProfile,
            sanctuaryPreferences?.settings ?: flowOf(SanctuarySettings())
        ) { isEditing, settings ->
            Pair(isEditing, Pair(settings.userName, settings.userTradition))
        }

        val uiControlsFlow = combine(
            _selectedTab,
            _selectedPassageForNote,
            _selectedPassageForContext,
            _selectedPassageForShare,
            profileFlow
        ) { tab, notePassage, contextPassage, sharePassage, profile ->
            Triple(
                Triple(tab, notePassage, contextPassage),
                sharePassage,
                profile
            )
        }

        combine(
            filteredDataFlow,
            uiControlsFlow
        ) { filteredData, controls ->
            val tab = controls.first.first
            val notePassage = controls.first.second
            val contextPassage = controls.first.third
            val sharePassage = controls.second
            val isEditing = controls.third.first
            val userName = controls.third.second.first
            val userTradition = controls.third.second.second

            LibraryUiState.Success(
                userName = userName,
                userTradition = userTradition,
                isEditingProfile = isEditing,
                selectedTab = tab,
                bookmarks = filteredData.bookmarks,
                notes = filteredData.notes,
                history = filteredData.history,
                totalReadCount = filteredData.totalReads,
                currentStreakDays = filteredData.currentStreak,
                weeklyHeatmap = filteredData.weeklyHeatmap,
                milestoneBadges = filteredData.badges,
                selectedPassageForNote = notePassage,
                selectedPassageForContext = contextPassage,
                selectedPassageForShare = sharePassage,
                selectedTraditionFilter = filteredData.traditionFilter,
                selectedTagFilter = filteredData.tagFilter,
                searchQuery = filteredData.searchQuery
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    private fun calculateStreakAndHeatmap(history: List<com.masum.fathom.core.model.user.ReadingRecord>): Pair<Int, List<HeatmapDay>> {
        val calendar = java.util.Calendar.getInstance()
        val nowMs = System.currentTimeMillis()
        calendar.timeInMillis = nowMs

        val todayYear = calendar.get(java.util.Calendar.YEAR)
        val todayDayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)

        val daysWithReads = mutableMapOf<String, Int>()
        val dayKeyFormatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

        history.forEach { record ->
            val d = java.util.Date(record.lastSeenAtEpochMs)
            val key = dayKeyFormatter.format(d)
            daysWithReads[key] = (daysWithReads[key] ?: 0) + record.readCount
        }

        var streak = 0
        val checkCal = java.util.Calendar.getInstance()
        checkCal.timeInMillis = nowMs

        val todayKey = dayKeyFormatter.format(checkCal.time)
        val hasReadToday = (daysWithReads[todayKey] ?: 0) > 0

        if (hasReadToday) {
            streak++
            checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        } else {
            checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val yesterdayKey = dayKeyFormatter.format(checkCal.time)
            if ((daysWithReads[yesterdayKey] ?: 0) > 0) {
                streak++
                checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            }
        }

        while (true) {
            val key = dayKeyFormatter.format(checkCal.time)
            if ((daysWithReads[key] ?: 0) > 0) {
                streak++
                checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        val heatmapList = mutableListOf<HeatmapDay>()
        val heatmapCal = java.util.Calendar.getInstance()
        heatmapCal.timeInMillis = nowMs
        heatmapCal.add(java.util.Calendar.DAY_OF_YEAR, -6)

        val dayLabels = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        for (i in 0..6) {
            val key = dayKeyFormatter.format(heatmapCal.time)
            val count = daysWithReads[key] ?: 0
            val dayOfWeek = heatmapCal.get(java.util.Calendar.DAY_OF_WEEK) - 1
            val dayOfMonth = heatmapCal.get(java.util.Calendar.DAY_OF_MONTH)
            val isToday = heatmapCal.get(java.util.Calendar.YEAR) == todayYear && heatmapCal.get(java.util.Calendar.DAY_OF_YEAR) == todayDayOfYear

            heatmapList.add(
                HeatmapDay(
                    dayOfWeekLabel = dayLabels.getOrElse(dayOfWeek) { "D" },
                    dayOfMonth = dayOfMonth,
                    dateString = key,
                    readCount = count,
                    isToday = isToday
                )
            )
            heatmapCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        return Pair(streak, heatmapList)
    }

    private fun calculateMilestoneBadges(
        history: List<com.masum.fathom.core.model.user.ReadingRecord>,
        totalReads: Int,
        streakDays: Int
    ): List<MilestoneBadge> {
        val uniquePassageCount = history.size
        val uniqueBooksCount = history.map { it.bookId }.distinct().size

        return listOf(
            MilestoneBadge(
                id = "first_step",
                title = "Initiate of Wisdom",
                traditionId = null,
                description = "Complete your first sacred contemplation.",
                requiredCount = 1,
                currentCount = totalReads.coerceAtMost(1),
                isUnlocked = totalReads >= 1,
                tierName = "Bronze Tier"
            ),
            MilestoneBadge(
                id = "streak_3",
                title = "Kindled Flame",
                traditionId = null,
                description = "Maintain a 3-day daily wisdom streak.",
                requiredCount = 3,
                currentCount = streakDays.coerceAtMost(3),
                isUnlocked = streakDays >= 3,
                tierName = "Silver Tier"
            ),
            MilestoneBadge(
                id = "streak_7",
                title = "Disciplined Mind",
                traditionId = null,
                description = "Reach an unbroken 7-day contemplative practice.",
                requiredCount = 7,
                currentCount = streakDays.coerceAtMost(7),
                isUnlocked = streakDays >= 7,
                tierName = "Gold Tier"
            ),
            MilestoneBadge(
                id = "passages_25",
                title = "Seeker of Truth",
                traditionId = null,
                description = "Contemplate 25 distinct verses across scriptures.",
                requiredCount = 25,
                currentCount = uniquePassageCount.coerceAtMost(25),
                isUnlocked = uniquePassageCount >= 25,
                tierName = "Silver Tier"
            ),
            MilestoneBadge(
                id = "passages_100",
                title = "Deep Scholar",
                traditionId = null,
                description = "Explore and contemplate 100 unique passages.",
                requiredCount = 100,
                currentCount = uniquePassageCount.coerceAtMost(100),
                isUnlocked = uniquePassageCount >= 100,
                tierName = "Platinum Tier"
            ),
            MilestoneBadge(
                id = "cross_tradition",
                title = "Universal Seeker",
                traditionId = null,
                description = "Study verses from at least 4 different sacred canons.",
                requiredCount = 4,
                currentCount = uniqueBooksCount.coerceAtMost(4),
                isUnlocked = uniqueBooksCount >= 4,
                tierName = "Gold Tier"
            )
        )
    }

    private fun removeBookmark(passageId: String) {
        viewModelScope.launch {
            userDataRepository.removeBookmark(passageId)
        }
    }

    private fun updateBookmarkTags(passageId: String, tags: List<String>) {
        viewModelScope.launch {
            val bookmark = Bookmark(
                id = "bm_${passageId}",
                passageId = passageId,
                createdAtEpochMs = System.currentTimeMillis(),
                tags = tags
            )
            userDataRepository.saveBookmark(bookmark)
        }
    }

    private fun saveNote(passageId: String, content: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val note = UserNote(
                id = "note_${now}",
                passageId = passageId,
                content = content,
                createdAtEpochMs = now,
                updatedAtEpochMs = now
            )
            userDataRepository.upsertNote(note)
            _selectedPassageForNote.value = null
        }
    }

    private fun deleteNote(noteId: String) {
        viewModelScope.launch {
            userDataRepository.deleteNote(noteId)
        }
    }
}
