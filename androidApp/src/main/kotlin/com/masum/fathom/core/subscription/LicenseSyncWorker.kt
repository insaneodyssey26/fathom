package com.masum.fathom.core.subscription

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class LicenseSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val WORK_NAME = "fathom_license_sync"
        private const val OFFLINE_GRACE_PERIOD_MS = 30L * 24L * 60L * 60L * 1000L

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<LicenseSyncWorker>(7, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        val manager = SubscriptionManager(applicationContext)
        val state = manager.subscriptionState.value

        if (!state.isPro) {
            return Result.success()
        }

        val key = manager.getSavedLicenseKey()
        if (key.isNullOrBlank()) {
            manager.resetToFree()
            return Result.success()
        }

        val deviceId = manager.getDeviceId()
        val prefs = SubscriptionManager.getEncryptedPreferences(applicationContext)
        val lastSync = prefs.getLong("last_license_sync_epoch", 0L)
        val now = System.currentTimeMillis()

        return when (val result = manager.licenseEngine.checkLicenseRemoteStatus(key, deviceId)) {
            is RemoteLicenseCheckResult.Valid -> {
                prefs.edit()
                    .putLong("last_license_sync_epoch", now)
                    .putLong("subscription_expires_at", result.expiresAtEpochMs)
                    .apply()
                Result.success()
            }
            is RemoteLicenseCheckResult.Revoked,
            is RemoteLicenseCheckResult.Expired,
            is RemoteLicenseCheckResult.NotFound -> {
                manager.resetToFree()
                Result.success()
            }
            is RemoteLicenseCheckResult.NetworkError -> {
                if (lastSync > 0L && (now - lastSync) > OFFLINE_GRACE_PERIOD_MS) {
                    manager.resetToFree()
                }
                Result.retry()
            }
        }
    }
}
