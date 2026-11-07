package com.openstream.app.data.remote.mapper

import com.google.gson.JsonPrimitive
import com.openstream.app.data.remote.dto.ArchiveDocDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchiveMovieMapperTest {

    @Test
    fun isVerifiedPublicDomain_pre1929_isAlwaysPublicDomain() {
        assertTrue(ArchiveMovieMapper.isVerifiedPublicDomain(1927, null, null))
        assertTrue(ArchiveMovieMapper.isVerifiedPublicDomain(1902, null, null))
        assertTrue(ArchiveMovieMapper.isVerifiedPublicDomain(1922, null, null))
    }

    @Test
    fun isVerifiedPublicDomain_withPublicDomainLicenseUrl_isTrue() {
        assertTrue(ArchiveMovieMapper.isVerifiedPublicDomain(1963, "https://creativecommons.org/publicdomain/zero/1.0/", null))
        assertTrue(ArchiveMovieMapper.isVerifiedPublicDomain(1940, "http://creativecommons.org/licenses/publicdomain/", null))
    }

    @Test
    fun isVerifiedPublicDomain_withPublicDomainRightsString_isTrue() {
        assertTrue(ArchiveMovieMapper.isVerifiedPublicDomain(1968, null, "Public Domain (Defective Notice)"))
        assertTrue(ArchiveMovieMapper.isVerifiedPublicDomain(1954, null, "No copyright registered"))
    }

    @Test
    fun isVerifiedPublicDomain_modernFilmWithoutLicense_isFalse() {
        assertFalse(ArchiveMovieMapper.isVerifiedPublicDomain(2015, null, null))
    }

    @Test
    fun isVerifiedPublicDomain_pre1929WithCopyrightRestrictions_isFalse() {
        assertFalse(ArchiveMovieMapper.isVerifiedPublicDomain(1922, null, "All rights reserved. Copyrighted score 2021"))
        assertFalse(ArchiveMovieMapper.isVerifiedPublicDomain(1925, "https://example.com/reserved", null))
    }

    @Test
    fun mapSubjectToGenre_correctlyCategorizes() {
        assertEquals("Comedy", ArchiveMovieMapper.mapSubjectToGenre("comedy slapstick", "The General"))
        assertEquals("Film Noir", ArchiveMovieMapper.mapSubjectToGenre("crime detective noir", "Charade"))
        assertEquals("Sci-Fi & Horror", ArchiveMovieMapper.mapSubjectToGenre("horror vampire", "Nosferatu"))
        assertEquals("Silent Era", ArchiveMovieMapper.mapSubjectToGenre("silent", "A Trip to the Moon"))
        assertEquals("Drama", ArchiveMovieMapper.mapSubjectToGenre("historical drama", "Jungle Book"))
    }

    @Test
    fun mapDocToMovie_mapsFieldsCorrectly() {
        val doc = ArchiveDocDto(
            identifier = "test_film_1925",
            title = "Test Silent Film",
            description = JsonPrimitive("A restored archival comedy print"),
            year = JsonPrimitive("1925"),
            creator = JsonPrimitive("Director Name"),
            subject = JsonPrimitive("comedy, silent"),
            licenseurl = "https://creativecommons.org/publicdomain/zero/1.0/",
            rights = "Public domain",
            downloads = 5400L,
            publicdate = "2020-01-01"
        )

        val movie = ArchiveMovieMapper.mapDocToMovie(doc)
        assertNotNull(movie)
        assertEquals("test_film_1925", movie?.id)
        assertEquals("Test Silent Film", movie?.title)
        assertEquals("Director Name", movie?.director)
        assertEquals(1925, movie?.year)
        assertEquals("Comedy", movie?.genre)
        assertTrue(movie?.isPublicDomain == true)
        assertTrue(movie?.posterUrl?.contains("test_film_1925") == true)
        assertEquals("https://archive.org/details/test_film_1925", movie?.metadata?.sourceUrl)
        assertEquals("Creative Commons CC0 1.0 Universal", movie?.metadata?.licenseType)
        assertTrue(movie?.metadata?.isRightsVerified == true)
        assertNotNull(movie?.metadata?.verificationNote)
    }
}
