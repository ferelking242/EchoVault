package com.aivos.echovault.presentation.viewmodel

  import android.app.Application
  import androidx.lifecycle.AndroidViewModel
  import androidx.lifecycle.viewModelScope
  import com.aivos.echovault.data.preferences.UserPreferences
  import dagger.hilt.android.lifecycle.HiltViewModel
  import kotlinx.coroutines.delay
  import kotlinx.coroutines.flow.*
  import kotlinx.coroutines.launch
  import javax.inject.Inject

  @HiltViewModel
  class MainViewModel @Inject constructor(
      application: Application,
      private val preferences: UserPreferences
  ) : AndroidViewModel(application) {

      private val _isReady = MutableStateFlow(false)
      val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

      val isDarkMode: StateFlow<Boolean> = preferences.darkMode
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

      val biometricEnabled: StateFlow<Boolean> = preferences.biometricEnabled
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

      val onboardingDone: StateFlow<Boolean> = preferences.onboardingDone
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

      private val _isAppLocked = MutableStateFlow(true)
      val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

      private var wasInBackground = false

      init {
          viewModelScope.launch {
              delay(600)
              _isReady.value = true
          }
      }

      fun unlock() { _isAppLocked.value = false }

      fun completeOnboarding() {
          viewModelScope.launch { preferences.setOnboardingDone(true) }
      }

      fun onAppResume() {
          viewModelScope.launch {
              val lockOnBg = preferences.lockOnBackground.first()
              val biometric = preferences.biometricEnabled.first()
              if (wasInBackground && lockOnBg && biometric) {
                  _isAppLocked.value = true
              }
              wasInBackground = false
          }
      }

      fun onAppPause() { wasInBackground = true }
  }