package com.openstream.app.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.openstream.app.R
import com.openstream.app.core.designsystem.component.OpenStreamEmptyState
import com.openstream.app.core.designsystem.component.OpenStreamMovieCard
import com.openstream.app.core.designsystem.component.OpenStreamTopBar
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import com.openstream.app.domain.model.Movie

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onMovieClick: (Movie) -> Unit,
    onExploreCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OpenStreamTheme.colors.background)
    ) {
        OpenStreamTopBar(
            title = stringResource(R.string.library_title),
            eyebrow = "PERSONAL ARCHIVE"
        )

        // Luxury Tab Selector (Watchlist / Favorites)
        LibraryTabSelector(
            selectedTab = selectedTab,
            watchlistCount = watchlist.size,
            favoritesCount = favorites.size,
            onTabSelected = viewModel::selectTab,
            modifier = Modifier.padding(horizontal = OpenStreamTheme.spacing.medium, vertical = OpenStreamTheme.spacing.small)
        )

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTab) {
                LibraryTab.WATCHLIST -> {
                    if (watchlist.isEmpty()) {
                        OpenStreamEmptyState(
                            icon = Icons.Outlined.BookmarkBorder,
                            title = stringResource(R.string.library_watchlist_empty_title),
                            description = stringResource(R.string.library_watchlist_empty_desc),
                            actionText = stringResource(R.string.library_explore_button),
                            onActionClick = onExploreCatalog
                        )
                    } else {
                        MovieGrid(movies = watchlist, onMovieClick = onMovieClick)
                    }
                }
                LibraryTab.FAVORITES -> {
                    if (favorites.isEmpty()) {
                        OpenStreamEmptyState(
                            icon = Icons.Outlined.FavoriteBorder,
                            title = stringResource(R.string.library_favorites_empty_title),
                            description = stringResource(R.string.library_favorites_empty_desc),
                            actionText = stringResource(R.string.library_explore_button),
                            onActionClick = onExploreCatalog
                        )
                    } else {
                        MovieGrid(movies = favorites, onMovieClick = onMovieClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryTabSelector(
    selectedTab: LibraryTab,
    watchlistCount: Int,
    favoritesCount: Int,
    onTabSelected: (LibraryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = CircleShape

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OpenStreamTheme.colors.surface)
            .border(1.dp, OpenStreamTheme.colors.border, shape)
            .padding(4.dp)
    ) {
        LibraryTabItem(
            label = "${stringResource(R.string.library_tab_watchlist)} ($watchlistCount)",
            isSelected = selectedTab == LibraryTab.WATCHLIST,
            onClick = { onTabSelected(LibraryTab.WATCHLIST) },
            modifier = Modifier.weight(1f)
        )

        LibraryTabItem(
            label = "${stringResource(R.string.library_tab_favorites)} ($favoritesCount)",
            isSelected = selectedTab == LibraryTab.FAVORITES,
            onClick = { onTabSelected(LibraryTab.FAVORITES) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LibraryTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = CircleShape
    val background = if (isSelected) OpenStreamTheme.colors.accent else androidx.compose.ui.graphics.Color.Transparent
    val textColor = if (isSelected) OpenStreamTheme.colors.background else OpenStreamTheme.colors.textSecondary

    Box(
        modifier = modifier
            .clip(shape)
            .background(background)
            .clickable(
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = OpenStreamTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Composable
private fun MovieGrid(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 144.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = OpenStreamTheme.spacing.medium,
            end = OpenStreamTheme.spacing.medium,
            bottom = 88.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium)
    ) {
        items(movies, key = { "library_${it.id}" }) { movie ->
            OpenStreamMovieCard(
                movie = movie,
                onClick = { onMovieClick(movie) },
                cardWidth = null
            )
        }
    }
}
