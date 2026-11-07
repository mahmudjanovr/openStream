package com.openstream.app.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ArchiveSearchResponseDto(
    @SerializedName("response") val response: ArchiveSearchBodyDto? = null
)

data class ArchiveSearchBodyDto(
    @SerializedName("numFound") val numFound: Long? = 0,
    @SerializedName("start") val start: Long? = 0,
    @SerializedName("docs") val docs: List<ArchiveDocDto>? = emptyList()
)

data class ArchiveDocDto(
    @SerializedName("identifier") val identifier: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: JsonElement? = null,
    @SerializedName("year") val year: JsonElement? = null,
    @SerializedName("creator") val creator: JsonElement? = null,
    @SerializedName("subject") val subject: JsonElement? = null,
    @SerializedName("licenseurl") val licenseurl: String? = null,
    @SerializedName("rights") val rights: String? = null,
    @SerializedName("downloads") val downloads: Long? = 0,
    @SerializedName("publicdate") val publicdate: String? = null
)

data class ArchiveMetadataResponseDto(
    @SerializedName("server") val server: String? = null,
    @SerializedName("dir") val dir: String? = null,
    @SerializedName("metadata") val metadata: ArchiveItemMetadataDto? = null,
    @SerializedName("files") val files: List<ArchiveFileDto>? = emptyList()
)

data class ArchiveFileDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("size") val size: String? = null,
    @SerializedName("length") val length: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("bitrate") val bitrate: String? = null
)

data class ArchiveItemMetadataDto(
    @SerializedName("identifier") val identifier: String? = null,
    @SerializedName("title") val title: JsonElement? = null,
    @SerializedName("description") val description: JsonElement? = null,
    @SerializedName("creator") val creator: JsonElement? = null,
    @SerializedName("date") val date: JsonElement? = null,
    @SerializedName("year") val year: JsonElement? = null,
    @SerializedName("licenseurl") val licenseurl: String? = null,
    @SerializedName("rights") val rights: String? = null,
    @SerializedName("collection") val collection: JsonElement? = null
)
