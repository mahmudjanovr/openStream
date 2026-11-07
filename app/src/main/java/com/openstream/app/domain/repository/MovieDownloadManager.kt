package com.openstream.app.domain.repository

import com.openstream.app.domain.model.DownloadedMovie
import com.openstream.app.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieDownloadManager {
    val downloads: Flow<List<DownloadedMovie>>

    fun getDownload(movieId: String): Flow<DownloadedMovie?>

    suspend fun startDownload(movie: Movie): Result<DownloadedMovie>

    suspend fun pauseDownload(movieId: String)

    suspend fun resumeDownload(movieId: String)

    suspend fun cancelDownload(movieId: String)

    suspend fun deleteDownload(movieId: String): Boolean

    fun isMovieDownloaded(movieId: String): Boolean

    fun getLocalPlaybackUri(movieId: String): String?
}
