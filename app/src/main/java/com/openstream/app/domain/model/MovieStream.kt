package com.openstream.app.domain.model

data class MovieStream(
    val url: String,
    val format: String = "MP4",
    val quality: String = "720p",
    val sizeBytes: Long? = null,
    val durationSeconds: Long? = null,
    val bitrate: String? = null
)
