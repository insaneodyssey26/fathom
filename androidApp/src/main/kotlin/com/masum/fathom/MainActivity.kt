package com.masum.fathom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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

import com.masum.fathom.ui.search.SearchScreen
import com.masum.fathom.ui.search.SearchViewModel

enum class NavigationItem {
    FEED,
    SEARCH,
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
                val dbFile = context.getDatabasePath("starter_content.db")
                val prefs = context.getSharedPreferences("fathom_content_prefs", android.content.Context.MODE_PRIVATE)
                val currentVersion = prefs.getInt("db_asset_version", 0)
                val TARGET_VERSION = 10
                val needsExtraction = !dbFile.exists() || dbFile.length() < 100_000L || currentVersion < TARGET_VERSION

                if (needsExtraction) {
                    dbFile.parentFile?.mkdirs()
                    if (dbFile.exists()) dbFile.delete()
                    context.assets.open("starter_content.db").use { input ->
                        java.io.FileOutputStream(dbFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    prefs.edit().putInt("db_asset_version", TARGET_VERSION).apply()
                }

                val userDriver = AndroidSqliteDriver(FathomUserDatabase.Schema, context, "app_user_data.db")
                val contentDriver = AndroidSqliteDriver(
                    schema = object : app.cash.sqldelight.db.SqlSchema<app.cash.sqldelight.db.QueryResult.Value<Unit>> {
                        override val version: Long get() = 1L
                        override fun create(driver: app.cash.sqldelight.db.SqlDriver): app.cash.sqldelight.db.QueryResult.Value<Unit> = app.cash.sqldelight.db.QueryResult.Unit
                        override fun migrate(driver: app.cash.sqldelight.db.SqlDriver, oldVersion: Long, newVersion: Long, vararg callbacks: app.cash.sqldelight.db.AfterVersion): app.cash.sqldelight.db.QueryResult.Value<Unit> = app.cash.sqldelight.db.QueryResult.Unit
                    },
                    context = context,
                    name = "starter_content.db"
                )

                val userDb = FathomUserDatabase(userDriver)
                val contentDb = FathomContentDatabase(contentDriver)

                val feed = FeedEngine(contentDb, userDb)
                val user = SqlDelightUserDataRepository(userDb)
                val passage = SqlDelightPassageRepository(contentDb)

                Triple(feed, user, passage)
            }

            val feedViewModel: FeedViewModel = viewModel {
                FeedViewModel(
                    feedRepository = feedEngine,
                    userDataRepository = userRepo,
                    passageRepository = passageRepo
                )
            }

            val searchViewModel: SearchViewModel = viewModel {
                SearchViewModel(
                    passageRepository = passageRepo,
                    userDataRepository = userRepo
                )
            }

            val libraryViewModel: LibraryViewModel = viewModel {
                LibraryViewModel(userDataRepository = userRepo, passageRepository = passageRepo)
            }

            val feedUiState by feedViewModel.uiState.collectAsStateWithLifecycle()
            val searchUiState by searchViewModel.uiState.collectAsStateWithLifecycle()
            val libraryUiState by libraryViewModel.uiState.collectAsStateWithLifecycle()

            val sanctuaryPreferences = remember { com.masum.fathom.core.sanctuary.SanctuaryPreferences(context) }
            val sanctuaryDataEngine = remember { com.masum.fathom.core.sanctuary.SanctuaryDataEngine(context, userRepo, passageRepo) }
            val sanctuarySettings by sanctuaryPreferences.settings.collectAsState()

            val activeTheme = sanctuarySettings.theme

            var showSanctuaryModal by remember { mutableStateOf(false) }
            var selectedNav by remember { mutableStateOf(NavigationItem.FEED) }

            if (selectedNav != NavigationItem.FEED) {
                androidx.activity.compose.BackHandler {
                    selectedNav = NavigationItem.FEED
                }
            }

            val audioEngine = remember { com.masum.fathom.core.audio.SacredAudioEngine(context) }
            androidx.compose.runtime.DisposableEffect(audioEngine) {
                onDispose {
                    audioEngine.release()
                }
            }

            androidx.compose.runtime.CompositionLocalProvider(
                com.masum.fathom.core.audio.LocalSacredAudioEngine provides audioEngine,
                com.masum.fathom.core.sanctuary.LocalSanctuaryPreferences provides sanctuaryPreferences,
                com.masum.fathom.core.sanctuary.LocalSanctuaryDataEngine provides sanctuaryDataEngine,
                com.masum.fathom.core.sanctuary.LocalOpenSanctuary provides { showSanctuaryModal = true }
            ) {
                FathomTheme(readingTheme = activeTheme) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(FathomTheme.colors.background)
                    ) {
                        androidx.compose.animation.AnimatedContent(
                            targetState = selectedNav,
                            transitionSpec = {
                                (androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(240)) +
                                    androidx.compose.animation.scaleIn(initialScale = 0.98f, animationSpec = androidx.compose.animation.core.tween(240)))
                                    .togetherWith(
                                        androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(200)) +
                                            androidx.compose.animation.scaleOut(targetScale = 0.98f, animationSpec = androidx.compose.animation.core.tween(200))
                                    )
                            },
                            label = "screen_nav_transition",
                            modifier = Modifier.fillMaxSize()
                        ) { targetNav ->
                            when (targetNav) {
                                NavigationItem.FEED -> FeedScreen(
                                    state = feedUiState,
                                    onEvent = feedViewModel::onEvent
                                )
                                NavigationItem.SEARCH -> SearchScreen(
                                    state = searchUiState,
                                    onEvent = searchViewModel::onEvent
                                )
                                NavigationItem.LIBRARY -> LibraryScreen(
                                    state = libraryUiState,
                                    onEvent = libraryViewModel::onEvent
                                )
                            }
                        }

                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            com.masum.fathom.ui.components.SacredAudioPlayerBar()
                            com.masum.fathom.ui.components.FathomBottomNavBar(
                                selectedItem = selectedNav,
                                onItemSelected = { selectedNav = it }
                            )
                        }

                        if (showSanctuaryModal) {
                            com.masum.fathom.ui.sanctuary.SanctuaryModal(
                                preferences = sanctuaryPreferences,
                                dataEngine = sanctuaryDataEngine,
                                onDismiss = { showSanctuaryModal = false }
                            )
                        }
                    }
                }
            }
        }
    }
}