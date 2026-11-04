package com.openstream.app.data.repository

import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class LibraryRepositoryImpl : LibraryRepository {

    private val watchlist = MutableStateFlow<List<Movie>>(emptyList())
    private val favorites = MutableStateFlow<List<Movie>>(emptyList())

    override fun getWatchlist(): Flow<List<Movie>> = watchlist.asStateFlow()

    override fun getFavorites(): Flow<List<Movie>> = favorites.asStateFlow()

    override suspend fun toggleWatchlist(movie: Movie) {
        val current = watchlist.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == movie.id }
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        } else {
            current.add(movie.copy(isInWatchlist = true))
        }
        watchlist.value = current
    }

    override suspend fun toggleFavorite(movie: Movie) {
        val current = favorites.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == movie.id }
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        } else {
            current.add(movie.copy(isFavorite = true))
        }
        favorites.value = current
    }

    override fun isInWatchlist(movieId: String): Flow<Boolean> {
        return watchlist.map { list -> list.any { it.id == movieId } }
    }

    override fun isFavorite(movieId: String): Flow<Boolean> {
        return favorites.map { list -> list.any { it.id == movieId } }
    }
}
