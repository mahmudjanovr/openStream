package com.openstream.app.data.local

import com.openstream.app.domain.model.Movie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CatalogCacheManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val sampleMovies = listOf(
        Movie(
            id = "test-movie-1",
            title = "Test Metropolis",
            director = "Fritz Lang",
            year = 1927,
            durationMinutes = 153,
            genre = "Sci-Fi",
            synopsis = "Futuristic city.",
            posterUrl = "",
            backdropUrl = "",
            heroImageUrl = "",
            isPublicDomain = true,
            rating = "NR",
            country = "Germany"
        )
    )

    @Test
    fun cachedEnvelope_validWithinTtl() {
        val envelope = CachedCatalogEnvelope(
            cachedAtMs = System.currentTimeMillis() - 1000L,
            ttlMs = 60_000L, // 1 minute
            movies = sampleMovies
        )
        val elapsed = System.currentTimeMillis() - envelope.cachedAtMs
        assertTrue(elapsed < envelope.ttlMs)
        assertEquals(1, envelope.movies.size)
        assertEquals("Test Metropolis", envelope.movies.first().title)
    }

    @Test
    fun cachedEnvelope_expiredWhenTtlExceeded() {
        val expiredEnvelope = CachedCatalogEnvelope(
            cachedAtMs = System.currentTimeMillis() - 120_000L, // 2 minutes ago
            ttlMs = 60_000L, // 1 minute TTL
            movies = sampleMovies
        )
        val elapsed = System.currentTimeMillis() - expiredEnvelope.cachedAtMs
        assertTrue("Cache should be expired", elapsed >= expiredEnvelope.ttlMs)
    }
}
