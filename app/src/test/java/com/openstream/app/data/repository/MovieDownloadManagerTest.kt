package com.openstream.app.data.repository

import com.openstream.app.domain.model.DownloadStatus
import com.openstream.app.domain.model.DownloadedMovie
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.MovieMetadata
import com.openstream.app.domain.model.MovieSource
import com.openstream.app.domain.model.MovieStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieDownloadManagerTest {

    private val publicDomainMovie = Movie(
        id = "his_girl_friday_1940",
        title = "His Girl Friday",
        director = "Howard Hawks",
        year = 1940,
        durationMinutes = 92,
        genre = "Comedy",
        synopsis = "A classic screwball comedy.",
        posterUrl = "https://archive.org/services/img/his_girl_friday",
        backdropUrl = "https://archive.org/services/img/his_girl_friday",
        heroImageUrl = "https://archive.org/services/img/his_girl_friday",
        isPublicDomain = true,
        rating = "Passed",
        country = "United States",
        licenseStatus = "Public Domain (Entered PD due to non-renewal of copyright)",
        streamUrl = "https://archive.org/download/his_girl_friday/his_girl_friday.mp4",
        movieSource = MovieSource.INTERNET_ARCHIVE,
        streams = listOf(
            MovieStream(
                url = "https://archive.org/download/his_girl_friday/his_girl_friday.mp4",
                format = "MP4",
                quality = "720p HD",
                sizeBytes = 500_000_000L
            )
        ),
        metadata = MovieMetadata(
            identifier = "his_girl_friday",
            rightsNote = "Public Domain",
            licenseType = "Public Domain Mark 1.0",
            isRightsVerified = true
        )
    )

    private val copyrightedMovie = Movie(
        id = "modern_film_2024",
        title = "Modern Copyrighted Film",
        director = "Contemporary Director",
        year = 2024,
        durationMinutes = 110,
        genre = "Drama",
        synopsis = "Commercial movie.",
        posterUrl = "",
        backdropUrl = "",
        heroImageUrl = "",
        isPublicDomain = false,
        rating = "PG-13",
        country = "United States",
        licenseStatus = "All Rights Reserved (Commercial)",
        streamUrl = "https://example.com/commercial.mp4",
        movieSource = MovieSource.INTERNET_ARCHIVE
    )

    @Test
    fun isDownloadEligible_publicDomainMovie_returnsTrue() {
        assertTrue(DownloadedMovie.isDownloadEligible(publicDomainMovie))
    }

    @Test
    fun isDownloadEligible_copyrightedMovie_returnsFalse() {
        assertFalse(DownloadedMovie.isDownloadEligible(copyrightedMovie))
    }

    @Test
    fun isDownloadEligible_pre1929WithCopyrightedSoundtrack_returnsFalse() {
        val silentMovieWithCopyrightedScore = Movie(
            id = "nosferatu-restored-2022",
            title = "Nosferatu Restored",
            director = "F.W. Murnau",
            year = 1922,
            durationMinutes = 94,
            genre = "Horror",
            synopsis = "Vampire classic with new score.",
            isPublicDomain = true,
            streamUrl = "https://archive.org/download/nosferatu/nosferatu.mp4",
            restorationNote = "New 2022 copyrighted score by modern chamber orchestra",
            licenseStatus = "Public Domain Video / Copyrighted Score",
            metadata = MovieMetadata(
                identifier = "nosferatu_restored",
                rightsNote = "Copyrighted score by modern orchestra",
                isRightsVerified = false
            )
        )
        assertFalse(DownloadedMovie.isDownloadEligible(silentMovieWithCopyrightedScore))
    }

    @Test
    fun isDownloadEligible_unverifiedRights_returnsFalse() {
        val unverifiedMovie = Movie(
            id = "unknown_reel_1920",
            title = "Unknown Archival Reel",
            director = "Unknown",
            year = 1920,
            durationMinutes = 20,
            genre = "Archival",
            synopsis = "Unclassified archival footage.",
            isPublicDomain = true,
            streamUrl = "https://archive.org/download/unknown/unknown.mp4",
            licenseStatus = "Archival Access (Unverified)",
            metadata = MovieMetadata(
                identifier = "unknown_reel",
                rightsNote = "Unverified rights provenance",
                isRightsVerified = false
            )
        )
        assertFalse(DownloadedMovie.isDownloadEligible(unverifiedMovie))
    }

    @Test
    fun isDownloadEligible_pre1929WithoutVerifiedRights_returnsFalse() {
        val pre1929Unverified = Movie(
            id = "vintage-1925",
            title = "Vintage Film 1925",
            director = "Director",
            year = 1925,
            durationMinutes = 60,
            genre = "Drama",
            synopsis = "Vintage film without verified rights statement.",
            isPublicDomain = true,
            streamUrl = "https://example.com/vintage.mp4",
            licenseStatus = "Status Pending",
            metadata = MovieMetadata(
                identifier = "vintage-1925",
                isRightsVerified = false
            )
        )
        assertFalse(DownloadedMovie.isDownloadEligible(pre1929Unverified))
    }

    @Test
    fun downloadedMovie_stateTransitions_correctStatusProperties() {
        val pending = DownloadedMovie(
            movie = publicDomainMovie,
            localFilePath = "/storage/emulated/0/movies/his_girl_friday.mp4",
            status = DownloadStatus.PENDING
        )
        assertTrue(pending.isDownloading)
        assertFalse(pending.isCompleted)

        val downloading = pending.copy(
            status = DownloadStatus.DOWNLOADING,
            downloadedBytes = 250_000_000L,
            totalBytes = 500_000_000L,
            progressPercentage = 0.5f
        )
        assertTrue(downloading.isDownloading)
        assertEquals("238.4 MB", downloading.formattedDownloadedSize)
        assertEquals("476.8 MB", downloading.formattedTotalSize)

        val paused = downloading.copy(status = DownloadStatus.PAUSED)
        assertTrue(paused.isPaused)
        assertFalse(paused.isDownloading)

        val completed = downloading.copy(
            status = DownloadStatus.COMPLETED,
            downloadedBytes = 500_000_000L,
            progressPercentage = 1f
        )
        assertTrue(completed.isCompleted)
        assertFalse(completed.isDownloading)
    }

    @Test
    fun formatBytes_displaysAppropriateUnits() {
        assertEquals("0 MB", DownloadedMovie.formatBytes(0))
        assertEquals("50.0 MB", DownloadedMovie.formatBytes(50L * 1024 * 1024))
        assertEquals("1.5 GB", DownloadedMovie.formatBytes((1.5 * 1024 * 1024 * 1024).toLong()))
    }
}
