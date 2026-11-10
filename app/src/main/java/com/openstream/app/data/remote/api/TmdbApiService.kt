package com.openstream.app.data.remote.api

import com.openstream.app.data.remote.dto.TmdbMovieDto
import com.openstream.app.data.remote.dto.TmdbPagedResponse
import com.openstream.app.data.remote.dto.TmdbVideosResponse
import com.openstream.app.data.remote.dto.TmdbWatchProvidersResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("trending/all/day")
    suspend fun getTrendingAll(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("tv/popular")
    suspend fun getPopularTvSeries(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("primary_release_year") primaryReleaseYear: Int? = null,
        @Query("with_genres") withGenres: String? = null,
        @Query("with_original_language") originalLanguage: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("vote_average.gte") minVoteAverage: Double? = null,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("primary_release_year") primaryReleaseYear: Int? = null,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TmdbMovieDto>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Long,
        @Query("api_key") apiKey: String,
        @Query("append_to_response") appendToResponse: String = "credits,videos,watch/providers"
    ): TmdbMovieDto

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Long,
        @Query("api_key") apiKey: String
    ): TmdbVideosResponse

    @GET("movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProviders(
        @Path("movie_id") movieId: Long,
        @Query("api_key") apiKey: String
    ): TmdbWatchProvidersResponse
}
