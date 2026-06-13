package com.aivos.echovault.presentation.ui.screens

  import androidx.compose.animation.*
  import androidx.compose.animation.core.*
  import androidx.compose.foundation.*
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.lazy.*
  import androidx.compose.foundation.shape.*
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.*
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.*
  import androidx.compose.ui.draw.*
  import androidx.compose.ui.graphics.*
  import androidx.compose.ui.hapticfeedback.HapticFeedbackType
  import androidx.compose.ui.platform.*
  import androidx.compose.ui.text.font.FontFamily
  import androidx.compose.ui.text.style.TextOverflow
  import androidx.compose.ui.unit.*
  import androidx.hilt.navigation.compose.hiltViewModel
  import androidx.navigation.NavController
  import com.aivos.echovault.data.db.entity.ClipboardEntryEntity
  import com.aivos.echovault.data.db.entity.ContentType
  import com.aivos.echovault.presentation.ui.Screen
  import com.aivos.echovault.presentation.ui.theme.*
  import com.aivos.echovault.presentation.viewmodel.*
  import java.text.SimpleDateFormat
  import java.util.*

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
      val uiState by viewModel.uiState.collectAsState()

      Scaffold(
          topBar = {
              TopAppBar(
                  title = {
                      Column {
                          Text("EchoVault", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                          Text("Clipboard History", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                      }
                  },
                  actions = {
                      AnimatedContent(uiState.entries.size, label = "count") { count ->
                          Badge { Text("$count") }
                      }
                      Spacer(Modifier.width(16.dp))
                  },
                  colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
              )
          }
      ) { padding ->
          Column(Modifier.padding(padding)) {
              TimeFilterRow(selected = uiState.selectedFilter, onSelect = viewModel::setFilter)
              if (uiState.isLoading) {
                  Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                      CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                  }
              } else if (uiState.entries.isEmpty()) {
                  EmptyState()
              } else {
                  LazyColumn(
                      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                      verticalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                      items(uiState.entries, key = { it.id }) { entry ->
                          AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically { it / 2 }) {
                              ClipboardEntryCard(
                                  entry = entry,
                                  onClick = { navController.navigate(Screen.Detail.createRoute(entry.id)) },
                                  onFavorite = { viewModel.toggleFavorite(entry) },
                                  onDelete = { viewModel.deleteEntry(entry) }
                              )
                          }
                      }
                      item { Spacer(Modifier.height(16.dp)) }
                  }
              }
          }
      }
  }

  @Composable
  fun TimeFilterRow(selected: TimeFilter, onSelect: (TimeFilter) -> Unit) {
      val filters = listOf(TimeFilter.ALL to "All", TimeFilter.TODAY to "Today", TimeFilter.YESTERDAY to "Yesterday", TimeFilter.WEEK to "Week", TimeFilter.MONTH to "Month")
      LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
          items(filters) { (filter, label) ->
              FilterChip(
                  selected = selected == filter,
                  onClick = { onSelect(filter) },
                  label = { Text(label) },
                  colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = MaterialTheme.colorScheme.primary,
                      selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                  )
              )
          }
      }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun ClipboardEntryCard(
      entry: ClipboardEntryEntity,
      onClick: () -> Unit,
      onFavorite: () -> Unit,
      onDelete: () -> Unit
  ) {
      val clipboard = LocalClipboardManager.current
      val haptic = LocalHapticFeedback.current
      var showMenu by remember { mutableStateOf(false) }
      val sdf = remember { SimpleDateFormat("HH:mm · MMM d", Locale.getDefault()) }

      val typeColor = when (entry.contentType) {
          ContentType.URL -> ColorUrl
          ContentType.EMAIL -> ColorEmail
          ContentType.PHONE -> ColorPhone
          ContentType.OTP -> ColorOtp
          ContentType.CODE -> ColorCode
          ContentType.MULTILINE -> ColorMultiline
          else -> ColorText
      }

      Card(
          onClick = onClick,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 4.dp)
      ) {
          Column(Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                      Modifier.size(8.dp).clip(CircleShape).background(typeColor)
                  )
                  Spacer(Modifier.width(8.dp))
                  Text(
                      entry.contentType.name.lowercase().replaceFirstChar { it.uppercase() },
                      style = MaterialTheme.typography.labelSmall,
                      color = typeColor
                  )
                  Spacer(Modifier.weight(1f))
                  Text(sdf.format(Date(entry.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                  Box {
                      IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                          Icon(Icons.Filled.MoreVert, contentDescription = null, modifier = Modifier.size(16.dp))
                      }
                      DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                          DropdownMenuItem(
                              text = { Text("Copy") },
                              leadingIcon = { Icon(Icons.Filled.ContentCopy, null) },
                              onClick = {
                                  clipboard.setText(androidx.compose.ui.text.AnnotatedString(entry.content))
                                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                  showMenu = false
                              }
                          )
                          DropdownMenuItem(
                              text = { Text(if (entry.isFavorite) "Unfavorite" else "Favorite") },
                              leadingIcon = { Icon(if (entry.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder, null) },
                              onClick = { onFavorite(); showMenu = false }
                          )
                          DropdownMenuItem(
                              text = { Text("Delete") },
                              leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) },
                              onClick = { onDelete(); showMenu = false }
                          )
                      }
                  }
              }
              Spacer(Modifier.height(8.dp))
              Text(
                  entry.content,
                  style = if (entry.contentType == ContentType.CODE) MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                          else MaterialTheme.typography.bodyMedium,
                  maxLines = 3,
                  overflow = TextOverflow.Ellipsis,
                  color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(Modifier.height(8.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Text("${entry.charCount} chars", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
                  if (entry.isFavorite) Icon(Icons.Filled.Star, null, modifier = Modifier.size(12.dp), tint = EchoAmber)
                  if (entry.isPinned) Icon(Icons.Filled.PushPin, null, modifier = Modifier.size(12.dp), tint = EchoViolet)
              }
          }
      }
  }

  @Composable
  fun EmptyState() {
      Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Icon(Icons.Filled.ContentPaste, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(0.3f))
              Text("No clipboard entries yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
              Text("Copy something to get started!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.3f))
          }
      }
  }