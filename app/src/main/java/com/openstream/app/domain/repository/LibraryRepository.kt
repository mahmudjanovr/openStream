package com.openstream.app.domain.repository

import com.openstream.app.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun getWatchlist(): Flow<List<Movie>>
    fun getFavorites(): Flow<List<Movie>>
    suspend fun toggleWatchlist(movie: Movie)
    suspend fun toggleFavorite(movie: Movie)
    fun isInWatchlist(movieId: String): Flow<Boolean>
    fun isFavorite(movieId: String): Flow<Boolean>
}
