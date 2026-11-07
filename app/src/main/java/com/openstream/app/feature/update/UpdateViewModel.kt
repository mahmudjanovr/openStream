package com.openstream.app.feature.update

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.openstream.app.domain.model.UpdateCheckResult
import com.openstream.app.domain.model.UpdateDownloadState
import com.openstream.app.domain.model.UpdateManifest
import com.openstream.app.domain.repository.AppUpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * UI State for the OTA In-App Update flow.
 */
data class UpdateUiState(
    val isVisible: Boolean = false,
    val manifest: UpdateManifest? = null,
    val isForced: Boolean = false,
    val downloadState: UpdateDownloadState = UpdateDownloadState.Idle,
    val isChecking: Boolean = false,
    val infoMessage: String? = null
)

/**
 * ViewModel orchestrating remote version check, user confirmation dialog,
 * download progress, and package installation.
 */
class UpdateViewModel(
    private val appUpdateManager: AppUpdateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appUpdateManager.downloadState.collect { downloadState ->
                _uiState.value = _uiState.value.copy(downloadState = downloadState)
            }
        }
    }

    /**
     * Checks for updates asynchronously without blocking startup or navigation.
     */
    fun checkForUpdates(isManualCheck: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChecking = true, infoMessage = null)
            val result = appUpdateManager.checkForUpdates(isManualCheck = isManualCheck)
            result.onSuccess { checkResult ->
                when (checkResult) {
                    is UpdateCheckResult.UpdateAvailable -> {
                        _uiState.value = _uiState.value.copy(
                            isVisible = true,
                            manifest = checkResult.manifest,
                            isForced = checkResult.isForced,
                            isChecking = false,
                            infoMessage = null
                        )
                    }
                    is UpdateCheckResult.NoUpdateAvailable -> {
                        _uiState.value = _uiState.value.copy(
                            isVisible = false,
                            isChecking = false,
                            infoMessage = if (isManualCheck) "UP_TO_DATE" else null
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isChecking = false,
                    infoMessage = if (isManualCheck) error.localizedMessage else null
                )
            }
        }
    }

    /**
     * Starts download of the new APK.
     */
    fun startUpdate() {
        val manifest = _uiState.value.manifest ?: return
        viewModelScope.launch {
            appUpdateManager.startDownload(manifest)
        }
    }

    /**
     * Dismisses the dialog for this session (only permitted for optional updates).
     */
    fun dismissDialog() {
        val currentState = _uiState.value
        if (!currentState.isForced) {
            currentState.manifest?.let {
                appUpdateManager.dismissUpdate(it.versionCode)
            }
            appUpdateManager.resetDownloadState()
            _uiState.value = _uiState.value.copy(isVisible = false)
        }
    }

    /**
     * Triggers installation of the verified APK file.
     */
    fun installApk(apkFile: File) {
        appUpdateManager.installApk(apkFile)
    }

    fun canRequestPackageInstalls(): Boolean = appUpdateManager.canRequestPackageInstalls()

    fun getManageUnknownAppSourcesIntent(): Intent = appUpdateManager.getManageUnknownAppSourcesIntent()

    fun clearInfoMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }

    companion object {
        fun provideFactory(
            appUpdateManager: AppUpdateManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UpdateViewModel(appUpdateManager) as T
            }
        }
    }
}
