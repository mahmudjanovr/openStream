package com.openstream.app.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.common.UiState
import com.openstream.app.domain.model.Genre
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.repository.MovieCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val selectedGenre: Genre = Genre.ALL,
    val selectedEra: EraFilter = EraFilter.ALL,
    val selectedRuntime: RuntimeFilter = RuntimeFilter.ALL,
    val sortOrder: SortOrder = SortOrder.YEAR_DESC,
    val moviesState: UiState<List<Movie>> = UiState.Loading
)

class DiscoverViewModel(
    private val catalogRepository: MovieCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private var allCatalogMovies: List<Movie> = emptyList()

    init {
        loadInitialCatalog()
    }

    private fun loadInitialCatalog() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(moviesState = UiState.Loading)
            try {
                val movies = catalogRepository.getMoviesByGenre(Genre.ALL).firstOrNull() ?: emptyList()
                allCatalogMovies = movies
                applyFilters()
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    moviesState = UiState.Error(t.message ?: "Failed to load catalog", t)
                )
            }
        }
    }

    fun selectGenre(genre: Genre) {
        _uiState.value = _uiState.value.copy(selectedGenre = genre)
        applyFilters()
    }

    fun selectEra(era: EraFilter) {
        _uiState.value = _uiState.value.copy(selectedEra = era)
        applyFilters()
    }

    fun selectRuntime(runtime: RuntimeFilter) {
        _uiState.value = _uiState.value.copy(selectedRuntime = runtime)
        applyFilters()
    }

    fun selectSortOrder(order: SortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = order)
        applyFilters()
    }

    fun resetFilters() {
        _uiState.value = _uiState.value.copy(
            selectedGenre = Genre.ALL,
            selectedEra = EraFilter.ALL,
            selectedRuntime = RuntimeFilter.ALL,
            sortOrder = SortOrder.YEAR_DESC
        )
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val filtered = allCatalogMovies.filter { movie ->
            // Genre filter
            val matchesGenre = when (currentState.selectedGenre) {
                Genre.ALL -> true
                Genre.SILENT -> movie.genre == "Silent Era" || movie.year <= 1927
                Genre.NOIR -> movie.genre.contains("Noir", ignoreCase = true)
                Genre.SCIFI_HORROR -> movie.genre.contains("Sci-Fi", ignoreCase = true) || movie.genre.contains("Horror", ignoreCase = true)
                Genre.COMEDY -> movie.genre.contains("Comedy", ignoreCase = true)
                Genre.DRAMA -> movie.genre.contains("Drama", ignoreCase = true)
            }

            // Era filter
            val matchesEra = currentState.selectedEra.predicate(movie.year)

            // Runtime filter
            val matchesRuntime = currentState.selectedRuntime.predicate(movie.durationMinutes)

            matchesGenre && matchesEra && matchesRuntime
        }

        // Apply sorting
        val sorted = when (currentState.sortOrder) {
            SortOrder.YEAR_DESC -> filtered.sortedByDescending { it.year }
            SortOrder.YEAR_ASC -> filtered.sortedBy { it.year }
            SortOrder.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortOrder.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortOrder.RUNTIME_ASC -> filtered.sortedBy { it.durationMinutes }
            SortOrder.RUNTIME_DESC -> filtered.sortedByDescending { it.durationMinutes }
        }

        _uiState.value = currentState.copy(
            moviesState = if (sorted.isEmpty()) UiState.Empty() else UiState.Success(sorted)
        )
    }

    companion object {
        fun provideFactory(catalogRepository: MovieCatalogRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DiscoverViewModel(catalogRepository) as T
                }
            }
    }
}
