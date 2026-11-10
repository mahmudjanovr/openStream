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
    val trendingMovies: List<Movie> = emptyList(),
    val popularMovies: List<Movie> = emptyList(),
    val nowPlayingMovies: List<Movie> = emptyList(),
    val recentlyReleased: List<Movie> = emptyList(),
    val upcomingMovies: List<Movie> = emptyList(),
    val topRatedMovies: List<Movie> = emptyList(),
    val popularTvSeries: List<Movie> = emptyList(),
    val classicCinema: List<Movie> = emptyList(),
    val continueWatching: List<Movie> = emptyList(),
    val collections: List<MovieCategory> = emptyList(),
    val trendingClassics: List<Movie> = emptyList(),
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
                val heroFlow = catalogRepository.getHeroMovie()
                val trendingFlow = catalogRepository.getTrendingMovies()
                val popularFlow = catalogRepository.getPopularMovies()
                val nowPlayingFlow = catalogRepository.getNowPlayingMovies()
                val recentReleasedFlow = catalogRepository.getRecentlyReleasedMovies()
                val upcomingFlow = catalogRepository.getUpcomingMovies()
                val topRatedFlow = catalogRepository.getTopRatedMovies()
                val tvSeriesFlow = catalogRepository.getPopularTvSeries()
                val classicsFlow = catalogRepository.getClassicCinema()
                val continueFlow = catalogRepository.getContinueWatching()
                val collectionsFlow = catalogRepository.getFeaturedCollections()
                val allMoviesFlow = catalogRepository.getMoviesByGenre(Genre.ALL)

                val groupA = combine(heroFlow, trendingFlow, popularFlow) { hero, trending, popular ->
                    Triple(hero, trending, popular)
                }
                val groupB = combine(nowPlayingFlow, recentReleasedFlow, upcomingFlow) { nowPlaying, recentReleased, upcoming ->
                    Triple(nowPlaying, recentReleased, upcoming)
                }
                val groupC = combine(topRatedFlow, tvSeriesFlow, classicsFlow) { topRated, tvSeries, classics ->
                    Triple(topRated, tvSeries, classics)
                }
                val groupD = combine(continueFlow, collectionsFlow, allMoviesFlow) { continueList, collections, allMovies ->
                    Triple(continueList, collections, allMovies)
                }

                combine(groupA, groupB, groupC, groupD) { (hero, trending, popular),
                                                          (nowPlaying, recentReleased, upcoming),
                                                          (topRated, tvSeries, classics),
                                                          (continueList, collections, allMovies) ->

                    val activeProfile = userProfileRepository?.getActiveProfileSync()
                        ?: com.openstream.app.domain.model.UserProfile.createDefault()
                    val watchlist = userPreferencesRepository?.watchlistMovieIds?.let { emptySet<String>() } ?: emptySet()
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
                        trendingMovies = trending,
                        popularMovies = popular,
                        nowPlayingMovies = nowPlaying,
                        recentlyReleased = recentReleased,
                        upcomingMovies = upcoming,
                        topRatedMovies = topRated,
                        popularTvSeries = tvSeries,
                        classicCinema = classics,
                        continueWatching = continueList,
                        collections = collections,
                        trendingClassics = classics.take(6),
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
