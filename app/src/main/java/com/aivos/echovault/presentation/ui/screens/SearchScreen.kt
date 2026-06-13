package com.aivos.echovault.presentation.ui.screens

  import androidx.compose.animation.*
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.lazy.*
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.*
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.*
  import androidx.compose.ui.unit.dp
  import androidx.hilt.navigation.compose.hiltViewModel
  import androidx.navigation.NavController
  import com.aivos.echovault.presentation.ui.Screen
  import com.aivos.echovault.presentation.viewmodel.SearchViewModel

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun SearchScreen(navController: NavController, viewModel: SearchViewModel = hiltViewModel()) {
      val query by viewModel.query.collectAsState()
      val results by viewModel.results.collectAsState()

      Scaffold(
          topBar = {
              TopAppBar(
                  title = { Text("Search", style = MaterialTheme.typography.titleLarge) },
                  colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
              )
          }
      ) { padding ->
          Column(Modifier.padding(padding).fillMaxSize()) {
              OutlinedTextField(
                  value = query,
                  onValueChange = viewModel::setQuery,
                  placeholder = { Text("Search clipboard history...") },
                  leadingIcon = { Icon(Icons.Filled.Search, null) },
                  trailingIcon = {
                      AnimatedVisibility(visible = query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                          IconButton(onClick = { viewModel.setQuery("") }) {
                              Icon(Icons.Filled.Clear, null)
                          }
                      }
                  },
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                  shape = RoundedCornerShape(16.dp),
                  singleLine = true,
                  colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = MaterialTheme.colorScheme.primary,
                      unfocusedBorderColor = MaterialTheme.colorScheme.outline
                  )
              )
              if (results.isEmpty() && query.isNotEmpty()) {
                  Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                      Column(horizontalAlignment = Alignment.CenterHorizontally) {
                          Icon(Icons.Filled.SearchOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(0.3f))
                          Spacer(Modifier.height(12.dp))
                          Text("No results for "$query"", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                      }
                  }
              } else {
                  LazyColumn(
                      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                      verticalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                      item {
                          AnimatedContent(results.size, label = "resultCount") { count ->
                              if (count > 0) Text("$count result${if (count != 1) "s" else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                          }
                      }
                      items(results, key = { it.id }) { entry ->
                          ClipboardEntryCard(
                              entry = entry,
                              onClick = { navController.navigate(Screen.Detail.createRoute(entry.id)) },
                              onFavorite = { viewModel.toggleFavorite(entry) },
                              onDelete = { viewModel.deleteEntry(entry) }
                          )
                      }
                  }
              }
          }
      }
  }