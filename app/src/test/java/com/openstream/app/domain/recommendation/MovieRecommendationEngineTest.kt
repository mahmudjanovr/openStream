package com.openstream.app.domain.recommendation

import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieRecommendationEngineTest {

    private val catalog = listOf(
        Movie(
            id = "metropolis-1927",
            title = "Metropolis",
            director = "Fritz Lang",
            year = 1927,
            durationMinutes = 153,
            genre = "Silent Era",
            synopsis = "A futuristic urban dystopia.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR",
            country = "Germany"
        ),
        Movie(
            id = "m-1931",
            title = "M",
            director = "Fritz Lang",
            year = 1931,
            durationMinutes = 117,
            genre = "Film Noir",
            synopsis = "A city searches for an elusive criminal.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR",
            country = "Germany"
        ),
        Movie(
            id = "the-general-1926",
            title = "The General",
            director = "Buster Keaton",
            year = 1926,
            durationMinutes = 75,
            genre = "Comedy",
            synopsis = "Keaton locomotives during the Civil War.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "Passed",
            country = "United States"
        ),
        Movie(
            id = "charade-1963",
            title = "Charade",
            director = "Stanley Donen",
            year = 1963,
            durationMinutes = 113,
            genre = "Film Noir",
            synopsis = "Romance and suspense in Paris.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "PG",
            country = "United States"
        )
    )

    @Test
    fun generateRecommendations_withPreferredGenres_ranksMatchingMoviesHigher() {
        val noirProfile = UserProfile(
            id = "noir_fan",
            name = "Noir Fan",
            avatarId = "reel",
            preferredGenres = listOf("Film Noir")
        )

        val recommendations = MovieRecommendationEngine.generateRecommendations(
            allMovies = catalog,
            activeProfile = noirProfile,
            watchlistIds = emptySet(),
            favoriteIds = emptySet(),
            playbackHistory = emptyList()
        )

        assertTrue(recommendations.recommendedForYou.isNotEmpty())
        val topMatch = recommendations.recommendedForYou.first()
        // Film Noir movies (M or Charade) should be at the top
        assertTrue(topMatch.genre == "Film Noir")
    }

    @Test
    fun generateRecommendations_withPlaybackHistory_populatesBecauseYouWatched() {
        val profile = UserProfile.createDefault()
        val metropolis = catalog.first { it.id == "metropolis-1927" }

        val recommendations = MovieRecommendationEngine.generateRecommendations(
            allMovies = catalog,
            activeProfile = profile,
            watchlistIds = emptySet(),
            favoriteIds = emptySet(),
            playbackHistory = listOf(metropolis)
        )

        assertEquals("Metropolis", recommendations.becauseYouWatchedTitle)
        assertTrue(recommendations.becauseYouWatchedMovies.isNotEmpty())
        // "M" shares the same director (Fritz Lang) and era proximity
        assertTrue(recommendations.becauseYouWatchedMovies.any { it.director == "Fritz Lang" })
    }

    @Test
    fun generateRecommendations_exploreClassics_includesPre1930Films() {
        val profile = UserProfile.createDefault()

        val recommendations = MovieRecommendationEngine.generateRecommendations(
            allMovies = catalog,
            activeProfile = profile,
            watchlistIds = emptySet(),
            favoriteIds = emptySet(),
            playbackHistory = emptyList()
        )

        assertTrue(recommendations.exploreClassics.isNotEmpty())
        assertTrue(recommendations.exploreClassics.all { it.year <= 1930 || it.genre == "Silent Era" })
    }
}
