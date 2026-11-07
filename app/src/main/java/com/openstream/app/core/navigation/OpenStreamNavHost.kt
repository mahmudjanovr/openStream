package com.openstream.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.openstream.app.R
import com.openstream.app.core.designsystem.component.OfflineModeBanner
import com.openstream.app.core.designsystem.component.OpenStreamBottomNavigation
import com.openstream.app.core.designsystem.component.OpenStreamNavigationItem
import com.openstream.app.core.di.AppContainer
import com.openstream.app.core.localization.AppLanguage
import com.openstream.app.feature.detail.MovieDetailScreen
import com.openstream.app.feature.detail.MovieDetailViewModel
import com.openstream.app.feature.player.PlayerScreen
import com.openstream.app.feature.player.PlayerViewModel
import com.openstream.app.feature.discover.DiscoverScreen
import com.openstream.app.feature.discover.DiscoverViewModel
import com.openstream.app.feature.home.HomeScreen
import com.openstream.app.feature.home.HomeViewModel
import com.openstream.app.feature.library.LibraryScreen
import com.openstream.app.feature.library.LibraryViewModel
import com.openstream.app.feature.onboarding.OnboardingScreen
import com.openstream.app.feature.onboarding.OnboardingViewModel
import com.openstream.app.feature.search.SearchScreen
import com.openstream.app.feature.search.SearchViewModel
import com.openstream.app.feature.settings.SettingsScreen
import com.openstream.app.feature.settings.SettingsViewModel
import com.openstream.app.feature.splash.SplashScreen
import com.openstream.app.feature.splash.SplashViewModel
import com.openstream.app.feature.update.UpdateDialog
import com.openstream.app.feature.update.UpdateViewModel

