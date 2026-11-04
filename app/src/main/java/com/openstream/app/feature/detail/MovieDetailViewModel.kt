package com.openstream.app.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.common.UiState
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.repository.LibraryRepository
import com.openstream.app.domain.repository.MovieCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MovieDetailData(
    val movie: Movie,
    val relatedMovies: List<Movie>,
    val isInWatchlist: Boolean,
    val isFavorite: Boolean
)

class MovieDetailViewModel(
    private val movieId: String,
    private val catalogRepository: MovieCatalogRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<MovieDetailData>>(UiState.Loading)
    val uiState: StateFlow<UiState<MovieDetailData>> = _uiState.asStateFlow()

    init {
        loadMovieDetail()
    }

    fun loadMovieDetail() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                combine(
                    catalogRepository.getMovieById(movieId),
                    catalogRepository.getRelatedMovies(movieId),
                    libraryRepository.isInWatchlist(movieId),
                    libraryRepository.isFavorite(movieId)
                ) { movie, related, inWatchlist, isFav ->
                    if (movie != null) {
                        UiState.Success(
                            MovieDetailData(
                                movie = movie.copy(isInWatchlist = inWatchlist, isFavorite = isFav),
                                relatedMovies = related,
                                isInWatchlist = inWatchlist,
                                isFavorite = isFav
                            )
                        )
                    } else {
                        UiState.Empty("Movie not found")
                    }
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (t: Throwable) {
                _uiState.value = UiState.Error(t.message ?: "Failed to load movie details", t)
            }
        }
    }

    fun toggleWatchlist(movie: Movie) {
        viewModelScope.launch {
            libraryRepository.toggleWatchlist(movie)
        }
    }

    fun toggleFavorite(movie: Movie) {
        viewModelScope.launch {
            libraryRepository.toggleFavorite(movie)
        }
    }

    companion object {
        fun provideFactory(
            movieId: String,
            catalogRepository: MovieCatalogRepository,
            libraryRepository: LibraryRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MovieDetailViewModel(movieId, catalogRepository, libraryRepository) as T
            }
        }
    }
}
