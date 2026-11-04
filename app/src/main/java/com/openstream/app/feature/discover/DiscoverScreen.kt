package com.openstream.app.feature.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
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
import com.openstream.app.core.common.UiState
import com.openstream.app.core.designsystem.component.OpenStreamEmptyState
import com.openstream.app.core.designsystem.component.OpenStreamErrorState
import com.openstream.app.core.designsystem.component.OpenStreamLoadingIndicator
import com.openstream.app.core.designsystem.component.OpenStreamMovieCard
import com.openstream.app.core.designsystem.component.OpenStreamTopBar
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import com.openstream.app.domain.model.Genre
import com.openstream.app.domain.model.Movie

private val genreFilters = listOf(
    Genre.ALL to R.string.genre_all,
    Genre.SILENT to R.string.genre_silent,
    Genre.NOIR to R.string.genre_noir,
    Genre.SCIFI_HORROR to R.string.genre_scifi,
    Genre.COMEDY to R.string.genre_comedy,
    Genre.DRAMA to R.string.genre_drama
)

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OpenStreamTheme.colors.background)
    ) {
        OpenStreamTopBar(title = stringResource(R.string.discover_title))

        // Genre Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = OpenStreamTheme.spacing.medium),
            contentPadding = PaddingValues(horizontal = OpenStreamTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.small)
        ) {
            items(genreFilters, key = { it.first.name }) { (genre, labelRes) ->
                val isSelected = uiState.selectedGenre == genre
                val shape = OpenStreamTheme.shapes.small
                val background = if (isSelected) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.surfaceElevated
                val textColor = if (isSelected) OpenStreamTheme.colors.background else OpenStreamTheme.colors.textPrimary
                val borderColor = if (isSelected) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.border

                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(background)
                        .border(1.dp, borderColor, shape)
                        .clickable(
                            role = Role.Button,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = true),
                            onClick = { viewModel.selectGenre(genre) }
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(labelRes),
                        style = OpenStreamTheme.typography.labelMedium,
                        color = textColor
                    )
                }
            }
        }

        // Movies Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val state = uiState.moviesState) {
                is UiState.Loading -> {
                    OpenStreamLoadingIndicator(
                        message = stringResource(R.string.common_loading)
                    )
                }
                is UiState.Error -> {
                    OpenStreamErrorState(
                        title = stringResource(R.string.common_error_title),
                        message = state.message,
                        onRetry = { viewModel.selectGenre(uiState.selectedGenre) }
                    )
                }
                is UiState.Empty -> {
                    OpenStreamEmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = stringResource(R.string.discover_empty_title),
                        description = stringResource(R.string.discover_empty_desc)
                    )
                }
                is UiState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = OpenStreamTheme.spacing.medium,
                            end = OpenStreamTheme.spacing.medium,
                            bottom = 80.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium)
                    ) {
                        items(state.data, key = { "discover_${it.id}" }) { movie ->
                            OpenStreamMovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) },
                                cardWidth = null
                            )
                        }
                    }
                }
            }
        }
    }
}
