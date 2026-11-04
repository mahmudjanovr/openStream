package com.openstream.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import com.openstream.app.R
import com.openstream.app.core.designsystem.component.OpenStreamNavigationItem

object OpenStreamDestinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val DISCOVER = "discover"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
}

val TopLevelNavItems = listOf(
    OpenStreamNavigationItem(
        route = OpenStreamDestinations.HOME,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    OpenStreamNavigationItem(
        route = OpenStreamDestinations.DISCOVER,
        label = "Discover",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore
    ),
    OpenStreamNavigationItem(
        route = OpenStreamDestinations.SEARCH,
        label = "Search",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    OpenStreamNavigationItem(
        route = OpenStreamDestinations.LIBRARY,
        label = "Library",
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder
    ),
    OpenStreamNavigationItem(
        route = OpenStreamDestinations.SETTINGS,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)
