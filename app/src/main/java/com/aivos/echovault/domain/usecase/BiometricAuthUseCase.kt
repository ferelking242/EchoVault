package com.aivos.echovault.domain.usecase

  import android.content.Context
  import androidx.biometric.BiometricManager
  import androidx.biometric.BiometricPrompt
  import androidx.core.content.ContextCompat
  import androidx.fragment.app.FragmentActivity
  import dagger.hilt.android.qualifiers.ApplicationContext
  import javax.inject.Inject

  class BiometricAuthUseCase @Inject constructor(
      @ApplicationContext private val context: Context
  ) {
      fun isBiometricAvailable(): Boolean {
          val bm = BiometricManager.from(context)
          return bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
      }

      fun authenticate(
          activity: FragmentActivity,
          onSuccess: () -> Unit,
          onError: (String) -> Unit,
          onFailed: () -> Unit
      ) {
          val executor = ContextCompat.getMainExecutor(activity)
          val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
              override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                  super.onAuthenticationSucceeded(result)
                  onSuccess()
              }
              override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                  super.onAuthenticationError(errorCode, errString)
                  onError(errString.toString())
              }
              override fun onAuthenticationFailed() {
                  super.onAuthenticationFailed()
                  onFailed()
              }
          })
          val promptInfo = BiometricPrompt.PromptInfo.Builder()
              .setTitle("EchoVault")
              .setSubtitle("Unlock your clipboard history")
              .setDescription("Use biometrics to access EchoVault")
              .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
              .build()
          biometricPrompt.authenticate(promptInfo)
      }
  }