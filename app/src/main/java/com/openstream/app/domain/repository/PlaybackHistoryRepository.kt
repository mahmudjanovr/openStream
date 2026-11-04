package com.openstream.app.domain.repository

import com.openstream.app.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface PlaybackHistoryRepository {
    fun getPlaybackHistory(): Flow<List<Movie>>
    suspend fun recordPlayback(movie: Movie, positionMs: Long)
    suspend fun clearHistory()
}
