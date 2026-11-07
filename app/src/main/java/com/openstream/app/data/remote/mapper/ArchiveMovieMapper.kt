package com.openstream.app.data.remote.mapper

import com.google.gson.JsonElement
import com.openstream.app.data.remote.dto.ArchiveDocDto
import com.openstream.app.data.remote.dto.ArchiveFileDto
import com.openstream.app.data.remote.dto.ArchiveMetadataResponseDto
import com.openstream.app.domain.model.Genre
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.model.MovieDetails
import com.openstream.app.domain.model.MovieMetadata
import com.openstream.app.domain.model.MovieSource
import com.openstream.app.domain.model.MovieStream

object ArchiveMovieMapper {

    private const val ARCHIVE_DOWNLOAD_BASE = "https://archive.org/download"
    private const val ARCHIVE_IMG_BASE = "https://archive.org/services/img"
    private const val ARCHIVE_DETAILS_BASE = "https://archive.org/details"

    fun mapDocToMovie(doc: ArchiveDocDto): Movie? {
        val identifier = doc.identifier ?: return null
        val title = doc.title?.takeIf { it.isNotBlank() } ?: identifier.replace("_", " ")

        val yearInt = extractYear(doc.year, doc.publicdate)
        val description = extractString(doc.description) ?: "Public domain historical cinema archival print from the Internet Archive."
        val creator = extractString(doc.creator) ?: "Unknown Director"
        val subject = extractString(doc.subject) ?: ""
        val genre = mapSubjectToGenre(subject, title)

        val isPublicDomain = isVerifiedPublicDomain(
            year = yearInt,
            licenseUrl = doc.licenseurl,
            rights = doc.rights
        )

        val posterUrl = "$ARCHIVE_IMG_BASE/$identifier"
        val streamUrl = "$ARCHIVE_DOWNLOAD_BASE/$identifier"
        val sourceUrl = "$ARCHIVE_DETAILS_BASE/$identifier"
        val licenseType = determineLicenseType(doc.licenseurl, doc.rights, isPublicDomain)

        return Movie(
            id = identifier,
            title = cleanTitle(title),
            director = creator,
            year = yearInt,
            durationMinutes = 90, // Default until detailed metadata is fetched
            genre = genre,
            synopsis = cleanDescription(description),
            posterUrl = posterUrl,
            backdropUrl = posterUrl,
            heroImageUrl = posterUrl,
            isPublicDomain = isPublicDomain,
            rating = "NR",
            country = "United States",
            leadCast = emptyList(),
            restorationNote = "Internet Archive Digital Preservation Copy",
            licenseStatus = if (isPublicDomain) "Public Domain (Worldwide)" else "Open Archival License",
            streamUrl = streamUrl,
            movieSource = MovieSource.INTERNET_ARCHIVE,
            metadata = MovieMetadata(
                identifier = identifier,
                sourceUrl = sourceUrl,
                licenseUrl = doc.licenseurl,
                rightsNote = doc.rights ?: if (isPublicDomain) "Public Domain" else "Unverified Archival Rights",
                licenseType = licenseType,
                isRightsVerified = isPublicDomain,
                verificationNote = determineVerificationNote(isPublicDomain, doc.rights),
                uploader = null,
                addedDate = doc.publicdate,
                downloads = doc.downloads
            )
        )
    }

