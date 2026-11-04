package com.openstream.app.data.repository

import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.repository.PlaybackHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackHistoryRepositoryImpl : PlaybackHistoryRepository {

    private val history = MutableStateFlow<List<Movie>>(emptyList())

    override fun getPlaybackHistory(): Flow<List<Movie>> = history.asStateFlow()

    override suspend fun recordPlayback(movie: Movie, positionMs: Long) {
        val current = history.value.toMutableList()
        current.removeAll { it.id == movie.id }
        current.add(0, movie)
        history.value = current.take(20)
    }

    override suspend fun clearHistory() {
        history.value = emptyList()
    }
}
