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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "openstream_user_prefs")

interface UserPreferencesRepository {
    val isOnboardingCompleted: Flow<Boolean>
    val selectedLanguage: Flow<AppLanguage>
    val themePreference: Flow<String>
    val watchlistMovieIds: Flow<Set<String>>
    val favoriteMovieIds: Flow<Set<String>>
    val recentSearches: Flow<List<String>>

    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setSelectedLanguage(language: AppLanguage)
    suspend fun setThemePreference(theme: String)
    suspend fun toggleWatchlistId(movieId: String)
    suspend fun toggleFavoriteId(movieId: String)
    suspend fun addRecentSearch(query: String)
    suspend fun clearRecentSearches()
}

class UserPreferencesRepositoryImpl(
    private val context: Context
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
        val WATCHLIST_IDS = stringSetPreferencesKey("watchlist_movie_ids")
        val FAVORITE_IDS = stringSetPreferencesKey("favorite_movie_ids")
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches_csv")
    }

    override val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    override val selectedLanguage: Flow<AppLanguage> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val code = preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: AppLanguage.ENGLISH.code
            AppLanguage.fromCode(code)
        }

    override val themePreference: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.THEME_PREFERENCE] ?: "dark"
        }

    override val watchlistMovieIds: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.WATCHLIST_IDS] ?: emptySet()
        }

    override val favoriteMovieIds: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.FAVORITE_IDS] ?: emptySet()
        }

    override val recentSearches: Flow<List<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val csv = preferences[PreferencesKeys.RECENT_SEARCHES]
            if (csv.isNullOrBlank()) {
                listOf("Metropolis", "Buster Keaton", "Silent Era", "Film Noir")
            } else {
                csv.split("|||").filter { it.isNotBlank() }
            }
        }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun setSelectedLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE] = language.code
        }
    }

    override suspend fun setThemePreference(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_PREFERENCE] = theme
        }
    }

    override suspend fun toggleWatchlistId(movieId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.WATCHLIST_IDS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(movieId)) {
                current.remove(movieId)
            } else {
                current.add(movieId)
            }
            preferences[PreferencesKeys.WATCHLIST_IDS] = current
        }
    }

    override suspend fun toggleFavoriteId(movieId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.FAVORITE_IDS]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(movieId)) {
                current.remove(movieId)
            } else {
                current.add(movieId)
            }
            preferences[PreferencesKeys.FAVORITE_IDS] = current
        }
    }

    override suspend fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        context.dataStore.edit { preferences ->
            val currentList = (preferences[PreferencesKeys.RECENT_SEARCHES]?.split("|||") ?: emptyList())
                .filter { it.isNotBlank() && !it.equals(trimmed, ignoreCase = true) }
                .toMutableList()
            currentList.add(0, trimmed)
            preferences[PreferencesKeys.RECENT_SEARCHES] = currentList.take(10).joinToString("|||")
        }
    }

    override suspend fun clearRecentSearches() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECENT_SEARCHES] = ""
        }
    }
}
