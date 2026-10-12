package com.masum.fathom.core.update

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.compositionLocalOf
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

val LocalAppUpdateEngine = compositionLocalOf<AppUpdateEngine> {
    error("AppUpdateEngine not provided")
}

sealed interface AppUpdateStatus {
    object Idle : AppUpdateStatus
    object Checking : AppUpdateStatus
    data class Available(val info: AppUpdateInfo, val availableVersionCode: Int) : AppUpdateStatus
    data class Downloading(val bytesDownloaded: Long, val totalBytes: Long) : AppUpdateStatus
    object Downloaded : AppUpdateStatus
    object UpToDate : AppUpdateStatus
    data class Error(val message: String) : AppUpdateStatus
}

class AppUpdateEngine(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)

    private val _updateStatus = MutableStateFlow<AppUpdateStatus>(AppUpdateStatus.Idle)
    val updateStatus: StateFlow<AppUpdateStatus> = _updateStatus.asStateFlow()

    private val installListener = InstallStateUpdatedListener { state: InstallState ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                _updateStatus.value = AppUpdateStatus.Downloading(
                    bytesDownloaded = state.bytesDownloaded(),
                    totalBytes = state.totalBytesToDownload()
                )
            }
            InstallStatus.DOWNLOADED -> {
                _updateStatus.value = AppUpdateStatus.Downloaded
            }
            InstallStatus.FAILED -> {
                _updateStatus.value = AppUpdateStatus.Error("Update download failed. Please try again.")
            }
            InstallStatus.CANCELED -> {
                _updateStatus.value = AppUpdateStatus.Idle
            }
            else -> {}
        }
    }

    init {
        try {
            appUpdateManager.registerListener(installListener)
        } catch (_: Exception) {}
    }

    fun checkForUpdates(manual: Boolean = false) {
        if (manual) {
            _updateStatus.value = AppUpdateStatus.Checking
        }

        try {
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    _updateStatus.value = AppUpdateStatus.Available(info, info.availableVersionCode())
                } else if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    _updateStatus.value = AppUpdateStatus.Downloaded
                } else {
                    if (manual) {
                        _updateStatus.value = AppUpdateStatus.UpToDate
                    }
                }
            }.addOnFailureListener { e ->
                if (manual) {
                    _updateStatus.value = AppUpdateStatus.Error(e.message ?: "Unable to check for updates")
                }
            }
        } catch (e: Exception) {
            if (manual) {
                _updateStatus.value = AppUpdateStatus.Error(e.message ?: "Failed to connect to Google Play")
            }
        }
    }

    fun startFlexibleUpdate(activity: Activity, info: AppUpdateInfo) {
        try {
            val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            appUpdateManager.startUpdateFlow(info, activity, options)
        } catch (e: Exception) {
            _updateStatus.value = AppUpdateStatus.Error(e.message ?: "Could not start update flow")
        }
    }

    fun startFlexibleUpdateWithLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>, info: AppUpdateInfo) {
        try {
            val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            appUpdateManager.startUpdateFlowForResult(info, launcher, options)
        } catch (e: Exception) {
            _updateStatus.value = AppUpdateStatus.Error(e.message ?: "Could not start update flow")
        }
    }

    fun completeUpdate() {
        try {
            appUpdateManager.completeUpdate()
        } catch (_: Exception) {}
    }

    fun resumeCheckOnResume(activity: Activity) {
        try {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    _updateStatus.value = AppUpdateStatus.Downloaded
                } else if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        val options = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                        appUpdateManager.startUpdateFlow(info, activity, options)
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}
    }

    fun dismissBanner() {
        if (_updateStatus.value is AppUpdateStatus.UpToDate || _updateStatus.value is AppUpdateStatus.Error) {
            _updateStatus.value = AppUpdateStatus.Idle
        }
    }

    fun release() {
        try {
            appUpdateManager.unregisterListener(installListener)
        } catch (_: Exception) {}
    }
}
