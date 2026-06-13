package com.aivos.echovault.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aivos.echovault.service.ClipboardForegroundService
import com.aivos.echovault.worker.ServiceWatchdogWorker

/**
 * Démarre les services au boot, reboot rapide et après une mise à jour de l'app.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val validActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.LOCKED_BOOT_COMPLETED",
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON"
        )
        if (action !in validActions) return

        Log.i(TAG, "Boot/update received ($action) — starting services")

        try {
            val serviceIntent = Intent(context, ClipboardForegroundService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
        }

        try {
            ServiceWatchdogWorker.schedule(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule watchdog", e)
        }
    }

    companion object {
        private const val TAG = "EchoVaultBoot"
    }
}