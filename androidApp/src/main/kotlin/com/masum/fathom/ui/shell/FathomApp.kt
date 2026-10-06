package com.masum.fathom.ui.shell

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.masum.fathom.NavigationItem
import com.masum.fathom.core.AppGraph
import com.masum.fathom.core.audio.LocalOpenAmbience
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.audio.LocalSanctuaryAmbienceEngine
import com.masum.fathom.core.audio.SacredAudioEngine
import com.masum.fathom.core.audio.SanctuaryAmbienceEngine
import com.masum.fathom.core.haptics.FathomHaptics
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.sanctuary.LocalOpenSanctuary
import com.masum.fathom.core.sanctuary.LocalSanctuaryDataEngine
import com.masum.fathom.core.sanctuary.LocalSanctuaryPreferences
import com.masum.fathom.core.sanctuary.SanctuaryDataEngine
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import com.masum.fathom.core.subscription.LocalOpenPaywall
import com.masum.fathom.core.subscription.LocalSubscriptionManager
import com.masum.fathom.core.subscription.LocalSubscriptionState
import com.masum.fathom.core.subscription.SubscriptionManager
import com.masum.fathom.core.notification.DailyNotificationScheduler
import com.masum.fathom.core.update.AppUpdateEngine
import com.masum.fathom.core.update.UpdateCheckScheduler
import com.masum.fathom.core.update.LocalAppUpdateEngine
import com.masum.fathom.ui.components.AmbienceSoundscapeSheet
import com.masum.fathom.ui.components.AppUpdateBanner
import com.masum.fathom.ui.components.DockBarHeight
import com.masum.fathom.ui.components.DockRailWidth
import com.masum.fathom.ui.components.FathomBottomNavBar
import com.masum.fathom.ui.components.SacredAudioPlayerBar
import com.masum.fathom.ui.design.LocalBackdrop
import com.masum.fathom.ui.design.LocalNavTradition
import com.masum.fathom.ui.design.LocalShell
import com.masum.fathom.ui.design.ShellMetrics
import com.masum.fathom.ui.design.widthClassOf
import com.masum.fathom.ui.feed.FeedScreen
import com.masum.fathom.ui.feed.FeedViewModel
import com.masum.fathom.ui.library.LibraryScreen
import com.masum.fathom.ui.library.LibraryViewModel
import com.masum.fathom.ui.onboarding.OnboardingScreen
import com.masum.fathom.ui.pathways.PathwaysScreen
import com.masum.fathom.ui.pathways.PathwaysUiState
import com.masum.fathom.ui.pathways.PathwaysViewModel
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.masum.fathom.ui.design.BreathingMark
import com.masum.fathom.ui.design.StateFrame
import com.masum.fathom.ui.library.JournalPage
import com.masum.fathom.ui.library.LibraryOverlays
import com.masum.fathom.ui.pathways.PathwaysOverlays
import com.masum.fathom.ui.pathways.PathwaysUiEvent
import com.masum.fathom.ui.pathways.PathwaysChapterScreen
import com.masum.fathom.ui.search.SearchBookScreen
import com.masum.fathom.ui.search.SearchOverlays
import com.masum.fathom.ui.search.SearchUiEvent
import com.masum.fathom.ui.sanctuary.SanctuaryScreen
import com.masum.fathom.ui.search.SearchScreen
import com.masum.fathom.ui.search.SearchViewModel
import com.masum.fathom.ui.splash.SplashScreen
import com.masum.fathom.ui.subscription.SanctuaryPaywallSheet
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.whatsnew.WhatsNewModal

private enum class AppStage { Splash, Onboarding, Main }

fun traditionIdForLabel(label: String?): TraditionId? = when (label) {
    "Sanatana Dharma", "Hinduism" -> TraditionId.HINDUISM
    "Islam" -> TraditionId.ISLAM
    "Christianity" -> TraditionId.CHRISTIANITY
    "Buddhism" -> TraditionId.BUDDHISM
    "Taoism" -> TraditionId.TAOISM
    "Stoicism" -> TraditionId.STOICISM
    "Judaism" -> TraditionId.JUDAISM
    else -> null
}

