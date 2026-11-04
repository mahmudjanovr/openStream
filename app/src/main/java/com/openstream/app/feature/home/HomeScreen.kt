package com.openstream.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.openstream.app.R
import com.openstream.app.core.common.UiState
import com.openstream.app.core.designsystem.colors.HeroOverlayGradient
import com.openstream.app.core.designsystem.component.MovieCardStyle
import com.openstream.app.core.designsystem.component.OpenStreamBadge
import com.openstream.app.core.designsystem.component.OpenStreamBadgeVariant
import com.openstream.app.core.designsystem.component.OpenStreamButton
import com.openstream.app.core.designsystem.component.OpenStreamButtonVariant
import com.openstream.app.core.designsystem.component.OpenStreamErrorState
import com.openstream.app.core.designsystem.component.OpenStreamLoadingIndicator
import com.openstream.app.core.designsystem.component.OpenStreamMovieCard
import com.openstream.app.core.designsystem.component.OpenStreamPosterPlaceholder
import com.openstream.app.core.designsystem.component.OpenStreamSectionHeader
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import com.openstream.app.domain.model.Movie

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isHeroInWatchlist by viewModel.isHeroInWatchlist.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OpenStreamTheme.colors.background)
    ) {
        when (val state = uiState) {
            is UiState.Loading -> {
                OpenStreamLoadingIndicator(message = stringResource(R.string.common_loading))
            }
            is UiState.Error -> {
                OpenStreamErrorState(
                    title = stringResource(R.string.common_error_title),
                    message = state.message,
                    onRetry = viewModel::loadHomeFeed
                )
            }
            is UiState.Success -> {
                HomeContent(
                    data = state.data,
                    isHeroInWatchlist = isHeroInWatchlist,
                    onToggleHeroWatchlist = viewModel::toggleHeroWatchlist,
                    onMovieClick = onMovieClick
                )
            }
            is UiState.Empty -> {
                OpenStreamLoadingIndicator(message = stringResource(R.string.discover_empty_title))
            }
        }
    }
}

