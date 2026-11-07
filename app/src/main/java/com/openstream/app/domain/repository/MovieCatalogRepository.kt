package com.openstream.app.domain.repository

import com.openstream.app.domain.model.Genre
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.MovieCategory
import com.openstream.app.domain.model.MovieDetails
import kotlinx.coroutines.flow.Flow

interface MovieCatalogRepository {
    fun getHeroMovie(): Flow<Movie?>
    fun getContinueWatching(): Flow<List<Movie>>
    fun getFeaturedCollections(): Flow<List<MovieCategory>>
    fun getRecentlyAdded(): Flow<List<Movie>>
    fun getPopularClassics(): Flow<List<Movie>>
    fun getMoviesByGenre(genre: Genre): Flow<List<Movie>>
    fun searchMovies(query: String): Flow<List<Movie>>
    fun getMovieById(id: String): Flow<Movie?>
    fun getRelatedMovies(movieId: String): Flow<List<Movie>>
    fun getRecentSearches(): Flow<List<String>>
    suspend fun addRecentSearch(query: String)
    suspend fun clearRecentSearches()

    // Enhanced real API & detail methods
    fun getMovieDetails(movieId: String): Flow<MovieDetails?>
    suspend fun resolveStreamUrl(movie: Movie): String
    fun searchLiveCatalog(query: String): Flow<List<Movie>>
}
