package com.openstream.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.common.UiState
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.MovieCategory
import com.openstream.app.domain.repository.LibraryRepository
import com.openstream.app.domain.repository.MovieCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeFeedData(
    val heroMovie: Movie?,
    val continueWatching: List<Movie>,
    val collections: List<MovieCategory>,
    val recentlyAdded: List<Movie>,
    val trendingClassics: List<Movie>
)

class HomeViewModel(
    private val catalogRepository: MovieCatalogRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeFeedData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeFeedData>> = _uiState.asStateFlow()

    private val _isHeroInWatchlist = MutableStateFlow(false)
    val isHeroInWatchlist: StateFlow<Boolean> = _isHeroInWatchlist.asStateFlow()

    init {
        loadHomeFeed()
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                combine(
                    catalogRepository.getHeroMovie(),
                    catalogRepository.getContinueWatching(),
                    catalogRepository.getFeaturedCollections(),
                    catalogRepository.getRecentlyAdded(),
                    catalogRepository.getPopularClassics()
                ) { hero, continueList, collections, recent, popular ->
                    HomeFeedData(
                        heroMovie = hero,
                        continueWatching = continueList,
                        collections = collections,
                        recentlyAdded = recent,
                        trendingClassics = popular
                    )
                }.collect { feedData ->
                    _uiState.value = UiState.Success(feedData)
                    feedData.heroMovie?.let { hero ->
                        launch {
                            libraryRepository.isInWatchlist(hero.id).collect { inWatchlist ->
                                _isHeroInWatchlist.value = inWatchlist
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                _uiState.value = UiState.Error(t.message ?: "Failed to load home feed", t)
            }
        }
    }

    fun toggleHeroWatchlist(movie: Movie) {
        viewModelScope.launch {
            libraryRepository.toggleWatchlist(movie)
        }
    }

    companion object {
        fun provideFactory(
            catalogRepository: MovieCatalogRepository,
            libraryRepository: LibraryRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(catalogRepository, libraryRepository) as T
            }
        }
    }
}
