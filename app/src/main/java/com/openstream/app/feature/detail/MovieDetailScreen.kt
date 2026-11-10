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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
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
    onWatchClick: (Movie) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()

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
                    downloadItem = downloadState,
                    onWatchClick = { onWatchClick(state.data.movie) },
                    onToggleWatchlist = { viewModel.toggleWatchlist(state.data.movie) },
                    onToggleFavorite = { viewModel.toggleFavorite(state.data.movie) },
                    onDownloadClick = { viewModel.startDownload(state.data.movie) },
                    onDeleteDownload = { viewModel.deleteDownload(state.data.movie.id) },
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
    downloadItem: com.openstream.app.domain.model.DownloadedMovie?,
    onWatchClick: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteDownload: () -> Unit,
    onNavigateBack: () -> Unit,
    onRelatedMovieClick: (Movie) -> Unit
) {
    val movie = data.movie
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 56.dp)
        ) {
            // Cinematic Backdrop & Overlapping Poster Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    val backdropImage = movie.backdropUrl.ifBlank { movie.posterUrl }
                    if (backdropImage.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(backdropImage)
                                .crossfade(250)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            loading = {
                                OpenStreamPosterPlaceholder(
                                    title = movie.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                            },
                            error = {
                                OpenStreamPosterPlaceholder(
                                    title = movie.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        OpenStreamPosterPlaceholder(
                            title = movie.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Scrim gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(HeroOverlayGradient)
                    )

                    // Overlapping luxury poster badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = OpenStreamTheme.spacing.medium, bottom = 16.dp)
                            .width(100.dp)
                            .aspectRatio(2f / 3f)
                            .shadow(8.dp, OpenStreamTheme.shapes.medium)
                            .clip(OpenStreamTheme.shapes.medium)
                            .background(OpenStreamTheme.colors.surfaceElevated)
                            .border(1.dp, OpenStreamTheme.colors.accent.copy(alpha = 0.5f), OpenStreamTheme.shapes.medium)
                    ) {
                        val posterImage = movie.posterUrl.ifBlank { movie.backdropUrl }
                        if (posterImage.isNotBlank()) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(posterImage)
                                    .crossfade(250)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = movie.title,
                                contentScale = ContentScale.Crop,
                                loading = {
                                    OpenStreamPosterPlaceholder(
                                        title = movie.title,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                },
                                error = {
                                    OpenStreamPosterPlaceholder(
                                        title = movie.title,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            OpenStreamPosterPlaceholder(
                                title = movie.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
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
                        if (movie.voteAverage != null && movie.voteAverage > 0) {
                            OpenStreamBadge(
                                text = "★ ${String.format(java.util.Locale.US, "%.1f", movie.voteAverage)}",
                                variant = OpenStreamBadgeVariant.Gold
                            )
                        } else if (movie.isPublicDomain) {
                            OpenStreamBadge(
                                text = stringResource(R.string.detail_badge_public_domain),
                                variant = OpenStreamBadgeVariant.Gold
                            )
                        }
                        if (movie.mediaType == "tv") {
                            OpenStreamBadge(
                                text = "TV SERIES",
                                variant = OpenStreamBadgeVariant.Subtle
                            )
                        }
                        OpenStreamBadge(
                            text = "${movie.year}",
                            variant = OpenStreamBadgeVariant.Subtle
                        )
                        if (movie.durationMinutes > 0) {
                            OpenStreamBadge(
                                text = "${movie.durationMinutes} MIN",
                                variant = OpenStreamBadgeVariant.Subtle
                            )
                        }
                        OpenStreamBadge(
                            text = movie.movieSource.displayName,
                            variant = OpenStreamBadgeVariant.Subtle
                        )
                    }

                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))

                    Text(
                        text = movie.title,
                        style = OpenStreamTheme.typography.displayLarge,
                        color = OpenStreamTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${stringResource(R.string.detail_director, movie.director)} • ${movie.genre} • ${movie.country}",
                        style = OpenStreamTheme.typography.bodyMedium,
                        color = OpenStreamTheme.colors.accent
                    )

                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.medium))

                    // Primary Action: WATCH NOW (for directly playable stream)
                    if (movie.isPlayableDirectly) {
                        OpenStreamButton(
                            text = stringResource(R.string.detail_action_watch_now),
                            onClick = onWatchClick,
                            leadingIcon = Icons.Default.PlayArrow,
                            variant = OpenStreamButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
                    }

                    // Official Trailer Action
                    val trailerKey = movie.trailerYoutubeKey ?: data.details?.trailerYoutubeKey
                    val trailerUrl = data.details?.trailerUrl ?: trailerKey?.let { "https://www.youtube.com/watch?v=$it" }
                    if (!trailerUrl.isNullOrBlank()) {
                        OpenStreamButton(
                            text = "Official Trailer",
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            leadingIcon = Icons.Default.PlayCircleOutline,
                            variant = if (movie.isPlayableDirectly) OpenStreamButtonVariant.Outlined else OpenStreamButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
                    }

                    // Secondary Actions: Watchlist & Favorite Toggles
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

                    // Offline Download Action (if eligible)
                    if (com.openstream.app.domain.model.DownloadedMovie.isDownloadEligible(movie)) {
                        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
                        val isDownloaded = downloadItem?.isCompleted == true
                        val isDownloading = downloadItem?.isDownloading == true

                        val buttonText = when {
                            isDownloaded -> stringResource(R.string.download_status_completed)
                            isDownloading -> "${stringResource(R.string.download_status_downloading)} (${((downloadItem?.progressPercentage ?: 0f) * 100).toInt()}%)"
                            else -> stringResource(R.string.download_button)
                        }

                        val buttonIcon = when {
                            isDownloaded -> Icons.Default.CheckCircle
                            isDownloading -> Icons.Default.Download
                            else -> Icons.Default.Download
                        }

                        OpenStreamButton(
                            text = buttonText,
                            onClick = {
                                if (isDownloaded) {
                                    onDeleteDownload()
                                } else if (!isDownloading) {
                                    onDownloadClick()
                                }
                            },
                            leadingIcon = buttonIcon,
                            variant = if (isDownloaded) OpenStreamButtonVariant.Pill else OpenStreamButtonVariant.Outlined,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Where to Stream (Commercial Availability)
                    val providers = (data.details?.watchProviders ?: emptyList())
                        .ifEmpty { movie.watchProviders }
                    if (providers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))
                        OpenStreamSectionHeader(title = "Where to Stream")
                        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            providers.forEach { provider ->
                                Box(
                                    modifier = Modifier
                                        .clip(OpenStreamTheme.shapes.small)
                                        .background(OpenStreamTheme.colors.surfaceElevated)
                                        .border(1.dp, OpenStreamTheme.colors.border, OpenStreamTheme.shapes.small)
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = provider.providerName,
                                        style = OpenStreamTheme.typography.labelMedium,
                                        color = OpenStreamTheme.colors.accent
                                    )
                                }
                            }
                        }
                    }

                    // TV Series Episodes Guide
                    val seasons = data.details?.seasons ?: emptyList()
                    if (seasons.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))
                        OpenStreamSectionHeader(title = "Episodes Guide")
                        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
                        seasons.forEach { season ->
                            Text(
                                text = "Season ${season.seasonNumber}",
                                style = OpenStreamTheme.typography.titleMedium,
                                color = OpenStreamTheme.colors.textPrimary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            season.episodes.take(5).forEach { ep ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "E${ep.episodeNumber}: ${ep.title}",
                                        style = OpenStreamTheme.typography.bodyMedium,
                                        color = OpenStreamTheme.colors.textSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (ep.durationMinutes != null) {
                                        Text(
                                            text = "${ep.durationMinutes}m",
                                            style = OpenStreamTheme.typography.bodySmall,
                                            color = OpenStreamTheme.colors.accent
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))

                    // Archival / Playback Notice Card
                    if (movie.isPublicDomain) {
                        ArchivalAvailabilityNotice()
                        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))
                    }

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

                    // Public Domain & Archival Status Card / Metadata Source Card
                    OpenStreamSectionHeader(
                        title = if (movie.isPublicDomain) stringResource(R.string.detail_section_preservation) else "Metadata & Source"
                    )
                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
                    PreservationCard(
                        licenseStatus = movie.licenseStatus,
                        restorationNote = movie.restorationNote.ifBlank { "Verified metadata from ${movie.movieSource.displayName} entertainment database" },
                        sourceName = movie.movieSource.displayName
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
    restorationNote: String,
    sourceName: String = "Internet Archive"
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
                text = "$licenseStatus • $sourceName",
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
