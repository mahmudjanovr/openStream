package com.openstream.app.data.repository

import com.openstream.app.domain.model.Movie
import com.openstream.app.fake.FakeArchiveApiService
import com.openstream.app.fake.TestUserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LibraryRepositoryTest {

    private lateinit var fallbackRepository: LibraryRepositoryImpl
    private lateinit var persistentRepository: LibraryRepositoryImpl
    private lateinit var testUserPrefs: TestUserPreferencesRepository
    private lateinit var catalogRepository: MovieCatalogRepositoryImpl

    private val sampleMovie = Movie(
        id = "metropolis-1927",
        title = "Metropolis",
        director = "Fritz Lang",
        year = 1927,
        durationMinutes = 153,
        genre = "Sci-Fi",
        synopsis = "In a futuristic city sharply divided between the working class and the city planners, the son of the city's mastermind falls in love with a working-class prophet."
    )

    @Before
    fun setup() {
        fallbackRepository = LibraryRepositoryImpl()
        testUserPrefs = TestUserPreferencesRepository()
        catalogRepository = MovieCatalogRepositoryImpl(testUserPrefs, FakeArchiveApiService())
        persistentRepository = LibraryRepositoryImpl(testUserPrefs, catalogRepository)
    }

    @Test
    fun toggleWatchlist_addsAndRemovesCorrectly_fallback() = runTest {
        val initialWatchlist = fallbackRepository.getWatchlist().first()
        assertTrue(initialWatchlist.isEmpty())

        // Add
        fallbackRepository.toggleWatchlist(sampleMovie)
        val addedWatchlist = fallbackRepository.getWatchlist().first()
        assertEquals(1, addedWatchlist.size)
        assertEquals("metropolis-1927", addedWatchlist.first().id)
        assertTrue(fallbackRepository.isInWatchlist("metropolis-1927").first())

        // Remove
        fallbackRepository.toggleWatchlist(sampleMovie)
        val removedWatchlist = fallbackRepository.getWatchlist().first()
        assertTrue(removedWatchlist.isEmpty())
        assertFalse(fallbackRepository.isInWatchlist("metropolis-1927").first())
    }

    @Test
    fun toggleFavorite_addsAndRemovesCorrectly_fallback() = runTest {
        val initialFavorites = fallbackRepository.getFavorites().first()
        assertTrue(initialFavorites.isEmpty())

        // Add
        fallbackRepository.toggleFavorite(sampleMovie)
        val addedFavorites = fallbackRepository.getFavorites().first()
        assertEquals(1, addedFavorites.size)
        assertEquals("metropolis-1927", addedFavorites.first().id)
        assertTrue(fallbackRepository.isFavorite("metropolis-1927").first())

        // Remove
        fallbackRepository.toggleFavorite(sampleMovie)
        val removedFavorites = fallbackRepository.getFavorites().first()
        assertTrue(removedFavorites.isEmpty())
        assertFalse(fallbackRepository.isFavorite("metropolis-1927").first())
    }

    @Test
    fun toggleWatchlist_persistsToUserPreferences() = runTest {
        val initialWatchlist = persistentRepository.getWatchlist().first()
        assertTrue(initialWatchlist.isEmpty())

        // Toggle on
        persistentRepository.toggleWatchlist(sampleMovie)
        val addedWatchlist = persistentRepository.getWatchlist().first()
        assertEquals(1, addedWatchlist.size)
        assertEquals("metropolis-1927", addedWatchlist.first().id)
        assertTrue(persistentRepository.isInWatchlist("metropolis-1927").first())
        assertTrue(testUserPrefs.watchlistMovieIds.first().contains("metropolis-1927"))

        // Toggle off
        persistentRepository.toggleWatchlist(sampleMovie)
        val emptyWatchlist = persistentRepository.getWatchlist().first()
        assertTrue(emptyWatchlist.isEmpty())
        assertFalse(persistentRepository.isInWatchlist("metropolis-1927").first())
    }

    @Test
    fun toggleFavorite_persistsToUserPreferences() = runTest {
        val initialFavorites = persistentRepository.getFavorites().first()
        assertTrue(initialFavorites.isEmpty())

        // Toggle on
        persistentRepository.toggleFavorite(sampleMovie)
        val addedFavorites = persistentRepository.getFavorites().first()
        assertEquals(1, addedFavorites.size)
        assertEquals("metropolis-1927", addedFavorites.first().id)
        assertTrue(persistentRepository.isFavorite("metropolis-1927").first())
        assertTrue(testUserPrefs.favoriteMovieIds.first().contains("metropolis-1927"))

        // Toggle off
        persistentRepository.toggleFavorite(sampleMovie)
        val emptyFavorites = persistentRepository.getFavorites().first()
        assertTrue(emptyFavorites.isEmpty())
        assertFalse(persistentRepository.isFavorite("metropolis-1927").first())
    }
}
