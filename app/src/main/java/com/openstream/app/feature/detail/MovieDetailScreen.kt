package com.openstream.app.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
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
import com.openstream.app.core.designsystem.component.OpenStreamBadge
import com.openstream.app.core.designsystem.component.OpenStreamBadgeVariant
import com.openstream.app.core.designsystem.component.OpenStreamButton
import com.openstream.app.core.designsystem.component.OpenStreamButtonVariant
import com.openstream.app.core.designsystem.component.OpenStreamErrorState
import com.openstream.app.core.designsystem.component.OpenStreamIconButton
import com.openstream.app.core.designsystem.component.OpenStreamLoadingIndicator
import com.openstream.app.core.designsystem.component.OpenStreamMovieCard
import com.openstream.app.core.designsystem.component.OpenStreamPosterPlaceholder
import com.openstream.app.core.designsystem.component.OpenStreamSectionHeader
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import com.openstream.app.domain.model.Movie

@Composable
fun MovieDetailScreen(
    viewModel: MovieDetailViewModel,
    onNavigateBack: () -> Unit,
    onRelatedMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    onRetry = viewModel::loadMovieDetail
                )
            }
            is UiState.Empty -> {
                OpenStreamErrorState(
                    title = stringResource(R.string.search_no_results_title),
                    message = stringResource(R.string.search_no_results_desc),
                    onRetry = onNavigateBack
                )
            }
            is UiState.Success -> {
                MovieDetailContent(
                    data = state.data,
                    onToggleWatchlist = { viewModel.toggleWatchlist(state.data.movie) },
                    onToggleFavorite = { viewModel.toggleFavorite(state.data.movie) },
                    onNavigateBack = onNavigateBack,
                    onRelatedMovieClick = onRelatedMovieClick
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MovieDetailContent(
    data: MovieDetailData,
    onToggleWatchlist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigateBack: () -> Unit,
    onRelatedMovieClick: (Movie) -> Unit
) {
    val movie = data.movie

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 56.dp)
        ) {
            // Backdrop Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    OpenStreamPosterPlaceholder(
                        title = movie.title,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Scrim gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(HeroOverlayGradient)
                    )
                }
            }

            // Movie Information & Badges
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = OpenStreamTheme.spacing.medium)
                ) {
                    // Badges row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OpenStreamBadge(
                            text = stringResource(R.string.detail_badge_public_domain),
                            variant = OpenStreamBadgeVariant.Gold
                        )
                        OpenStreamBadge(
                            text = stringResource(R.string.detail_badge_restored),
                            variant = OpenStreamBadgeVariant.Subtle
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
                        color = OpenStreamTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${stringResource(R.string.detail_director, movie.director)} • ${movie.genre} • ${movie.country}",
                        style = OpenStreamTheme.typography.bodyMedium,
                        color = OpenStreamTheme.colors.accent
                    )

                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))

                    // Action buttons (Watchlist & Favorite toggles)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.small)
                    ) {
                        OpenStreamButton(
                            text = stringResource(
                                if (data.isInWatchlist) R.string.detail_action_in_watchlist else R.string.detail_action_watchlist
                            ),
                            onClick = onToggleWatchlist,
                            leadingIcon = if (data.isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            variant = if (data.isInWatchlist) OpenStreamButtonVariant.Primary else OpenStreamButtonVariant.Outlined,
                            modifier = Modifier.weight(1f)
                        )

                        OpenStreamButton(
                            text = stringResource(
                                if (data.isFavorite) R.string.detail_action_in_favorite else R.string.detail_action_favorite
                            ),
                            onClick = onToggleFavorite,
                            leadingIcon = if (data.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            variant = if (data.isFavorite) OpenStreamButtonVariant.Primary else OpenStreamButtonVariant.Outlined,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))

                    // Archival Availability Notice Card
                    ArchivalAvailabilityNotice()

                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))

                    // Synopsis & Context Section
                    OpenStreamSectionHeader(title = stringResource(R.string.detail_section_synopsis))
                    Text(
                        text = movie.synopsis,
                        style = OpenStreamTheme.typography.bodyLarge,
                        color = OpenStreamTheme.colors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))

                    // Principal Cast Section
                    if (movie.leadCast.isNotEmpty()) {
                        OpenStreamSectionHeader(title = stringResource(R.string.detail_section_cast))
                        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.small),
                            verticalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.small),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            movie.leadCast.forEach { actor ->
                                CastMemberPill(name = actor)
                            }
                        }
                        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))
                    }

                    // Public Domain & Archival Status Card
                    OpenStreamSectionHeader(title = stringResource(R.string.detail_section_preservation))
                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
                    PreservationCard(
                        licenseStatus = movie.licenseStatus,
                        restorationNote = movie.restorationNote
                    )
                }
            }

            // Related Masterworks Section
            if (data.relatedMovies.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = OpenStreamTheme.spacing.sectionSpacing)) {
                        OpenStreamSectionHeader(
                            title = stringResource(R.string.detail_section_related),
                            modifier = Modifier.padding(horizontal = OpenStreamTheme.spacing.medium)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = OpenStreamTheme.spacing.medium),
                            horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium)
                        ) {
                            items(data.relatedMovies, key = { "related_${it.id}" }) { related ->
                                OpenStreamMovieCard(
                                    movie = related,
                                    onClick = { onRelatedMovieClick(related) },
                                    cardWidth = 144.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Back Button in Top-Left Corner
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(OpenStreamTheme.spacing.medium)
        ) {
            OpenStreamIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_back),
                onClick = onNavigateBack,
                containerColor = OpenStreamTheme.colors.surfaceElevated.copy(alpha = 0.85f),
                borderColor = OpenStreamTheme.colors.border,
                isCircle = true,
                tint = OpenStreamTheme.colors.textPrimary
            )
        }
    }
}

@Composable
private fun ArchivalAvailabilityNotice() {
    val shape = OpenStreamTheme.shapes.medium
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OpenStreamTheme.colors.surface)
            .border(1.dp, OpenStreamTheme.colors.accent.copy(alpha = 0.35f), shape)
            .padding(OpenStreamTheme.spacing.medium)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = OpenStreamTheme.colors.accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.small))
            Text(
                text = stringResource(R.string.detail_playback_notice),
                style = OpenStreamTheme.typography.bodySmall,
                color = OpenStreamTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun CastMemberPill(name: String) {
    val shape = CircleShape
    Row(
        modifier = Modifier
            .clip(shape)
            .background(OpenStreamTheme.colors.surfaceElevated)
            .border(1.dp, OpenStreamTheme.colors.border, shape)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = OpenStreamTheme.colors.accent,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.small))
        Text(
            text = name,
            style = OpenStreamTheme.typography.labelMedium,
            color = OpenStreamTheme.colors.textPrimary
        )
    }
}

@Composable
private fun PreservationCard(
    licenseStatus: String,
    restorationNote: String
) {
    val shape = OpenStreamTheme.shapes.medium
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OpenStreamTheme.colors.surface)
            .border(1.dp, OpenStreamTheme.colors.border, shape)
            .padding(OpenStreamTheme.spacing.medium)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = OpenStreamTheme.colors.accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.small))
            Text(
                text = licenseStatus,
                style = OpenStreamTheme.typography.labelLarge,
                color = OpenStreamTheme.colors.accent
            )
        }

        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))

        Text(
            text = restorationNote,
            style = OpenStreamTheme.typography.bodyMedium,
            color = OpenStreamTheme.colors.textSecondary
        )
    }
}
