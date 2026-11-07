package com.openstream.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.openstream.app.domain.model.UpdateCheckResult
import com.openstream.app.domain.model.UpdateDownloadState
import com.openstream.app.domain.model.UpdateManifest
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest

class AppUpdateManagerTest {

    private lateinit var tempDir: File
    private var mockInstalledVersionCode: Int = 2
    private val mockResponses = mutableMapOf<String, Pair<Int, ByteArray>>()
    private val gson = Gson()

    private val testHttpClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain ->
            val url = chain.request().url.toString()
            val (code, bytes) = mockResponses[url] ?: (404 to ByteArray(0))
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(if (code in 200..299) "OK" else "Error")
                .body(bytes.toResponseBody("application/octet-stream".toMediaTypeOrNull()))
                .build()
        })
        .build()

    // Test instance with controlled providers
    private fun createManager(
        installedVersion: Int = mockInstalledVersionCode
    ): AppUpdateManagerImpl {
        return AppUpdateManagerImpl(
            context = null,
            httpClient = testHttpClient,
            gson = gson,
            installedVersionCodeProvider = { installedVersion },
            cacheDirProvider = { tempDir },
            packageNameProvider = { "com.openstream.app" }
        )
    }

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("openstream_update_test").toFile()
        mockResponses.clear()
        mockInstalledVersionCode = 2
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data).joinToString("") { "%02x".format(it) }
    }

    @Test
    fun checkForUpdates_higherRemoteVersion_returnsUpdateAvailable() = runTest {
        val manifestUrl = "https://updates.openstream.org/update.json"
        val manifestJson = """
            {
              "versionCode": 3,
              "versionName": "1.2.0",
              "apkUrl": "https://updates.openstream.org/openStream.apk",
              "releaseNotes": ["Performance improvements"],
              "forceUpdate": false,
              "minimumSupportedVersion": 1
            }
        """.trimIndent()

        mockResponses[manifestUrl] = 200 to manifestJson.toByteArray()

        val manager = createManager(installedVersion = 2)
        val result = manager.checkForUpdates(manifestUrl = manifestUrl)

        assertTrue(result.isSuccess)
        val checkResult = result.getOrThrow()
        assertTrue(checkResult is UpdateCheckResult.UpdateAvailable)
        val update = checkResult as UpdateCheckResult.UpdateAvailable
        assertEquals(3, update.manifest.versionCode)
        assertEquals("1.2.0", update.manifest.versionName)
        assertFalse(update.isForced)
    }

    @Test
    fun checkForUpdates_equalOrLowerVersion_returnsNoUpdateAvailable() = runTest {
        val manifestUrl = "https://updates.openstream.org/update.json"
        val manifestJson = """
            {
              "versionCode": 2,
              "versionName": "1.1.0",
              "apkUrl": "https://updates.openstream.org/openStream.apk",
              "forceUpdate": false,
              "minimumSupportedVersion": 1
            }
        """.trimIndent()

        mockResponses[manifestUrl] = 200 to manifestJson.toByteArray()

        val manager = createManager(installedVersion = 2)
        val result = manager.checkForUpdates(manifestUrl = manifestUrl)

        assertTrue(result.isSuccess)
        assertEquals(UpdateCheckResult.NoUpdateAvailable, result.getOrThrow())
    }

    @Test
    fun checkForUpdates_forceUpdateTrue_marksUpdateAsForced() = runTest {
        val manifestUrl = "https://updates.openstream.org/update.json"
        val manifestJson = """
            {
              "versionCode": 4,
              "versionName": "2.0.0",
              "apkUrl": "https://updates.openstream.org/openStream.apk",
              "forceUpdate": true,
              "minimumSupportedVersion": 1
            }
        """.trimIndent()

        mockResponses[manifestUrl] = 200 to manifestJson.toByteArray()

        val manager = createManager(installedVersion = 2)
        val result = manager.checkForUpdates(manifestUrl = manifestUrl)

        assertTrue(result.isSuccess)
        val checkResult = result.getOrThrow() as UpdateCheckResult.UpdateAvailable
        assertTrue(checkResult.isForced)
    }

    @Test
    fun checkForUpdates_installedBelowMinimumSupportedVersion_marksUpdateAsForced() = runTest {
        val manifestUrl = "https://updates.openstream.org/update.json"
        val manifestJson = """
            {
              "versionCode": 4,
              "versionName": "2.0.0",
              "apkUrl": "https://updates.openstream.org/openStream.apk",
              "forceUpdate": false,
              "minimumSupportedVersion": 3
            }
        """.trimIndent()

        mockResponses[manifestUrl] = 200 to manifestJson.toByteArray()

        val manager = createManager(installedVersion = 2)
        val result = manager.checkForUpdates(manifestUrl = manifestUrl)

        assertTrue(result.isSuccess)
        val checkResult = result.getOrThrow() as UpdateCheckResult.UpdateAvailable
        assertTrue(checkResult.isForced)
    }

    @Test
    fun checkForUpdates_optionalUpdateDismissedInSession_isSuppressedOnAutomaticCheck() = runTest {
        val manifestUrl = "https://updates.openstream.org/update.json"
        val manifestJson = """
            {
              "versionCode": 3,
              "versionName": "1.2.0",
              "apkUrl": "https://updates.openstream.org/openStream.apk",
              "forceUpdate": false,
              "minimumSupportedVersion": 1
            }
        """.trimIndent()

        mockResponses[manifestUrl] = 200 to manifestJson.toByteArray()

        val manager = createManager(installedVersion = 2)
        manager.dismissUpdate(3)
        assertTrue(manager.isUpdateDismissed(3))

        val autoCheckResult = manager.checkForUpdates(manifestUrl = manifestUrl, isManualCheck = false)
        assertTrue(autoCheckResult.isSuccess)
        assertEquals(UpdateCheckResult.NoUpdateAvailable, autoCheckResult.getOrThrow())

        // Manual check from Settings should still display the update
        val manualCheckResult = manager.checkForUpdates(manifestUrl = manifestUrl, isManualCheck = true)
        assertTrue(manualCheckResult.isSuccess)
        assertTrue(manualCheckResult.getOrThrow() is UpdateCheckResult.UpdateAvailable)
    }

    @Test
    fun checkForUpdates_nonHttpsManifestUrl_isRejectedBySecurityPolicy() = runTest {
        val insecureUrl = "http://insecure.openstream.org/update.json"
        val manager = createManager(installedVersion = 1)
        val result = manager.checkForUpdates(manifestUrl = insecureUrl)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }

    @Test
    fun checkForUpdates_nonHttpsApkUrl_isRejectedBySecurityPolicy() = runTest {
        val manifestUrl = "https://updates.openstream.org/update.json"
        val manifestJson = """
            {
              "versionCode": 3,
              "versionName": "1.2.0",
              "apkUrl": "http://insecure.openstream.org/openStream.apk",
              "forceUpdate": false,
              "minimumSupportedVersion": 1
            }
        """.trimIndent()

        mockResponses[manifestUrl] = 200 to manifestJson.toByteArray()

        val manager = createManager(installedVersion = 1)
        val result = manager.checkForUpdates(manifestUrl = manifestUrl)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }

    @Test
    fun checkForUpdates_malformedJson_failsGracefullyWithoutCrashing() = runTest {
        val manifestUrl = "https://updates.openstream.org/update.json"
        mockResponses[manifestUrl] = 200 to "INVALID_JSON_CONTENT".toByteArray()

        val manager = createManager(installedVersion = 1)
        val result = manager.checkForUpdates(manifestUrl = manifestUrl)

        assertTrue(result.isFailure)
    }

    @Test
    fun startDownload_validSha256_downloadsAndEmitsReadyToInstall() = runTest {
        val apkUrl = "https://updates.openstream.org/openStream.apk"
        val sampleApkBytes = "MOCK_APK_BINARY_CONTENT_V1.2.0".toByteArray()
        val validSha256 = sha256Hex(sampleApkBytes)

        mockResponses[apkUrl] = 200 to sampleApkBytes

        val manifest = UpdateManifest(
            versionCode = 3,
            versionName = "1.2.0",
            apkUrl = apkUrl,
            sha256 = validSha256
        )

        val manager = createManager(installedVersion = 2)
        manager.startDownload(manifest)

        val finalState = manager.downloadState.value
        assertTrue("Expected ReadyToInstall but was $finalState", finalState is UpdateDownloadState.ReadyToInstall)
        val readyState = finalState as UpdateDownloadState.ReadyToInstall
        assertTrue(readyState.apkFile.exists())
        assertEquals(sampleApkBytes.size.toLong(), readyState.apkFile.length())
    }

    @Test
    fun startDownload_mismatchedSha256_failsVerificationAndDeletesFile() = runTest {
        val apkUrl = "https://updates.openstream.org/openStream.apk"
        val sampleApkBytes = "CORRUPTED_OR_TAMPERED_APK_CONTENT".toByteArray()
        val expectedSha256 = "0000000000000000000000000000000000000000000000000000000000000000"

        mockResponses[apkUrl] = 200 to sampleApkBytes

        val manifest = UpdateManifest(
            versionCode = 3,
            versionName = "1.2.0",
            apkUrl = apkUrl,
            sha256 = expectedSha256
        )

        val manager = createManager(installedVersion = 2)
        manager.startDownload(manifest)

        val finalState = manager.downloadState.value
        assertTrue("Expected Error state on checksum failure but was $finalState", finalState is UpdateDownloadState.Error)
        val errorState = finalState as UpdateDownloadState.Error
        assertTrue(errorState.message.contains("SHA-256", ignoreCase = true))

        // Ensure corrupted file was removed from disk
        val partFile = File(File(tempDir, "updates"), "openStream_3.apk.part")
        val finalFile = File(File(tempDir, "updates"), "openStream_3.apk")
        assertFalse(partFile.exists())
        assertFalse(finalFile.exists())
    }

    @Test
    fun resetDownloadState_restoresIdle() = runTest {
        val manager = createManager(installedVersion = 2)
        manager.resetDownloadState()
        assertEquals(UpdateDownloadState.Idle, manager.downloadState.value)
    }
}
