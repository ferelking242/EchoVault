package com.aivos.echovault.data.preferences

  import android.content.Context
  import androidx.datastore.core.DataStore
  import androidx.datastore.preferences.core.*
  import androidx.datastore.preferences.preferencesDataStore
  import dagger.hilt.android.qualifiers.ApplicationContext
  import kotlinx.coroutines.flow.Flow
  import kotlinx.coroutines.flow.catch
  import kotlinx.coroutines.flow.map
  import java.io.IOException
  import javax.inject.Inject
  import javax.inject.Singleton

  val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "echovault_prefs")

  @Singleton
  class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {

      private object Keys {
          val DARK_MODE = booleanPreferencesKey("dark_mode")
          val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
          val MONITORING_ENABLED = booleanPreferencesKey("monitoring_enabled")
          val ENCRYPTION_ENABLED = booleanPreferencesKey("encryption_enabled")
          val LOCK_ON_BACKGROUND = booleanPreferencesKey("lock_on_background")
          val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
      }

      val darkMode: Flow<Boolean> = context.dataStore.data
          .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
          .map { it[Keys.DARK_MODE] ?: false }

      val biometricEnabled: Flow<Boolean> = context.dataStore.data
          .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
          .map { it[Keys.BIOMETRIC_ENABLED] ?: false }

      val monitoringEnabled: Flow<Boolean> = context.dataStore.data
          .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
          .map { it[Keys.MONITORING_ENABLED] ?: true }

      val encryptionEnabled: Flow<Boolean> = context.dataStore.data
          .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
          .map { it[Keys.ENCRYPTION_ENABLED] ?: true }

      val lockOnBackground: Flow<Boolean> = context.dataStore.data
          .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
          .map { it[Keys.LOCK_ON_BACKGROUND] ?: false }

      val isFirstLaunch: Flow<Boolean> = context.dataStore.data
          .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
          .map { it[Keys.FIRST_LAUNCH] ?: true }

      suspend fun setDarkMode(value: Boolean) = context.dataStore.edit { it[Keys.DARK_MODE] = value }
      suspend fun setBiometricEnabled(value: Boolean) = context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = value }
      suspend fun setMonitoringEnabled(value: Boolean) = context.dataStore.edit { it[Keys.MONITORING_ENABLED] = value }
      suspend fun setEncryptionEnabled(value: Boolean) = context.dataStore.edit { it[Keys.ENCRYPTION_ENABLED] = value }
      suspend fun setLockOnBackground(value: Boolean) = context.dataStore.edit { it[Keys.LOCK_ON_BACKGROUND] = value }
      suspend fun setFirstLaunch(value: Boolean) = context.dataStore.edit { it[Keys.FIRST_LAUNCH] = value }
  }