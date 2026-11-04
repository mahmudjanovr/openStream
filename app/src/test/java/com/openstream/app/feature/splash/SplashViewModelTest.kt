package com.openstream.app.feature.splash

import com.openstream.app.core.navigation.OpenStreamDestinations
import com.openstream.app.fake.TestUserPreferencesRepository
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
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun checkInitialRoute_whenNotCompleted_navigatesToOnboarding() = runTest(testDispatcher) {
        val testPrefs = TestUserPreferencesRepository(initialOnboarding = false)
        val viewModel = SplashViewModel(testPrefs)

        viewModel.checkInitialRoute()
        testDispatcher.scheduler.advanceUntilIdle()

        val target = viewModel.navigationTarget.value
        assertTrue(target is SplashNavigationTarget.Navigate)
        assertEquals(OpenStreamDestinations.ONBOARDING, (target as SplashNavigationTarget.Navigate).destination)
    }

    @Test
    fun checkInitialRoute_whenCompleted_navigatesToHome() = runTest(testDispatcher) {
        val testPrefs = TestUserPreferencesRepository(initialOnboarding = true)
        val viewModel = SplashViewModel(testPrefs)

        viewModel.checkInitialRoute()
        testDispatcher.scheduler.advanceUntilIdle()

        val target = viewModel.navigationTarget.value
        assertTrue(target is SplashNavigationTarget.Navigate)
        assertEquals(OpenStreamDestinations.HOME, (target as SplashNavigationTarget.Navigate).destination)
    }
}
