package com.aivos.echovault

  import android.app.Application
  import android.app.NotificationChannel
  import android.app.NotificationManager
  import android.os.Build
  import dagger.hilt.android.HiltAndroidApp

  @HiltAndroidApp
  class EchoVaultApp : Application() {

      companion object {
          const val CHANNEL_ID = "echovault_service"
          const val CHANNEL_NAME = "EchoVault Service"
      }

      override fun onCreate() {
          super.onCreate()
          createNotificationChannel()
      }

      private fun createNotificationChannel() {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              val channel = NotificationChannel(
                  CHANNEL_ID,
                  CHANNEL_NAME,
                  NotificationManager.IMPORTANCE_LOW
              ).apply {
                  description = "EchoVault clipboard monitoring service"
                  setShowBadge(false)
              }
              val nm = getSystemService(NotificationManager::class.java)
              nm.createNotificationChannel(channel)
          }
      }
  }