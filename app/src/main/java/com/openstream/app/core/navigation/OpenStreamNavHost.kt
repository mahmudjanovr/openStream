package com.openstream.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.openstream.app.R
import com.openstream.app.core.designsystem.component.OpenStreamBottomNavigation
import com.openstream.app.core.designsystem.component.OpenStreamNavigationItem
import com.openstream.app.core.di.AppContainer
import com.openstream.app.core.localization.AppLanguage
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

@Composable
fun OpenStreamApp(
    appContainer: AppContainer,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavDestinations = listOf(
        OpenStreamDestinations.HOME,
        OpenStreamDestinations.DISCOVER,
        OpenStreamDestinations.SEARCH,
        OpenStreamDestinations.LIBRARY,
        OpenStreamDestinations.SETTINGS
    )

    val showBottomBar = currentRoute in bottomNavDestinations

    val navItems = listOf(
        TopLevelNavItems[0].copy(label = androidx.compose.ui.res.stringResource(R.string.nav_home)),
        TopLevelNavItems[1].copy(label = androidx.compose.ui.res.stringResource(R.string.nav_discover)),
        TopLevelNavItems[2].copy(label = androidx.compose.ui.res.stringResource(R.string.nav_search)),
        TopLevelNavItems[3].copy(label = androidx.compose.ui.res.stringResource(R.string.nav_library)),
        TopLevelNavItems[4].copy(label = androidx.compose.ui.res.stringResource(R.string.nav_settings))
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                OpenStreamBottomNavigation(
                    items = navItems,
                    currentRoute = currentRoute,
                    onItemSelected = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = OpenStreamDestinations.SPLASH,
            modifier = Modifier.padding(innerPadding)
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

            // Home Destination
            composable(OpenStreamDestinations.HOME) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.provideFactory(
                        appContainer.movieCatalogRepository,
                        appContainer.libraryRepository
                    )
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onMovieClick = { _ ->
                        // Future Phase: movie detail & playback
                    }
                )
            }

            // Discover Destination
            composable(OpenStreamDestinations.DISCOVER) {
                val discoverViewModel: DiscoverViewModel = viewModel(
                    factory = DiscoverViewModel.provideFactory(appContainer.movieCatalogRepository)
                )
                DiscoverScreen(
                    viewModel = discoverViewModel,
                    onMovieClick = { _ ->
                        // Future Phase: movie detail & playback
                    }
                )
            }

            // Search Destination
            composable(OpenStreamDestinations.SEARCH) {
                val searchViewModel: SearchViewModel = viewModel(
                    factory = SearchViewModel.provideFactory(appContainer.movieCatalogRepository)
                )
                SearchScreen(
                    viewModel = searchViewModel,
                    onMovieClick = { _ ->
                        // Future Phase: movie detail & playback
                    }
                )
            }

            // Library Destination
            composable(OpenStreamDestinations.LIBRARY) {
                val libraryViewModel: LibraryViewModel = viewModel(
                    factory = LibraryViewModel.provideFactory(appContainer.libraryRepository)
                )
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onMovieClick = { _ ->
                        // Future Phase: movie detail & playback
                    }
                )
            }

            // Settings Destination
            composable(OpenStreamDestinations.SETTINGS) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.provideFactory(appContainer.userPreferencesRepository)
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onLanguageChange = onLanguageChange
                )
            }
        }
    }
}
