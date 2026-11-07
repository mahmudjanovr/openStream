package com.openstream.app.core.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.openstream.app.domain.model.MovieStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SubtitleTrack(
    val id: String,
    val name: String,
    val language: String,
    val isSelected: Boolean = false
)

data class AudioTrack(
    val id: String,
    val name: String,
    val language: String,
    val channelCount: Int = 2,
    val isSelected: Boolean = false
)

data class QualityOption(
    val label: String,
    val url: String,
    val isSelected: Boolean = false
)

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val isBuffering: Boolean = false,
    val isEnded: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val availableSubtitles: List<SubtitleTrack> = emptyList(),
    val selectedSubtitle: SubtitleTrack? = null,
    val availableAudioTracks: List<AudioTrack> = emptyList(),
    val selectedAudioTrack: AudioTrack? = null,
    val availableQualities: List<QualityOption> = emptyList(),
    val selectedQuality: QualityOption? = null,
    val isPictureInPicture: Boolean = false,
    val errorMessage: String? = null
)

class OpenStreamPlayerManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private var exoPlayer: ExoPlayer? = null
    private var progressJob: Job? = null
    private var currentMediaUrl: String? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    @OptIn(UnstableApi::class)
    fun getOrCreatePlayer(): ExoPlayer {
        if (exoPlayer == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()

            val player = ExoPlayer.Builder(context)
                .setSeekBackIncrementMs(10_000)
                .setSeekForwardIncrementMs(10_000)
                .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
                .build()

            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            _playerState.value = _playerState.value.copy(
                                isBuffering = true,
                                errorMessage = null
                            )
                        }
                        Player.STATE_READY -> {
                            val duration = if (player.duration > 0) player.duration else 0L
                            _playerState.value = _playerState.value.copy(
                                isBuffering = false,
                                durationMs = duration,
                                currentPositionMs = player.currentPosition,
                                bufferedPositionMs = player.bufferedPosition,
                                isEnded = false,
                                errorMessage = null
                            )
                        }
                        Player.STATE_ENDED -> {
                            _playerState.value = _playerState.value.copy(
                                isPlaying = false,
                                isEnded = true,
                                isBuffering = false
                            )
                            stopProgressPolling()
                        }
                        Player.STATE_IDLE -> {
                            _playerState.value = _playerState.value.copy(isBuffering = false)
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.value = _playerState.value.copy(
                        isPlaying = isPlaying,
                        isEnded = false
                    )
                    if (isPlaying) {
                        startProgressPolling()
                    } else {
                        stopProgressPolling()
                    }
                }

                override fun onTracksChanged(tracks: Tracks) {
                    inspectTracks(tracks)
                }

                override fun onPlayerError(error: PlaybackException) {
                    _playerState.value = _playerState.value.copy(
                        isBuffering = false,
                        isPlaying = false,
                        errorMessage = error.localizedMessage ?: "Playback error occurred. Tap to retry."
                    )
                    stopProgressPolling()
                }
            })

            exoPlayer = player
        }
        return exoPlayer!!
    }

    fun prepareAndPlay(url: String, startPositionMs: Long = 0L) {
        val player = getOrCreatePlayer()
        currentMediaUrl = url
        _playerState.value = _playerState.value.copy(
            isBuffering = true,
            errorMessage = null,
            isEnded = false
        )

        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        if (startPositionMs > 0) {
            player.seekTo(startPositionMs)
        }
        player.prepare()
        player.playWhenReady = true
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
        updateProgressState()
    }

    fun togglePlayPause() {
        if (exoPlayer?.isPlaying == true) {
            pause()
        } else {
            play()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        val clamped = positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L))
        player.seekTo(clamped)
        _playerState.value = _playerState.value.copy(currentPositionMs = clamped)
    }

    fun seekBack(deltaMs: Long = 10_000L) {
        val current = exoPlayer?.currentPosition ?: 0L
        seekTo((current - deltaMs).coerceAtLeast(0L))
    }

    fun seekForward(deltaMs: Long = 10_000L) {
        val player = exoPlayer ?: return
        val current = player.currentPosition
        val duration = player.duration.coerceAtLeast(0L)
        seekTo((current + deltaMs).coerceAtMost(duration))
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }

    fun selectSubtitleTrack(languageCode: String?) {
        val player = exoPlayer ?: return
        if (languageCode == null) {
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .build()
            _playerState.value = _playerState.value.copy(
                selectedSubtitle = null,
                availableSubtitles = _playerState.value.availableSubtitles.map { it.copy(isSelected = false) }
            )
        } else {
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .setPreferredTextLanguage(languageCode)
                .build()
            val match = _playerState.value.availableSubtitles.firstOrNull { it.language == languageCode }
            _playerState.value = _playerState.value.copy(
                selectedSubtitle = match,
                availableSubtitles = _playerState.value.availableSubtitles.map {
                    it.copy(isSelected = it.language == languageCode)
                }
            )
        }
    }

    fun selectAudioTrack(languageCode: String) {
        val player = exoPlayer ?: return
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .setPreferredAudioLanguage(languageCode)
            .build()
        val match = _playerState.value.availableAudioTracks.firstOrNull { it.language == languageCode }
        _playerState.value = _playerState.value.copy(
            selectedAudioTrack = match,
            availableAudioTracks = _playerState.value.availableAudioTracks.map {
                it.copy(isSelected = it.language == languageCode)
            }
        )
    }

    fun setupAvailableQualities(streams: List<MovieStream>, currentUrl: String) {
        if (streams.isEmpty()) return
        val options = streams.map { stream ->
            QualityOption(
                label = stream.quality,
                url = stream.url,
                isSelected = stream.url == currentUrl
            )
        }
        val currentSelected = options.firstOrNull { it.isSelected } ?: options.firstOrNull()
        _playerState.value = _playerState.value.copy(
            availableQualities = options,
            selectedQuality = currentSelected
        )
    }

    fun switchStreamQuality(option: QualityOption) {
        val player = exoPlayer ?: return
        if (option.url == currentMediaUrl) return

        val currentPos = player.currentPosition
        val isPlaying = player.isPlaying
        currentMediaUrl = option.url

        val mediaItem = MediaItem.fromUri(option.url)
        player.setMediaItem(mediaItem, currentPos)
        player.prepare()
        if (isPlaying) player.play()

        _playerState.value = _playerState.value.copy(
            selectedQuality = option,
            availableQualities = _playerState.value.availableQualities.map {
                it.copy(isSelected = it.url == option.url)
            }
        )
    }

    fun setPictureInPictureMode(isInPip: Boolean) {
        _playerState.value = _playerState.value.copy(isPictureInPicture = isInPip)
    }

    fun retry() {
        currentMediaUrl?.let { url ->
            val resumePos = _playerState.value.currentPositionMs
            prepareAndPlay(url, resumePos)
        }
    }

    fun release() {
        stopProgressPolling()
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        _playerState.value = PlayerState()
    }

    private fun inspectTracks(tracks: Tracks) {
        val textList = mutableListOf<SubtitleTrack>()
        val audioList = mutableListOf<AudioTrack>()

        for (group in tracks.groups) {
            when (group.type) {
                C.TRACK_TYPE_TEXT -> {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        val lang = format.language ?: "und"
                        val name = format.label ?: format.language?.uppercase() ?: "Subtitle Track ${i + 1}"
                        val isSelected = group.isTrackSelected(i)
                        textList.add(
                            SubtitleTrack(
                                id = "$lang-$i",
                                name = name,
                                language = lang,
                                isSelected = isSelected
                            )
                        )
                    }
                }
                C.TRACK_TYPE_AUDIO -> {
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        val lang = format.language ?: "und"
                        val channels = format.channelCount
                        val name = format.label ?: "${lang.uppercase()} (${if (channels == 1) "Mono" else "Stereo"})"
                        val isSelected = group.isTrackSelected(i)
                        audioList.add(
                            AudioTrack(
                                id = "$lang-$i",
                                name = name,
                                language = lang,
                                channelCount = channels,
                                isSelected = isSelected
                            )
                        )
                    }
                }
            }
        }

        _playerState.value = _playerState.value.copy(
            availableSubtitles = textList,
            selectedSubtitle = textList.firstOrNull { it.isSelected },
            availableAudioTracks = audioList,
            selectedAudioTrack = audioList.firstOrNull { it.isSelected }
        )
    }

    private fun startProgressPolling() {
        stopProgressPolling()
        progressJob = scope.launch {
            while (isActive) {
                updateProgressState()
                delay(300)
            }
        }
    }

    private fun stopProgressPolling() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updateProgressState() {
        val player = exoPlayer ?: return
        val duration = if (player.duration > 0) player.duration else _playerState.value.durationMs
        _playerState.value = _playerState.value.copy(
            currentPositionMs = player.currentPosition,
            durationMs = duration,
            bufferedPositionMs = player.bufferedPosition
        )
    }
}
