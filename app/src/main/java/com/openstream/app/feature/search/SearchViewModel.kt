package com.openstream.app.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.common.UiState
import com.openstream.app.domain.model.Genre
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.repository.MovieCatalogRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val catalogRepository: MovieCatalogRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchState = MutableStateFlow<UiState<List<Movie>>>(UiState.Empty())
    val searchState: StateFlow<UiState<List<Movie>>> = _searchState.asStateFlow()

    val recentSearches: StateFlow<List<String>> = catalogRepository.getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()

    private val _selectedMediaType = MutableStateFlow<String?>("all")
    val selectedMediaType: StateFlow<String?> = _selectedMediaType.asStateFlow()

    private val _selectedEra = MutableStateFlow<String?>("all")
    val selectedEra: StateFlow<String?> = _selectedEra.asStateFlow()

    private var allCatalogTitles = mutableListOf<String>()

    init {
        viewModelScope.launch {
            val movies = catalogRepository.getMoviesByGenre(Genre.ALL).firstOrNull() ?: emptyList()
            allCatalogTitles.addAll(movies.map { it.title })
            allCatalogTitles.addAll(movies.map { it.director }.distinct())
            allCatalogTitles.addAll(listOf("Silent Era", "Film Noir", "Sci-Fi & Horror", "Comedy", "Drama", "Action", "TV Series"))
        }

        viewModelScope.launch {
            _query
                .debounce(300)
                .collect { q ->
                    updateSuggestions(q)
                    executeSearch(q)
                }
        }
    }

    private fun updateSuggestions(q: String) {
        val trimmed = q.trim()
        if (trimmed.length < 2) {
            _searchSuggestions.value = emptyList()
            return
        }
        val matches = allCatalogTitles
            .filter { it.contains(trimmed, ignoreCase = true) && !it.equals(trimmed, ignoreCase = true) }
            .distinct()
            .take(5)
        _searchSuggestions.value = matches
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun setMediaTypeFilter(type: String?) {
        _selectedMediaType.value = type
        executeSearch(_query.value)
    }

    fun setEraFilter(era: String?) {
        _selectedEra.value = era
        executeSearch(_query.value)
    }

    fun clearQuery() {
        _query.value = ""
        _searchSuggestions.value = emptyList()
        _searchState.value = UiState.Empty()
    }

    fun onRecentSearchSelected(search: String) {
        _query.value = search
        submitSearch(search)
    }

    fun submitSearch(search: String) {
        val trimmed = search.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                catalogRepository.addRecentSearch(trimmed)
            }
            executeSearch(trimmed)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            catalogRepository.clearRecentSearches()
        }
    }

    private var searchJob: Job? = null

    private fun executeSearch(q: String) {
        val trimmed = q.trim()
        searchJob?.cancel()

        if (trimmed.isBlank()) {
            _searchState.value = UiState.Empty()
            return
        }

        val mediaType = when (_selectedMediaType.value) {
            "movie" -> "movie"
            "tv" -> "tv"
            else -> null
        }

        searchJob = viewModelScope.launch {
            _searchState.value = UiState.Loading
            try {
                catalogRepository.searchWithFilters(
                    query = trimmed,
                    mediaType = mediaType
                ).collect { rawResults ->
                    val era = _selectedEra.value
                    val filtered = when (era) {
                        "2024-2026" -> rawResults.filter { it.year in 2024..2026 }
                        "2020-2023" -> rawResults.filter { it.year in 2020..2023 }
                        "classic" -> rawResults.filter { it.year < 1980 }
                        else -> rawResults
                    }
                    if (filtered.isEmpty()) {
                        _searchState.value = UiState.Empty("No results matching filters")
                    } else {
                        _searchState.value = UiState.Success(filtered)
                    }
                }
            } catch (t: Throwable) {
                _searchState.value = UiState.Error(t.message ?: "Search failed", t)
            }
        }
    }

    companion object {
        fun provideFactory(catalogRepository: MovieCatalogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SearchViewModel(catalogRepository) as T
                }
            }
    }
}
