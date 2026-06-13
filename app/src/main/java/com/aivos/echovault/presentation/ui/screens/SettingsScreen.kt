package com.aivos.echovault.presentation.ui.screens

  import android.content.Intent
  import android.net.Uri
  import androidx.activity.compose.rememberLauncherForActivityResult
  import androidx.activity.result.contract.ActivityResultContracts
  import androidx.compose.animation.*
  import androidx.compose.foundation.*
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.shape.*
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.*
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.*
  import androidx.compose.ui.graphics.vector.ImageVector
  import androidx.compose.ui.platform.LocalContext
  import androidx.compose.ui.unit.dp
  import androidx.hilt.navigation.compose.hiltViewModel
  import com.aivos.echovault.presentation.ui.theme.*
  import com.aivos.echovault.presentation.viewmodel.SettingsViewModel
  import kotlinx.coroutines.launch

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
      val state by viewModel.uiState.collectAsState()
      val scope = rememberCoroutineScope()
      val context = LocalContext.current
      var showDeleteDialog by remember { mutableStateOf(false) }
      var showExportSheet by remember { mutableStateOf(false) }
      var snackbarMsg by remember { mutableStateOf("") }
      val snackbarState = remember { SnackbarHostState() }

      LaunchedEffect(snackbarMsg) {
          if (snackbarMsg.isNotEmpty()) { snackbarState.showSnackbar(snackbarMsg); snackbarMsg = "" }
      }

      val exportTxtLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
          uri?.let { scope.launch { context.contentResolver.openOutputStream(it)?.use { os -> os.write(viewModel.exportTxt().toByteArray()) }; snackbarMsg = "Exported as TXT" } }
      }
      val exportJsonLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
          uri?.let { scope.launch { context.contentResolver.openOutputStream(it)?.use { os -> os.write(viewModel.exportJson().toByteArray()) }; snackbarMsg = "Exported as JSON" } }
      }
      val exportCsvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
          uri?.let { scope.launch { context.contentResolver.openOutputStream(it)?.use { os -> os.write(viewModel.exportCsv().toByteArray()) }; snackbarMsg = "Exported as CSV" } }
      }

      Scaffold(
          topBar = {
              TopAppBar(
                  title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                  colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
              )
          },
          snackbarHost = { SnackbarHost(snackbarState) }
      ) { padding ->
          Column(
              modifier = Modifier.verticalScroll(rememberScrollState()).padding(padding).padding(horizontal = 16.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
              Spacer(Modifier.height(4.dp))

              // Appearance
              SettingsSection(title = "Appearance", icon = Icons.Filled.Palette) {
                  SettingsToggleRow(
                      icon = Icons.Filled.DarkMode,
                      title = "Dark Mode",
                      subtitle = "Switch between light and dark theme",
                      checked = state.darkMode,
                      onCheckedChange = viewModel::setDarkMode
                  )
              }

              // Monitoring
              SettingsSection(title = "Clipboard Monitoring", icon = Icons.Filled.ContentPaste) {
                  SettingsToggleRow(
                      icon = Icons.Filled.RadioButtonChecked,
                      title = "Enable Monitoring",
                      subtitle = "Capture clipboard entries in the background",
                      checked = state.monitoringEnabled,
                      onCheckedChange = viewModel::setMonitoring,
                      accentColor = EchoGreen
                  )
              }

              // Security
              SettingsSection(title = "Security", icon = Icons.Filled.Security) {
                  SettingsToggleRow(
                      icon = Icons.Filled.Fingerprint,
                      title = "Biometric Lock",
                      subtitle = if (state.biometricAvailable) "Lock app with fingerprint or face ID" else "No biometric hardware available",
                      checked = state.biometricEnabled,
                      onCheckedChange = viewModel::setBiometric,
                      enabled = state.biometricAvailable,
                      accentColor = EchoViolet
                  )
                  AnimatedVisibility(visible = state.biometricEnabled) {
                      SettingsToggleRow(
                          icon = Icons.Filled.Lock,
                          title = "Lock on Background",
                          subtitle = "Re-lock when app goes to background",
                          checked = state.lockOnBackground,
                          onCheckedChange = viewModel::setLockOnBackground,
                          accentColor = EchoViolet
                      )
                  }
                  SettingsToggleRow(
                      icon = Icons.Filled.EnhancedEncryption,
                      title = "Data Encryption",
                      subtitle = if (state.encryptionEnabled) "All clipboard data is encrypted at rest" else "Encryption disabled — data stored in plain text",
                      checked = state.encryptionEnabled,
                      onCheckedChange = viewModel::setEncryption,
                      accentColor = EchoTeal
                  )
                  SettingsInfoRow(
                      icon = Icons.Filled.Shield,
                      title = "Encryption Status",
                      value = if (state.encryptionEnabled) "Active" else "Inactive",
                      valueColor = if (state.encryptionEnabled) EchoGreen else EchoRed
                  )
              }

              // Data
              SettingsSection(title = "Data Management", icon = Icons.Filled.Storage) {
                  SettingsActionRow(
                      icon = Icons.Filled.FileUpload,
                      title = "Export Data",
                      subtitle = "Export as TXT, JSON, or CSV",
                      onClick = { showExportSheet = true }
                  )
                  SettingsActionRow(
                      icon = Icons.Filled.DeleteForever,
                      title = "Delete All Data",
                      subtitle = "Permanently remove all clipboard history",
                      onClick = { showDeleteDialog = true },
                      iconTint = MaterialTheme.colorScheme.error,
                      titleColor = MaterialTheme.colorScheme.error
                  )
              }

              // About
              SettingsSection(title = "About", icon = Icons.Filled.Info) {
                  SettingsInfoRow(Icons.Filled.Apps, "App", "EchoVault")
                  SettingsInfoRow(Icons.Filled.Numbers, "Version", "1.0.0")
                  SettingsInfoRow(Icons.Filled.Business, "Developer", "AIVOS")
                  SettingsInfoRow(Icons.Filled.Tag, "Tagline", "Never lose a copy again.")
              }

              Spacer(Modifier.height(32.dp))
          }
      }

      // Export Sheet
      if (showExportSheet) {
          ModalBottomSheet(onDismissRequest = { showExportSheet = false }) {
              Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                  Text("Export Data", style = MaterialTheme.typography.titleLarge)
                  Text("Choose export format:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                  Spacer(Modifier.height(8.dp))
                  ExportButton("Export as TXT", Icons.Filled.TextFields, EchoGreen) { exportTxtLauncher.launch("echovault_export.txt"); showExportSheet = false }
                  ExportButton("Export as JSON", Icons.Filled.Code, EchoTeal) { exportJsonLauncher.launch("echovault_export.json"); showExportSheet = false }
                  ExportButton("Export as CSV", Icons.Filled.TableChart, EchoAmber) { exportCsvLauncher.launch("echovault_export.csv"); showExportSheet = false }
                  Spacer(Modifier.height(24.dp))
              }
          }
      }

      // Delete Dialog
      if (showDeleteDialog) {
          AlertDialog(
              onDismissRequest = { showDeleteDialog = false },
              icon = { Icon(Icons.Filled.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
              title = { Text("Delete All Data?") },
              text = { Text("This will permanently delete ALL clipboard history. This action cannot be undone.") },
              confirmButton = {
                  Button(
                      onClick = { viewModel.deleteAllData(); showDeleteDialog = false; snackbarMsg = "All data deleted" },
                      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                  ) { Text("Delete All") }
              },
              dismissButton = { OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
          )
      }
  }

  @Composable
  fun SettingsSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
      Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
      ) {
          Column(Modifier.padding(4.dp)) {
              Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                  Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
              }
              HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.3f))
              content()
          }
      }
  }

  @Composable
  fun SettingsToggleRow(
      icon: ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
      enabled: Boolean = true, accentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
  ) {
      Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
          Icon(icon, null, tint = if (enabled) accentColor else MaterialTheme.colorScheme.onSurface.copy(0.3f), modifier = Modifier.size(22.dp))
          Column(Modifier.weight(1f)) {
              Text(title, style = MaterialTheme.typography.bodyLarge, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(0.4f))
              Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(if (enabled) 0.5f else 0.3f))
          }
          Switch(
              checked = checked, onCheckedChange = onCheckedChange, enabled = enabled,
              colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(0.3f))
          )
      }
  }

  @Composable
  fun SettingsActionRow(
      icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit,
      iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
      titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
  ) {
      Row(
          modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
          Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
          Column(Modifier.weight(1f)) {
              Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
              Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
          }
          Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.3f))
      }
  }

  @Composable
  fun SettingsInfoRow(
      icon: ImageVector, title: String, value: String,
      valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
  ) {
      Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
          Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
          Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
          Text(value, style = MaterialTheme.typography.bodyMedium, color = valueColor)
      }
  }

  @Composable
  fun ExportButton(label: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
      OutlinedButton(
          onClick = onClick,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.dp, color.copy(0.5f))
      ) {
          Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(8.dp))
          Text(label, color = color)
      }
  }