@Composable
fun OpenStreamApp(
    appContainer: AppContainer,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isOnline by appContainer.networkMonitor.isOnline.collectAsState(initial = true)

    val updateViewModel: UpdateViewModel = viewModel(
        factory = UpdateViewModel.provideFactory(appContainer.appUpdateManager)
    )
    val updateUiState by updateViewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        updateViewModel.checkForUpdates(isManualCheck = false)
    }

    val appContext = androidx.compose.ui.platform.LocalContext.current
    val upToDateMessage = androidx.compose.ui.res.stringResource(R.string.update_no_update_toast)
    androidx.compose.runtime.LaunchedEffect(updateUiState.infoMessage) {
        if (updateUiState.infoMessage == "UP_TO_DATE") {
            android.widget.Toast.makeText(appContext, upToDateMessage, android.widget.Toast.LENGTH_SHORT).show()
            updateViewModel.clearInfoMessage()
        }
    }

    val bottomNavDestinations = listOf(
        OpenStreamDestinations.HOME,
        OpenStreamDestinations.DISCOVER,
        OpenStreamDestinations.SEARCH,
        OpenStreamDestinations.LIBRARY,
        OpenStreamDestinations.SETTINGS
    )

    val showBottomBar = currentRoute in bottomNavDestinations

    val homeLabel = androidx.compose.ui.res.stringResource(R.string.nav_home)
    val discoverLabel = androidx.compose.ui.res.stringResource(R.string.nav_discover)
    val searchLabel = androidx.compose.ui.res.stringResource(R.string.nav_search)
    val libraryLabel = androidx.compose.ui.res.stringResource(R.string.nav_library)
    val settingsLabel = androidx.compose.ui.res.stringResource(R.string.nav_settings)

    val navItems = androidx.compose.runtime.remember(homeLabel, discoverLabel, searchLabel, libraryLabel, settingsLabel) {
        listOf(
            TopLevelNavItems[0].copy(label = homeLabel),
            TopLevelNavItems[1].copy(label = discoverLabel),
            TopLevelNavItems[2].copy(label = searchLabel),
            TopLevelNavItems[3].copy(label = libraryLabel),
            TopLevelNavItems[4].copy(label = settingsLabel)
        )
    }

    var lastNavTime = androidx.compose.runtime.remember { 0L }
    val onDebouncedMovieClick: (com.openstream.app.domain.model.Movie) -> Unit = androidx.compose.runtime.remember(navController) {
        { movie ->
            val now = System.currentTimeMillis()
            if (now - lastNavTime > 500) {
                lastNavTime = now
                navController.navigate(OpenStreamDestinations.movieDetailRoute(movie.id))
            }
        }
    }

    val onDebouncedWatchClick: (com.openstream.app.domain.model.Movie) -> Unit = androidx.compose.runtime.remember(navController) {
        { movie ->
            val now = System.currentTimeMillis()
            if (now - lastNavTime > 500) {
                lastNavTime = now
                navController.navigate(OpenStreamDestinations.playerRoute(movie.id))
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                OpenStreamBottomNavigation(
                    items = navItems,
                    currentRoute = currentRoute,
                    onItemSelected = { item ->
                        if (currentRoute != item.route) {
                            val now = System.currentTimeMillis()
                            if (now - lastNavTime > 300) {
                                lastNavTime = now
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Global smart offline status indicator
            if (showBottomBar) {
                OfflineModeBanner(isOnline = isOnline)
            }

            NavHost(
                navController = navController,
                startDestination = OpenStreamDestinations.SPLASH,
                modifier = Modifier.weight(1f)
            ) {
                // Splash Destination
                composable(OpenStreamDestinations.SPLASH) {
                    val splashViewModel: SplashViewModel = viewModel(
                        factory = SplashViewModel.provideFactory(appContainer.userPreferencesRepository)
                    )
                    SplashScreen(
                        viewModel = splashViewModel,
                        onNavigate = { destination ->
                            navController.navigate(destination) {
                                popUpTo(OpenStreamDestinations.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                // Onboarding Destination
                composable(OpenStreamDestinations.ONBOARDING) {
                    val onboardingViewModel: OnboardingViewModel = viewModel(
                        factory = OnboardingViewModel.provideFactory(appContainer.userPreferencesRepository)
                    )
                    OnboardingScreen(
                        viewModel = onboardingViewModel,
                        onComplete = {
                            navController.navigate(OpenStreamDestinations.HOME) {
                                popUpTo(OpenStreamDestinations.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }

                // Home Destination with Intelligent Recommendation Engine
                composable(OpenStreamDestinations.HOME) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.provideFactory(
                            catalogRepository = appContainer.movieCatalogRepository,
                            libraryRepository = appContainer.libraryRepository,
                            userProfileRepository = appContainer.userProfileRepository,
                            userPreferencesRepository = appContainer.userPreferencesRepository,
                            playbackHistoryRepository = appContainer.playbackHistoryRepository
                        )
                    )
                    HomeScreen(
                        viewModel = homeViewModel,
                        onMovieClick = onDebouncedMovieClick
                    )
                }

                // Discover Destination with Multi-Faceted Filters & Sorting
                composable(OpenStreamDestinations.DISCOVER) {
                    val discoverViewModel: DiscoverViewModel = viewModel(
                        factory = DiscoverViewModel.provideFactory(appContainer.movieCatalogRepository)
                    )
                    DiscoverScreen(
                        viewModel = discoverViewModel,
                        onMovieClick = onDebouncedMovieClick
                    )
                }

                // Search Destination with Live Suggestions & History
                composable(OpenStreamDestinations.SEARCH) {
                    val searchViewModel: SearchViewModel = viewModel(
                        factory = SearchViewModel.provideFactory(appContainer.movieCatalogRepository)
                    )
                    SearchScreen(
                        viewModel = searchViewModel,
                        onMovieClick = onDebouncedMovieClick
                    )
                }

                // Library Destination with Offline Downloads & Isolated Profiles
                composable(OpenStreamDestinations.LIBRARY) {
                    val libraryViewModel: LibraryViewModel = viewModel(
                        factory = LibraryViewModel.provideFactory(
                            libraryRepository = appContainer.libraryRepository,
                            downloadManager = appContainer.downloadManager
                        )
                    )
                    LibraryScreen(
                        viewModel = libraryViewModel,
                        onMovieClick = onDebouncedMovieClick,
                        onExploreCatalog = {
                            navController.navigate(OpenStreamDestinations.DISCOVER) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                // Settings Destination with User Profiles & Languages
                composable(OpenStreamDestinations.SETTINGS) {
                    val settingsViewModel: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.provideFactory(
                            userPreferencesRepository = appContainer.userPreferencesRepository,
                            userProfileRepository = appContainer.userProfileRepository
                        )
                    )
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onLanguageChange = onLanguageChange,
                        onCheckForUpdates = {
                            updateViewModel.checkForUpdates(isManualCheck = true)
                        }
                    )
                }

                // Movie Detail Destination
                composable(
                    route = OpenStreamDestinations.MOVIE_DETAIL,
                    arguments = listOf(
                        navArgument("movieId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                    val detailViewModel: MovieDetailViewModel = viewModel(
                        factory = MovieDetailViewModel.provideFactory(
                            movieId = movieId,
                            catalogRepository = appContainer.movieCatalogRepository,
                            libraryRepository = appContainer.libraryRepository,
                            downloadManager = appContainer.downloadManager
                        )
                    )
                    MovieDetailScreen(
                        viewModel = detailViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onRelatedMovieClick = onDebouncedMovieClick,
                        onWatchClick = onDebouncedWatchClick
                    )
                }

                // Real Media3 Player Destination with PiP, Subtitles & Quality
                composable(
                    route = OpenStreamDestinations.PLAYER,
                    arguments = listOf(
                        navArgument("movieId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val playerManager = androidx.compose.runtime.remember {
                        com.openstream.app.core.player.OpenStreamPlayerManager(context)
                    }
                    val playerViewModel: PlayerViewModel = viewModel(
                        factory = PlayerViewModel.provideFactory(
                            movieId = movieId,
                            catalogRepository = appContainer.movieCatalogRepository,
                            playbackHistoryRepository = appContainer.playbackHistoryRepository,
                            userPreferencesRepository = appContainer.userPreferencesRepository,
                            playerManager = playerManager
                        )
                    )
                    PlayerScreen(
                        viewModel = playerViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }

        if (updateUiState.isVisible && updateUiState.manifest != null) {
            UpdateDialog(
                manifest = updateUiState.manifest!!,
                isForced = updateUiState.isForced,
                downloadState = updateUiState.downloadState,
                onStartDownload = updateViewModel::startUpdate,
                onInstallApk = updateViewModel::installApk,
                onDismiss = updateViewModel::dismissDialog
            )
        }
    }
}
