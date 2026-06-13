package com.aivos.echovault.service

  import android.app.*
  import android.content.*
  import android.os.*
  import androidx.core.app.NotificationCompat
  import com.aivos.echovault.EchoVaultApp
  import com.aivos.echovault.MainActivity
  import com.aivos.echovault.R
  import com.aivos.echovault.data.preferences.UserPreferences
  import com.aivos.echovault.data.repository.ClipboardRepository
  import dagger.hilt.android.AndroidEntryPoint
  import kotlinx.coroutines.*
  import kotlinx.coroutines.flow.first
  import javax.inject.Inject

  @AndroidEntryPoint
  class ClipboardForegroundService : Service() {

      @Inject lateinit var repository: ClipboardRepository
      @Inject lateinit var preferences: UserPreferences

      private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
      private var clipboardManager: ClipboardManager? = null
      private var lastContent: String = ""
      private var monitoringJob: Job? = null

      private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
          serviceScope.launch {
              try {
                  val monitoring = preferences.monitoringEnabled.first()
                  if (!monitoring) return@launch
                  val clip = clipboardManager?.primaryClip ?: return@launch
                  if (clip.itemCount == 0) return@launch
                  val text = clip.getItemAt(0)?.text?.toString()?.trim() ?: return@launch
                  if (text.isBlank() || text == lastContent) return@launch
                  lastContent = text
                  repository.addEntry(text)
              } catch (e: Exception) {
                  // Silent failure — clipboard access may be restricted
              }
          }
      }

      override fun onCreate() {
          super.onCreate()
          clipboardManager = getSystemService(ClipboardManager::class.java)
          clipboardManager?.addPrimaryClipChangedListener(clipListener)
          startForeground(NOTIFICATION_ID, buildNotification())
      }

      override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
          return START_STICKY
      }

      override fun onDestroy() {
          super.onDestroy()
          clipboardManager?.removePrimaryClipChangedListener(clipListener)
          serviceScope.cancel()
      }

      override fun onBind(intent: Intent?): IBinder? = null

      override fun onTaskRemoved(rootIntent: Intent?) {
          super.onTaskRemoved(rootIntent)
          val restartIntent = Intent(applicationContext, ClipboardForegroundService::class.java)
          val pendingIntent = PendingIntent.getService(applicationContext, 1, restartIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
          val alarmManager = getSystemService(AlarmManager::class.java)
          alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, pendingIntent)
      }

      private fun buildNotification(): Notification {
          val openIntent = PendingIntent.getActivity(
              this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          return NotificationCompat.Builder(this, EchoVaultApp.CHANNEL_ID)
              .setContentTitle("EchoVault")
              .setContentText("EchoVault is protecting your clipboard history")
              .setSmallIcon(R.drawable.ic_notification)
              .setContentIntent(openIntent)
              .setOngoing(true)
              .setSilent(true)
              .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
              .build()
      }

      companion object {
          const val NOTIFICATION_ID = 1001
      }
  }