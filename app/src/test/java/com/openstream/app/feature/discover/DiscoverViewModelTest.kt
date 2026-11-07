package com.openstream.app.feature.discover

import com.openstream.app.core.common.UiState
import com.openstream.app.data.repository.MovieCatalogRepositoryImpl
import com.openstream.app.domain.model.Genre
import com.openstream.app.fake.FakeArchiveApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class DiscoverViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var catalogRepo: MovieCatalogRepositoryImpl
    private lateinit var viewModel: DiscoverViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        catalogRepo = MovieCatalogRepositoryImpl(
            apiService = FakeArchiveApiService(),
            ioDispatcher = testDispatcher
        )
        viewModel = DiscoverViewModel(catalogRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialGenre_isAll_andLoadsMovies() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(Genre.ALL, state.selectedGenre)
        assertTrue(state.moviesState is UiState.Success)
        val movies = (state.moviesState as UiState.Success).data
        assertTrue(movies.isNotEmpty())
    }

    @Test
    fun selectGenre_updatesSelectionAndFilters() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectGenre(Genre.COMEDY)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(Genre.COMEDY, state.selectedGenre)
        assertTrue(state.moviesState is UiState.Success)
        val movies = (state.moviesState as UiState.Success).data
        assertTrue(movies.all { it.genre == "Comedy" })
    }

    @Test
    fun selectEra_filtersMoviesByYear() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectEra(EraFilter.SILENT)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(EraFilter.SILENT, state.selectedEra)
        assertTrue(state.moviesState is UiState.Success)
        val movies = (state.moviesState as UiState.Success).data
        assertTrue(movies.all { it.year <= 1929 })
    }

    @Test
    fun selectSortOrder_sortsAlphabetically() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectSortOrder(SortOrder.TITLE_ASC)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SortOrder.TITLE_ASC, state.sortOrder)
        assertTrue(state.moviesState is UiState.Success)
        val movies = (state.moviesState as UiState.Success).data
        val titles = movies.map { it.title.lowercase() }
        assertEquals(titles.sorted(), titles)
    }
}
