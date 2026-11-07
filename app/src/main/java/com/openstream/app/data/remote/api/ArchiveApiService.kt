package com.openstream.app.data.remote.api

import com.openstream.app.data.remote.dto.ArchiveMetadataResponseDto
import com.openstream.app.data.remote.dto.ArchiveSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ArchiveApiService {

    @GET("advancedsearch.php")
    suspend fun searchPublicDomain(
        @Query("q") query: String,
        @Query("fl[]") fields: List<String> = listOf(
            "identifier", "title", "description", "year", "creator", "subject", "licenseurl", "rights", "publicdate", "downloads"
        ),
        @Query("sort[]") sort: List<String> = listOf("downloads desc"),
        @Query("rows") rows: Int = 30,
        @Query("page") page: Int = 1,
        @Query("output") output: String = "json"
    ): ArchiveSearchResponseDto

    @GET("metadata/{identifier}")
    suspend fun getMetadata(
        @Path("identifier") identifier: String
    ): ArchiveMetadataResponseDto
}