@Composable
private fun HomeContent(
    data: HomeFeedData,
    isHeroInWatchlist: Boolean,
    onToggleHeroWatchlist: (Movie) -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Hero Cinematic Banner
        item {
            data.heroMovie?.let { hero ->
                HomeHeroBanner(
                    movie = hero,
                    isInWatchlist = isHeroInWatchlist,
                    onToggleWatchlist = { onToggleHeroWatchlist(hero) },
                    onExploreClick = { onMovieClick(hero) }
                )
            }
        }

        // Continue Watching Section with Landscape Progress Cards
        if (data.continueWatching.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = OpenStreamTheme.spacing.sectionSpacing)) {
                    OpenStreamSectionHeader(
                        title = stringResource(R.string.home_section_continue),
                        eyebrow = stringResource(R.string.home_continue_eyebrow),
                        modifier = Modifier.padding(horizontal = OpenStreamTheme.spacing.medium)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = OpenStreamTheme.spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium)
                    ) {
                        items(data.continueWatching, key = { "continue_${it.id}" }) { movie ->
                            OpenStreamMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) },
                                cardStyle = MovieCardStyle.Landscape,
                                cardWidth = 230.dp,
                                showProgress = true
                            )
                        }
                    }
                }
            }
        }

        // Trending Masterpieces Section with Rank Badges
        if (data.trendingClassics.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = OpenStreamTheme.spacing.sectionSpacing)) {
                    OpenStreamSectionHeader(
                        title = stringResource(R.string.home_section_classics),
                        eyebrow = stringResource(R.string.home_featured_tag),
                        modifier = Modifier.padding(horizontal = OpenStreamTheme.spacing.medium)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = OpenStreamTheme.spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium)
                    ) {
                        itemsIndexed(data.trendingClassics, key = { _, movie -> "trending_${movie.id}" }) { index, movie ->
                            Box {
                                OpenStreamMovieCard(
                                    movie = movie,
                                    onClick = { onMovieClick(movie) },
                                    cardStyle = MovieCardStyle.Portrait,
                                    cardWidth = 144.dp
                                )
                                // Luxury Gold Rank Badge
                                Box(
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(OpenStreamTheme.colors.surfaceElevated.copy(alpha = 0.9f))
                                        .border(1.dp, OpenStreamTheme.colors.accent, CircleShape)
                                        .align(Alignment.TopStart),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = OpenStreamTheme.typography.labelSmall,
                                        color = OpenStreamTheme.colors.accent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Curated Collections
        items(data.collections, key = { "collection_${it.id}" }) { category ->
            Column(modifier = Modifier.padding(top = OpenStreamTheme.spacing.sectionSpacing)) {
                OpenStreamSectionHeader(
                    title = category.title,
                    eyebrow = if (category.subtitle.isNotBlank()) category.subtitle else null,
                    modifier = Modifier.padding(horizontal = OpenStreamTheme.spacing.medium)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = OpenStreamTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium)
                ) {
                    items(category.movies, key = { "coll_${category.id}_${it.id}" }) { movie ->
                        OpenStreamMovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie) },
                            cardStyle = MovieCardStyle.Portrait,
                            cardWidth = 144.dp
                        )
                    }
                }
            }
        }

        // Recently Restored Masterpieces
        if (data.recentlyAdded.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = OpenStreamTheme.spacing.sectionSpacing)) {
                    OpenStreamSectionHeader(
                        title = stringResource(R.string.home_section_recent),
                        modifier = Modifier.padding(horizontal = OpenStreamTheme.spacing.medium)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = OpenStreamTheme.spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium)
                    ) {
                        items(data.recentlyAdded, key = { "recent_${it.id}" }) { movie ->
                            OpenStreamMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) },
                                cardStyle = MovieCardStyle.Portrait,
                                cardWidth = 144.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeroBanner(
    movie: Movie,
    isInWatchlist: Boolean,
    onToggleWatchlist: () -> Unit,
    onExploreClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(440.dp)
            .background(OpenStreamTheme.colors.surfaceElevated)
    ) {
        // Backdrop placeholder or image
        OpenStreamPosterPlaceholder(
            title = movie.title,
            modifier = Modifier.fillMaxSize()
        )

        // Multi-stop cinematic gradient scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HeroOverlayGradient)
        )

        // Brand header integrated on top of the hero banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = OpenStreamTheme.spacing.medium, vertical = OpenStreamTheme.spacing.small)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(OpenStreamTheme.colors.surfaceElevated.copy(alpha = 0.85f))
                    .border(1.dp, OpenStreamTheme.colors.accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = stringResource(R.string.content_desc_logo),
                    tint = OpenStreamTheme.colors.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.small))
            Text(
                text = stringResource(R.string.app_name),
                style = OpenStreamTheme.typography.displaySmall,
                color = OpenStreamTheme.colors.textPrimary
            )
        }

        // Hero narrative and action controls at the bottom of the banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = OpenStreamTheme.spacing.medium, vertical = OpenStreamTheme.spacing.medium)
        ) {
            // Badges row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OpenStreamBadge(
                    text = stringResource(R.string.home_featured_tag),
                    variant = OpenStreamBadgeVariant.Gold
                )
                OpenStreamBadge(
                    text = "${movie.year}",
                    variant = OpenStreamBadgeVariant.Subtle
                )
                OpenStreamBadge(
                    text = "${movie.durationMinutes} MIN",
                    variant = OpenStreamBadgeVariant.Subtle
                )
            }

            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))

            Text(
                text = movie.title,
                style = OpenStreamTheme.typography.displayLarge,
                color = OpenStreamTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${movie.director} • ${movie.genre} • ${movie.country}",
                style = OpenStreamTheme.typography.bodyMedium,
                color = OpenStreamTheme.colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))

            Text(
                text = movie.synopsis,
                style = OpenStreamTheme.typography.bodySmall,
                color = OpenStreamTheme.colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.medium))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.small)
            ) {
                OpenStreamButton(
                    text = stringResource(R.string.home_hero_watch),
                    onClick = onExploreClick,
                    leadingIcon = Icons.Default.Info,
                    modifier = Modifier.weight(1.2f)
                )

                OpenStreamButton(
                    text = stringResource(
                        if (isInWatchlist) R.string.home_hero_in_watchlist else R.string.home_hero_watchlist
                    ),
                    onClick = onToggleWatchlist,
                    leadingIcon = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    variant = OpenStreamButtonVariant.Outlined,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
