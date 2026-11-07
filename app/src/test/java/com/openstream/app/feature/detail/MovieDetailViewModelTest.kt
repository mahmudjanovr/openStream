package com.openstream.app.feature.detail

import com.openstream.app.core.common.UiState
import com.openstream.app.data.repository.LibraryRepositoryImpl
import com.openstream.app.data.repository.MovieCatalogRepositoryImpl
import com.openstream.app.fake.FakeArchiveApiService
import com.openstream.app.fake.TestUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var userPrefs: TestUserPreferencesRepository
    private lateinit var catalogRepo: MovieCatalogRepositoryImpl
    private lateinit var libraryRepo: LibraryRepositoryImpl

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userPrefs = TestUserPreferencesRepository()
        catalogRepo = MovieCatalogRepositoryImpl(
            userPreferencesRepository = userPrefs,
            apiService = FakeArchiveApiService(),
            ioDispatcher = testDispatcher
        )
        libraryRepo = LibraryRepositoryImpl(userPrefs, catalogRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadMovieDetail_success_loadsMovieAndRelated() = runTest(testDispatcher) {
        val viewModel = MovieDetailViewModel("metropolis-1927", catalogRepo, libraryRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected UiState.Success but got $state", state is UiState.Success)
        val data = (state as UiState.Success).data
        assertEquals("Metropolis", data.movie.title)
        assertEquals(1927, data.movie.year)
        assertEquals("Fritz Lang", data.movie.director)
        assertFalse(data.isInWatchlist)
        assertFalse(data.isFavorite)
        assertTrue(data.relatedMovies.isNotEmpty())
        assertTrue(data.relatedMovies.none { it.id == "metropolis-1927" })
    }

    @Test
    fun loadMovieDetail_unknownMovie_setsEmptyState() = runTest(testDispatcher) {
        val viewModel = MovieDetailViewModel("invalid-movie-id-999", catalogRepo, libraryRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected UiState.Empty but got $state", state is UiState.Empty)
    }

    @Test
    fun toggleWatchlist_updatesState() = runTest(testDispatcher) {
        val viewModel = MovieDetailViewModel("metropolis-1927", catalogRepo, libraryRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialData = (viewModel.uiState.value as UiState.Success).data
        assertFalse(initialData.isInWatchlist)

        viewModel.toggleWatchlist(initialData.movie)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedData = (viewModel.uiState.value as UiState.Success).data
        assertTrue(updatedData.isInWatchlist)
        assertTrue(updatedData.movie.isInWatchlist)

        viewModel.toggleWatchlist(updatedData.movie)
        testDispatcher.scheduler.advanceUntilIdle()

        val toggledOffData = (viewModel.uiState.value as UiState.Success).data
        assertFalse(toggledOffData.isInWatchlist)
    }

    @Test
    fun toggleFavorite_updatesState() = runTest(testDispatcher) {
        val viewModel = MovieDetailViewModel("metropolis-1927", catalogRepo, libraryRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialData = (viewModel.uiState.value as UiState.Success).data
        assertFalse(initialData.isFavorite)

        viewModel.toggleFavorite(initialData.movie)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedData = (viewModel.uiState.value as UiState.Success).data
        assertTrue(updatedData.isFavorite)
        assertTrue(updatedData.movie.isFavorite)

        viewModel.toggleFavorite(updatedData.movie)
        testDispatcher.scheduler.advanceUntilIdle()

        val toggledOffData = (viewModel.uiState.value as UiState.Success).data
        assertFalse(toggledOffData.isFavorite)
    }
}
