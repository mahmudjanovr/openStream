package com.openstream.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TmdbPagedResponse<T>(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("results") val results: List<T> = emptyList(),
    @SerializedName("total_pages") val totalPages: Int = 1,
    @SerializedName("total_results") val totalResults: Int = 0
)

data class TmdbMovieDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null, // Used for TV shows
    @SerializedName("original_title") val originalTitle: String? = null,
    @SerializedName("original_name") val originalName: String? = null,
    @SerializedName("overview") val overview: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("first_air_date") val firstAirDate: String? = null,
    @SerializedName("vote_average") val voteAverage: Double? = null,
    @SerializedName("vote_count") val voteCount: Int? = null,
    @SerializedName("genre_ids") val genreIds: List<Int>? = emptyList(),
    @SerializedName("genres") val genres: List<TmdbGenreDto>? = emptyList(),
    @SerializedName("original_language") val originalLanguage: String? = null,
    @SerializedName("popularity") val popularity: Double? = null,
    @SerializedName("runtime") val runtime: Int? = null,
    @SerializedName("tagline") val tagline: String? = null,
    @SerializedName("credits") val credits: TmdbCreditsDto? = null,
    @SerializedName("videos") val videos: TmdbVideosResponse? = null,
    @SerializedName("media_type") val mediaType: String? = null
)

data class TmdbGenreDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class TmdbCreditsDto(
    @SerializedName("cast") val cast: List<TmdbCastDto>? = emptyList(),
    @SerializedName("crew") val crew: List<TmdbCrewDto>? = emptyList()
)

data class TmdbCastDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String?,
    @SerializedName("profile_path") val profilePath: String?
)

data class TmdbCrewDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("job") val job: String?,
    @SerializedName("department") val department: String?
)

data class TmdbVideoDto(
    @SerializedName("id") val id: String,
    @SerializedName("key") val key: String,
    @SerializedName("name") val name: String,
    @SerializedName("site") val site: String,
    @SerializedName("type") val type: String
)

data class TmdbVideosResponse(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("results") val results: List<TmdbVideoDto> = emptyList()
)

data class TmdbWatchProvidersResponse(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("results") val results: Map<String, TmdbCountryWatchProvidersDto>? = null
)

data class TmdbCountryWatchProvidersDto(
    @SerializedName("link") val link: String? = null,
    @SerializedName("flatrate") val flatrate: List<TmdbWatchProviderItemDto>? = null,
    @SerializedName("rent") val rent: List<TmdbWatchProviderItemDto>? = null,
    @SerializedName("buy") val buy: List<TmdbWatchProviderItemDto>? = null,
    @SerializedName("free") val free: List<TmdbWatchProviderItemDto>? = null
)

data class TmdbWatchProviderItemDto(
    @SerializedName("provider_id") val providerId: Long,
    @SerializedName("provider_name") val providerName: String,
    @SerializedName("logo_path") val logoPath: String?
)
