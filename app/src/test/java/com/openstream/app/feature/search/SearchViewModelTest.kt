package com.openstream.app.feature.search

import com.openstream.app.core.common.UiState
import com.openstream.app.data.repository.MovieCatalogRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var catalogRepo: MovieCatalogRepositoryImpl
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        catalogRepo = MovieCatalogRepositoryImpl()
        viewModel = SearchViewModel(catalogRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialQuery_isEmpty() {
        assertEquals("", viewModel.query.value)
        assertTrue(viewModel.searchState.value is UiState.Empty)
    }

    @Test
    fun queryDebounce_executesSearch() = runTest(testDispatcher) {
        viewModel.onQueryChanged("Nosferatu")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.searchState.value
        assertTrue("Expected UiState.Success but got $state", state is UiState.Success)
        val results = (state as UiState.Success).data
        assertEquals(1, results.size)
        assertEquals("Nosferatu", results.first().title)
    }

    @Test
    fun clearQuery_resetsState() = runTest(testDispatcher) {
        viewModel.onQueryChanged("Nosferatu")
        advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearQuery()
        assertEquals("", viewModel.query.value)
        assertTrue(viewModel.searchState.value is UiState.Empty)
    }
}