@Composable
fun FathomApp(graph: AppGraph, launchPassageId: String?) {
    val context = LocalContext.current

    val preferences = remember { SanctuaryPreferences(context) }
    val dataEngine = remember { SanctuaryDataEngine(context, graph.userRepository, graph.passageRepository) }
    val settings by preferences.settings.collectAsState()

    val feedViewModel: FeedViewModel = viewModel {
        FeedViewModel(
            feedRepository = graph.feedEngine,
            userDataRepository = graph.userRepository,
            passageRepository = graph.passageRepository,
            sanctuaryPreferences = preferences
        )
    }
    val searchViewModel: SearchViewModel = viewModel {
        SearchViewModel(passageRepository = graph.passageRepository, userDataRepository = graph.userRepository)
    }

    val libraryViewModel: LibraryViewModel = viewModel {
        LibraryViewModel(
            userDataRepository = graph.userRepository,
            passageRepository = graph.passageRepository,
            sanctuaryPreferences = preferences
        )
    }
    val pathwaysViewModel: PathwaysViewModel = viewModel {
        PathwaysViewModel(passageRepository = graph.passageRepository, userDataRepository = graph.userRepository)
    }

    val feedState by feedViewModel.uiState.collectAsStateWithLifecycle()
    val pathwaysState by pathwaysViewModel.uiState.collectAsStateWithLifecycle()
    val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val libraryState by libraryViewModel.uiState.collectAsStateWithLifecycle()

    val subscriptionManager = remember { SubscriptionManager(context) }
    val subscriptionState by subscriptionManager.subscriptionState.collectAsState()
    val updateEngine = remember { AppUpdateEngine(context) }
    DisposableEffect(updateEngine) {
        updateEngine.checkForUpdates(manual = false)
        onDispose { updateEngine.release() }
    }

    val haptics = remember { FathomHaptics(context) }
    val audioEngine = remember { SacredAudioEngine(context) }
    DisposableEffect(audioEngine) { onDispose { audioEngine.release() } }
    val ambienceEngine = remember { SanctuaryAmbienceEngine(context) }
    DisposableEffect(ambienceEngine) { onDispose { ambienceEngine.release() } }

    val versionCode = remember(context) { currentVersionCode(context) }

    var showPaywall by remember { mutableStateOf(false) }
    var showWhatsNew by remember { mutableStateOf(false) }
    var showAmbience by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }
    val navigator = rememberAppNavigator()
    var splashDone by remember { mutableStateOf(!launchPassageId.isNullOrEmpty()) }

    LaunchedEffect(settings.hasCompletedOnboarding, versionCode) {
        if (settings.hasCompletedOnboarding && preferences.shouldShowWhatsNew(versionCode)) {
            showWhatsNew = true
        }
    }

    LaunchedEffect(settings.dailyNotificationEnabled, settings.notificationHour, settings.notificationMinute, settings.updateAlertsEnabled) {
        if (settings.dailyNotificationEnabled) {
            DailyNotificationScheduler.scheduleDailyReminder(context, settings.notificationHour, settings.notificationMinute)
        } else {
            DailyNotificationScheduler.cancelDailyReminder(context)
        }
        UpdateCheckScheduler.sync(context, settings.updateAlertsEnabled)
    }

    LaunchedEffect(launchPassageId) {
        if (!launchPassageId.isNullOrEmpty()) {
            navigator.resetToFeed()
            feedViewModel.loadSpecificPassage(launchPassageId)
        }
    }

    CompositionLocalProvider(
        LocalFathomHaptics provides haptics,
        LocalSacredAudioEngine provides audioEngine,
        LocalSanctuaryAmbienceEngine provides ambienceEngine,
        LocalOpenAmbience provides { showAmbience = true },
        LocalSanctuaryPreferences provides preferences,
        LocalSanctuaryDataEngine provides dataEngine,
        LocalOpenSanctuary provides { navigator.push(AppRoute.Settings) },
        LocalOpenMore provides { showMore = true },
        LocalOpenLegal provides { navigator.push(AppRoute.Legal(it)) },
        LocalSubscriptionManager provides subscriptionManager,
        LocalSubscriptionState provides subscriptionState,
        LocalOpenPaywall provides { showPaywall = true },
        LocalAppUpdateEngine provides updateEngine
    ) {
        FathomTheme(readingTheme = settings.theme) {
            val stage = when {
                !splashDone -> AppStage.Splash
                !settings.hasCompletedOnboarding -> AppStage.Onboarding
                else -> AppStage.Main
            }

            AnimatedContent(
                targetState = stage,
                transitionSpec = {
                    (fadeIn(FathomMotion.enter(520, 80)) + scaleIn(FathomMotion.enter(520, 80), initialScale = 0.985f))
                        .togetherWith(fadeOut(FathomMotion.exit(260)))
                },
                label = "app_stage",
                modifier = Modifier.fillMaxSize().background(FathomTheme.colors.background)
            ) { target ->
                when (target) {
                    AppStage.Splash -> SplashScreen(onSplashFinished = { splashDone = true })
                    AppStage.Onboarding -> OnboardingScreen(
                        onComplete = { name, tradition, personalize ->
                            preferences.completeOnboarding(name, tradition, personalize)
                            feedViewModel.setTraditionFocus(if (personalize) traditionIdForLabel(tradition) else null)
                        }
                    )
                    AppStage.Main -> MainStage(
                        navigator = navigator,
                        feedState = feedState,
                        onFeedEvent = feedViewModel::onEvent,
                        pathwaysState = pathwaysState,
                        onPathwaysEvent = pathwaysViewModel::onEvent,
                        searchState = searchState,
                        onSearchEvent = searchViewModel::onEvent,
                        libraryState = libraryState,
                        onLibraryEvent = libraryViewModel::onEvent,
                        updateEngine = updateEngine,
                        showPaywall = showPaywall,
                        onDismissPaywall = { showPaywall = false },
                        showWhatsNew = showWhatsNew,
                        onDismissWhatsNew = {
                            preferences.markWhatsNewSeen(versionCode)
                            showWhatsNew = false
                        },
                        onOpenWhatsNew = { showWhatsNew = true },
                        showMore = showMore,
                        onDismissMore = { showMore = false },
                        showAmbience = showAmbience,
                        onOpenAmbience = { showAmbience = true },
                        onDismissAmbience = { showAmbience = false },
                        preferences = preferences,
                        dataEngine = dataEngine
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun MainStage(
    navigator: AppNavigator,
    feedState: com.masum.fathom.ui.feed.FeedUiState,
    onFeedEvent: (com.masum.fathom.ui.feed.FeedUiEvent) -> Unit,
    pathwaysState: PathwaysUiState,
    onPathwaysEvent: (com.masum.fathom.ui.pathways.PathwaysUiEvent) -> Unit,
    searchState: com.masum.fathom.ui.search.SearchUiState,
    onSearchEvent: (com.masum.fathom.ui.search.SearchUiEvent) -> Unit,
    libraryState: com.masum.fathom.ui.library.LibraryUiState,
    onLibraryEvent: (com.masum.fathom.ui.library.LibraryUiEvent) -> Unit,
    updateEngine: AppUpdateEngine,
    onOpenWhatsNew: () -> Unit,
    showMore: Boolean,
    onDismissMore: () -> Unit,
    showPaywall: Boolean,
    onDismissPaywall: () -> Unit,
    showWhatsNew: Boolean,
    onDismissWhatsNew: () -> Unit,
    showAmbience: Boolean,
    onOpenAmbience: () -> Unit,
    onDismissAmbience: () -> Unit,
    preferences: SanctuaryPreferences,
    dataEngine: SanctuaryDataEngine
) {
    val colors = FathomTheme.colors
    val navTradition = remember { mutableStateOf<TraditionId?>(null) }
    val backdrop = rememberLayerBackdrop {
        drawRect(colors.background)
        drawContent()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val widthClass = widthClassOf(maxWidth)
        val usesRail = maxWidth >= 600.dp || maxHeight < 480.dp
        val direction = LocalLayoutDirection.current
        val safe = WindowInsets.systemBars.union(WindowInsets.displayCutout).asPaddingValues()
        val navBottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        val leftInset = safe.calculateLeftPadding(direction)
        val dockFootprint = if (usesRail) 0.dp else DockBarHeight
        val metrics = ShellMetrics(
            widthClass = widthClass,
            usesRail = usesRail,
            bottomInset = navBottom + dockFootprint,
            startInset = if (usesRail) DockRailWidth + leftInset else 0.dp,
            contentMaxWidth = if (widthClass == com.masum.fathom.ui.design.WidthClass.Compact) 640.dp else 720.dp,
            isShort = maxHeight < 480.dp
        )

        val imeVisible = WindowInsets.isImeVisible
        val topRoute = navigator.top
        val isStudyingChapter = topRoute is AppRoute.Chapter || topRoute == AppRoute.Settings || topRoute is AppRoute.Legal
        val selectedNav = navigator.currentTab
        val onNavSelected: (NavigationItem) -> Unit = navigator::selectTab

        val desiredChapter = (navigator.stack.firstOrNull { it is AppRoute.Chapter } as? AppRoute.Chapter)?.number
        val activeChapter = (pathwaysState as? PathwaysUiState.Success)?.activeChapterForReading
        val pathwaysReady = pathwaysState is PathwaysUiState.Success
        LaunchedEffect(desiredChapter, activeChapter, pathwaysReady) {
            if (!pathwaysReady) return@LaunchedEffect
            if (desiredChapter != null && activeChapter != desiredChapter) {
                onPathwaysEvent(PathwaysUiEvent.OnOpenChapter(desiredChapter))
            } else if (desiredChapter == null && activeChapter != null) {
                onPathwaysEvent(PathwaysUiEvent.OnCloseChapter)
            }
        }

        val desiredBook = (navigator.stack.firstOrNull { it is AppRoute.Book } as? AppRoute.Book)?.id
        val activeBook = searchState.selectedBook
        LaunchedEffect(desiredBook, activeBook?.id, searchState.allBooks.size) {
            if (desiredBook != null && activeBook?.id != desiredBook) {
                searchState.allBooks.firstOrNull { it.id == desiredBook }?.let { onSearchEvent(SearchUiEvent.OnSelectBook(it)) }
            } else if (desiredBook == null && activeBook != null) {
                onSearchEvent(SearchUiEvent.OnSelectBook(null))
            }
        }
        val lastBook = remember { mutableStateOf<com.masum.fathom.core.model.Book?>(null) }
        if (activeBook != null) lastBook.value = activeBook

        CompositionLocalProvider(LocalBackdrop provides backdrop, LocalShell provides metrics, LocalNavTradition provides navTradition) {
            Box(modifier = Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                CompositionLocalProvider(LocalBackdrop provides null) {
                    NavDisplay(
                        backStack = navigator.stack,
                        modifier = Modifier.fillMaxSize(),
                        onBack = navigator::pop,
                        transitionSpec = { routeTransition(forward = true) },
                        popTransitionSpec = { routeTransition(forward = false) },
                        predictivePopTransitionSpec = { _ ->
                            (scaleIn(tween(300, easing = FathomMotion.Emphasized), initialScale = 0.94f))
                                .togetherWith(fadeOut(tween(260, delayMillis = 180)) + scaleOut(tween(300, easing = FathomMotion.Emphasized), targetScale = 0.9f))
                        },
                        entryProvider = entryProvider {
                            entry<AppRoute.Tab> { route ->
                                when (route.item) {
                                    NavigationItem.FEED -> FeedScreen(state = feedState, onEvent = onFeedEvent)
                                    NavigationItem.PATHWAYS -> PathwaysScreen(
                                        state = pathwaysState,
                                        onEvent = onPathwaysEvent,
                                        onOpenChapter = { navigator.push(AppRoute.Chapter(it)) }
                                    )
                                    NavigationItem.SEARCH -> SearchScreen(
                                        state = searchState,
                                        onEvent = onSearchEvent,
                                        onOpenBook = { navigator.push(AppRoute.Book(it.id)) }
                                    )
                                    NavigationItem.LIBRARY -> LibraryScreen(
                                        state = libraryState,
                                        onEvent = onLibraryEvent,
                                        page = JournalPage.Hub,
                                        onNavigate = { navigator.push(AppRoute.Journal(it)) },
                                        onBack = navigator::pop,
                                        onOpenWhatsNew = onOpenWhatsNew
                                    )
                                }
                            }
                            entry<AppRoute.Chapter> { route ->
                                PathwaysChapterScreen(
                                    state = pathwaysState,
                                    chapterNumber = route.number,
                                    onEvent = onPathwaysEvent,
                                    onComplete = {
                                        navigator.pop()
                                        onPathwaysEvent(PathwaysUiEvent.OnCompleteChapter(route.number))
                                    },
                                    onBack = navigator::pop
                                )
                            }
                            entry<AppRoute.Book> { route ->
                                val book = lastBook.value?.takeIf { it.id == route.id } ?: searchState.allBooks.firstOrNull { it.id == route.id }
                                if (book != null) {
                                    SearchBookScreen(state = searchState, book = book, onEvent = onSearchEvent, onBack = navigator::pop)
                                } else {
                                    StateFrame { BreathingMark() }
                                }
                            }
                            entry<AppRoute.Journal> { route ->
                                LibraryScreen(
                                    state = libraryState,
                                    onEvent = onLibraryEvent,
                                    page = route.page,
                                    onNavigate = { navigator.push(AppRoute.Journal(it)) },
                                    onBack = navigator::pop,
                                    onOpenWhatsNew = onOpenWhatsNew
                                )
                            }
                            entry<AppRoute.Settings> {
                                SanctuaryScreen(preferences = preferences, dataEngine = dataEngine, onDismiss = navigator::pop)
                            }
                            entry<AppRoute.Legal> { route ->
                                LegalScreen(doc = route.doc, onBack = navigator::pop)
                            }
                        }
                    )
                }
            }

            if (!usesRail && !isStudyingChapter) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(metrics.bottomInset + 44.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colors.background.copy(alpha = 0.50f),
                                    colors.background.copy(alpha = 0.88f),
                                    colors.background
                                )
                            )
                        )
                )
            }

            if (usesRail && !isStudyingChapter) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(metrics.startInset + 16.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    colors.background,
                                    colors.background.copy(alpha = 0.88f),
                                    colors.background.copy(alpha = 0.45f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = metrics.startInset),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SacredAudioPlayerBar(isNavbarVisible = !isStudyingChapter && !usesRail)
                if (!usesRail) {
                    AnimatedVisibility(
                        visible = !isStudyingChapter && !imeVisible,
                        enter = fadeIn(tween(240)) + slideInVertically(tween(420, 0, FathomMotion.Emphasized)) { it },
                        exit = fadeOut(tween(160)) + slideOutVertically(tween(260, 0, FathomMotion.EmphasizedOut)) { it }
                    ) {
                        FathomBottomNavBar(
                            selectedItem = selectedNav,
                            onItemSelected = onNavSelected,
                            traditionId = navTradition.value
                        )
                    }
                } else {
                    Box(Modifier.padding(bottom = navBottom + 4.dp))
                }
            }

            if (usesRail) {
                AnimatedVisibility(
                    visible = !isStudyingChapter && !imeVisible,
                    modifier = Modifier.align(Alignment.CenterStart),
                    enter = fadeIn(tween(240)) + slideInHorizontally(tween(420, 0, FathomMotion.Emphasized)) { -it },
                    exit = fadeOut(tween(160)) + slideOutHorizontally(tween(260, 0, FathomMotion.EmphasizedOut)) { -it }
                ) {
                    FathomBottomNavBar(
                        selectedItem = selectedNav,
                        onItemSelected = onNavSelected,
                        traditionId = navTradition.value,
                        vertical = true
                    )
                }
            }

            AppUpdateBanner(updateEngine = updateEngine, modifier = Modifier.align(Alignment.TopCenter))

            PathwaysOverlays(state = pathwaysState, onEvent = onPathwaysEvent)
            SearchOverlays(state = searchState, onEvent = onSearchEvent)
            LibraryOverlays(state = libraryState, onEvent = onLibraryEvent)
            if (showPaywall) SanctuaryPaywallSheet(onDismiss = onDismissPaywall)
            if (showWhatsNew) WhatsNewModal(onDismiss = onDismissWhatsNew)
            if (showAmbience) AmbienceSoundscapeSheet(onDismiss = onDismissAmbience)
            if (showMore) {
                MoreSheet(
                    onDismiss = onDismissMore,
                    onSoundscapes = onOpenAmbience,
                    onSettings = { navigator.push(AppRoute.Settings) },
                    onWhatsNew = onOpenWhatsNew,
                    onLegal = { navigator.push(AppRoute.Legal(it)) }
                )
            }
        }
    }
}

private fun AnimatedContentTransitionScope<Scene<AppRoute>>.routeTransition(forward: Boolean): ContentTransform {
    val from = initialState.entries.lastOrNull()?.contentKey as? AppRoute
    val to = targetState.entries.lastOrNull()?.contentKey as? AppRoute
    val sign = if (forward) 1 else -1
    return when {
        from is AppRoute.Tab && to is AppRoute.Tab -> {
            val dir = if (to.item.ordinal >= from.item.ordinal) 1 else -1
            (fadeIn(tween(320, 60)) + slideInHorizontally(tween(420, 0, FathomMotion.Emphasized)) { dir * it / 14 })
                .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(320, 0, FathomMotion.Emphasized)) { -dir * it / 18 })
        }
        to == AppRoute.Settings || from == AppRoute.Settings -> {
            if (forward) {
                (fadeIn(tween(300, 40)) + slideInVertically(tween(460, 0, FathomMotion.Emphasized)) { it / 10 })
                    .togetherWith(fadeOut(tween(220)))
            } else {
                fadeIn(tween(260)).togetherWith(fadeOut(tween(200)) + slideOutVertically(tween(360, 0, FathomMotion.Emphasized)) { it / 10 })
            }
        }
        forward -> (fadeIn(tween(320, 60)) + slideInHorizontally(tween(460, 0, FathomMotion.Emphasized)) { it / 5 })
            .togetherWith(fadeOut(tween(200)) + slideOutHorizontally(tween(360, 0, FathomMotion.Emphasized)) { -it / 8 })
        else -> (fadeIn(tween(320, 60)) + slideInHorizontally(tween(460, 0, FathomMotion.Emphasized)) { -it / 8 })
            .togetherWith(fadeOut(tween(200)) + slideOutHorizontally(tween(360, 0, FathomMotion.Emphasized)) { it / 5 })
    }
}

private fun currentVersionCode(context: android.content.Context): Int {
    val info = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
    } catch (_: Exception) {
        null
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info?.longVersionCode?.toInt() ?: 4
    } else {
        @Suppress("DEPRECATION")
        info?.versionCode ?: 4
    }
}
