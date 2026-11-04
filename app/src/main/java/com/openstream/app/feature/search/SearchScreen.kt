package com.openstream.app.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.openstream.app.R
import com.openstream.app.core.common.UiState
import com.openstream.app.core.designsystem.component.OpenStreamEmptyState
import com.openstream.app.core.designsystem.component.OpenStreamErrorState
import com.openstream.app.core.designsystem.component.OpenStreamIconButton
import com.openstream.app.core.designsystem.component.OpenStreamLoadingIndicator
import com.openstream.app.core.designsystem.component.OpenStreamMovieCard
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import com.openstream.app.domain.model.Movie

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OpenStreamTheme.colors.background)
            .statusBarsPadding()
    ) {
        // Search Input Bar
        SearchBarInput(
            query = query,
            onQueryChange = viewModel::onQueryChanged,
            onClear = viewModel::clearQuery,
            onSearchSubmit = {
                viewModel.submitSearch(query)
                focusManager.clearFocus()
            }
        )

        Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.medium))

        // Main Body: Recent searches or Search results
        if (query.isBlank()) {
            RecentSearchesSection(
                recentSearches = recentSearches,
                onSelect = {
                    viewModel.onRecentSearchSelected(it)
                    focusManager.clearFocus()
                },
                onClearAll = viewModel::clearRecentSearches
            )
        } else {
            SearchResultsSection(
                searchState = searchState,
                onMovieClick = onMovieClick,
                onRetry = { viewModel.submitSearch(query) }
            )
        }
    }
}

@Composable
private fun SearchBarInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearchSubmit: () -> Unit
) {
    val shape = OpenStreamTheme.shapes.medium

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = OpenStreamTheme.spacing.medium,
                vertical = OpenStreamTheme.spacing.small
            )
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .border(1.dp, OpenStreamTheme.colors.border, shape),
            placeholder = {
                Text(
                    text = stringResource(R.string.search_hint),
                    style = OpenStreamTheme.typography.bodyMedium,
                    color = OpenStreamTheme.colors.textSecondary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = OpenStreamTheme.colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    OpenStreamIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = stringResource(R.string.content_desc_clear_search),
                        onClick = onClear,
                        iconSize = 18.dp
                    )
                }
            },
            singleLine = true,
            textStyle = OpenStreamTheme.typography.bodyMedium.copy(color = OpenStreamTheme.colors.textPrimary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = OpenStreamTheme.colors.surfaceElevated,
                unfocusedContainerColor = OpenStreamTheme.colors.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = OpenStreamTheme.colors.accent
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentSearchesSection(
    recentSearches: List<String>,
    onSelect: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OpenStreamTheme.spacing.medium)
    ) {
        if (recentSearches.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.search_recent_title),
                    style = OpenStreamTheme.typography.titleMedium,
                    color = OpenStreamTheme.colors.textPrimary
                )
                TextButton(onClick = onClearAll) {
                    Text(
                        text = stringResource(R.string.search_clear),
                        style = OpenStreamTheme.typography.labelMedium,
                        color = OpenStreamTheme.colors.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                recentSearches.forEach { search ->
                    val shape = OpenStreamTheme.shapes.small
                    Row(
                        modifier = Modifier
                            .clip(shape)
                            .background(OpenStreamTheme.colors.surfaceElevated)
                            .border(1.dp, OpenStreamTheme.colors.border, shape)
                            .clickable { onSelect(search) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = OpenStreamTheme.colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(OpenStreamTheme.spacing.small))
                        Text(
                            text = search,
                            style = OpenStreamTheme.typography.bodyMedium,
                            color = OpenStreamTheme.colors.textPrimary
                        )
                    }
                }
            }
        } else {
            OpenStreamEmptyState(
                icon = Icons.Default.Search,
                title = stringResource(R.string.search_recent_title),
                description = stringResource(R.string.search_empty_prompt)
            )
        }
    }
}

@Composable
private fun SearchResultsSection(
    searchState: UiState<List<Movie>>,
    onMovieClick: (Movie) -> Unit,
    onRetry: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (searchState) {
            is UiState.Loading -> {
                OpenStreamLoadingIndicator(message = stringResource(R.string.common_loading))
            }
            is UiState.Error -> {
                OpenStreamErrorState(
                    title = stringResource(R.string.common_error_title),
                    message = searchState.message,
                    onRetry = onRetry
                )
            }
            is UiState.Empty -> {
                OpenStreamEmptyState(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.search_no_results_title),
                    description = stringResource(R.string.search_no_results_desc)
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
                    items(searchState.data, key = { "search_${it.id}" }) { movie ->
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
