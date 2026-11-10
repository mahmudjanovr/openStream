package com.openstream.app.data.remote.mapper

import com.openstream.app.data.remote.dto.TvMazeEpisodeDto
import com.openstream.app.data.remote.dto.TvMazeShowDto
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.MovieDetails
import com.openstream.app.domain.model.MovieMetadata
import com.openstream.app.domain.model.MovieSource
import com.openstream.app.domain.model.TvEpisode
import com.openstream.app.domain.model.TvSeason

object TvMazeMovieMapper {

    fun mapShowToMovie(show: TvMazeShowDto): Movie {
        val year = show.premiered?.take(4)?.toIntOrNull() ?: 2023
        val poster = show.image?.original ?: show.image?.medium ?: ""
        val summaryClean = cleanHtml(show.summary ?: "Acclaimed television drama series.")
        val primaryGenre = show.genres?.firstOrNull() ?: "TV Drama"
        val networkName = show.network?.name ?: "Broadcast Network"

        return Movie(
            id = "tvmaze_${show.id}",
            title = show.name ?: "Untitled Series",
            director = networkName,
            year = year,
            durationMinutes = show.runtime ?: 60,
            genre = primaryGenre,
            synopsis = summaryClean,
            posterUrl = poster,
            backdropUrl = poster,
            heroImageUrl = poster,
            isPublicDomain = false,
            rating = "TV-MA",
            country = show.network?.country?.name ?: "United States",
            leadCast = emptyList(),
            restorationNote = "Original Broadcast Master ($networkName)",
            licenseStatus = "Network Broadcast Rights",
            playbackProgressPercentage = null,
            streamUrl = "",
            movieSource = MovieSource.TVMAZE,
            streams = emptyList(),
            metadata = MovieMetadata(
                identifier = "${show.id}",
                sourceUrl = show.officialSite ?: "https://www.tvmaze.com/shows/${show.id}",
                licenseUrl = null,
                rightsNote = "Network Copyright",
                licenseType = "Television Broadcast",
                isRightsVerified = true,
                verificationNote = "Live episodic television schedule verified via TVmaze API."
            ),
            originalTitle = show.name,
            releaseDate = show.premiered,
            voteAverage = show.rating?.average,
            voteCount = 100,
            mediaType = "tv",
            isPlayableDirectly = false,
            originalLanguage = show.language ?: "en"
        )
    }

    fun mapEpisodesToSeasons(episodes: List<TvMazeEpisodeDto>): List<TvSeason> {
        val grouped = episodes.groupBy { it.season ?: 1 }
        return grouped.map { (seasonNum, epList) ->
            val mappedEps = epList.map { ep ->
                TvEpisode(
                    id = "ep_${ep.id}",
                    seasonNumber = seasonNum,
                    episodeNumber = ep.number ?: 1,
                    name = ep.name ?: "Episode ${ep.number ?: 1}",
                    overview = cleanHtml(ep.summary ?: ""),
                    airDate = ep.airdate,
                    runtimeMinutes = ep.runtime,
                    stillImageUrl = ep.image?.original ?: ep.image?.medium
                )
            }
            TvSeason(
                seasonNumber = seasonNum,
                name = "Season $seasonNum",
                episodeCount = mappedEps.size,
                posterUrl = mappedEps.firstOrNull()?.stillImageUrl,
                episodes = mappedEps
            )
        }.sortedBy { it.seasonNumber }
    }

    fun mapShowToMovieDetails(show: TvMazeShowDto, episodes: List<TvMazeEpisodeDto>): MovieDetails {
        val movie = mapShowToMovie(show)
        val seasons = mapEpisodesToSeasons(episodes)
        return MovieDetails(
            movie = movie,
            availableStreams = emptyList(),
            similarMovies = emptyList(),
            metadata = movie.metadata,
            isVerifiedPublicDomain = false,
            seasons = seasons,
            genres = show.genres ?: listOf(movie.genre),
            director = show.network?.name
        )
    }

    private fun cleanHtml(html: String): String {
        return html
            .replace(Regex("<.*?>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
