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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.openstream.app.R
import com.openstream.app.core.designsystem.component.OpenStreamBadge
import com.openstream.app.core.designsystem.component.OpenStreamBadgeVariant
import com.openstream.app.core.designsystem.component.OpenStreamEmptyState
import com.openstream.app.core.designsystem.component.OpenStreamMovieCard
import com.openstream.app.core.designsystem.component.OpenStreamPosterPlaceholder
import com.openstream.app.core.designsystem.component.OpenStreamTopBar
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import com.openstream.app.domain.model.DownloadStatus
import com.openstream.app.domain.model.DownloadedMovie
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
    val downloads by viewModel.downloads.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OpenStreamTheme.colors.background)
    ) {
        OpenStreamTopBar(
            title = stringResource(R.string.library_title),
            eyebrow = "PERSONAL ARCHIVE"
        )

        // Tab Selector (Watchlist / Favorites / Downloads)
        LibraryTabSelector(
            selectedTab = selectedTab,
            watchlistCount = watchlist.size,
            favoritesCount = favorites.size,
            downloadsCount = downloads.size,
            onTabSelected = viewModel::selectTab,
            modifier = Modifier.padding(
                horizontal = OpenStreamTheme.spacing.medium,
                vertical = OpenStreamTheme.spacing.small
            )
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
                LibraryTab.DOWNLOADS -> {
                    if (downloads.isEmpty()) {
                        OpenStreamEmptyState(
                            icon = Icons.Outlined.FileDownload,
                            title = stringResource(R.string.library_downloads_empty_title),
                            description = stringResource(R.string.library_downloads_empty_desc),
                            actionText = stringResource(R.string.library_explore_button),
                            onActionClick = onExploreCatalog
                        )
                    } else {
                        DownloadsList(
                            downloads = downloads,
                            onMovieClick = onMovieClick,
                            onPause = viewModel::pauseDownload,
                            onResume = viewModel::resumeDownload,
                            onDelete = viewModel::deleteDownload
                        )
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
    downloadsCount: Int,
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

        LibraryTabItem(
            label = "${stringResource(R.string.library_tab_downloads)} ($downloadsCount)",
            isSelected = selectedTab == LibraryTab.DOWNLOADS,
            onClick = { onTabSelected(LibraryTab.DOWNLOADS) },
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
    val background = if (isSelected) OpenStreamTheme.colors.accent else Color.Transparent
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
            style = OpenStreamTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MovieGrid(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(OpenStreamTheme.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.large),
        modifier = Modifier.fillMaxSize()
    ) {
        items(movies, key = { it.id }) { movie ->
            OpenStreamMovieCard(
                movie = movie,
                onClick = { onMovieClick(movie) }
            )
        }
    }
}

@Composable
private fun DownloadsList(
    downloads: List<DownloadedMovie>,
    onMovieClick: (Movie) -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(OpenStreamTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium),
        modifier = Modifier.fillMaxSize()
    ) {
        items(downloads, key = { it.movie.id }) { item ->
            DownloadedMovieRow(
                item = item,
                onClick = { onMovieClick(item.movie) },
                onPause = { onPause(item.movie.id) },
                onResume = { onResume(item.movie.id) },
                onDelete = { onDelete(item.movie.id) }
            )
        }
    }
}

@Composable
private fun DownloadedMovieRow(
    item: DownloadedMovie,
    onClick: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OpenStreamTheme.colors.surface)
            .border(1.dp, OpenStreamTheme.colors.border, shape)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with fallback placeholder
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.movie.posterUrl)
                .crossfade(200)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = item.movie.title,
            contentScale = ContentScale.Crop,
            loading = {
                OpenStreamPosterPlaceholder(
                    title = item.movie.title,
                    modifier = Modifier.fillMaxSize()
                )
            },
            error = {
                OpenStreamPosterPlaceholder(
                    title = item.movie.title,
                    modifier = Modifier.fillMaxSize()
                )
            },
            modifier = Modifier
                .size(width = 60.dp, height = 85.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Info & Progress
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.movie.title,
                style = OpenStreamTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = OpenStreamTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${item.movie.year} • ${item.movie.genre}",
                style = OpenStreamTheme.typography.bodySmall,
                color = OpenStreamTheme.colors.textSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            when (item.status) {
                DownloadStatus.COMPLETED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OpenStreamBadge(
                            text = stringResource(R.string.download_status_completed),
                            variant = OpenStreamBadgeVariant.Gold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.formattedTotalSize,
                            style = OpenStreamTheme.typography.labelSmall,
                            color = OpenStreamTheme.colors.textSecondary
                        )
                    }
                }
                DownloadStatus.DOWNLOADING -> {
                    Column {
                        LinearProgressIndicator(
                            progress = { item.progressPercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = OpenStreamTheme.colors.accent,
                            trackColor = OpenStreamTheme.colors.border
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(item.progressPercentage * 100).toInt()}%",
                                style = OpenStreamTheme.typography.labelSmall,
                                color = OpenStreamTheme.colors.accent
                            )
                            Text(
                                text = "${item.formattedDownloadedSize} / ${item.formattedTotalSize}",
                                style = OpenStreamTheme.typography.labelSmall,
                                color = OpenStreamTheme.colors.textSecondary
                            )
                        }
                    }
                }
                DownloadStatus.PAUSED -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OpenStreamBadge(
                            text = stringResource(R.string.download_status_paused),
                            variant = OpenStreamBadgeVariant.Subtle
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.formattedDownloadedSize,
                            style = OpenStreamTheme.typography.labelSmall,
                            color = OpenStreamTheme.colors.textSecondary
                        )
                    }
                }
                DownloadStatus.FAILED -> {
                    OpenStreamBadge(
                        text = stringResource(R.string.download_status_failed),
                        variant = OpenStreamBadgeVariant.Subtle
                    )
                }
                else -> {}
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Action controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            when (item.status) {
                DownloadStatus.COMPLETED -> {
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.detail_action_watch_now),
                            tint = OpenStreamTheme.colors.accent
                        )
                    }
                }
                DownloadStatus.DOWNLOADING -> {
                    IconButton(
                        onClick = onPause,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = stringResource(R.string.action_pause),
                            tint = OpenStreamTheme.colors.textPrimary
                        )
                    }
                }
                DownloadStatus.PAUSED, DownloadStatus.FAILED -> {
                    IconButton(
                        onClick = onResume,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.action_resume),
                            tint = OpenStreamTheme.colors.accent
                        )
                    }
                }
                else -> {}
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete_download),
                    tint = OpenStreamTheme.colors.textSecondary
                )
            }
        }
    }
}
