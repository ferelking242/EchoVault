package com.aivos.echovault.presentation.viewmodel

  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.aivos.echovault.data.preferences.UserPreferences
  import com.aivos.echovault.data.repository.ClipboardRepository
  import com.aivos.echovault.domain.usecase.BiometricAuthUseCase
  import com.aivos.echovault.domain.usecase.ExportDataUseCase
  import dagger.hilt.android.lifecycle.HiltViewModel
  import kotlinx.coroutines.flow.*
  import kotlinx.coroutines.launch
  import javax.inject.Inject

  data class SettingsUiState(
      val darkMode: Boolean = false,
      val biometricEnabled: Boolean = false,
      val monitoringEnabled: Boolean = true,
      val encryptionEnabled: Boolean = true,
      val lockOnBackground: Boolean = false,
      val biometricAvailable: Boolean = false,
      val exportContent: String = "",
      val showExportDialog: Boolean = false,
      val showDeleteDialog: Boolean = false,
      val snackbarMessage: String = ""
  )

  @HiltViewModel
  class SettingsViewModel @Inject constructor(
      private val preferences: UserPreferences,
      private val repository: ClipboardRepository,
      private val exportUseCase: ExportDataUseCase,
      private val biometricAuthUseCase: BiometricAuthUseCase
  ) : ViewModel() {

      val uiState: StateFlow<SettingsUiState> = combine(
          preferences.darkMode,
          preferences.biometricEnabled,
          preferences.monitoringEnabled,
          preferences.encryptionEnabled,
          preferences.lockOnBackground
      ) { dark, biometric, monitoring, encryption, lockBg ->
          SettingsUiState(
              darkMode = dark, biometricEnabled = biometric,
              monitoringEnabled = monitoring, encryptionEnabled = encryption,
              lockOnBackground = lockBg,
              biometricAvailable = biometricAuthUseCase.isBiometricAvailable()
          )
      }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

      fun setDarkMode(v: Boolean) = viewModelScope.launch { preferences.setDarkMode(v) }
      fun setBiometric(v: Boolean) = viewModelScope.launch { preferences.setBiometricEnabled(v) }
      fun setMonitoring(v: Boolean) = viewModelScope.launch { preferences.setMonitoringEnabled(v) }
      fun setEncryption(v: Boolean) = viewModelScope.launch { preferences.setEncryptionEnabled(v) }
      fun setLockOnBackground(v: Boolean) = viewModelScope.launch { preferences.setLockOnBackground(v) }

      suspend fun exportTxt() = exportUseCase.exportAsTxt()
      suspend fun exportJson() = exportUseCase.exportAsJson()
      suspend fun exportCsv() = exportUseCase.exportAsCsv()

      fun deleteAllData() = viewModelScope.launch { repository.deleteAll() }
  }