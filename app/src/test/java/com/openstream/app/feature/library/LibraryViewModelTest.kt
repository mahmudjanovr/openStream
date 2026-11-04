package com.openstream.app.feature.library

import com.openstream.app.data.repository.LibraryRepositoryImpl
import com.openstream.app.data.repository.MovieCatalogRepositoryImpl
import com.openstream.app.domain.model.Movie
import com.openstream.app.fake.TestUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userPrefs: TestUserPreferencesRepository
    private lateinit var catalogRepo: MovieCatalogRepositoryImpl
    private lateinit var libraryRepo: LibraryRepositoryImpl
    private lateinit var viewModel: LibraryViewModel

    private val sampleMovie = Movie(
        id = "metropolis-1927",
        title = "Metropolis",
        director = "Fritz Lang",
        year = 1927,
        durationMinutes = 153,
        genre = "Sci-Fi",
        synopsis = "A futuristic sci-fi masterpiece."
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userPrefs = TestUserPreferencesRepository()
        catalogRepo = MovieCatalogRepositoryImpl(userPrefs)
        libraryRepo = LibraryRepositoryImpl(userPrefs, catalogRepo)
        viewModel = LibraryViewModel(libraryRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaultSelectedTab_isWatchlist() {
        assertEquals(LibraryTab.WATCHLIST, viewModel.selectedTab.value)
    }

    @Test
    fun selectTab_changesTab() {
        viewModel.selectTab(LibraryTab.FAVORITES)
        assertEquals(LibraryTab.FAVORITES, viewModel.selectedTab.value)
    }

    @Test
    fun toggleWatchlist_addsMovieToWatchlist() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.watchlist.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.watchlist.value.isEmpty())

        viewModel.toggleWatchlist(sampleMovie)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.watchlist.value.size)
        assertEquals("metropolis-1927", viewModel.watchlist.value.first().id)

        collectJob.cancel()
    }

    @Test
    fun toggleFavorite_addsMovieToFavorites() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.favorites.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.favorites.value.isEmpty())

        viewModel.toggleFavorite(sampleMovie)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.favorites.value.size)
        assertEquals("metropolis-1927", viewModel.favorites.value.first().id)

        collectJob.cancel()
    }
}