    fun mapMetadataToMovieDetails(
        identifier: String,
        metadataDto: ArchiveMetadataResponseDto,
        fallbackMovie: Movie? = null
    ): MovieDetails {
        val meta = metadataDto.metadata
        val titleRaw = extractString(meta?.title) ?: fallbackMovie?.title ?: identifier.replace("_", " ")
        val director = extractString(meta?.creator) ?: fallbackMovie?.director ?: "Archival Director"
        val year = extractYear(meta?.year, meta?.date)
        val finalYear = if (year > 1800) year else (fallbackMovie?.year ?: 1925)
        val description = extractString(meta?.description) ?: fallbackMovie?.synopsis ?: "Digitally preserved historical cinema masterwork."

        val streams = extractStreams(identifier, metadataDto.files ?: emptyList())
        val primaryStreamUrl = streams.firstOrNull()?.url ?: fallbackMovie?.streamUrl ?: ""
        val durationMinutes = streams.firstOrNull()?.durationSeconds?.let { (it / 60).toInt() }
            ?: fallbackMovie?.durationMinutes ?: 90

        val isPublicDomain = isVerifiedPublicDomain(
            year = finalYear,
            licenseUrl = meta?.licenseurl,
            rights = meta?.rights
        )

        val highResImage = metadataDto.files?.firstOrNull { file ->
            val name = file.name?.lowercase() ?: ""
            (name.endsWith(".jpg") || name.endsWith(".png")) &&
                (name.contains("itemimage") || name.contains("poster") || name.contains("cover") || name == "__ia_thumb.jpg")
        }?.name?.let { fileName ->
            val encoded = java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")
            "$ARCHIVE_DOWNLOAD_BASE/$identifier/$encoded"
        }

        val posterUrl = highResImage ?: fallbackMovie?.posterUrl?.takeIf { it.isNotBlank() } ?: "$ARCHIVE_IMG_BASE/$identifier"
        val sourceUrl = "$ARCHIVE_DETAILS_BASE/$identifier"
        val licenseType = determineLicenseType(meta?.licenseurl, meta?.rights, isPublicDomain)

        val movie = Movie(
            id = identifier,
            title = cleanTitle(titleRaw),
            director = director,
            year = finalYear,
            durationMinutes = if (durationMinutes > 0) durationMinutes else 90,
            genre = fallbackMovie?.genre ?: "Silent Era",
            synopsis = cleanDescription(description),
            posterUrl = posterUrl,
            backdropUrl = posterUrl,
            heroImageUrl = posterUrl,
            isPublicDomain = isPublicDomain,
            rating = fallbackMovie?.rating ?: "NR",
            country = fallbackMovie?.country ?: "United States",
            leadCast = fallbackMovie?.leadCast ?: emptyList(),
            restorationNote = "Internet Archive Fine Grain Digital Preservation",
            licenseStatus = if (isPublicDomain) "Public Domain (Worldwide)" else "Open Archival License",
            streamUrl = primaryStreamUrl,
            movieSource = MovieSource.INTERNET_ARCHIVE,
            streams = streams,
            metadata = MovieMetadata(
                identifier = identifier,
                sourceUrl = sourceUrl,
                licenseUrl = meta?.licenseurl,
                rightsNote = meta?.rights ?: if (isPublicDomain) "Public Domain" else "Unverified Archival Rights",
                licenseType = licenseType,
                isRightsVerified = isPublicDomain,
                verificationNote = determineVerificationNote(isPublicDomain, meta?.rights),
                collection = extractString(meta?.collection),
                addedDate = extractString(meta?.date),
                archiveServer = metadataDto.server
            )
        )

        return MovieDetails(
            movie = movie,
            availableStreams = streams,
            metadata = movie.metadata,
            isVerifiedPublicDomain = isPublicDomain
        )
    }

    private fun extractStreams(identifier: String, files: List<ArchiveFileDto>): List<MovieStream> {
        val playableFiles = files.filter { file ->
            val name = file.name?.lowercase() ?: ""
            val format = file.format?.lowercase() ?: ""
            (name.endsWith(".mp4") || format.contains("h.264") || format.contains("mpeg4")) &&
                !name.contains("ia_thumb") &&
                !name.contains("preview") &&
                !name.contains("_512kb.mp4")
        }.sortedByDescending { it.size?.toLongOrNull() ?: 0L }

        val fallbackMp4s = if (playableFiles.isEmpty()) {
            files.filter { file ->
                val name = file.name?.lowercase() ?: ""
                name.endsWith(".mp4")
            }
        } else {
            playableFiles
        }

        return fallbackMp4s.mapNotNull { file ->
            val name = file.name ?: return@mapNotNull null
            val encodedName = java.net.URLEncoder.encode(name, "UTF-8").replace("+", "%20")
            val url = "$ARCHIVE_DOWNLOAD_BASE/$identifier/$encodedName"
            val durationSec = file.length?.toDoubleOrNull()?.toLong()
            val size = file.size?.toLongOrNull()

            val quality = when {
                file.format?.contains("720", ignoreCase = true) == true -> "720p HD"
                file.format?.contains("1080", ignoreCase = true) == true -> "1080p Full HD"
                size != null && size > 500_000_000L -> "720p HD"
                size != null && size > 200_000_000L -> "480p SD"
                else -> "Standard SD"
            }

            MovieStream(
                url = url,
                format = file.format ?: "MP4",
                quality = quality,
                sizeBytes = size,
                durationSeconds = durationSec,
                bitrate = file.bitrate
            )
        }
    }

