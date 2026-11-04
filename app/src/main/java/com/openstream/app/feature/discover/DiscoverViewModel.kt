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
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val selectedGenre: Genre = Genre.ALL,
    val moviesState: UiState<List<Movie>> = UiState.Loading
)

class DiscoverViewModel(
    private val catalogRepository: MovieCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        selectGenre(Genre.ALL)
    }

    fun selectGenre(genre: Genre) {
        _uiState.value = _uiState.value.copy(
            selectedGenre = genre,
            moviesState = UiState.Loading
        )

        viewModelScope.launch {
            try {
                catalogRepository.getMoviesByGenre(genre).collect { movies ->
                    if (movies.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            moviesState = UiState.Empty()
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            moviesState = UiState.Success(movies)
                        )
                    }
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    moviesState = UiState.Error(t.message ?: "Failed to load catalog", t)
                )
            }
        }
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
