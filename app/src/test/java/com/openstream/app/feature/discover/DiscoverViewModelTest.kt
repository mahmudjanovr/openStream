package com.openstream.app.feature.discover

import com.openstream.app.core.common.UiState
import com.openstream.app.data.repository.MovieCatalogRepositoryImpl
import com.openstream.app.domain.model.Genre
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
        catalogRepo = MovieCatalogRepositoryImpl()
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
        viewModel.selectGenre(Genre.COMEDY)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(Genre.COMEDY, state.selectedGenre)
        assertTrue(state.moviesState is UiState.Success)
        val movies = (state.moviesState as UiState.Success).data
        assertTrue(movies.all { it.genre == "Comedy" })
    }
}
