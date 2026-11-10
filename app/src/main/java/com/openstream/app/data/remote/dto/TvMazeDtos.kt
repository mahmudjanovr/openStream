package com.openstream.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TvMazeShowDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("genres") val genres: List<String>? = emptyList(),
    @SerializedName("status") val status: String? = null,
    @SerializedName("runtime") val runtime: Int? = null,
    @SerializedName("premiered") val premiered: String? = null,
    @SerializedName("officialSite") val officialSite: String? = null,
    @SerializedName("rating") val rating: TvMazeRatingDto? = null,
    @SerializedName("network") val network: TvMazeNetworkDto? = null,
    @SerializedName("image") val image: TvMazeImageDto? = null,
    @SerializedName("summary") val summary: String? = null
)

data class TvMazeRatingDto(
    @SerializedName("average") val average: Double? = null
)

data class TvMazeNetworkDto(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("country") val country: TvMazeCountryDto? = null
)

data class TvMazeCountryDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("code") val code: String? = null
)

data class TvMazeImageDto(
    @SerializedName("medium") val medium: String? = null,
    @SerializedName("original") val original: String? = null
)

data class TvMazeEpisodeDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String? = null,
    @SerializedName("season") val season: Int? = null,
    @SerializedName("number") val number: Int? = null,
    @SerializedName("airdate") val airdate: String? = null,
    @SerializedName("runtime") val runtime: Int? = null,
    @SerializedName("image") val image: TvMazeImageDto? = null,
    @SerializedName("summary") val summary: String? = null
)

data class TvMazeSearchResultDto(
    @SerializedName("score") val score: Double = 0.0,
    @SerializedName("show") val show: TvMazeShowDto
)
