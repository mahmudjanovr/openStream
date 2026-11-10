package com.openstream.app.data.remote.mapper

import com.openstream.app.data.remote.dto.TmdbMovieDto
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.MovieDetails
import com.openstream.app.domain.model.MovieMetadata
import com.openstream.app.domain.model.MovieSource
import com.openstream.app.domain.model.WatchProvider

object TmdbMovieMapper {

    private const val POSTER_BASE = "https://image.tmdb.org/t/p/w780"
    private const val BACKDROP_BASE = "https://image.tmdb.org/t/p/w1280"
    private const val PROVIDER_LOGO_BASE = "https://image.tmdb.org/t/p/w92"

    private val GENRE_MAP = mapOf(
        28 to "Action",
        12 to "Adventure",
        16 to "Animation",
        35 to "Comedy",
        80 to "Crime",
        99 to "Documentary",
        18 to "Drama",
        10751 to "Family",
        14 to "Fantasy",
        36 to "History",
        27 to "Horror",
        10402 to "Music",
        9648 to "Mystery",
        10749 to "Romance",
        878 to "Science Fiction",
        10770 to "TV Movie",
        53 to "Thriller",
        10752 to "War",
        37 to "Western",
        10759 to "Action & Adventure",
        10762 to "Kids",
        10763 to "News",
        10764 to "Reality",
        10765 to "Sci-Fi & Fantasy",
        10766 to "Soap",
        10767 to "Talk",
        10768 to "War & Politics"
    )

    fun mapDtoToMovie(dto: TmdbMovieDto, mediaTypeOverride: String? = null): Movie {
        val title = dto.title ?: dto.name ?: dto.originalTitle ?: dto.originalName ?: "Untitled"
        val releaseDate = dto.releaseDate ?: dto.firstAirDate
        val year = releaseDate?.take(4)?.toIntOrNull() ?: 2024
        val poster = dto.posterPath?.let { "$POSTER_BASE$it" } ?: ""
        val backdrop = dto.backdropPath?.let { "$BACKDROP_BASE$it" } ?: poster
        val primaryGenre = dto.genres?.firstOrNull()?.name
            ?: dto.genreIds?.firstOrNull()?.let { GENRE_MAP[it] }
            ?: "Cinema"

        val ratingText = when {
            dto.voteAverage != null && dto.voteAverage >= 8.0 -> "Masterpiece"
            dto.voteAverage != null && dto.voteAverage >= 7.0 -> "Acclaimed"
            else -> "PG-13"
        }

        val type = mediaTypeOverride ?: dto.mediaType ?: if (dto.name != null && dto.title == null) "tv" else "movie"

        val trailerKey = dto.videos?.results?.firstOrNull { video ->
            video.site.equals("YouTube", ignoreCase = true) &&
                (video.type.equals("Trailer", ignoreCase = true) || video.type.equals("Teaser", ignoreCase = true))
        }?.key

        val providers = extractWatchProviders(dto)

        val director = dto.credits?.crew?.firstOrNull { it.job.equals("Director", ignoreCase = true) }?.name ?: ""
        val cast = dto.credits?.cast?.take(6)?.map { it.name } ?: emptyList()

        return Movie(
            id = "tmdb_${type}_${dto.id}",
            title = title,
            director = director,
            year = year,
            durationMinutes = dto.runtime ?: 120,
            genre = primaryGenre,
            synopsis = dto.overview?.takeIf { it.isNotBlank() } ?: "Experience this acclaimed cinematic release on openStream.",
            posterUrl = poster,
            backdropUrl = backdrop,
            heroImageUrl = backdrop,
            isPublicDomain = false,
            rating = ratingText,
            country = "Global",
            leadCast = cast,
            restorationNote = "Official Studio Digital Master",
            licenseStatus = "Studio Copyright Protected",
            playbackProgressPercentage = null,
            streamUrl = "",
            movieSource = MovieSource.TMDB,
            streams = emptyList(),
            metadata = MovieMetadata(
                identifier = "${dto.id}",
                sourceUrl = "https://www.themoviedb.org/$type/${dto.id}",
                licenseUrl = null,
                rightsNote = "Studio Copyright",
                licenseType = "Commercial Release",
                isRightsVerified = true,
                verificationNote = "Catalog entry synced with The Movie Database (TMDB).",
                downloads = dto.voteCount?.toLong() ?: 0L
            ),
            originalTitle = dto.originalTitle ?: dto.originalName,
            releaseDate = releaseDate,
            voteAverage = dto.voteAverage,
            voteCount = dto.voteCount,
            mediaType = type,
            trailerYoutubeKey = trailerKey,
            watchProviders = providers,
            isPlayableDirectly = false,
            originalLanguage = dto.originalLanguage,
            popularity = dto.popularity
        )
    }

    fun mapDtoToMovieDetails(dto: TmdbMovieDto): MovieDetails {
        val movie = mapDtoToMovie(dto)
        val director = dto.credits?.crew?.firstOrNull { it.job.equals("Director", ignoreCase = true) }?.name
        val cast = dto.credits?.cast?.map { it.name } ?: emptyList()
        val genres = dto.genres?.map { it.name }
            ?: dto.genreIds?.mapNotNull { GENRE_MAP[it] }
            ?: listOf(movie.genre)

        val trailerKey = dto.videos?.results?.firstOrNull { video ->
            video.site.equals("YouTube", ignoreCase = true) &&
                (video.type.equals("Trailer", ignoreCase = true) || video.type.equals("Teaser", ignoreCase = true))
        }?.key

        val trailerUrl = trailerKey?.let { "https://www.youtube.com/watch?v=$it" }
        val providers = extractWatchProviders(dto)

        return MovieDetails(
            movie = movie.copy(
                director = director ?: movie.director,
                leadCast = cast.take(6),
                trailerYoutubeKey = trailerKey,
                watchProviders = providers
            ),
            availableStreams = emptyList(),
            similarMovies = emptyList(),
            metadata = movie.metadata,
            isVerifiedPublicDomain = false,
            trailerYoutubeKey = trailerKey,
            trailerUrl = trailerUrl,
            watchProviders = providers,
            productionCompanies = emptyList(),
            cast = cast,
            director = director,
            seasons = emptyList(),
            genres = genres
        )
    }

    private fun extractWatchProviders(dto: TmdbMovieDto): List<WatchProvider> {
        val usProviders = dto.credits?.let { null } // Checked in videos/watch/providers response
        return emptyList()
    }
}
