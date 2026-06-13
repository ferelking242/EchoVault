package com.aivos.echovault.presentation.ui.screens

  import androidx.compose.animation.*
  import androidx.compose.foundation.*
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.shape.*
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.*
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.*
  import androidx.compose.ui.hapticfeedback.HapticFeedbackType
  import androidx.compose.ui.platform.*
  import androidx.compose.ui.text.font.FontFamily
  import androidx.compose.ui.unit.dp
  import androidx.hilt.navigation.compose.hiltViewModel
  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.aivos.echovault.data.db.entity.ClipboardEntryEntity
  import com.aivos.echovault.data.db.entity.ContentType
  import com.aivos.echovault.data.repository.ClipboardRepository
  import com.aivos.echovault.presentation.ui.theme.*
  import dagger.hilt.android.lifecycle.HiltViewModel
  import kotlinx.coroutines.flow.*
  import kotlinx.coroutines.launch
  import java.text.SimpleDateFormat
  import java.util.*
  import javax.inject.Inject

  @HiltViewModel
  class EntryDetailViewModel @Inject constructor(private val repository: ClipboardRepository) : ViewModel() {
      private val _entry = MutableStateFlow<ClipboardEntryEntity?>(null)
      val entry: StateFlow<ClipboardEntryEntity?> = _entry.asStateFlow()
      fun load(id: Long) = viewModelScope.launch { _entry.value = repository.getEntryById(id) }
      fun toggleFavorite() = viewModelScope.launch { _entry.value?.let { repository.toggleFavorite(it); _entry.value = repository.getEntryById(it.id) } }
      fun togglePin() = viewModelScope.launch { _entry.value?.let { repository.togglePin(it); _entry.value = repository.getEntryById(it.id) } }
      fun delete(onDone: () -> Unit) = viewModelScope.launch { _entry.value?.let { repository.deleteEntry(it); onDone() } }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun EntryDetailScreen(entryId: Long, onBack: () -> Unit, viewModel: EntryDetailViewModel = hiltViewModel()) {
      val entry by viewModel.entry.collectAsState()
      val clipboard = LocalClipboardManager.current
      val haptic = LocalHapticFeedback.current
      val sdf = remember { SimpleDateFormat("EEEE, MMMM d yyyy · HH:mm:ss", Locale.getDefault()) }
      var showDeleteDialog by remember { mutableStateOf(false) }
      var copied by remember { mutableStateOf(false) }

      LaunchedEffect(entryId) { viewModel.load(entryId) }

      Scaffold(
          topBar = {
              TopAppBar(
                  title = { Text("Entry Detail", style = MaterialTheme.typography.titleLarge) },
                  navigationIcon = {
                      IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                  },
                  actions = {
                      entry?.let { e ->
                          IconButton(onClick = { viewModel.togglePin() }) {
                              Icon(if (e.isPinned) Icons.Filled.PushPin else Icons.Filled.PushPin, null, tint = if (e.isPinned) EchoViolet else MaterialTheme.colorScheme.onSurface.copy(0.5f))
                          }
                          IconButton(onClick = { viewModel.toggleFavorite() }) {
                              Icon(if (e.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder, null, tint = if (e.isFavorite) EchoAmber else MaterialTheme.colorScheme.onSurface.copy(0.5f))
                          }
                          IconButton(onClick = { showDeleteDialog = true }) {
                              Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
                          }
                      }
                  },
                  colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
              )
          },
          floatingActionButton = {
              ExtendedFloatingActionButton(
                  onClick = {
                      entry?.let {
                          clipboard.setText(androidx.compose.ui.text.AnnotatedString(it.content))
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                          copied = true
                      }
                  },
                  containerColor = MaterialTheme.colorScheme.primary
              ) {
                  Icon(if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy, null)
                  Spacer(Modifier.width(8.dp))
                  Text(if (copied) "Copied!" else "Copy to clipboard")
              }
          }
      ) { padding ->
          entry?.let { e ->
              val typeColor = when (e.contentType) {
                  ContentType.URL -> ColorUrl; ContentType.EMAIL -> ColorEmail; ContentType.PHONE -> ColorPhone
                  ContentType.OTP -> ColorOtp; ContentType.CODE -> ColorCode; ContentType.MULTILINE -> ColorMultiline
                  else -> ColorText
              }
              Column(
                  Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
                  verticalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                  Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = typeColor.copy(0.1f)), modifier = Modifier.fillMaxWidth()) {
                      Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                              Box(Modifier.size(10.dp).background(typeColor, CircleShape))
                              Text(e.contentType.name, style = MaterialTheme.typography.labelMedium, color = typeColor)
                          }
                          Text(
                              e.content,
                              style = if (e.contentType == ContentType.CODE) MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace) else MaterialTheme.typography.bodyLarge,
                              color = MaterialTheme.colorScheme.onSurface
                          )
                      }
                  }
                  Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
                      Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                          Text("Details", style = MaterialTheme.typography.titleMedium)
                          InfoRow(Icons.Filled.AccessTime, "Copied at", sdf.format(Date(e.timestamp)))
                          InfoRow(Icons.Filled.TextFields, "Characters", e.charCount.toString())
                          InfoRow(Icons.Filled.Category, "Type", e.contentType.name)
                          InfoRow(Icons.Filled.Star, "Favorited", if (e.isFavorite) "Yes" else "No")
                          InfoRow(Icons.Filled.PushPin, "Pinned", if (e.isPinned) "Yes" else "No")
                      }
                  }
              }
          } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator()
          }

          if (showDeleteDialog) {
              AlertDialog(
                  onDismissRequest = { showDeleteDialog = false },
                  title = { Text("Delete entry?") },
                  text = { Text("This entry will be permanently deleted.") },
                  confirmButton = {
                      TextButton(onClick = { viewModel.delete { onBack() }; showDeleteDialog = false }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                  },
                  dismissButton = {
                      TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                  }
              )
          }
      }
  }