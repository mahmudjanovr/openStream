package com.openstream.app.domain.model

data class WatchProvider(
    val id: String,
    val name: String,
    val logoUrl: String,
    val type: String = "flatrate", // flatrate (subscription), rent, buy, free
    val watchUrl: String? = null
)
