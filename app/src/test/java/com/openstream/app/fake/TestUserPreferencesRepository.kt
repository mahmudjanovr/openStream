package com.openstream.app.fake

import com.openstream.app.core.localization.AppLanguage
import com.openstream.app.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestUserPreferencesRepository(
    initialOnboarding: Boolean = false,
    initialLanguage: AppLanguage = AppLanguage.ENGLISH,
    initialTheme: String = "dark"
) : UserPreferencesRepository {

    private val _isOnboardingCompleted = MutableStateFlow(initialOnboarding)
    override val isOnboardingCompleted: Flow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(initialLanguage)
    override val selectedLanguage: Flow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _themePreference = MutableStateFlow(initialTheme)
    override val themePreference: Flow<String> = _themePreference.asStateFlow()

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _isOnboardingCompleted.value = completed
    }

    override suspend fun setSelectedLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
    }

    override suspend fun setThemePreference(theme: String) {
        _themePreference.value = theme
    }
}
