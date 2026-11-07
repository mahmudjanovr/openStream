package com.openstream.app.fake

import com.openstream.app.core.localization.AppLanguage
import com.openstream.app.data.local.UserPreferencesRepository
import com.openstream.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TestUserPreferencesRepository(
    initialOnboarding: Boolean = false,
    initialLanguage: AppLanguage = AppLanguage.ENGLISH,
    initialTheme: String = "dark",
    initialWatchlist: Set<String> = emptySet(),
    initialFavorites: Set<String> = emptySet(),
    initialRecentSearches: List<String> = emptyList(),
    initialActiveProfileId: String = UserProfile.DEFAULT_PROFILE_ID
) : UserPreferencesRepository {

    private val _activeProfileId = MutableStateFlow(initialActiveProfileId)
    override val activeProfileId: Flow<String> = _activeProfileId.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(initialOnboarding)
    override val isOnboardingCompleted: Flow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(initialLanguage)
    override val selectedLanguage: Flow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _themePreference = MutableStateFlow(initialTheme)
    override val themePreference: Flow<String> = _themePreference.asStateFlow()

    private val _profileWatchlists = mutableMapOf<String, MutableStateFlow<Set<String>>>(
        initialActiveProfileId to MutableStateFlow(initialWatchlist)
    )
    private val _profileFavorites = mutableMapOf<String, MutableStateFlow<Set<String>>>(
        initialActiveProfileId to MutableStateFlow(initialFavorites)
    )
    private val _profilePositions = mutableMapOf<String, MutableStateFlow<Map<String, Long>>>(
        initialActiveProfileId to MutableStateFlow(emptyMap())
    )

    private val _watchlistMovieIds = MutableStateFlow(initialWatchlist)
    override val watchlistMovieIds: Flow<Set<String>> = _watchlistMovieIds.asStateFlow()

    private val _favoriteMovieIds = MutableStateFlow(initialFavorites)
    override val favoriteMovieIds: Flow<Set<String>> = _favoriteMovieIds.asStateFlow()

    private val _recentSearches = MutableStateFlow(initialRecentSearches)
    override val recentSearches: Flow<List<String>> = _recentSearches.asStateFlow()

    private val _playbackPositions = MutableStateFlow<Map<String, Long>>(emptyMap())
    override val playbackPositions: Flow<Map<String, Long>> = _playbackPositions.asStateFlow()

    fun setActiveProfile(profileId: String) {
        _activeProfileId.value = profileId
        _watchlistMovieIds.value = _profileWatchlists.getOrPut(profileId) { MutableStateFlow(emptySet()) }.value
        _favoriteMovieIds.value = _profileFavorites.getOrPut(profileId) { MutableStateFlow(emptySet()) }.value
        _playbackPositions.value = _profilePositions.getOrPut(profileId) { MutableStateFlow(emptyMap()) }.value
    }

    override fun getWatchlistForProfile(profileId: String): Flow<Set<String>> {
        return _profileWatchlists.getOrPut(profileId) { MutableStateFlow(emptySet()) }.asStateFlow()
    }

    override fun getFavoritesForProfile(profileId: String): Flow<Set<String>> {
        return _profileFavorites.getOrPut(profileId) { MutableStateFlow(emptySet()) }.asStateFlow()
    }

    override fun getPlaybackPositionsForProfile(profileId: String): Flow<Map<String, Long>> {
        return _profilePositions.getOrPut(profileId) { MutableStateFlow(emptyMap()) }.asStateFlow()
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _isOnboardingCompleted.value = completed
    }

    override suspend fun setSelectedLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
    }

    override suspend fun setThemePreference(theme: String) {
        _themePreference.value = theme
    }

    override suspend fun toggleWatchlistId(movieId: String, profileId: String?) {
        val target = profileId ?: _activeProfileId.value
        val flow = _profileWatchlists.getOrPut(target) { MutableStateFlow(emptySet()) }
        val current = flow.value
        val updated = if (current.contains(movieId)) current - movieId else current + movieId
        flow.value = updated
        if (target == _activeProfileId.value) {
            _watchlistMovieIds.value = updated
        }
    }

    override suspend fun toggleFavoriteId(movieId: String, profileId: String?) {
        val target = profileId ?: _activeProfileId.value
        val flow = _profileFavorites.getOrPut(target) { MutableStateFlow(emptySet()) }
        val current = flow.value
        val updated = if (current.contains(movieId)) current - movieId else current + movieId
        flow.value = updated
        if (target == _activeProfileId.value) {
            _favoriteMovieIds.value = updated
        }
    }

    override suspend fun addRecentSearch(query: String, profileId: String?) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            val updated = (_recentSearches.value.filter { !it.equals(trimmed, ignoreCase = true) }).toMutableList()
            updated.add(0, trimmed)
            _recentSearches.value = updated.take(10)
        }
    }

    override suspend fun clearRecentSearches(profileId: String?) {
        _recentSearches.value = emptyList()
    }

    override suspend fun savePlaybackPosition(movieId: String, positionMs: Long, profileId: String?) {
        val target = profileId ?: _activeProfileId.value
        val flow = _profilePositions.getOrPut(target) { MutableStateFlow(emptyMap()) }
        val updated = flow.value + (movieId to positionMs)
        flow.value = updated
        if (target == _activeProfileId.value) {
            _playbackPositions.value = updated
        }
    }

    override suspend fun clearPlaybackPositions(profileId: String?) {
        val target = profileId ?: _activeProfileId.value
        _profilePositions.getOrPut(target) { MutableStateFlow(emptyMap()) }.value = emptyMap()
        if (target == _activeProfileId.value) {
            _playbackPositions.value = emptyMap()
        }
    }
}
