package com.aivos.echovault

  import android.content.Intent
  import android.os.Bundle
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
  import com.aivos.echovault.presentation.ui.EchoVaultNavHost
  import com.aivos.echovault.presentation.ui.theme.EchoVaultTheme
  import com.aivos.echovault.presentation.viewmodel.MainViewModel
  import com.aivos.echovault.service.ClipboardForegroundService
  import dagger.hilt.android.AndroidEntryPoint
  import kotlinx.coroutines.launch

  @AndroidEntryPoint
  class MainActivity : ComponentActivity() {

      private val viewModel: MainViewModel by viewModels()

      override fun onCreate(savedInstanceState: Bundle?) {
          val splashScreen = installSplashScreen()
          super.onCreate(savedInstanceState)

          splashScreen.setKeepOnScreenCondition { !viewModel.isReady.value }

          startClipboardService()

          setContent {
              val isDarkMode by viewModel.isDarkMode.collectAsState()
              val isLocked by viewModel.isAppLocked.collectAsState()
              val biometricEnabled by viewModel.biometricEnabled.collectAsState()

              EchoVaultTheme(darkTheme = isDarkMode) {
                  Surface(
                      modifier = Modifier.fillMaxSize(),
                      color = MaterialTheme.colorScheme.background
                  ) {
                      EchoVaultNavHost(
                          isLocked = isLocked && biometricEnabled,
                          onUnlock = { viewModel.unlock() },
                          viewModel = viewModel
                      )
                  }
              }
          }
      }

      override fun onResume() {
          super.onResume()
          viewModel.onAppResume()
      }

      private fun startClipboardService() {
          val intent = Intent(this, ClipboardForegroundService::class.java)
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
              startForegroundService(intent)
          } else {
              startService(intent)
          }
      }
  }