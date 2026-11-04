package com.openstream.app.domain.model

data class Movie(
    val id: String,
    val title: String,
    val director: String,
    val year: Int,
    val durationMinutes: Int,
    val genre: String,
    val synopsis: String,
    val posterUrl: String = "",
    val heroImageUrl: String = "",
    val isPublicDomain: Boolean = true,
    val rating: String = "All Ages",
    val isInWatchlist: Boolean = false,
    val isFavorite: Boolean = false
)

enum class Genre(val key: String) {
    ALL("all"),
    SILENT("silent"),
    NOIR("noir"),
    SCIFI_HORROR("scifi_horror"),
    COMEDY("comedy"),
    DRAMA("drama")
}

data class MovieCategory(
    val id: String,
    val title: String,
    val movies: List<Movie>
)
