package com.openstream.app.domain.model

data class Movie(
    val id: String,
    val title: String,
    val director: String = "",
    val year: Int = 2024,
    val durationMinutes: Int = 120,
    val genre: String = "Drama",
    val synopsis: String = "",
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val heroImageUrl: String = "",
    val isPublicDomain: Boolean = true,
    val rating: String = "PG-13",
    val country: String = "United States",
    val leadCast: List<String> = emptyList(),
    val restorationNote: String = "Digitally restored archival print",
    val licenseStatus: String = "Public Domain (Worldwide)",
    val playbackProgressPercentage: Float? = null,
    val isInWatchlist: Boolean = false,
    val isFavorite: Boolean = false,
    val streamUrl: String = "",
    val movieSource: MovieSource = MovieSource.INTERNET_ARCHIVE,
    val streams: List<MovieStream> = emptyList(),
    val metadata: MovieMetadata? = null,
    // Modern cinema & TV series expansion fields
    val originalTitle: String? = null,
    val releaseDate: String? = null,
    val voteAverage: Double? = null,
    val voteCount: Int? = null,
    val mediaType: String = "movie", // "movie" or "tv"
    val trailerYoutubeKey: String? = null,
    val watchProviders: List<WatchProvider> = emptyList(),
    val isPlayableDirectly: Boolean = true,
    val originalLanguage: String? = "en",
    val popularity: Double? = null
)

enum class Genre(val key: String) {
    ALL("all"),
    SILENT("silent"),
    NOIR("noir"),
    SCIFI_HORROR("scifi_horror"),
    COMEDY("comedy"),
    DRAMA("drama"),
    ACTION("action"),
    ANIMATION("animation"),
    THRILLER("thriller");

    companion object {
        fun fromKey(key: String): Genre = values().firstOrNull { it.key == key } ?: ALL
    }
}

data class MovieCategory(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val movies: List<Movie>
)
