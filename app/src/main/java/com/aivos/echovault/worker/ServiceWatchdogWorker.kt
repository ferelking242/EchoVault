package com.aivos.echovault.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aivos.echovault.service.ClipboardForegroundService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Worker qui vérifie périodiquement que le service clipboard tourne.
 * Relance le service s'il a été tué par le système.
 * Planifié toutes les 15 minutes (minimum Android WorkManager).
 */
@HiltWorker
class ServiceWatchdogWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Watchdog check — ensuring clipboard service is running")
            val intent = Intent(context, ClipboardForegroundService::class.java)
            context.startForegroundService(intent)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Watchdog failed to restart service", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "EchoVaultWatchdog"
        private const val WORK_NAME = "echovault_service_watchdog"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ServiceWatchdogWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.i(TAG, "Watchdog scheduled every 15 minutes")
        }
    }
}
