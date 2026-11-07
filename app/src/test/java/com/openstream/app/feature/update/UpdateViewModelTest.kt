package com.openstream.app.feature.update

import android.content.Intent
import com.openstream.app.domain.model.UpdateCheckResult
import com.openstream.app.domain.model.UpdateDownloadState
import com.openstream.app.domain.model.UpdateManifest
import com.openstream.app.domain.repository.AppUpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeUpdateManager: FakeAppUpdateManager
    private lateinit var viewModel: UpdateViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeUpdateManager = FakeAppUpdateManager()
        viewModel = UpdateViewModel(fakeUpdateManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun checkForUpdates_whenUpdateAvailable_showsDialogWithManifest() = runTest {
        val manifest = UpdateManifest(
            versionCode = 3,
            versionName = "1.2.0",
            apkUrl = "https://updates.openstream.org/openStream.apk",
            forceUpdate = false
        )
        fakeUpdateManager.checkResult = Result.success(
            UpdateCheckResult.UpdateAvailable(manifest, isForced = false)
        )

        viewModel.checkForUpdates(isManualCheck = false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isVisible)
        assertFalse(state.isForced)
        assertEquals(3, state.manifest?.versionCode)
        assertNull(state.infoMessage)
    }

    @Test
    fun checkForUpdates_whenUpToDateManualCheck_setsUpToDateMessage() = runTest {
        fakeUpdateManager.checkResult = Result.success(UpdateCheckResult.NoUpdateAvailable)

        viewModel.checkForUpdates(isManualCheck = true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isVisible)
        assertEquals("UP_TO_DATE", state.infoMessage)
    }

    @Test
    fun dismissDialog_whenOptionalUpdate_hidesDialog() = runTest {
        val manifest = UpdateManifest(
            versionCode = 3,
            versionName = "1.2.0",
            apkUrl = "https://updates.openstream.org/openStream.apk",
            forceUpdate = false
        )
        fakeUpdateManager.checkResult = Result.success(
            UpdateCheckResult.UpdateAvailable(manifest, isForced = false)
        )

        viewModel.checkForUpdates()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isVisible)

        viewModel.dismissDialog()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isVisible)
        assertTrue(fakeUpdateManager.dismissedCodes.contains(3))
    }

    @Test
    fun dismissDialog_whenForcedUpdate_refusesDismissal() = runTest {
        val manifest = UpdateManifest(
            versionCode = 3,
            versionName = "1.2.0",
            apkUrl = "https://updates.openstream.org/openStream.apk",
            forceUpdate = true
        )
        fakeUpdateManager.checkResult = Result.success(
            UpdateCheckResult.UpdateAvailable(manifest, isForced = true)
        )

        viewModel.checkForUpdates()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isVisible)
        assertTrue(viewModel.uiState.value.isForced)

        viewModel.dismissDialog()
        advanceUntilIdle()

        // Dialog must remain visible for mandatory updates
        assertTrue(viewModel.uiState.value.isVisible)
        assertFalse(fakeUpdateManager.dismissedCodes.contains(3))
    }

    @Test
    fun startUpdate_triggersDownloadOnManager() = runTest {
        val manifest = UpdateManifest(
            versionCode = 3,
            versionName = "1.2.0",
            apkUrl = "https://updates.openstream.org/openStream.apk"
        )
        fakeUpdateManager.checkResult = Result.success(
            UpdateCheckResult.UpdateAvailable(manifest, isForced = false)
        )

        viewModel.checkForUpdates()
        advanceUntilIdle()

        viewModel.startUpdate()
        advanceUntilIdle()

        assertEquals(manifest, fakeUpdateManager.downloadedManifest)
    }

    private class FakeAppUpdateManager : AppUpdateManager {
        private val _downloadState = MutableStateFlow<UpdateDownloadState>(UpdateDownloadState.Idle)
        override val downloadState: StateFlow<UpdateDownloadState> = _downloadState.asStateFlow()

        var checkResult: Result<UpdateCheckResult> = Result.success(UpdateCheckResult.NoUpdateAvailable)
        val dismissedCodes = mutableSetOf<Int>()
        var downloadedManifest: UpdateManifest? = null

        override suspend fun checkForUpdates(
            manifestUrl: String?,
            isManualCheck: Boolean
        ): Result<UpdateCheckResult> = checkResult

        override suspend fun startDownload(manifest: UpdateManifest) {
            downloadedManifest = manifest
        }

        override fun installApk(apkFile: File): Result<Unit> = Result.success(Unit)

        override fun dismissUpdate(versionCode: Int) {
            dismissedCodes.add(versionCode)
        }

        override fun isUpdateDismissed(versionCode: Int): Boolean = dismissedCodes.contains(versionCode)

        override fun canRequestPackageInstalls(): Boolean = true

        override fun getManageUnknownAppSourcesIntent(): Intent = Intent()

        override fun resetDownloadState() {
            _downloadState.value = UpdateDownloadState.Idle
        }
    }
}
