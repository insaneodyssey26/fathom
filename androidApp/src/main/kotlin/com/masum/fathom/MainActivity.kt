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
                val dbFile = context.getDatabasePath("starter_content.db")
                val prefs = context.getSharedPreferences("fathom_content_prefs", android.content.Context.MODE_PRIVATE)
                val currentVersion = prefs.getInt("db_asset_version", 0)
                val TARGET_VERSION = 4
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