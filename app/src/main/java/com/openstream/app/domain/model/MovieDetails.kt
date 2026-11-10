package com.openstream.app.domain.model

data class TvEpisode(
    val id: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val name: String,
    val overview: String = "",
    val airDate: String? = null,
    val runtimeMinutes: Int? = null,
    val stillImageUrl: String? = null
)

data class TvSeason(
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int,
    val posterUrl: String? = null,
    val episodes: List<TvEpisode> = emptyList()
)

data class MovieDetails(
    val movie: Movie,
    val availableStreams: List<MovieStream> = emptyList(),
    val similarMovies: List<Movie> = emptyList(),
    val metadata: MovieMetadata? = null,
    val isVerifiedPublicDomain: Boolean = true,
    // Modern metadata expansion
    val trailerYoutubeKey: String? = null,
    val trailerUrl: String? = null,
    val watchProviders: List<WatchProvider> = emptyList(),
    val productionCompanies: List<String> = emptyList(),
    val cast: List<String> = emptyList(),
    val director: String? = null,
    val seasons: List<TvSeason> = emptyList(),
    val genres: List<String> = emptyList()
)
