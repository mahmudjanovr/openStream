package com.openstream.app.domain.recommendation

import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.UserProfile
import kotlin.math.abs

data class RecommendationRails(
    val recommendedForYou: List<Movie> = emptyList(),
    val becauseYouWatchedTitle: String? = null,
    val becauseYouWatchedMovies: List<Movie> = emptyList(),
    val exploreClassics: List<Movie> = emptyList(),
    val hiddenGems: List<Movie> = emptyList()
)

object MovieRecommendationEngine {

    fun generateRecommendations(
        allMovies: List<Movie>,
        activeProfile: UserProfile,
        watchlistIds: Set<String>,
        favoriteIds: Set<String>,
        playbackHistory: List<Movie>
    ): RecommendationRails {
        if (allMovies.isEmpty()) return RecommendationRails()

        val watchedIds = playbackHistory.map { it.id }.toSet()
        val highAffinityIds = (watchlistIds + favoriteIds + watchedIds).toSet()

        // 1. Recommended For You: Score based on profile genre preferences, favorite directors, and eras
        val preferredGenres = activeProfile.preferredGenres.toSet()
        val affinityDirectors = (playbackHistory + allMovies.filter { it.id in favoriteIds })
            .map { it.director }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()

        val scoredCandidates = allMovies.map { movie ->
            var score = 0
            // Boost preferred genres
            if (preferredGenres.any { movie.genre.contains(it, ignoreCase = true) }) {
                score += 4
            }
            // Boost favorite directors
            val directorCount = affinityDirectors[movie.director] ?: 0
            score += (directorCount * 3)

            // Favor high-quality public domain restorations
            if (movie.isPublicDomain) score += 2
            if (movie.streams.any { it.quality.contains("HD", ignoreCase = true) }) score += 2

            // Slightly penalize already watched
            if (movie.id in watchedIds) score -= 3

            movie to score
        }.sortedByDescending { it.second }
            .map { it.first }

        val recommendedForYou = scoredCandidates.take(8)

        // 2. Because You Watched {Last Movie}
        val lastWatched = playbackHistory.firstOrNull() ?: allMovies.firstOrNull { it.id in favoriteIds }
        val becauseYouWatchedTitle = lastWatched?.title
        val becauseYouWatchedMovies = if (lastWatched != null) {
            allMovies.filter { candidate ->
                candidate.id != lastWatched.id && (
                    candidate.genre == lastWatched.genre ||
                    candidate.director == lastWatched.director ||
                    abs(candidate.year - lastWatched.year) <= 8
                )
            }.take(6)
        } else {
            emptyList()
        }

        // 3. Explore Classics: Historical landmark films (Pre-1930 / Golden Age landmarks)
        val exploreClassics = allMovies.filter { it.year <= 1930 || it.genre == "Silent Era" }
            .sortedBy { it.year }
            .take(6)

        // 4. Hidden Gems: Rich feature-length titles (> 75 min) with high narrative depth
        val hiddenGems = allMovies.filter {
            it.durationMinutes >= 75 && it.id !in highAffinityIds
        }.shuffled().take(6)

        return RecommendationRails(
            recommendedForYou = recommendedForYou,
            becauseYouWatchedTitle = becauseYouWatchedTitle,
            becauseYouWatchedMovies = becauseYouWatchedMovies,
            exploreClassics = exploreClassics.ifEmpty { allMovies.take(6) },
            hiddenGems = hiddenGems.ifEmpty { allMovies.reversed().take(6) }
        )
    }
}
