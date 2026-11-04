package com.openstream.app.feature.onboarding

import com.openstream.app.fake.TestUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testPrefs: TestUserPreferencesRepository
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testPrefs = TestUserPreferencesRepository(initialOnboarding = false)
        viewModel = OnboardingViewModel(testPrefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isFirstPageAndNotCompleted() {
        val state = viewModel.uiState.value
        assertEquals(0, state.currentPageIndex)
        assertEquals(3, state.totalPages)
        assertEquals(false, state.isCompleted)
    }

    @Test
    fun onNextClicked_incrementsPageIndex() {
        viewModel.onNextClicked()
        assertEquals(1, viewModel.uiState.value.currentPageIndex)
        assertEquals(false, viewModel.uiState.value.isCompleted)

        viewModel.onNextClicked()
        assertEquals(2, viewModel.uiState.value.currentPageIndex)
        assertEquals(false, viewModel.uiState.value.isCompleted)
    }

    @Test
    fun onNextClicked_onLastPage_completesAndPersists() = runTest(testDispatcher) {
        viewModel.onPageChanged(2)
        viewModel.onNextClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCompleted)
        assertTrue(testPrefs.isOnboardingCompleted.first())
    }

    @Test
    fun onSkipClicked_completesAndPersistsImmediately() = runTest(testDispatcher) {
        viewModel.onSkipClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCompleted)
        assertTrue(testPrefs.isOnboardingCompleted.first())
    }
}
