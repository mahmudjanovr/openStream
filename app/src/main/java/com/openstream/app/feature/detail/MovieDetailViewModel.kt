package com.openstream.app.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.common.UiState
import com.openstream.app.domain.model.DownloadedMovie
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.repository.LibraryRepository
import com.openstream.app.domain.repository.MovieCatalogRepository
import com.openstream.app.domain.repository.MovieDownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
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
    private val libraryRepository: LibraryRepository,
    private val downloadManager: MovieDownloadManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<MovieDetailData>>(UiState.Loading)
    val uiState: StateFlow<UiState<MovieDetailData>> = _uiState.asStateFlow()

    val downloadState: StateFlow<DownloadedMovie?> = (downloadManager?.getDownload(movieId) ?: flowOf(null))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    private var isTogglingWatchlist = false
    private var isTogglingFavorite = false
    private var isManagingDownload = false

    fun toggleWatchlist(movie: Movie) {
        if (isTogglingWatchlist) return
        isTogglingWatchlist = true
        viewModelScope.launch {
            try {
                libraryRepository.toggleWatchlist(movie)
            } finally {
                isTogglingWatchlist = false
            }
        }
    }

    fun toggleFavorite(movie: Movie) {
        if (isTogglingFavorite) return
        isTogglingFavorite = true
        viewModelScope.launch {
            try {
                libraryRepository.toggleFavorite(movie)
            } finally {
                isTogglingFavorite = false
            }
        }
    }

    fun startDownload(movie: Movie) {
        if (isManagingDownload) return
        isManagingDownload = true
        viewModelScope.launch {
            try {
                downloadManager?.startDownload(movie)
            } finally {
                isManagingDownload = false
            }
        }
    }

    fun deleteDownload(movieId: String) {
        if (isManagingDownload) return
        isManagingDownload = true
        viewModelScope.launch {
            try {
                downloadManager?.deleteDownload(movieId)
            } finally {
                isManagingDownload = false
            }
        }
    }

    companion object {
        fun provideFactory(
            movieId: String,
            catalogRepository: MovieCatalogRepository,
            libraryRepository: LibraryRepository,
            downloadManager: MovieDownloadManager? = null
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MovieDetailViewModel(movieId, catalogRepository, libraryRepository, downloadManager) as T
                }
            }
    }
}
