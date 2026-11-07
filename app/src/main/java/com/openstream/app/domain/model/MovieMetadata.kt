package com.openstream.app.domain.model

data class MovieMetadata(
    val identifier: String,
    val sourceUrl: String? = null,
    val licenseUrl: String? = null,
    val rightsNote: String? = null,
    val licenseType: String? = null,
    val isRightsVerified: Boolean = false,
    val verificationNote: String? = null,
    val collection: String? = null,
    val uploader: String? = null,
    val addedDate: String? = null,
    val downloads: Long? = null,
    val originalFormat: String? = null,
    val archiveServer: String? = null
)
