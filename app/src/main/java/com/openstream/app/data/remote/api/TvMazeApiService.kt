package com.openstream.app.data.remote.api

import com.openstream.app.data.remote.dto.TvMazeEpisodeDto
import com.openstream.app.data.remote.dto.TvMazeSearchResultDto
import com.openstream.app.data.remote.dto.TvMazeShowDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TvMazeApiService {

    @GET("shows")
    suspend fun getShows(
        @Query("page") page: Int = 0
    ): List<TvMazeShowDto>

    @GET("search/shows")
    suspend fun searchShows(
        @Query("q") query: String
    ): List<TvMazeSearchResultDto>

    @GET("shows/{id}")
    suspend fun getShowDetails(
        @Path("id") showId: Long
    ): TvMazeShowDto

    @GET("shows/{id}/episodes")
    suspend fun getShowEpisodes(
        @Path("id") showId: Long
    ): List<TvMazeEpisodeDto>
}
