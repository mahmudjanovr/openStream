package com.openstream.app.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.core.common.UiState
import com.openstream.app.core.player.OpenStreamPlayerManager
import com.openstream.app.core.player.PlayerState
import com.openstream.app.core.player.QualityOption
import com.openstream.app.data.local.UserPreferencesRepository
import com.openstream.app.domain.model.Movie
import com.openstream.app.domain.repository.MovieCatalogRepository
import com.openstream.app.domain.repository.PlaybackHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class PlayerViewModel(
    val movieId: String,
    private val catalogRepository: MovieCatalogRepository,
    private val playbackHistoryRepository: PlaybackHistoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    val playerManager: OpenStreamPlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<Movie>>(UiState.Loading)
    val uiState: StateFlow<UiState<Movie>> = _uiState.asStateFlow()

    val playerState: StateFlow<PlayerState> = playerManager.playerState

    private var currentMovie: Movie? = null

    init {
        loadMovieAndStartPlayback()
    }

    fun loadMovieAndStartPlayback() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val movie = catalogRepository.getMovieById(movieId).firstOrNull()

            if (movie != null) {
                currentMovie = movie
                _uiState.value = UiState.Success(movie)

                // Resolve stream URL (supports local offline playback seamlessly)
                val streamUrl = catalogRepository.resolveStreamUrl(movie)

                // Setup available qualities if stream variants exist
                playerManager.setupAvailableQualities(movie.streams, streamUrl)

                // Retrieve saved position if available
                val savedPositions = userPreferencesRepository.playbackPositions.firstOrNull() ?: emptyMap()
                val resumePositionMs = savedPositions[movieId] ?: 0L

                playerManager.prepareAndPlay(
                    url = streamUrl,
                    startPositionMs = resumePositionMs
                )
            } else {
                _uiState.value = UiState.Error("Movie not found or unavailable in the public archive.")
            }
        }
    }

    fun onPausePlayback() {
        playerManager.pause()
        saveCurrentProgress()
    }

    fun saveCurrentProgress() {
        val movie = currentMovie ?: return
        val currentPos = playerManager.playerState.value.currentPositionMs
        if (currentPos > 0) {
            viewModelScope.launch {
                playbackHistoryRepository.recordPlayback(movie, currentPos)
                userPreferencesRepository.savePlaybackPosition(movie.id, currentPos)
            }
        }
    }

    fun retryPlayback() {
        playerManager.retry()
    }

    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
    }

    fun selectSubtitle(languageCode: String?) {
        playerManager.selectSubtitleTrack(languageCode)
    }

    fun selectAudioTrack(languageCode: String) {
        playerManager.selectAudioTrack(languageCode)
    }

    fun switchQuality(option: QualityOption) {
        playerManager.switchStreamQuality(option)
    }

    fun setPictureInPictureMode(isInPip: Boolean) {
        playerManager.setPictureInPictureMode(isInPip)
    }

    override fun onCleared() {
        saveCurrentProgress()
        playerManager.release()
        super.onCleared()
    }

    companion object {
        fun provideFactory(
            movieId: String,
            catalogRepository: MovieCatalogRepository,
            playbackHistoryRepository: PlaybackHistoryRepository,
            userPreferencesRepository: UserPreferencesRepository,
            playerManager: OpenStreamPlayerManager
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlayerViewModel(
                        movieId,
                        catalogRepository,
                        playbackHistoryRepository,
                        userPreferencesRepository,
                        playerManager
                    ) as T
                }
            }
    }
}
