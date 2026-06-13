package com.aivos.echovault.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.aivos.echovault.data.preferences.UserPreferences
import com.aivos.echovault.data.repository.ClipboardRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Accessibility Service — seule méthode fiable pour capturer le clipboard
 * en arrière-plan sur Android 10+ (API 29+).
 *
 * Les services d'accessibilité sont exemptés de la restriction clipboard
 * introduite en Android 10 (voir Android documentation sur les services système).
 * Le service se relance automatiquement via START_STICKY + onServiceConnected.
 */
@AndroidEntryPoint
class EchoVaultAccessibilityService : AccessibilityService() {

    @Inject lateinit var repository: ClipboardRepository
    @Inject lateinit var preferences: UserPreferences

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var clipboardManager: ClipboardManager? = null
    private var lastContent: String = ""

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        scope.launch {
            try {
                val monitoring = preferences.monitoringEnabled.first()
                if (!monitoring) return@launch

                val clip = clipboardManager?.primaryClip ?: return@launch
                if (clip.itemCount == 0) return@launch
                val text = clip.getItemAt(0)?.text?.toString()?.trim() ?: return@launch
                if (text.isBlank() || text == lastContent) return@launch
                lastContent = text
                repository.addEntry(text)
                Log.d(TAG, "Captured via AccessibilityService: ${text.take(40)}")
            } catch (e: SecurityException) {
                Log.w(TAG, "Clipboard read blocked: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error capturing clipboard", e)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = info

        clipboardManager = getSystemService(ClipboardManager::class.java)
        clipboardManager?.addPrimaryClipChangedListener(clipListener)

        // Also start the foreground service for extra resilience
        val intent = Intent(this, ClipboardForegroundService::class.java)
        startForegroundService(intent)

        Log.i(TAG, "AccessibilityService connected — clipboard monitoring active")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        Log.w(TAG, "AccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        clipboardManager?.removePrimaryClipChangedListener(clipListener)
        scope.cancel()
        Log.w(TAG, "AccessibilityService destroyed — will reconnect automatically")
    }

    companion object {
        private const val TAG = "EchoVaultA11y"
    }
}
