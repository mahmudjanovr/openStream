package com.openstream.app.data.repository

import com.openstream.app.data.local.UserPreferencesRepository
import com.openstream.app.domain.model.Genre
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.repository.LibraryRepository
import com.openstream.app.domain.repository.MovieCatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class LibraryRepositoryImpl(
    private val userPreferencesRepository: UserPreferencesRepository? = null,
    private val catalogRepository: MovieCatalogRepository? = null
) : LibraryRepository {

    // Fallback in-memory state for testing without DataStore
    private val fallbackWatchlist = MutableStateFlow<List<Movie>>(emptyList())
    private val fallbackFavorites = MutableStateFlow<List<Movie>>(emptyList())

    override fun getWatchlist(): Flow<List<Movie>> {
        if (userPreferencesRepository != null && catalogRepository != null) {
            return combine(
                userPreferencesRepository.watchlistMovieIds,
                catalogRepository.getMoviesByGenre(Genre.ALL)
            ) { ids, allMovies ->
                allMovies.filter { ids.contains(it.id) }.map { it.copy(isInWatchlist = true) }
            }
        }
        return fallbackWatchlist.asStateFlow()
    }

    override fun getFavorites(): Flow<List<Movie>> {
        if (userPreferencesRepository != null && catalogRepository != null) {
            return combine(
                userPreferencesRepository.favoriteMovieIds,
                catalogRepository.getMoviesByGenre(Genre.ALL)
            ) { ids, allMovies ->
                allMovies.filter { ids.contains(it.id) }.map { it.copy(isFavorite = true) }
            }
        }
        return fallbackFavorites.asStateFlow()
    }

    override suspend fun toggleWatchlist(movie: Movie) {
        if (userPreferencesRepository != null) {
            userPreferencesRepository.toggleWatchlistId(movie.id)
        } else {
            val current = fallbackWatchlist.value.toMutableList()
            val existingIndex = current.indexOfFirst { it.id == movie.id }
            if (existingIndex >= 0) {
                current.removeAt(existingIndex)
            } else {
                current.add(movie.copy(isInWatchlist = true))
            }
            fallbackWatchlist.value = current
        }
    }

    override suspend fun toggleFavorite(movie: Movie) {
        if (userPreferencesRepository != null) {
            userPreferencesRepository.toggleFavoriteId(movie.id)
        } else {
            val current = fallbackFavorites.value.toMutableList()
            val existingIndex = current.indexOfFirst { it.id == movie.id }
            if (existingIndex >= 0) {
                current.removeAt(existingIndex)
            } else {
                current.add(movie.copy(isFavorite = true))
            }
            fallbackFavorites.value = current
        }
    }

    override fun isInWatchlist(movieId: String): Flow<Boolean> {
        if (userPreferencesRepository != null) {
            return userPreferencesRepository.watchlistMovieIds.map { it.contains(movieId) }
        }
        return fallbackWatchlist.map { list -> list.any { it.id == movieId } }
    }

    override fun isFavorite(movieId: String): Flow<Boolean> {
        if (userPreferencesRepository != null) {
            return userPreferencesRepository.favoriteMovieIds.map { it.contains(movieId) }
        }
        return fallbackFavorites.map { list -> list.any { it.id == movieId } }
    }
}
