package com.openstream.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.openstream.app.domain.model.Movie
import java.io.File

data class CachedCatalogEnvelope(
    val cachedAtMs: Long = System.currentTimeMillis(),
    val ttlMs: Long = 24 * 60 * 60 * 1000L, // 24 hours default TTL
    val movies: List<Movie> = emptyList()
)

class CatalogCacheManager(
    private val context: Context,
    private val defaultTtlMs: Long = 24 * 60 * 60 * 1000L
) {
    private val gson = Gson()
    private val cacheFile: File by lazy {
        File(context.cacheDir, "openstream_catalog_cache.json")
    }

    @Synchronized
    fun saveCatalog(movies: List<Movie>) {
        try {
            val envelope = CachedCatalogEnvelope(
                cachedAtMs = System.currentTimeMillis(),
                ttlMs = defaultTtlMs,
                movies = movies
            )
            cacheFile.writeText(gson.toJson(envelope))
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun getCachedCatalog(): List<Movie>? {
        return try {
            if (!cacheFile.exists()) return null
            val json = cacheFile.readText()
            val envelope = gson.fromJson(json, CachedCatalogEnvelope::class.java) ?: return null
            envelope.movies.takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    fun isCacheValid(): Boolean {
        return try {
            if (!cacheFile.exists()) return false
            val json = cacheFile.readText()
            val envelope = gson.fromJson(json, CachedCatalogEnvelope::class.java) ?: return false
            val elapsed = System.currentTimeMillis() - envelope.cachedAtMs
            elapsed < envelope.ttlMs
        } catch (_: Exception) {
            false
        }
    }

    @Synchronized
    fun invalidateCache() {
        try {
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
        } catch (_: Exception) {
        }
    }

    @Synchronized
    fun pruneOldCache(maxAgeMs: Long = 7 * 24 * 60 * 60 * 1000L) {
        try {
            if (cacheFile.exists()) {
                val age = System.currentTimeMillis() - cacheFile.lastModified()
                if (age > maxAgeMs) {
                    cacheFile.delete()
                }
            }
        } catch (_: Exception) {
        }
    }
}
