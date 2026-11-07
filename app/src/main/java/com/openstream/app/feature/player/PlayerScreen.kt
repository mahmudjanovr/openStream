package com.openstream.app.feature.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.util.Rational
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import com.openstream.app.R
import com.openstream.app.core.common.UiState
import com.openstream.app.core.designsystem.component.OpenStreamBadge
import com.openstream.app.core.designsystem.component.OpenStreamBadgeVariant
import com.openstream.app.core.designsystem.component.OpenStreamButton
import com.openstream.app.core.designsystem.component.OpenStreamButtonVariant
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import com.openstream.app.core.player.AudioTrack
import com.openstream.app.core.player.QualityOption
import com.openstream.app.core.player.SubtitleTrack
import com.openstream.app.domain.model.Movie
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    var areControlsVisible by remember { mutableStateOf(true) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }

    // Dialog states for Subtitles, Audio, and Quality
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val focusRequester = remember { FocusRequester() }

    // Auto-hide controls timer
    LaunchedEffect(areControlsVisible, playerState.isPlaying, isScrubbing) {
        if (areControlsVisible && playerState.isPlaying && !isScrubbing) {
            delay(3500)
            areControlsVisible = false
        }
    }

    // Lifecycle observer to handle pause/resume and state persistence
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.onPausePlayback()
                }
                Lifecycle.Event.ON_STOP -> {
                    viewModel.saveCurrentProgress()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.saveCurrentProgress()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    fun handleBack() {
        viewModel.saveCurrentProgress()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onNavigateBack()
    }

    BackHandler {
        handleBack()
    }

    // Track Selection Dialogs
    if (showSubtitleDialog) {
        SubtitleSelectionDialog(
            available = playerState.availableSubtitles,
            selected = playerState.selectedSubtitle,
            onSelect = { lang ->
                viewModel.selectSubtitle(lang)
                showSubtitleDialog = false
            },
            onDismiss = { showSubtitleDialog = false }
        )
    }

    if (showAudioDialog) {
        AudioTrackSelectionDialog(
            available = playerState.availableAudioTracks,
            selected = playerState.selectedAudioTrack,
            onSelect = { lang ->
                viewModel.selectAudioTrack(lang)
                showAudioDialog = false
            },
            onDismiss = { showAudioDialog = false }
        )
    }

    if (showQualityDialog) {
        QualitySelectionDialog(
            available = playerState.availableQualities,
            selected = playerState.selectedQuality,
            onSelect = { opt ->
                viewModel.switchQuality(opt)
                showQualityDialog = false
            },
            onDismiss = { showQualityDialog = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            viewModel.playerManager.togglePlayPause()
                            areControlsVisible = true
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY -> {
                            viewModel.playerManager.play()
                            areControlsVisible = true
                            true
                        }
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            viewModel.playerManager.pause()
                            areControlsVisible = true
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_MEDIA_REWIND -> {
                            viewModel.playerManager.seekBack(10_000)
                            areControlsVisible = true
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                            viewModel.playerManager.seekForward(10_000)
                            areControlsVisible = true
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                areControlsVisible = !areControlsVisible
            }
    ) {
        // Video View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.playerManager.getOrCreatePlayer()
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    keepScreenOn = true

                    // High contrast cinematic subtitle styling
                    val captionStyle = CaptionStyleCompat(
                        Color.White.hashCode(),
                        Color(0xB3000000).hashCode(),
                        Color.Transparent.hashCode(),
                        CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                        Color.Black.hashCode(),
                        null
                    )
                    subtitleView?.setStyle(captionStyle)
                }
            },
            update = { playerView ->
                playerView.player = viewModel.playerManager.getOrCreatePlayer()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (playerState.isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = OpenStreamTheme.colors.accent,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.medium))
                    Text(
                        text = stringResource(R.string.player_buffering),
                        style = OpenStreamTheme.typography.bodyMedium,
                        color = OpenStreamTheme.colors.textPrimary
                    )
                }
            }
        }

        // Error Overlay
        playerState.errorMessage?.let { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(OpenStreamTheme.spacing.large),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium)
                ) {
                    Text(
                        text = stringResource(R.string.player_error_title),
                        style = OpenStreamTheme.typography.displayMedium,
                        color = OpenStreamTheme.colors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.player_error_desc),
                        style = OpenStreamTheme.typography.bodyMedium,
                        color = OpenStreamTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))
                    OpenStreamButton(
                        text = stringResource(R.string.player_retry),
                        onClick = { viewModel.retryPlayback() },
                        variant = OpenStreamButtonVariant.Primary
                    )
                }
            }
        }

        // Cinematic Controls Overlay (Hidden when in PiP mode)
        AnimatedVisibility(
            visible = areControlsVisible && !playerState.isPictureInPicture,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            val movie = (uiState as? UiState.Success<Movie>)?.data

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { handleBack() },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(OpenStreamTheme.colors.surfaceElevated.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                            tint = OpenStreamTheme.colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.medium))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = movie?.title ?: "",
                            style = OpenStreamTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OpenStreamTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.player_stream_info),
                            style = OpenStreamTheme.typography.labelSmall,
                            color = OpenStreamTheme.colors.accent
                        )
                    }

                    OpenStreamBadge(
                        text = stringResource(R.string.detail_badge_public_domain),
                        variant = OpenStreamBadgeVariant.Gold
                    )
                }

                // Center Action Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = {
                            viewModel.playerManager.seekBack(10_000)
                            areControlsVisible = true
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(OpenStreamTheme.colors.surfaceElevated.copy(alpha = 0.75f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = stringResource(R.string.player_rewind_10),
                            tint = OpenStreamTheme.colors.textPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // Play / Pause Toggle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(OpenStreamTheme.colors.accent)
                            .border(2.dp, OpenStreamTheme.colors.accent, CircleShape)
                            .clickable {
                                viewModel.playerManager.togglePlayPause()
                                areControlsVisible = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.content_desc_play),
                            tint = OpenStreamTheme.colors.background,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = {
                            viewModel.playerManager.seekForward(10_000)
                            areControlsVisible = true
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(OpenStreamTheme.colors.surfaceElevated.copy(alpha = 0.75f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = stringResource(R.string.player_forward_10),
                            tint = OpenStreamTheme.colors.textPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Bottom Progress Bar, Advanced Settings & Time
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    val duration = playerState.durationMs
                    val currentPos = if (isScrubbing) {
                        (scrubProgress * duration).toLong()
                    } else {
                        playerState.currentPositionMs
                    }

                    val progressFraction = if (duration > 0) {
                        (currentPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Slider(
                        value = if (isScrubbing) scrubProgress else progressFraction,
                        onValueChange = { newFraction ->
                            isScrubbing = true
                            scrubProgress = newFraction
                            areControlsVisible = true
                        },
                        onValueChangeFinished = {
                            val targetMs = (scrubProgress * duration).toLong()
                            viewModel.playerManager.seekTo(targetMs)
                            isScrubbing = false
                            areControlsVisible = true
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = OpenStreamTheme.colors.accent,
                            activeTrackColor = OpenStreamTheme.colors.accent,
                            inactiveTrackColor = OpenStreamTheme.colors.surfaceElevated.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPos),
                            style = OpenStreamTheme.typography.labelMedium,
                            color = OpenStreamTheme.colors.textPrimary
                        )

                        // Secondary control actions: Speed, Subtitle, Audio, Quality, PiP
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Speed Selector
                            TextButton(
                                onClick = {
                                    val nextSpeed = when (playerState.playbackSpeed) {
                                        1.0f -> 1.25f
                                        1.25f -> 1.5f
                                        1.5f -> 0.75f
                                        else -> 1.0f
                                    }
                                    viewModel.setPlaybackSpeed(nextSpeed)
                                }
                            ) {
                                Text(
                                    text = "${playerState.playbackSpeed}x",
                                    style = OpenStreamTheme.typography.labelSmall,
                                    color = OpenStreamTheme.colors.accent
                                )
                            }

                            // Subtitle selector (if tracks exist)
                            if (playerState.availableSubtitles.isNotEmpty()) {
                                IconButton(
                                    onClick = { showSubtitleDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ClosedCaption,
                                        contentDescription = stringResource(R.string.player_subtitles),
                                        tint = if (playerState.selectedSubtitle != null) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Audio track selector (if multiple audio tracks exist)
                            if (playerState.availableAudioTracks.size > 1) {
                                IconButton(
                                    onClick = { showAudioDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Audiotrack,
                                        contentDescription = stringResource(R.string.player_audio_tracks),
                                        tint = OpenStreamTheme.colors.textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Quality selector (if multiple stream variants exist)
                            if (playerState.availableQualities.size > 1) {
                                IconButton(
                                    onClick = { showQualityDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HighQuality,
                                        contentDescription = stringResource(R.string.player_quality),
                                        tint = OpenStreamTheme.colors.textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Picture-in-Picture Button
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                IconButton(
                                    onClick = {
                                        val pipParams = PictureInPictureParams.Builder()
                                            .setAspectRatio(Rational(16, 9))
                                            .build()
                                        activity?.enterPictureInPictureMode(pipParams)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureInPicture,
                                        contentDescription = stringResource(R.string.player_pip_mode),
                                        tint = OpenStreamTheme.colors.textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatTime(duration),
                                style = OpenStreamTheme.typography.labelMedium,
                                color = OpenStreamTheme.colors.textSecondary
                            )

                            Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.small))

                            IconButton(
                                onClick = {
                                    activity?.let { act ->
                                        act.requestedOrientation = if (isLandscape) {
                                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                        } else {
                                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                        }
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLandscape) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = stringResource(R.string.player_fullscreen),
                                    tint = OpenStreamTheme.colors.textPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtitleSelectionDialog(
    available: List<SubtitleTrack>,
    selected: SubtitleTrack?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.player_subtitles),
                style = OpenStreamTheme.typography.displayMedium,
                color = OpenStreamTheme.colors.textPrimary
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    TrackOptionRow(
                        title = stringResource(R.string.player_subtitles_off),
                        isSelected = selected == null,
                        onClick = { onSelect(null) }
                    )
                }
                items(available, key = { it.id }) { track ->
                    TrackOptionRow(
                        title = track.name,
                        isSelected = selected?.id == track.id,
                        onClick = { onSelect(track.language) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_close), color = OpenStreamTheme.colors.accent)
            }
        },
        containerColor = OpenStreamTheme.colors.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AudioTrackSelectionDialog(
    available: List<AudioTrack>,
    selected: AudioTrack?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.player_audio_tracks),
                style = OpenStreamTheme.typography.displayMedium,
                color = OpenStreamTheme.colors.textPrimary
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(available, key = { it.id }) { track ->
                    TrackOptionRow(
                        title = track.name,
                        isSelected = selected?.id == track.id,
                        onClick = { onSelect(track.language) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_close), color = OpenStreamTheme.colors.accent)
            }
        },
        containerColor = OpenStreamTheme.colors.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun QualitySelectionDialog(
    available: List<QualityOption>,
    selected: QualityOption?,
    onSelect: (QualityOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.player_quality),
                style = OpenStreamTheme.typography.displayMedium,
                color = OpenStreamTheme.colors.textPrimary
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(available, key = { it.url }) { option ->
                    TrackOptionRow(
                        title = option.label,
                        isSelected = selected?.url == option.url,
                        onClick = { onSelect(option) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_close), color = OpenStreamTheme.colors.accent)
            }
        },
        containerColor = OpenStreamTheme.colors.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun TrackOptionRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) OpenStreamTheme.colors.surfaceElevated else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = OpenStreamTheme.typography.bodyMedium,
            color = if (isSelected) OpenStreamTheme.colors.accent else OpenStreamTheme.colors.textPrimary
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = OpenStreamTheme.colors.accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
    }
}
