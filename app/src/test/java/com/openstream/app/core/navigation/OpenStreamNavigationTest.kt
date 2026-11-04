package com.openstream.app.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenStreamNavigationTest {

    @Test
    fun primaryDestinations_countIsFive() {
        assertEquals(5, TopLevelNavItems.size)
    }

    @Test
    fun primaryDestinations_routesAreUnique() {
        val routes = TopLevelNavItems.map { it.route }.toSet()
        assertEquals(5, routes.size)
        assertTrue(routes.contains(OpenStreamDestinations.HOME))
        assertTrue(routes.contains(OpenStreamDestinations.DISCOVER))
        assertTrue(routes.contains(OpenStreamDestinations.SEARCH))
        assertTrue(routes.contains(OpenStreamDestinations.LIBRARY))
        assertTrue(routes.contains(OpenStreamDestinations.SETTINGS))
    }

    @Test
    fun splashAndOnboardingDestinations_areDefined() {
        assertEquals("splash", OpenStreamDestinations.SPLASH)
        assertEquals("onboarding", OpenStreamDestinations.ONBOARDING)
        assertEquals("movie_detail/{movieId}", OpenStreamDestinations.MOVIE_DETAIL)
        assertEquals("movie_detail/movie-123", OpenStreamDestinations.movieDetailRoute("movie-123"))
    }
}
