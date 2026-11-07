package com.openstream.app.feature.update

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.openstream.app.R
import com.openstream.app.core.designsystem.component.OpenStreamBadge
import com.openstream.app.core.designsystem.component.OpenStreamBadgeVariant
import com.openstream.app.core.designsystem.component.OpenStreamButton
import com.openstream.app.core.designsystem.component.OpenStreamButtonVariant
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import com.openstream.app.domain.model.UpdateDownloadState
import com.openstream.app.domain.model.UpdateManifest
import java.io.File

/**
 * Premium in-app OTT update dialog adhering strictly to openStream design guidelines.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(
    manifest: UpdateManifest,
    isForced: Boolean,
    downloadState: UpdateDownloadState,
    onStartDownload: () -> Unit,
    onInstallApk: (File) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDownloading = downloadState is UpdateDownloadState.Downloading ||
            downloadState is UpdateDownloadState.VerifyingIntegrity

    BasicAlertDialog(
        onDismissRequest = {
            if (!isForced && !isDownloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isForced && !isDownloading,
            dismissOnClickOutside = !isForced && !isDownloading
        ),
        modifier = modifier
    ) {
        val shape = OpenStreamTheme.shapes.large
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(OpenStreamTheme.colors.surfaceElevated)
                .border(
                    width = 1.dp,
                    color = OpenStreamTheme.colors.accent.copy(alpha = 0.4f),
                    shape = shape
                )
                .padding(OpenStreamTheme.spacing.large)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Branding Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(OpenStreamTheme.colors.accent.copy(alpha = 0.15f))
                        .border(1.dp, OpenStreamTheme.colors.accent.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = stringResource(R.string.update_available_title),
                        tint = OpenStreamTheme.colors.accent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.medium))

                // Mandatory or Optional Badge
                OpenStreamBadge(
                    text = if (isForced) {
                        stringResource(R.string.update_forced_badge)
                    } else {
                        stringResource(R.string.update_optional_badge)
                    },
                    variant = if (isForced) OpenStreamBadgeVariant.Gold else OpenStreamBadgeVariant.Subtle
                )

                Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))

                // Title
                Text(
                    text = stringResource(R.string.update_available_title),
                    style = OpenStreamTheme.typography.titleLarge,
                    color = OpenStreamTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle / Version Target
                Text(
                    text = stringResource(R.string.update_available_subtitle, "v${manifest.versionName}"),
                    style = OpenStreamTheme.typography.bodyMedium,
                    color = OpenStreamTheme.colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))

                // Release Notes Section
                if (manifest.releaseNotes.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(OpenStreamTheme.shapes.medium)
                            .background(OpenStreamTheme.colors.surface)
                            .border(1.dp, OpenStreamTheme.colors.border, OpenStreamTheme.shapes.medium)
                            .padding(OpenStreamTheme.spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.update_release_notes_title).uppercase(),
                            style = OpenStreamTheme.typography.eyebrow,
                            color = OpenStreamTheme.colors.accent
                        )

                        manifest.releaseNotes.forEach { note ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(OpenStreamTheme.colors.accent)
                                )
                                Text(
                                    text = note,
                                    style = OpenStreamTheme.typography.bodySmall,
                                    color = OpenStreamTheme.colors.textPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.large))
                }

                // Download / Verification / State Section
                when (downloadState) {
                    is UpdateDownloadState.Idle -> {
                        // Action buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.small)
                        ) {
                            OpenStreamButton(
                                text = stringResource(R.string.update_action_update),
                                leadingIcon = Icons.Default.Download,
                                onClick = onStartDownload,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (!isForced) {
                                OpenStreamButton(
                                    text = stringResource(R.string.update_action_later),
                                    onClick = onDismiss,
                                    variant = OpenStreamButtonVariant.Text,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    is UpdateDownloadState.Downloading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { downloadState.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                                color = OpenStreamTheme.colors.accent,
                                trackColor = OpenStreamTheme.colors.surface
                            )

                            Spacer(modifier = Modifier.height(OpenStreamTheme.spacing.small))

                            val progressText = if (downloadState.totalBytes > 0) {
                                val currentMb = downloadState.bytesDownloaded / (1024f * 1024f)
                                val totalMb = downloadState.totalBytes / (1024f * 1024f)
                                stringResource(
                                    R.string.update_download_progress,
                                    currentMb,
                                    totalMb,
                                    downloadState.progressPercent
                                )
                            } else {
                                stringResource(R.string.update_status_downloading, downloadState.progressPercent)
                            }

                            Text(
                                text = progressText,
                                style = OpenStreamTheme.typography.labelSmall,
                                color = OpenStreamTheme.colors.textSecondary
                            )
                        }
                    }

                    is UpdateDownloadState.VerifyingIntegrity -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = OpenStreamTheme.colors.accent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(OpenStreamTheme.spacing.small))
                            Text(
                                text = stringResource(R.string.update_status_verifying),
                                style = OpenStreamTheme.typography.bodySmall,
                                color = OpenStreamTheme.colors.textSecondary
                            )
                        }
                    }

                    is UpdateDownloadState.ReadyToInstall -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.medium)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = OpenStreamTheme.colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.update_status_ready),
                                    style = OpenStreamTheme.typography.bodySmall,
                                    color = OpenStreamTheme.colors.textPrimary
                                )
                            }

                            OpenStreamButton(
                                text = stringResource(R.string.update_action_install),
                                leadingIcon = Icons.Default.SystemUpdate,
                                onClick = { onInstallApk(downloadState.apkFile) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    is UpdateDownloadState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(OpenStreamTheme.spacing.small)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = OpenStreamTheme.colors.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = downloadState.message,
                                    style = OpenStreamTheme.typography.bodySmall,
                                    color = OpenStreamTheme.colors.error
                                )
                            }

                            if (downloadState.canRetry) {
                                OpenStreamButton(
                                    text = stringResource(R.string.update_action_retry),
                                    leadingIcon = Icons.Default.Refresh,
                                    onClick = onStartDownload,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (!isForced) {
                                OpenStreamButton(
                                    text = stringResource(R.string.update_action_later),
                                    onClick = onDismiss,
                                    variant = OpenStreamButtonVariant.Text,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
