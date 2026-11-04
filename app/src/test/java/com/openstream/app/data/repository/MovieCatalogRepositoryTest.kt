package com.openstream.app.data.repository

import com.openstream.app.domain.model.Genre
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
        repository = MovieCatalogRepositoryImpl()
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
}
