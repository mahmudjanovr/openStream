package com.openstream.app.fake

import com.openstream.app.data.remote.api.ArchiveApiService
import com.openstream.app.data.remote.dto.ArchiveDocDto
import com.openstream.app.data.remote.dto.ArchiveFileDto
import com.openstream.app.data.remote.dto.ArchiveItemMetadataDto
import com.openstream.app.data.remote.dto.ArchiveMetadataResponseDto
import com.openstream.app.data.remote.dto.ArchiveSearchBodyDto
import com.openstream.app.data.remote.dto.ArchiveSearchResponseDto

class FakeArchiveApiService : ArchiveApiService {

    var shouldThrowError = false

    override suspend fun searchPublicDomain(
        query: String,
        fields: List<String>,
        sort: List<String>,
        rows: Int,
        page: Int,
        output: String
    ): ArchiveSearchResponseDto {
        if (shouldThrowError) throw RuntimeException("Simulated network failure")
        val docs = if (query.contains("Chaplin", ignoreCase = true)) {
            listOf(
                ArchiveDocDto(
                    identifier = "charlie_chaplin_film_fest",
                    title = "Charlie Chaplin Festival",
                    downloads = 1500000L
                )
            )
        } else {
            emptyList()
        }

        return ArchiveSearchResponseDto(
            response = ArchiveSearchBodyDto(
                numFound = docs.size.toLong(),
                docs = docs
            )
        )
    }

    override suspend fun getMetadata(identifier: String): ArchiveMetadataResponseDto {
        if (shouldThrowError || identifier.contains("invalid", ignoreCase = true) || identifier.contains("non-existent", ignoreCase = true)) {
            throw NoSuchElementException("Item $identifier not found")
        }
        return ArchiveMetadataResponseDto(
            server = "ia600300.us.archive.org",
            dir = "/0/items/$identifier",
            metadata = ArchiveItemMetadataDto(
                identifier = identifier,
                rights = "Public Domain"
            ),
            files = listOf(
                ArchiveFileDto(
                    name = "$identifier.mp4",
                    format = "h.264",
                    size = "480000000",
                    length = "4600"
                )
            )
        )
    }
}
