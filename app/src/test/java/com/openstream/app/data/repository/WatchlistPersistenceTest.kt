package com.openstream.app.data.repository

import com.openstream.app.fake.FakeArchiveApiService
import com.openstream.app.fake.TestUserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WatchlistPersistenceTest {

    private lateinit var preferencesRepository: TestUserPreferencesRepository
    private lateinit var libraryRepository: LibraryRepositoryImpl
    private lateinit var catalogRepository: MovieCatalogRepositoryImpl

    @Before
    fun setup() {
        preferencesRepository = TestUserPreferencesRepository()
        catalogRepository = MovieCatalogRepositoryImpl(preferencesRepository, FakeArchiveApiService())
        libraryRepository = LibraryRepositoryImpl(preferencesRepository, catalogRepository)
    }

    @Test
    fun watchlist_togglePersistsCorrectly() = runTest {
        val movie = catalogRepository.getMovieById("metropolis-1927").first()!!

        // Initially not in watchlist
        assertFalse(libraryRepository.isInWatchlist(movie.id).first())

        // Toggle on
        libraryRepository.toggleWatchlist(movie)
        assertTrue(libraryRepository.isInWatchlist(movie.id).first())
        assertTrue(preferencesRepository.watchlistMovieIds.first().contains(movie.id))

        // Toggle off
        libraryRepository.toggleWatchlist(movie)
        assertFalse(libraryRepository.isInWatchlist(movie.id).first())
        assertFalse(preferencesRepository.watchlistMovieIds.first().contains(movie.id))
    }

    @Test
    fun favorite_togglePersistsCorrectly() = runTest {
        val movie = catalogRepository.getMovieById("nosferatu-1922").first()!!

        // Initially not favorite
        assertFalse(libraryRepository.isFavorite(movie.id).first())

        // Toggle on
        libraryRepository.toggleFavorite(movie)
        assertTrue(libraryRepository.isFavorite(movie.id).first())
        assertTrue(preferencesRepository.favoriteMovieIds.first().contains(movie.id))

        // Toggle off
        libraryRepository.toggleFavorite(movie)
        assertFalse(libraryRepository.isFavorite(movie.id).first())
    }

    @Test
    fun playbackPosition_persistsAndRetrieves() = runTest {
        preferencesRepository.savePlaybackPosition("metropolis-1927", 45_000L)
        val positions = preferencesRepository.playbackPositions.first()
        assertEquals(45_000L, positions["metropolis-1927"])

        preferencesRepository.clearPlaybackPositions()
        val cleared = preferencesRepository.playbackPositions.first()
        assertTrue(cleared.isEmpty())
    }
}
