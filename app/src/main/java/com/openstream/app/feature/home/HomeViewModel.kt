package com.openstream.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.common.UiState
import com.openstream.app.data.local.UserPreferencesRepository
import com.openstream.app.domain.model.Genre
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.MovieCategory
import com.openstream.app.domain.recommendation.MovieRecommendationEngine
import com.openstream.app.domain.recommendation.RecommendationRails
import com.openstream.app.domain.repository.LibraryRepository
import com.openstream.app.domain.repository.MovieCatalogRepository
import com.openstream.app.domain.repository.PlaybackHistoryRepository
import com.openstream.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.Job
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
    val trendingClassics: List<Movie>,
    val recommendations: RecommendationRails? = null
)

class HomeViewModel(
    private val catalogRepository: MovieCatalogRepository,
    private val libraryRepository: LibraryRepository,
    private val userProfileRepository: UserProfileRepository? = null,
    private val userPreferencesRepository: UserPreferencesRepository? = null,
    private val playbackHistoryRepository: PlaybackHistoryRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeFeedData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeFeedData>> = _uiState.asStateFlow()

    private val _isHeroInWatchlist = MutableStateFlow(false)
    val isHeroInWatchlist: StateFlow<Boolean> = _isHeroInWatchlist.asStateFlow()

    private var heroWatchlistJob: Job? = null
    private var lastObservedHeroId: String? = null
    private var isTogglingHero = false

    init {
        loadHomeFeed()
    }

    private fun observeHeroWatchlist(heroId: String) {
        if (lastObservedHeroId == heroId && heroWatchlistJob?.isActive == true) return
        lastObservedHeroId = heroId
        heroWatchlistJob?.cancel()
        heroWatchlistJob = viewModelScope.launch {
            libraryRepository.isInWatchlist(heroId).collect { inWatchlist ->
                _isHeroInWatchlist.value = inWatchlist
            }
        }
    }

    fun loadHomeFeed() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val allMoviesFlow = catalogRepository.getMoviesByGenre(Genre.ALL)
                val heroFlow = catalogRepository.getHeroMovie()
                val continueFlow = catalogRepository.getContinueWatching()
                val collectionsFlow = catalogRepository.getFeaturedCollections()
                val recentFlow = catalogRepository.getRecentlyAdded()
                val popularFlow = catalogRepository.getPopularClassics()

                val firstGroup = combine(heroFlow, continueFlow, collectionsFlow) { hero, continueList, collections ->
                    Triple(hero, continueList, collections)
                }
                val secondGroup = combine(recentFlow, popularFlow, allMoviesFlow) { recent, popular, allMovies ->
                    Triple(recent, popular, allMovies)
                }

                combine(firstGroup, secondGroup) { (hero, continueList, collections), (recent, popular, allMovies) ->
                    val activeProfile = userProfileRepository?.getActiveProfileSync()
                        ?: com.openstream.app.domain.model.UserProfile.createDefault()
                    val watchlist = userPreferencesRepository?.watchlistMovieIds?.let {
                        emptySet<String>()
                    } ?: emptySet()
                    val history = playbackHistoryRepository?.let { emptyList<Movie>() } ?: continueList

                    val rails = MovieRecommendationEngine.generateRecommendations(
                        allMovies = allMovies,
                        activeProfile = activeProfile,
                        watchlistIds = watchlist,
                        favoriteIds = emptySet(),
                        playbackHistory = history
                    )

                    HomeFeedData(
                        heroMovie = hero,
                        continueWatching = continueList,
                        collections = collections,
                        recentlyAdded = recent,
                        trendingClassics = popular,
                        recommendations = rails
                    )
                }.collect { feedData ->
                    _uiState.value = UiState.Success(feedData)
                    feedData.heroMovie?.id?.let { observeHeroWatchlist(it) }
                }
            } catch (t: Throwable) {
                _uiState.value = UiState.Error(t.message ?: "Failed to load home feed", t)
            }
        }
    }

    fun toggleHeroWatchlist(heroMovie: Movie) {
        if (isTogglingHero) return
        isTogglingHero = true
        viewModelScope.launch {
            try {
                libraryRepository.toggleWatchlist(heroMovie)
            } finally {
                isTogglingHero = false
            }
        }
    }

    companion object {
        fun provideFactory(
            catalogRepository: MovieCatalogRepository,
            libraryRepository: LibraryRepository,
            userProfileRepository: UserProfileRepository? = null,
            userPreferencesRepository: UserPreferencesRepository? = null,
            playbackHistoryRepository: PlaybackHistoryRepository? = null
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(
                        catalogRepository,
                        libraryRepository,
                        userProfileRepository,
                        userPreferencesRepository,
                        playbackHistoryRepository
                    ) as T
                }
            }
    }
}
