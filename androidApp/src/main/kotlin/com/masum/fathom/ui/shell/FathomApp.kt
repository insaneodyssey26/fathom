package com.masum.fathom.ui.shell

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
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
import com.masum.fathom.core.update.AppUpdateEngine
import com.masum.fathom.core.update.LocalAppUpdateEngine
import com.masum.fathom.ui.components.AmbienceFloatingPill
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
import com.masum.fathom.ui.sanctuary.SanctuaryModal
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

    val feedViewModel: FeedViewModel = viewModel {
        FeedViewModel(
            feedRepository = graph.feedEngine,
            userDataRepository = graph.userRepository,
            passageRepository = graph.passageRepository
        )
    }
    val searchViewModel: SearchViewModel = viewModel {
        SearchViewModel(passageRepository = graph.passageRepository, userDataRepository = graph.userRepository)
    }

    val preferences = remember { SanctuaryPreferences(context) }
    val dataEngine = remember { SanctuaryDataEngine(context, graph.userRepository, graph.passageRepository) }
    val settings by preferences.settings.collectAsState()

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

    var showSanctuary by remember { mutableStateOf(false) }
    var showPaywall by remember { mutableStateOf(false) }
    var showWhatsNew by remember { mutableStateOf(false) }
    var showAmbience by remember { mutableStateOf(false) }
    var selectedNav by remember { mutableStateOf(NavigationItem.FEED) }
    var splashDone by remember { mutableStateOf(false) }

    LaunchedEffect(settings.hasCompletedOnboarding, versionCode) {
        if (settings.hasCompletedOnboarding && preferences.shouldShowWhatsNew(versionCode)) showWhatsNew = true
    }

    LaunchedEffect(launchPassageId) {
        if (!launchPassageId.isNullOrEmpty()) {
            selectedNav = NavigationItem.FEED
            feedViewModel.loadSpecificPassage(launchPassageId)
        }
    }

    LaunchedEffect(settings.hasCompletedOnboarding, settings.userTradition, settings.feedPersonalizedToTradition) {
        if (settings.hasCompletedOnboarding && settings.feedPersonalizedToTradition) {
            traditionIdForLabel(settings.userTradition)?.let { feedViewModel.setTraditionFocus(it) }
        }
    }

    if (selectedNav != NavigationItem.FEED) BackHandler { selectedNav = NavigationItem.FEED }

    CompositionLocalProvider(
        LocalFathomHaptics provides haptics,
        LocalSacredAudioEngine provides audioEngine,
        LocalSanctuaryAmbienceEngine provides ambienceEngine,
        LocalOpenAmbience provides { showAmbience = true },
        LocalSanctuaryPreferences provides preferences,
        LocalSanctuaryDataEngine provides dataEngine,
        LocalOpenSanctuary provides { showSanctuary = true },
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
                        selectedNav = selectedNav,
                        onNavSelected = { selectedNav = it },
                        feedState = feedState,
                        onFeedEvent = feedViewModel::onEvent,
                        pathwaysState = pathwaysState,
                        onPathwaysEvent = pathwaysViewModel::onEvent,
                        searchState = searchState,
                        onSearchEvent = searchViewModel::onEvent,
                        libraryState = libraryState,
                        onLibraryEvent = libraryViewModel::onEvent,
                        updateEngine = updateEngine,
                        showSanctuary = showSanctuary,
                        onDismissSanctuary = { showSanctuary = false },
                        showPaywall = showPaywall,
                        onDismissPaywall = { showPaywall = false },
                        showWhatsNew = showWhatsNew,
                        onDismissWhatsNew = {
                            preferences.markWhatsNewSeen(versionCode)
                            showWhatsNew = false
                        },
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
    selectedNav: NavigationItem,
    onNavSelected: (NavigationItem) -> Unit,
    feedState: com.masum.fathom.ui.feed.FeedUiState,
    onFeedEvent: (com.masum.fathom.ui.feed.FeedUiEvent) -> Unit,
    pathwaysState: PathwaysUiState,
    onPathwaysEvent: (com.masum.fathom.ui.pathways.PathwaysUiEvent) -> Unit,
    searchState: com.masum.fathom.ui.search.SearchUiState,
    onSearchEvent: (com.masum.fathom.ui.search.SearchUiEvent) -> Unit,
    libraryState: com.masum.fathom.ui.library.LibraryUiState,
    onLibraryEvent: (com.masum.fathom.ui.library.LibraryUiEvent) -> Unit,
    updateEngine: AppUpdateEngine,
    showSanctuary: Boolean,
    onDismissSanctuary: () -> Unit,
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
        val isStudyingChapter = selectedNav == NavigationItem.PATHWAYS &&
            (pathwaysState as? PathwaysUiState.Success)?.activeChapterForReading != null

        CompositionLocalProvider(LocalBackdrop provides backdrop, LocalShell provides metrics, LocalNavTradition provides navTradition) {
            Box(modifier = Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                CompositionLocalProvider(LocalBackdrop provides null) {
                AnimatedContent(
                    targetState = selectedNav,
                    transitionSpec = {
                        val dir = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                        (fadeIn(tween(320, 60)) + slideInHorizontally(tween(420, 0, FathomMotion.Emphasized)) { dir * it / 14 })
                            .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(320, 0, FathomMotion.Emphasized)) { -dir * it / 18 })
                    },
                    label = "screen_nav",
                    modifier = Modifier.fillMaxSize()
                ) { target ->
                    when (target) {
                        NavigationItem.FEED -> FeedScreen(state = feedState, onEvent = onFeedEvent)
                        NavigationItem.PATHWAYS -> PathwaysScreen(state = pathwaysState, onEvent = onPathwaysEvent)
                        NavigationItem.SEARCH -> SearchScreen(state = searchState, onEvent = onSearchEvent)
                        NavigationItem.LIBRARY -> LibraryScreen(state = libraryState, onEvent = onLibraryEvent)
                    }
                }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = metrics.startInset),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AmbienceFloatingPill(
                    onClick = onOpenAmbience,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
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

            if (showSanctuary) SanctuaryModal(preferences = preferences, dataEngine = dataEngine, onDismiss = onDismissSanctuary)
            if (showPaywall) SanctuaryPaywallSheet(onDismiss = onDismissPaywall)
            if (showWhatsNew) WhatsNewModal(onDismiss = onDismissWhatsNew)
            if (showAmbience) AmbienceSoundscapeSheet(onDismiss = onDismissAmbience)
        }
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
