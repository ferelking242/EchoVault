package com.aivos.echovault

import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.aivos.echovault.data.repository.ClipboardRepository
import com.aivos.echovault.presentation.ui.EchoVaultNavHost
import com.aivos.echovault.presentation.ui.theme.EchoVaultTheme
import com.aivos.echovault.presentation.viewmodel.MainViewModel
import com.aivos.echovault.service.ClipboardForegroundService
import com.aivos.echovault.worker.ServiceWatchdogWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject lateinit var repository: ClipboardRepository

    private var lastCaptured: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !viewModel.isReady.value }

        startClipboardService()
        ServiceWatchdogWorker.schedule(this)

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val isLocked by viewModel.isAppLocked.collectAsState()
            val biometricEnabled by viewModel.biometricEnabled.collectAsState()
            val onboardingDone by viewModel.onboardingDone.collectAsState()

            EchoVaultTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EchoVaultNavHost(
                        isLocked = isLocked && biometricEnabled,
                        onUnlock = { viewModel.unlock() },
                        onboardingDone = onboardingDone,
                        onOnboardingComplete = { viewModel.completeOnboarding() },
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppResume()
        captureClipboardNow()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppPause()
    }

    /**
     * Capture la plus fiable sur Android 10+ :
     * l'utilisateur copie dans une autre app → revient sur EchoVault → on capture immédiatement.
     * L'AccessibilityService gère la capture en arrière-plan, l'Activity prend le relais au focus.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) captureClipboardNow()
    }

    private fun captureClipboardNow() {
        try {
            val cm = getSystemService(ClipboardManager::class.java) ?: return
            val clip = cm.primaryClip ?: return
            if (clip.itemCount == 0) return
            val text = clip.getItemAt(0)?.text?.toString()?.trim() ?: return
            if (text.isBlank() || text == lastCaptured) return
            lastCaptured = text
            lifecycleScope.launch {
                try {
                    repository.addEntry(text)
                    Log.d(TAG, "Captured from activity focus: ${text.take(40)}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save clipboard entry", e)
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Clipboard read blocked: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected clipboard error", e)
        }
    }

    private fun startClipboardService() {
        val intent = Intent(this, ClipboardForegroundService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    companion object {
        private const val TAG = "EchoVaultMain"
    }
}