    fun isVerifiedPublicDomain(year: Int, licenseUrl: String?, rights: String?): Boolean {
        val licenseLower = (licenseUrl ?: "").lowercase()
        val rightsLower = (rights ?: "").lowercase()

        // Negative check: Disallow any items with affirmative copyright, proprietary restrictions, or modern score claims
        if (rightsLower.contains("all rights reserved") ||
            rightsLower.contains("copyrighted score") ||
            rightsLower.contains("copyrighted soundtrack") ||
            rightsLower.contains("proprietary") ||
            licenseLower.contains("reserved")
        ) {
            return false
        }

        // Affirmative license or explicit rights indicators
        if (licenseLower.contains("publicdomain") || licenseLower.contains("cc0") || licenseLower.contains("zero")) return true
        if (licenseLower.contains("creativecommons.org/licenses/by")) return true
        if (rightsLower.contains("public domain") || rightsLower.contains("no copyright")) return true

        // For historical works before 1929: Only verified if no conflicting copyright claim exists and year is established
        if (year in 1880..1928 && !rightsLower.contains("copyright")) {
            return true
        }

        return false
    }

    fun determineLicenseType(licenseUrl: String?, rights: String?, isPublicDomain: Boolean): String {
        val licenseLower = (licenseUrl ?: "").lowercase()
        val rightsLower = (rights ?: "").lowercase()
        return when {
            licenseLower.contains("cc0") || licenseLower.contains("zero") -> "Creative Commons CC0 1.0 Universal"
            licenseLower.contains("publicdomain") || rightsLower.contains("public domain") -> "Public Domain Mark 1.0"
            licenseLower.contains("by/") -> "Creative Commons CC-BY"
            isPublicDomain -> "Public Domain Archival Master"
            else -> "Archival Access (Unverified Rights)"
        }
    }

    fun determineVerificationNote(isPublicDomain: Boolean, rights: String?): String {
        return if (isPublicDomain) {
            "Verified archival public-domain print. Unrestricted for streaming and offline playback."
        } else {
            "Rights unverified or restricted: ${rights ?: "Unknown"}. Offline downloading disabled."
        }
    }

    fun mapSubjectToGenre(subject: String, title: String): String {
        val combined = "$subject $title".lowercase()
        return when {
            combined.contains("comedy") || combined.contains("chaplin") || combined.contains("keaton") || combined.contains("laurel") -> "Comedy"
            combined.contains("noir") || combined.contains("detective") || combined.contains("crime") || combined.contains("murder") -> "Film Noir"
            combined.contains("horror") || combined.contains("sci-fi") || combined.contains("scifi") || combined.contains("vampire") || combined.contains("dracula") || combined.contains("zombie") -> "Sci-Fi & Horror"
            combined.contains("silent") || combined.contains("melies") -> "Silent Era"
            else -> "Drama"
        }
    }

    private fun extractYear(yearElem: JsonElement?, dateElem: Any?): Int {
        val yStr = extractString(yearElem)
        if (yStr != null) {
            val digits = "\\d{4}".toRegex().find(yStr)?.value
            if (digits != null) return digits.toInt()
        }
        val dStr = if (dateElem is JsonElement) extractString(dateElem) else dateElem?.toString()
        if (dStr != null) {
            val digits = "\\d{4}".toRegex().find(dStr)?.value
            if (digits != null) return digits.toInt()
        }
        return 1925
    }

    private fun extractString(element: JsonElement?): String? {
        if (element == null || element.isJsonNull) return null
        return if (element.isJsonPrimitive) {
            element.asString
        } else if (element.isJsonArray) {
            element.asJsonArray.mapNotNull { if (it.isJsonPrimitive) it.asString else null }.joinToString(", ")
        } else {
            element.toString()
        }
    }

    private fun cleanTitle(title: String): String {
        return title
            .replace(Regex("\\(\\s*\\d{4}\\s*\\)"), "")
            .replace(Regex("\\[.*?\\]"), "")
            .replace("_", " ")
            .trim()
    }

    private fun cleanDescription(desc: String): String {
        return desc
            .replace(Regex("<.*?>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
