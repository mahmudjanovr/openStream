package com.openstream.app.data.repository

import com.openstream.app.fake.TestUserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProfileDataIsolationTest {

    private lateinit var userPrefs: TestUserPreferencesRepository
    private val profileA = "profile_cinephile"
    private val profileB = "profile_casual"

    @Before
    fun setup() {
        userPrefs = TestUserPreferencesRepository(initialActiveProfileId = profileA)
    }

    @Test
    fun watchlist_remainsIsolatedBetweenProfiles() = runTest {
        // Profile A adds Metropolis to watchlist
        userPrefs.toggleWatchlistId("metropolis-1927", profileId = profileA)

        val watchlistA = userPrefs.getWatchlistForProfile(profileA).first()
        assertTrue(watchlistA.contains("metropolis-1927"))

        // Profile B should NOT have Metropolis in its watchlist
        val watchlistB = userPrefs.getWatchlistForProfile(profileB).first()
        assertFalse(watchlistB.contains("metropolis-1927"))

        // Profile B adds Charade
        userPrefs.toggleWatchlistId("charade-1963", profileId = profileB)

        val updatedB = userPrefs.getWatchlistForProfile(profileB).first()
        assertTrue(updatedB.contains("charade-1963"))
        assertFalse(updatedB.contains("metropolis-1927"))

        // Profile A still only has Metropolis
        val currentA = userPrefs.getWatchlistForProfile(profileA).first()
        assertTrue(currentA.contains("metropolis-1927"))
        assertFalse(currentA.contains("charade-1963"))
    }

    @Test
    fun switchingActiveProfile_switchesEmittedWatchlistFlow() = runTest {
        userPrefs.toggleWatchlistId("movie-1", profileId = profileA)
        userPrefs.toggleWatchlistId("movie-2", profileId = profileB)

        // Active profile is A:
        userPrefs.setActiveProfile(profileA)
        val emittedA = userPrefs.watchlistMovieIds.first()
        assertTrue(emittedA.contains("movie-1"))
        assertFalse(emittedA.contains("movie-2"))

        // Switch active profile to B:
        userPrefs.setActiveProfile(profileB)
        val emittedB = userPrefs.watchlistMovieIds.first()
        assertTrue(emittedB.contains("movie-2"))
        assertFalse(emittedB.contains("movie-1"))
    }

    @Test
    fun playbackPositions_remainIsolatedBetweenProfiles() = runTest {
        userPrefs.savePlaybackPosition("metropolis-1927", 45_000L, profileId = profileA)
        userPrefs.savePlaybackPosition("metropolis-1927", 120_000L, profileId = profileB)

        val posA = userPrefs.getPlaybackPositionsForProfile(profileA).first()
        val posB = userPrefs.getPlaybackPositionsForProfile(profileB).first()

        assertEquals(45_000L, posA["metropolis-1927"])
        assertEquals(120_000L, posB["metropolis-1927"])
    }
}
