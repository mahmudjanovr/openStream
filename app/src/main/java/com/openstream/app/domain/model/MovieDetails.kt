package com.openstream.app.domain.model

data class MovieDetails(
    val movie: Movie,
    val availableStreams: List<MovieStream> = emptyList(),
    val similarMovies: List<Movie> = emptyList(),
    val metadata: MovieMetadata? = null,
    val isVerifiedPublicDomain: Boolean = true
)
