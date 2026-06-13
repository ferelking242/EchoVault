package com.aivos.echovault.receiver

  import android.content.BroadcastReceiver
  import android.content.Context
  import android.content.Intent
  import com.aivos.echovault.service.ClipboardForegroundService

  class BootReceiver : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
          if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
              intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
              intent.action == "android.intent.action.QUICKBOOT_POWERON") {
              val serviceIntent = Intent(context, ClipboardForegroundService::class.java)
              if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                  context.startForegroundService(serviceIntent)
              } else {
                  context.startService(serviceIntent)
              }
          }
      }
  }