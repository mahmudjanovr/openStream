package com.openstream.app.data.repository

import com.openstream.app.domain.model.Genre
import com.openstream.app.fake.FakeArchiveApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MovieCatalogRepositoryTest {

    private lateinit var repository: MovieCatalogRepositoryImpl

    @Before
    fun setup() {
        repository = MovieCatalogRepositoryImpl(apiService = FakeArchiveApiService())
    }

    @Test
    fun getHeroMovie_returnsMetropolis() = runTest {
        val hero = repository.getHeroMovie().first()
        assertNotNull(hero)
        assertEquals("Metropolis", hero?.title)
        assertEquals(1927, hero?.year)
        assertTrue(hero?.isPublicDomain == true)
    }

    @Test
    fun getFeaturedCollections_returnsNonEmptyList() = runTest {
        val collections = repository.getFeaturedCollections().first()
        assertTrue(collections.isNotEmpty())
        assertTrue(collections.any { it.title.contains("Silent") })
    }

    @Test
    fun getMoviesByGenre_filtersProperly() = runTest {
        val comedyMovies = repository.getMoviesByGenre(Genre.COMEDY).first()
        assertTrue(comedyMovies.isNotEmpty())
        assertTrue(comedyMovies.all { it.genre == "Comedy" })

        val allMovies = repository.getMoviesByGenre(Genre.ALL).first()
        assertTrue(allMovies.size >= comedyMovies.size)
    }

    @Test
    fun searchMovies_findsByTitleAndDirector() = runTest {
        val titleResults = repository.searchMovies("Nosferatu").first()
        assertEquals(1, titleResults.size)
        assertEquals("Nosferatu", titleResults.first().title)

        val directorResults = repository.searchMovies("Keaton").first()
        assertTrue(directorResults.size >= 2)
        assertTrue(directorResults.any { it.title == "The General" })
        assertTrue(directorResults.any { it.title == "Sherlock Jr." })

        val emptyResults = repository.searchMovies("NonExistentMovie12345").first()
        assertTrue(emptyResults.isEmpty())
    }

    @Test
    fun recentSearches_addAndClear() = runTest {
        repository.addRecentSearch("Chaplin")
        val recentAfterAdd = repository.getRecentSearches().first()
        assertEquals("Chaplin", recentAfterAdd.first())

        repository.clearRecentSearches()
        val recentAfterClear = repository.getRecentSearches().first()
        assertTrue(recentAfterClear.isEmpty())
    }

    @Test
    fun getMovieById_returnsExpectedMovie() = runTest {
        val movie = repository.getMovieById("metropolis-1927").first()
        assertNotNull(movie)
        assertEquals("Metropolis", movie?.title)
        assertEquals("Fritz Lang", movie?.director)
    }

    @Test
    fun getMovieById_withInvalidId_returnsNull() = runTest {
        val movie = repository.getMovieById("non-existent-id").first()
        org.junit.Assert.assertNull(movie)
    }

    @Test
    fun getRelatedMovies_returnsMoviesExcludingSelf() = runTest {
        val related = repository.getRelatedMovies("metropolis-1927").first()
        assertTrue(related.isNotEmpty())
        assertTrue(related.none { it.id == "metropolis-1927" })
    }

    @Test
    fun getMovieDetails_returnsDetailedDataWithStreams() = runTest {
        val details = repository.getMovieDetails("metropolis-1927").first()
        assertNotNull(details)
        assertEquals("Metropolis", details?.movie?.title)
        assertTrue(details?.availableStreams?.isNotEmpty() == true)
        assertTrue(details?.availableStreams?.first()?.url?.endsWith(".mp4") == true)
        assertTrue(details?.isVerifiedPublicDomain == true)
    }

    @Test
    fun resolveStreamUrl_returnsPlayableMp4() = runTest {
        val movie = repository.getMovieById("his-girl-friday-1940").first()
        assertNotNull(movie)
        val streamUrl = repository.resolveStreamUrl(movie!!)
        assertTrue(streamUrl.isNotBlank())
        assertTrue(streamUrl.endsWith(".mp4"))
    }

    @Test
    fun getMovieById_carnivalOfSouls_verifiedPublicDomain() = runTest {
        val movie = repository.getMovieById("carnival-of-souls-1962").first()
        assertNotNull(movie)
        assertEquals("Carnival of Souls", movie?.title)
        assertEquals("Herk Harvey", movie?.director)
        assertTrue(movie?.isPublicDomain == true)
        assertTrue(movie?.metadata?.isRightsVerified == true)
    }

    @Test
    fun getMovieById_houseOnHauntedHill_verifiedPublicDomain() = runTest {
        val movie = repository.getMovieById("house-on-haunted-hill-1959").first()
        assertNotNull(movie)
        assertEquals("House on Haunted Hill", movie?.title)
        assertEquals("William Castle", movie?.director)
        assertTrue(movie?.leadCast?.contains("Vincent Price") == true)
        assertTrue(movie?.isPublicDomain == true)
    }

    @Test
    fun getMovieById_phantomOfTheOpera_verifiedPublicDomain() = runTest {
        val movie = repository.getMovieById("phantom-opera-1925").first()
        assertNotNull(movie)
        assertEquals("The Phantom of the Opera", movie?.title)
        assertEquals(1925, movie?.year)
        assertTrue(movie?.isPublicDomain == true)
    }

    @Test
    fun getTrendingMovies_returnsModernCinema() = runTest {
        val trending = repository.getTrendingMovies().first()
        assertTrue(trending.isNotEmpty())
        assertTrue(trending.any { it.year in 2023..2026 })
        assertTrue(trending.any { it.title.contains("Dune") || it.title.contains("Oppenheimer") })
    }

    @Test
    fun getNowPlaying_and_Upcoming_returnExpectedLists() = runTest {
        val nowPlaying = repository.getNowPlayingMovies().first()
        assertTrue(nowPlaying.isNotEmpty())

        val upcoming = repository.getUpcomingMovies().first()
        assertTrue(upcoming.isNotEmpty())
        assertTrue(upcoming.any { it.year in 2025..2026 })
    }

    @Test
    fun getPopularTvSeries_returnsTvShows() = runTest {
        val tvSeries = repository.getPopularTvSeries().first()
        assertTrue(tvSeries.isNotEmpty())
        assertTrue(tvSeries.all { it.mediaType == "tv" })
        assertTrue(tvSeries.any { it.title == "Shōgun" || it.title == "Severance" })
    }

    @Test
    fun getMovieById_modernMovie_hasTrailersAndWatchProviders() = runTest {
        val dune = repository.getMovieById("dune-part-two-2024").first()
        assertNotNull(dune)
        assertEquals("Dune: Part Two", dune?.title)
        assertEquals("Denis Villeneuve", dune?.director)
        assertEquals(2024, dune?.year)
        assertNotNull(dune?.trailerYoutubeKey)
        assertTrue(dune?.watchProviders?.isNotEmpty() == true)
    }

    @Test
    fun searchWithFilters_filtersByMediaTypeAndYear() = runTest {
        val tvOnly = repository.searchWithFilters("Shōgun", mediaType = "tv").first()
        assertTrue(tvOnly.isNotEmpty())
        assertEquals("Shōgun", tvOnly.first().title)
        assertEquals("tv", tvOnly.first().mediaType)

        val year2024 = repository.searchWithFilters("Dune", year = 2024).first()
        assertTrue(year2024.isNotEmpty())
        assertEquals(2024, year2024.first().year)
    }
}
