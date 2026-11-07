package com.openstream.app.domain.model

import com.google.gson.annotations.SerializedName
import java.io.File

/**
 * Manifest data model describing a remote openStream application release.
 */
data class UpdateManifest(
    @SerializedName("versionCode") val versionCode: Int,
    @SerializedName("versionName") val versionName: String,
    @SerializedName("apkUrl") val apkUrl: String,
    @SerializedName("releaseNotes") val releaseNotes: List<String> = emptyList(),
    @SerializedName("forceUpdate") val forceUpdate: Boolean = false,
    @SerializedName("minimumSupportedVersion") val minimumSupportedVersion: Int = 1,
    @SerializedName("sha256") val sha256: String? = null
)

/**
 * Result of checking remote version against installed version.
 */
sealed interface UpdateCheckResult {
    data object NoUpdateAvailable : UpdateCheckResult
    data class UpdateAvailable(
        val manifest: UpdateManifest,
        val isForced: Boolean
    ) : UpdateCheckResult
}

/**
 * State representing APK download, integrity verification, and install readiness.
 */
sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState

    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val progressPercent: Int
    ) : UpdateDownloadState

    data object VerifyingIntegrity : UpdateDownloadState

    data class ReadyToInstall(
        val apkFile: File
    ) : UpdateDownloadState

    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : UpdateDownloadState
}
