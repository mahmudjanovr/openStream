package com.openstream.app.feature.settings

import com.openstream.app.core.localization.AppLanguage
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testPrefs: TestUserPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testPrefs = TestUserPreferencesRepository(initialLanguage = AppLanguage.ENGLISH)
        viewModel = SettingsViewModel(testPrefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaultLanguage_isEnglish() {
        assertEquals(AppLanguage.ENGLISH, viewModel.currentLanguage.value)
    }

    @Test
    fun onLanguageSelected_updatesRepository() = runTest(testDispatcher) {
        viewModel.onLanguageSelected(AppLanguage.UZBEK)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AppLanguage.UZBEK, testPrefs.selectedLanguage.first())
    }
}
