package com.openstream.app.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.openstream.app.BuildConfig
import com.openstream.app.domain.model.UpdateCheckResult
import com.openstream.app.domain.model.UpdateDownloadState
import com.openstream.app.domain.model.UpdateManifest
import com.openstream.app.domain.repository.AppUpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.Collections
import java.util.concurrent.TimeUnit

/**
 * Production-ready implementation of OTA (Over-The-Air) in-app updates.
 *
 * Responsibilities:
 * - Asynchronously fetches and parses remote JSON manifest over HTTPS.
 * - Compares remote versionCode against installed application versionCode.
 * - Enforces mandatory/forced updates when specified or when below minimumSupportedVersion.
 * - Remembers in-session dismissed updates to prevent annoying prompts.
 * - Streams APK download to an app-private cache with granular progress tracking.
 * - Computes and verifies SHA-256 cryptographic digest before installation.
 * - Interacts with Android FileProvider and Package Installer.
 * - Manages unknown app sources permission flow for Android 8.0+ (API 26+).
 */
class AppUpdateManagerImpl(
    private val context: Context? = null,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson(),
    private val installedVersionCodeProvider: () -> Int = {
        try {
            if (context != null) {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    pInfo.versionCode
                }
            } else {
                1
            }
        } catch (e: Exception) {
            1
        }
    },
    private val cacheDirProvider: () -> File = {
        context?.cacheDir ?: File(System.getProperty("java.io.tmpdir") ?: ".")
    },
    private val packageNameProvider: () -> String = {
        context?.packageName ?: "com.openstream.app"
    }
) : AppUpdateManager {

    private val _downloadState = MutableStateFlow<UpdateDownloadState>(UpdateDownloadState.Idle)
    override val downloadState: StateFlow<UpdateDownloadState> = _downloadState.asStateFlow()

    // Session-only cache of dismissed version codes
    private val dismissedVersionCodes = Collections.synchronizedSet(mutableSetOf<Int>())

    override suspend fun checkForUpdates(
        manifestUrl: String?,
        isManualCheck: Boolean
    ): Result<UpdateCheckResult> = withContext(Dispatchers.IO) {
        try {
            val url = manifestUrl ?: BuildConfig.UPDATE_MANIFEST_URL

            // Security constraint: enforce HTTPS protocol
            if (!url.startsWith("https://", ignoreCase = true)) {
                return@withContext Result.failure(
                    SecurityException("Manifest URL must use secure HTTPS: $url")
                )
            }

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "openStream-Android/${BuildConfig.VERSION_NAME}")
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IOException("Server returned HTTP error ${response.code}: ${response.message}")
                )
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrBlank()) {
                return@withContext Result.failure(
                    IOException("Remote update manifest response was empty")
                )
            }

            val manifest = try {
                gson.fromJson(responseBody, UpdateManifest::class.java)
            } catch (e: Exception) {
                return@withContext Result.failure(
                    IllegalArgumentException("Failed to parse update manifest JSON: ${e.message}", e)
                )
            }

            if (manifest == null || manifest.versionCode <= 0 || manifest.apkUrl.isBlank()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Invalid update manifest payload: missing required fields")
                )
            }

            // Security constraint: APK URL must also enforce HTTPS
            if (!manifest.apkUrl.startsWith("https://", ignoreCase = true)) {
                return@withContext Result.failure(
                    SecurityException("APK download URL must use secure HTTPS: ${manifest.apkUrl}")
                )
            }

            val installedCode = installedVersionCodeProvider()

            // Update only if remote version code is strictly greater than installed version code
            if (manifest.versionCode <= installedCode) {
                return@withContext Result.success(UpdateCheckResult.NoUpdateAvailable)
            }

            // Determine if update is mandatory (forced)
            val isForced = manifest.forceUpdate || (installedCode < manifest.minimumSupportedVersion)

            // Honor user dismissal during the current app session for optional updates
            if (!isForced && !isManualCheck && dismissedVersionCodes.contains(manifest.versionCode)) {
                return@withContext Result.success(UpdateCheckResult.NoUpdateAvailable)
            }

            Result.success(UpdateCheckResult.UpdateAvailable(manifest, isForced))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startDownload(manifest: UpdateManifest): Unit = withContext(Dispatchers.IO) {
        try {
            if (!manifest.apkUrl.startsWith("https://", ignoreCase = true)) {
                _downloadState.value = UpdateDownloadState.Error(
                    "APK download URL rejected: HTTPS is required",
                    canRetry = false
                )
                return@withContext
            }

            val updatesDir = File(cacheDirProvider(), "updates")
            if (!updatesDir.exists()) {
                updatesDir.mkdirs()
            }

            val finalFile = File(updatesDir, "openStream_${manifest.versionCode}.apk")
            val partFile = File(updatesDir, "openStream_${manifest.versionCode}.apk.part")

            // If an identical verified file already exists, reuse it immediately
            if (finalFile.exists() && finalFile.length() > 0) {
                if (verifySha256(finalFile, manifest.sha256)) {
                    _downloadState.value = UpdateDownloadState.ReadyToInstall(finalFile)
                    return@withContext
                } else {
                    finalFile.delete()
                }
            }

            if (partFile.exists()) {
                partFile.delete()
            }

            _downloadState.value = UpdateDownloadState.Downloading(
                bytesDownloaded = 0L,
                totalBytes = -1L,
                progressPercent = 0
            )

            val request = Request.Builder()
                .url(manifest.apkUrl)
                .header("User-Agent", "openStream-Android/${BuildConfig.VERSION_NAME}")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                _downloadState.value = UpdateDownloadState.Error(
                    "Failed to download APK: HTTP ${response.code} ${response.message}"
                )
                return@withContext
            }

            val body = response.body
            if (body == null) {
                _downloadState.value = UpdateDownloadState.Error("Empty download response from server")
                return@withContext
            }

            val totalBytes = body.contentLength()
            val digest = MessageDigest.getInstance("SHA-256")
            var bytesDownloaded = 0L
            var lastEmittedPercent = -1

            body.byteStream().use { input ->
                FileOutputStream(partFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        digest.update(buffer, 0, bytesRead)
                        bytesDownloaded += bytesRead

                        val percent = if (totalBytes > 0) {
                            ((bytesDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }

                        if (percent != lastEmittedPercent) {
                            lastEmittedPercent = percent
                            _downloadState.value = UpdateDownloadState.Downloading(
                                bytesDownloaded = bytesDownloaded,
                                totalBytes = totalBytes,
                                progressPercent = percent
                            )
                        }
                    }
                    output.flush()
                }
            }

            // Verification phase
            _downloadState.value = UpdateDownloadState.VerifyingIntegrity

            val calculatedHash = digest.digest().joinToString("") { "%02x".format(it) }

            if (!manifest.sha256.isNullOrBlank()) {
                val expectedHash = manifest.sha256.trim()
                if (!calculatedHash.equals(expectedHash, ignoreCase = true)) {
                    if (partFile.exists()) partFile.delete()
                    _downloadState.value = UpdateDownloadState.Error(
                        "Integrity verification failed. Computed SHA-256 does not match server signature.",
                        canRetry = true
                    )
                    return@withContext
                }
            }

            // Rename part file to completed APK
            if (partFile.renameTo(finalFile)) {
                _downloadState.value = UpdateDownloadState.ReadyToInstall(finalFile)
            } else {
                // Fallback copy if rename fails across file system boundaries
                partFile.copyTo(finalFile, overwrite = true)
                partFile.delete()
                _downloadState.value = UpdateDownloadState.ReadyToInstall(finalFile)
            }
        } catch (e: Exception) {
            _downloadState.value = UpdateDownloadState.Error(
                e.localizedMessage ?: "An error occurred while downloading update"
            )
        }
    }

    override fun installApk(apkFile: File): Result<Unit> {
        val nonNullContext = context ?: return Result.failure(
            IllegalStateException("Android Context is required for package installation")
        )
        return try {
            if (!apkFile.exists() || apkFile.length() == 0L) {
                return Result.failure(IOException("Target APK file does not exist or is empty"))
            }

            if (!canRequestPackageInstalls()) {
                val settingsIntent = getManageUnknownAppSourcesIntent()
                nonNullContext.startActivity(settingsIntent)
                return Result.failure(
                    SecurityException("Permission to install unknown apps is not granted. Directed to settings.")
                )
            }

            val authority = "${packageNameProvider()}.fileprovider"
            val contentUri = FileProvider.getUriForFile(nonNullContext, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            nonNullContext.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun dismissUpdate(versionCode: Int) {
        dismissedVersionCodes.add(versionCode)
    }

    override fun isUpdateDismissed(versionCode: Int): Boolean {
        return dismissedVersionCodes.contains(versionCode)
    }

    override fun canRequestPackageInstalls(): Boolean {
        val nonNullContext = context ?: return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                nonNullContext.packageManager.canRequestPackageInstalls()
            } catch (e: Exception) {
                false
            }
        } else {
            true
        }
    }

    override fun getManageUnknownAppSourcesIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${packageNameProvider()}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    override fun resetDownloadState() {
        _downloadState.value = UpdateDownloadState.Idle
    }

    private fun verifySha256(file: File, expectedHash: String?): Boolean {
        if (expectedHash.isNullOrBlank()) return true
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            val calculated = digest.digest().joinToString("") { "%02x".format(it) }
            calculated.equals(expectedHash.trim(), ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
}
