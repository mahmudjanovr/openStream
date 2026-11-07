package com.openstream.app.data.repository

import com.openstream.app.data.local.UserPreferencesRepository
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.repository.PlaybackHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackHistoryRepositoryImpl(
    private val userPreferencesRepository: UserPreferencesRepository? = null
) : PlaybackHistoryRepository {

    private val history = MutableStateFlow<List<Movie>>(emptyList())

    override fun getPlaybackHistory(): Flow<List<Movie>> = history.asStateFlow()

    override suspend fun recordPlayback(movie: Movie, positionMs: Long) {
        val updatedMovie = movie.copy(
            playbackProgressPercentage = if (movie.durationMinutes > 0) {
                val totalMs = movie.durationMinutes * 60 * 1000L
                (positionMs.toFloat() / totalMs).coerceIn(0f, 1f)
            } else null
        )
        val current = history.value.toMutableList()
        current.removeAll { it.id == movie.id }
        current.add(0, updatedMovie)
        history.value = current.take(20)

        userPreferencesRepository?.savePlaybackPosition(movie.id, positionMs)
    }

    override suspend fun clearHistory() {
        history.value = emptyList()
        userPreferencesRepository?.clearPlaybackPositions()
    }
}
