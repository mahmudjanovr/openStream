package com.openstream.app.data.repository

import com.openstream.app.domain.model.Movie
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LibraryRepositoryTest {

    private lateinit var repository: LibraryRepositoryImpl

    private val sampleMovie = Movie(
        id = "test-movie-1",
        title = "Test Cinema Classic",
        director = "Director Name",
        year = 1930,
        durationMinutes = 90,
        genre = "Drama",
        synopsis = "A classic test movie synopsis."
    )

    @Before
    fun setup() {
        repository = LibraryRepositoryImpl()
    }

    @Test
    fun toggleWatchlist_addsAndRemovesCorrectly() = runTest {
        val initialWatchlist = repository.getWatchlist().first()
        assertTrue(initialWatchlist.isEmpty())

        // Add
        repository.toggleWatchlist(sampleMovie)
        val addedWatchlist = repository.getWatchlist().first()
        assertEquals(1, addedWatchlist.size)
        assertEquals("test-movie-1", addedWatchlist.first().id)
        assertTrue(repository.isInWatchlist("test-movie-1").first())

        // Remove
        repository.toggleWatchlist(sampleMovie)
        val removedWatchlist = repository.getWatchlist().first()
        assertTrue(removedWatchlist.isEmpty())
        assertFalse(repository.isInWatchlist("test-movie-1").first())
    }

    @Test
    fun toggleFavorite_addsAndRemovesCorrectly() = runTest {
        val initialFavorites = repository.getFavorites().first()
        assertTrue(initialFavorites.isEmpty())

        // Add
        repository.toggleFavorite(sampleMovie)
        val addedFavorites = repository.getFavorites().first()
        assertEquals(1, addedFavorites.size)
        assertEquals("test-movie-1", addedFavorites.first().id)
        assertTrue(repository.isFavorite("test-movie-1").first())

        // Remove
        repository.toggleFavorite(sampleMovie)
        val removedFavorites = repository.getFavorites().first()
        assertTrue(removedFavorites.isEmpty())
        assertFalse(repository.isFavorite("test-movie-1").first())
    }
}
