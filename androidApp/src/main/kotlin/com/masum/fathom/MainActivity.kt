package com.masum.fathom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.masum.fathom.core.database.SqlDelightPassageRepository
import com.masum.fathom.core.database.SqlDelightUserDataRepository
import com.masum.fathom.core.database.content.FathomContentDatabase
import com.masum.fathom.core.database.user.FathomUserDatabase
import com.masum.fathom.core.feed.FeedEngine
import com.masum.fathom.ui.feed.FeedScreen
import com.masum.fathom.ui.feed.FeedViewModel
import com.masum.fathom.ui.library.LibraryScreen
import com.masum.fathom.ui.library.LibraryViewModel
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.ReadingTheme

enum class NavigationItem {
    FEED,
    LIBRARY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)



        setContent {
            val context = LocalContext.current
            val (feedEngine, userRepo, passageRepo) = remember {
                val userDriver = AndroidSqliteDriver(FathomUserDatabase.Schema, context, "app_user_data.db")
                val contentDriver = AndroidSqliteDriver(FathomContentDatabase.Schema, context, "starter_content.db")

                val userDb = FathomUserDatabase(userDriver)
                val contentDb = FathomContentDatabase(contentDriver)

                seedInitialWisdomIfEmpty(contentDb)

                val feed = FeedEngine(contentDb, userDb)
                val user = SqlDelightUserDataRepository(userDb)
                val passage = SqlDelightPassageRepository(contentDb)

                Triple(feed, user, passage)
            }

            val feedViewModel: FeedViewModel = viewModel {
                FeedViewModel(feedRepository = feedEngine, userDataRepository = userRepo)
            }

            val libraryViewModel: LibraryViewModel = viewModel {
                LibraryViewModel(userDataRepository = userRepo, passageRepository = passageRepo)
            }

            val feedUiState by feedViewModel.uiState.collectAsStateWithLifecycle()
            val libraryUiState by libraryViewModel.uiState.collectAsStateWithLifecycle()

            val activeTheme = (feedUiState as? com.masum.fathom.ui.feed.FeedUiState.Success)?.activeTheme ?: ReadingTheme.OBSIDIAN

            var selectedNav by remember { mutableStateOf(NavigationItem.FEED) }

            FathomTheme(readingTheme = activeTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = FathomTheme.colors.background,
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        com.masum.fathom.ui.components.FathomBottomNavBar(
                            selectedItem = selectedNav,
                            onItemSelected = { selectedNav = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(FathomTheme.colors.background)
                    ) {
                        androidx.compose.animation.AnimatedContent(
                            targetState = selectedNav,
                            transitionSpec = {
                                if (targetState == NavigationItem.LIBRARY) {
                                    (androidx.compose.animation.slideInHorizontally(androidx.compose.animation.core.tween(320)) { width -> (width * 0.12f).toInt() } +
                                        androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(320)) +
                                        androidx.compose.animation.scaleIn(initialScale = 0.98f, animationSpec = androidx.compose.animation.core.tween(320)))
                                        .togetherWith(
                                            androidx.compose.animation.slideOutHorizontally(androidx.compose.animation.core.tween(280)) { width -> (-width * 0.12f).toInt() } +
                                                androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(250)) +
                                                androidx.compose.animation.scaleOut(targetScale = 0.98f, animationSpec = androidx.compose.animation.core.tween(280))
                                        )
                                } else {
                                    (androidx.compose.animation.slideInHorizontally(androidx.compose.animation.core.tween(320)) { width -> (-width * 0.12f).toInt() } +
                                        androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(320)) +
                                        androidx.compose.animation.scaleIn(initialScale = 0.98f, animationSpec = androidx.compose.animation.core.tween(320)))
                                        .togetherWith(
                                            androidx.compose.animation.slideOutHorizontally(androidx.compose.animation.core.tween(280)) { width -> (width * 0.12f).toInt() } +
                                                androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(250)) +
                                                androidx.compose.animation.scaleOut(targetScale = 0.98f, animationSpec = androidx.compose.animation.core.tween(280))
                                        )
                                }
                            },
                            label = "screen_nav_transition",
                            modifier = Modifier.fillMaxSize()
                        ) { targetNav ->
                            when (targetNav) {
                                NavigationItem.FEED -> FeedScreen(
                                    state = feedUiState,
                                    onEvent = feedViewModel::onEvent
                                )
                                NavigationItem.LIBRARY -> LibraryScreen(
                                    state = libraryUiState,
                                    onEvent = libraryViewModel::onEvent
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}

private fun seedInitialWisdomIfEmpty(database: FathomContentDatabase) {
    val cq = database.contentDatabaseQueries
    if (cq.countPassages().executeAsOne() == 0L) {
        cq.insertTradition("hinduism", "Hinduism", "Ancient Vedic and Upanishadic wisdom")
        cq.insertTradition("stoicism", "Stoicism", "Hellenistic philosophy of virtue and resilience")
        cq.insertTradition("buddhism", "Buddhism", "The path of mindfulness, compassion, and liberation")
        cq.insertTradition("taoism", "Taoism", "The natural way and effortless action")

        cq.insertBook("bhagavad_gita", "hinduism", "Bhagavad Gita", "Vyasa", "Sanskrit", 700L, 1L)
        cq.insertBook("meditations", "stoicism", "Meditations", "Marcus Aurelius", "Greek", 400L, 1L)
        cq.insertBook("dhammapada", "buddhism", "Dhammapada", "Buddha", "Pali", 423L, 1L)
        cq.insertBook("tao_te_ching", "taoism", "Tao Te Ching", "Lao Tzu", "Classical Chinese", 81L, 1L)

        cq.insertPassage(
            id = "gita_2_20",
            book_id = "bhagavad_gita",
            tradition_id = "hinduism",
            canonical_reference = "Bhagavad Gita 2.20",
            original_text = "न जायते म्रियते वा कदाचिन्...",
            translation = "The soul is never born nor does it die at any time. It has not come to be, does not come to be, and will not come to be. It is unborn, eternal, ever-existing, and primeval.",
            order_index = 20L,
            chapter = 2L,
            verse_number = 20L
        )

        cq.insertPassage(
            id = "meditations_4_3",
            book_id = "meditations",
            tradition_id = "stoicism",
            canonical_reference = "Meditations 4.3",
            original_text = "Οὐδαμοῦ γὰρ οὔτε ἡσυχιώτερον οὔτε ἀπραγμονέστερον ἄνθρωπος ἀναχωρεῖ ἢ εἰς τὴν ἑαυτοῦ ψυχήν...",
            translation = "Nowhere can man find a quieter or more untroubled retreat than in his own soul. Constantly then give to yourself this retreat, and renew yourself.",
            order_index = 3L,
            chapter = 4L,
            verse_number = 3L
        )

        cq.insertPassage(
            id = "dhammapada_1",
            book_id = "dhammapada",
            tradition_id = "buddhism",
            canonical_reference = "Dhammapada 1.1",
            original_text = "मनोपुब्बङ्गमा धम्मा मनोसेट्ठा मनोमया...",
            translation = "Mind is the forerunner of all states. Mind is their chief; mind-made are they. If one speaks or acts with a pure mind, happiness follows as a shadow that never departs.",
            order_index = 1L,
            chapter = 1L,
            verse_number = 1L
        )

        cq.insertPassage(
            id = "tao_te_ching_8",
            book_id = "tao_te_ching",
            tradition_id = "taoism",
            canonical_reference = "Tao Te Ching 8",
            original_text = "上善若水。水善利萬物而不爭，處衆人之所惡，故幾於道。",
            translation = "The highest goodness is like water. Water benefits all things and does not compete with them. It stays in the low places which others despise. Therefore it is near to the Tao.",
            order_index = 8L,
            chapter = 8L,
            verse_number = 1L
        )

        cq.insertExplanation(
            id = "exp_gita_2_20",
            passage_id = "gita_2_20",
            title = "The Nature of Consciousness",
            body = "This foundational verse establishes the distinction between the transient physical vessel and unchanging witness consciousness.",
            author = "Traditional Commentary",
            source = "Sanskrit Philosophical Corpus"
        )

        cq.insertExplanation(
            id = "exp_med_4_3",
            passage_id = "meditations_4_3",
            title = "The Inner Citadel",
            body = "Marcus Aurelius reminds himself that true tranquility does not require physical isolation or countryside retreats, but the quiet command of one's own governing mind.",
            author = "Pierre Hadot",
            source = "Philosophy as a Way of Life"
        )
    }
}