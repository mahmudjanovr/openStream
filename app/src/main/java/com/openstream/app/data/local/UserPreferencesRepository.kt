package com.openstream.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.openstream.app.core.localization.AppLanguage
import com.openstream.app.domain.model.UserProfile
import com.openstream.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "openstream_user_prefs")

interface UserPreferencesRepository {
    val activeProfileId: Flow<String>
    val isOnboardingCompleted: Flow<Boolean>
    val selectedLanguage: Flow<AppLanguage>
    val themePreference: Flow<String>
    val watchlistMovieIds: Flow<Set<String>>
    val favoriteMovieIds: Flow<Set<String>>
    val recentSearches: Flow<List<String>>
    val playbackPositions: Flow<Map<String, Long>>

    fun getWatchlistForProfile(profileId: String): Flow<Set<String>>
    fun getFavoritesForProfile(profileId: String): Flow<Set<String>>
    fun getPlaybackPositionsForProfile(profileId: String): Flow<Map<String, Long>>

    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setSelectedLanguage(language: AppLanguage)
    suspend fun setThemePreference(theme: String)
    suspend fun toggleWatchlistId(movieId: String, profileId: String? = null)
    suspend fun toggleFavoriteId(movieId: String, profileId: String? = null)
    suspend fun addRecentSearch(query: String, profileId: String? = null)
    suspend fun clearRecentSearches(profileId: String? = null)
    suspend fun savePlaybackPosition(movieId: String, positionMs: Long, profileId: String? = null)
    suspend fun clearPlaybackPositions(profileId: String? = null)
}

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesRepositoryImpl(
    private val context: Context,
    private val profileRepository: UserProfileRepository? = null
) : UserPreferencesRepository {

    private object LegacyKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        val WATCHLIST_IDS = stringSetPreferencesKey("watchlist_movie_ids")
        val FAVORITE_IDS = stringSetPreferencesKey("favorite_movie_ids")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches_csv")
        val PLAYBACK_POSITIONS = stringPreferencesKey("playback_positions_csv")
    }

    private fun watchlistKey(profileId: String) =
        stringSetPreferencesKey("watchlist_movie_ids_${profileId}")

    private fun favoriteKey(profileId: String) =
        stringSetPreferencesKey("favorite_movie_ids_${profileId}")

    private fun recentSearchesKey(profileId: String) =
        stringPreferencesKey("recent_searches_csv_${profileId}")

    private fun playbackPositionsKey(profileId: String) =
        stringPreferencesKey("playback_positions_csv_${profileId}")

    override val activeProfileId: Flow<String> = profileRepository?.activeProfileId
        ?: flowOf(UserProfile.DEFAULT_PROFILE_ID)

    override val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[LegacyKeys.ONBOARDING_COMPLETED] ?: false
        }

    override val selectedLanguage: Flow<AppLanguage> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val code = preferences[LegacyKeys.SELECTED_LANGUAGE] ?: AppLanguage.ENGLISH.code
            AppLanguage.fromCode(code)
        }

    override val themePreference: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[LegacyKeys.THEME_PREFERENCE] ?: "dark"
        }

    override fun getWatchlistForProfile(profileId: String): Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val profileSet = preferences[watchlistKey(profileId)]
            if (profileSet != null) {
                profileSet
            } else if (profileId == UserProfile.DEFAULT_PROFILE_ID) {
                preferences[LegacyKeys.WATCHLIST_IDS] ?: emptySet()
            } else {
                emptySet()
            }
        }

    override val watchlistMovieIds: Flow<Set<String>> = activeProfileId.flatMapLatest { profileId ->
        getWatchlistForProfile(profileId)
    }

    override fun getFavoritesForProfile(profileId: String): Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val profileSet = preferences[favoriteKey(profileId)]
            if (profileSet != null) {
                profileSet
            } else if (profileId == UserProfile.DEFAULT_PROFILE_ID) {
                preferences[LegacyKeys.FAVORITE_IDS] ?: emptySet()
            } else {
                emptySet()
            }
        }

    override val favoriteMovieIds: Flow<Set<String>> = activeProfileId.flatMapLatest { profileId ->
        getFavoritesForProfile(profileId)
    }

    override val recentSearches: Flow<List<String>> = activeProfileId.flatMapLatest { profileId ->
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences ->
                val csv = preferences[recentSearchesKey(profileId)]
                    ?: if (profileId == UserProfile.DEFAULT_PROFILE_ID) preferences[LegacyKeys.RECENT_SEARCHES] else null

                if (csv.isNullOrBlank()) {
                    listOf("Metropolis", "Buster Keaton", "Silent Era", "Film Noir")
                } else {
                    csv.split("|||").filter { it.isNotBlank() }
                }
            }
    }

    override fun getPlaybackPositionsForProfile(profileId: String): Flow<Map<String, Long>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val raw = preferences[playbackPositionsKey(profileId)]
                ?: if (profileId == UserProfile.DEFAULT_PROFILE_ID) preferences[LegacyKeys.PLAYBACK_POSITIONS] else null

            if (raw.isNullOrBlank()) {
                emptyMap()
            } else {
                raw.split(";;;").mapNotNull { entry ->
                    val parts = entry.split(":::")
                    if (parts.size == 2) {
                        parts[0] to (parts[1].toLongOrNull() ?: 0L)
                    } else null
                }.toMap()
            }
        }

    override val playbackPositions: Flow<Map<String, Long>> = activeProfileId.flatMapLatest { profileId ->
        getPlaybackPositionsForProfile(profileId)
    }

    private suspend fun resolveTargetProfile(explicitProfileId: String?): String {
        return explicitProfileId ?: activeProfileId.firstOrNull() ?: UserProfile.DEFAULT_PROFILE_ID
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LegacyKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun setSelectedLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[LegacyKeys.SELECTED_LANGUAGE] = language.code
        }
    }

    override suspend fun setThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[LegacyKeys.THEME_PREFERENCE] = theme
        }
    }

    override suspend fun toggleWatchlistId(movieId: String, profileId: String?) {
        val target = resolveTargetProfile(profileId)
        context.dataStore.edit { preferences ->
            val current = (preferences[watchlistKey(target)]
                ?: if (target == UserProfile.DEFAULT_PROFILE_ID) preferences[LegacyKeys.WATCHLIST_IDS] else null)
                ?.toMutableSet() ?: mutableSetOf()

            if (current.contains(movieId)) {
                current.remove(movieId)
            } else {
                current.add(movieId)
            }
            preferences[watchlistKey(target)] = current
            if (target == UserProfile.DEFAULT_PROFILE_ID) {
                preferences[LegacyKeys.WATCHLIST_IDS] = current
            }
        }
    }

    override suspend fun toggleFavoriteId(movieId: String, profileId: String?) {
        val target = resolveTargetProfile(profileId)
        context.dataStore.edit { preferences ->
            val current = (preferences[favoriteKey(target)]
                ?: if (target == UserProfile.DEFAULT_PROFILE_ID) preferences[LegacyKeys.FAVORITE_IDS] else null)
                ?.toMutableSet() ?: mutableSetOf()

            if (current.contains(movieId)) {
                current.remove(movieId)
            } else {
                current.add(movieId)
            }
            preferences[favoriteKey(target)] = current
            if (target == UserProfile.DEFAULT_PROFILE_ID) {
                preferences[LegacyKeys.FAVORITE_IDS] = current
            }
        }
    }

    override suspend fun addRecentSearch(query: String, profileId: String?) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        val target = resolveTargetProfile(profileId)
        context.dataStore.edit { preferences ->
            val currentRaw = preferences[recentSearchesKey(target)]
                ?: if (target == UserProfile.DEFAULT_PROFILE_ID) preferences[LegacyKeys.RECENT_SEARCHES] else null

            val currentList = (currentRaw?.split("|||") ?: emptyList())
                .filter { it.isNotBlank() && !it.equals(trimmed, ignoreCase = true) }
                .toMutableList()
            currentList.add(0, trimmed)
            val updated = currentList.take(10).joinToString("|||")
            preferences[recentSearchesKey(target)] = updated
            if (target == UserProfile.DEFAULT_PROFILE_ID) {
                preferences[LegacyKeys.RECENT_SEARCHES] = updated
            }
        }
    }

    override suspend fun clearRecentSearches(profileId: String?) {
        val target = resolveTargetProfile(profileId)
        context.dataStore.edit { preferences ->
            preferences[recentSearchesKey(target)] = ""
            if (target == UserProfile.DEFAULT_PROFILE_ID) {
                preferences[LegacyKeys.RECENT_SEARCHES] = ""
            }
        }
    }

    override suspend fun savePlaybackPosition(movieId: String, positionMs: Long, profileId: String?) {
        if (movieId.isBlank()) return
        val target = resolveTargetProfile(profileId)
        context.dataStore.edit { preferences ->
            val raw = preferences[playbackPositionsKey(target)]
                ?: if (target == UserProfile.DEFAULT_PROFILE_ID) preferences[LegacyKeys.PLAYBACK_POSITIONS] else null

            val map = if (raw.isNullOrBlank()) {
                mutableMapOf<String, Long>()
            } else {
                raw.split(";;;").mapNotNull { entry ->
                    val parts = entry.split(":::")
                    if (parts.size == 2) parts[0] to (parts[1].toLongOrNull() ?: 0L) else null
                }.toMap().toMutableMap()
            }
            map[movieId] = positionMs
            val encoded = map.entries.joinToString(";;;") { "${it.key}:::${it.value}" }
            preferences[playbackPositionsKey(target)] = encoded
            if (target == UserProfile.DEFAULT_PROFILE_ID) {
                preferences[LegacyKeys.PLAYBACK_POSITIONS] = encoded
            }
        }
    }

    override suspend fun clearPlaybackPositions(profileId: String?) {
        val target = resolveTargetProfile(profileId)
        context.dataStore.edit { preferences ->
            preferences[playbackPositionsKey(target)] = ""
            if (target == UserProfile.DEFAULT_PROFILE_ID) {
                preferences[LegacyKeys.PLAYBACK_POSITIONS] = ""
            }
        }
    }
}
