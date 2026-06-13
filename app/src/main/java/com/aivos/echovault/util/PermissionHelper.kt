package com.aivos.echovault.util

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.PowerManager
import android.provider.Settings
import com.aivos.echovault.receiver.EchoVaultDeviceAdminReceiver

object PermissionHelper {

    fun isAccessibilityEnabled(context: Context): Boolean {
        val service = "${context.packageName}/${context.packageName}.service.EchoVaultAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").any { it.equals(service, ignoreCase = true) }
    }

    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val pm = context.getSystemService(PowerManager::class.java)
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun isDeviceAdminEnabled(context: Context): Boolean {
        val dpm = context.getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(context, EchoVaultDeviceAdminReceiver::class.java)
        return dpm.isAdminActive(admin)
    }

    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isFullyConfigured(context: Context): Boolean {
        return isAccessibilityEnabled(context) && isBatteryOptimizationIgnored(context)
    }
}
