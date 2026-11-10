package com.openstream.app.feature.home

import com.openstream.app.core.common.UiState
import com.openstream.app.data.repository.LibraryRepositoryImpl
import com.openstream.app.data.repository.MovieCatalogRepositoryImpl
import com.openstream.app.fake.FakeArchiveApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var catalogRepo: MovieCatalogRepositoryImpl
    private lateinit var libraryRepo: LibraryRepositoryImpl
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        catalogRepo = MovieCatalogRepositoryImpl(apiService = FakeArchiveApiService(), ioDispatcher = testDispatcher)
        libraryRepo = LibraryRepositoryImpl()
        viewModel = HomeViewModel(catalogRepo, libraryRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadHomeFeed_emitsSuccessWithData() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected UiState.Success but got $state", state is UiState.Success)
        val data = (state as UiState.Success).data
        assertNotNull(data.heroMovie)
        assertTrue(data.trendingMovies.isNotEmpty())
        assertTrue(data.popularMovies.isNotEmpty())
        assertTrue(data.nowPlayingMovies.isNotEmpty())
        assertTrue(data.upcomingMovies.isNotEmpty())
        assertTrue(data.topRatedMovies.isNotEmpty())
        assertTrue(data.tvSeries.isNotEmpty())
        assertTrue(data.classicCinema.isNotEmpty())
        assertTrue(data.continueWatching.isNotEmpty())
        assertTrue(data.collections.isNotEmpty())
    }

    @Test
    fun toggleHeroWatchlist_updatesState() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        val hero = (viewModel.uiState.value as UiState.Success).data.heroMovie
        assertNotNull(hero)

        viewModel.toggleHeroWatchlist(hero!!)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.isHeroInWatchlist.value)
    }
}
