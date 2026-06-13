package com.aivos.echovault.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Device Administrator Receiver.
 * Une fois activé, l'app ne peut pas être désinstallée ni forcée à s'arrêter
 * sans que l'utilisateur révoque d'abord les droits administrateur.
 */
class EchoVaultDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Device Admin enabled — EchoVault is now protected")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w(TAG, "Device Admin disabled")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Désactiver l'administration supprimera la protection de l'historique clipboard."
    }

    companion object {
        private const val TAG = "EchoVaultAdmin"
    }
}
