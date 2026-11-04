package com.openstream.app.fake

import com.openstream.app.core.localization.AppLanguage
import com.openstream.app.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestUserPreferencesRepository(
    initialOnboarding: Boolean = false,
    initialLanguage: AppLanguage = AppLanguage.ENGLISH,
    initialTheme: String = "dark",
    initialWatchlist: Set<String> = emptySet(),
    initialFavorites: Set<String> = emptySet(),
    initialRecentSearches: List<String> = emptyList()
) : UserPreferencesRepository {

    private val _isOnboardingCompleted = MutableStateFlow(initialOnboarding)
    override val isOnboardingCompleted: Flow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(initialLanguage)
    override val selectedLanguage: Flow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _themePreference = MutableStateFlow(initialTheme)
    override val themePreference: Flow<String> = _themePreference.asStateFlow()

    private val _watchlistMovieIds = MutableStateFlow(initialWatchlist)
    override val watchlistMovieIds: Flow<Set<String>> = _watchlistMovieIds.asStateFlow()

    private val _favoriteMovieIds = MutableStateFlow(initialFavorites)
    override val favoriteMovieIds: Flow<Set<String>> = _favoriteMovieIds.asStateFlow()

    private val _recentSearches = MutableStateFlow(initialRecentSearches)
    override val recentSearches: Flow<List<String>> = _recentSearches.asStateFlow()

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _isOnboardingCompleted.value = completed
    }

    override suspend fun setSelectedLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
    }

    override suspend fun setThemePreference(theme: String) {
        _themePreference.value = theme
    }

    override suspend fun toggleWatchlistId(movieId: String) {
        val current = _watchlistMovieIds.value
        _watchlistMovieIds.value = if (current.contains(movieId)) {
            current - movieId
        } else {
            current + movieId
        }
    }

    override suspend fun toggleFavoriteId(movieId: String) {
        val current = _favoriteMovieIds.value
        _favoriteMovieIds.value = if (current.contains(movieId)) {
            current - movieId
        } else {
            current + movieId
        }
    }

    override suspend fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            val updated = (_recentSearches.value.filter { !it.equals(trimmed, ignoreCase = true) }).toMutableList()
            updated.add(0, trimmed)
            _recentSearches.value = updated.take(10)
        }
    }

    override suspend fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }
}
