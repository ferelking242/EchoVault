package com.aivos.echovault.presentation.ui.screens

  import androidx.compose.animation.*
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.lazy.*
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.*
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.*
  import androidx.compose.ui.unit.dp
  import androidx.hilt.navigation.compose.hiltViewModel
  import androidx.navigation.NavController
  import com.aivos.echovault.data.repository.ClipboardRepository
  import com.aivos.echovault.presentation.ui.Screen
  import dagger.hilt.android.lifecycle.HiltViewModel
  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.aivos.echovault.data.db.entity.ClipboardEntryEntity
  import kotlinx.coroutines.flow.*
  import kotlinx.coroutines.launch
  import javax.inject.Inject

  @HiltViewModel
  class FavoritesViewModel @Inject constructor(private val repository: ClipboardRepository) : ViewModel() {
      val favorites: StateFlow<List<ClipboardEntryEntity>> = repository.getFavorites()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
      fun toggleFavorite(entry: ClipboardEntryEntity) = viewModelScope.launch { repository.toggleFavorite(entry) }
      fun deleteEntry(entry: ClipboardEntryEntity) = viewModelScope.launch { repository.deleteEntry(entry) }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun FavoritesScreen(navController: NavController, viewModel: FavoritesViewModel = hiltViewModel()) {
      val favorites by viewModel.favorites.collectAsState()

      Scaffold(
          topBar = {
              TopAppBar(
                  title = { Text("Favorites", style = MaterialTheme.typography.titleLarge) },
                  actions = {
                      AnimatedContent(favorites.size, label = "favCount") { count ->
                          Badge { Text("$count") }
                      }
                      Spacer(Modifier.width(16.dp))
                  },
                  colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
              )
          }
      ) { padding ->
          if (favorites.isEmpty()) {
              Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                      Icon(Icons.Filled.StarBorder, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(0.3f))
                      Text("No favorites yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                      Text("Star entries to see them here", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.3f))
                  }
              }
          } else {
              LazyColumn(
                  contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.padding(padding)
              ) {
                  items(favorites, key = { it.id }) { entry ->
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