package com.openstream.app.domain.repository

import android.content.Intent
import com.openstream.app.domain.model.UpdateCheckResult
import com.openstream.app.domain.model.UpdateDownloadState
import com.openstream.app.domain.model.UpdateManifest
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Interface defining OTA (Over-The-Air) update checking, downloading, integrity verification,
 * and Android package installer triggering.
 */
interface AppUpdateManager {

    /**
     * Observable download, verification, and installation progress state.
     */
    val downloadState: StateFlow<UpdateDownloadState>

    /**
     * Checks remote server for available update.
     *
     * @param manifestUrl Optional manifest URL override (defaults to BuildConfig.UPDATE_MANIFEST_URL).
     * @param isManualCheck When true (e.g. from Settings), bypasses session dismissal filter.
     */
    suspend fun checkForUpdates(
        manifestUrl: String? = null,
        isManualCheck: Boolean = false
    ): Result<UpdateCheckResult>

    /**
     * Begins streaming APK download with progress updates, followed by SHA-256 verification.
     */
    suspend fun startDownload(manifest: UpdateManifest)

    /**
     * Dispatches official Android package installation intent via FileProvider.
     */
    fun installApk(apkFile: File): Result<Unit>

    /**
     * Dismisses an optional update for the current application session.
     */
    fun dismissUpdate(versionCode: Int)

    /**
     * Returns true if the user dismissed this update during the current session.
     */
    fun isUpdateDismissed(versionCode: Int): Boolean

    /**
     * Checks if REQUEST_INSTALL_PACKAGES permission is granted (Android 8.0+ / API 26+).
     */
    fun canRequestPackageInstalls(): Boolean

    /**
     * Creates an intent pointing to the unknown app sources system settings screen.
     */
    fun getManageUnknownAppSourcesIntent(): Intent

    /**
     * Resets the download state back to Idle.
     */
    fun resetDownloadState()
}
