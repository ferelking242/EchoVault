package com.aivos.echovault.presentation.ui

  import androidx.compose.animation.*
  import androidx.compose.animation.core.*
  import androidx.compose.foundation.layout.padding
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.*
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.platform.LocalContext
  import androidx.hilt.navigation.compose.hiltViewModel
  import androidx.navigation.NavDestination.Companion.hierarchy
  import androidx.navigation.NavGraph.Companion.findStartDestination
  import androidx.navigation.compose.*
  import com.aivos.echovault.presentation.ui.screens.*
  import com.aivos.echovault.presentation.viewmodel.MainViewModel

  sealed class Screen(val route: String, val label: String) {
      object Home : Screen("home", "History")
      object Search : Screen("search", "Search")
      object Favorites : Screen("favorites", "Favorites")
      object Stats : Screen("stats", "Stats")
      object Settings : Screen("settings", "Settings")
      object Detail : Screen("detail/{entryId}", "Detail") {
          fun createRoute(id: Long) = "detail/$id"
      }
  }

  @Composable
  fun EchoVaultNavHost(
      isLocked: Boolean,
      onUnlock: () -> Unit,
      viewModel: MainViewModel
  ) {
      if (isLocked) {
          LockScreen(onUnlock = onUnlock)
          return
      }

      val navController = rememberNavController()
      val navItems = listOf(Screen.Home, Screen.Search, Screen.Favorites, Screen.Stats, Screen.Settings)

      Scaffold(
          bottomBar = {
              NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                  val navBackStackEntry by navController.currentBackStackEntryAsState()
                  val currentDest = navBackStackEntry?.destination
                  navItems.forEach { screen ->
                      val selected = currentDest?.hierarchy?.any { it.route == screen.route } == true
                      NavigationBarItem(
                          icon = {
                              Icon(
                                  imageVector = when (screen) {
                                      Screen.Home -> Icons.Filled.History
                                      Screen.Search -> Icons.Filled.Search
                                      Screen.Favorites -> Icons.Filled.Star
                                      Screen.Stats -> Icons.Filled.BarChart
                                      Screen.Settings -> Icons.Filled.Settings
                                      else -> Icons.Filled.Home
                                  },
                                  contentDescription = screen.label
                              )
                          },
                          label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) },
                          selected = selected,
                          onClick = {
                              navController.navigate(screen.route) {
                                  popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                  launchSingleTop = true; restoreState = true
                              }
                          }
                      )
                  }
              }
          }
      ) { innerPadding ->
          NavHost(
              navController = navController,
              startDestination = Screen.Home.route,
              modifier = Modifier.padding(innerPadding),
              enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 8 } },
              exitTransition = { fadeOut(tween(200)) },
              popEnterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -it / 8 } },
              popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(220)) { it / 8 } }
          ) {
              composable(Screen.Home.route) { HomeScreen(navController = navController) }
              composable(Screen.Search.route) { SearchScreen(navController = navController) }
              composable(Screen.Favorites.route) { FavoritesScreen(navController = navController) }
              composable(Screen.Stats.route) { StatsScreen() }
              composable(Screen.Settings.route) { SettingsScreen() }
              composable(Screen.Detail.route) { backStackEntry ->
                  val entryId = backStackEntry.arguments?.getString("entryId")?.toLongOrNull() ?: return@composable
                  EntryDetailScreen(entryId = entryId, onBack = { navController.popBackStack() })
              }
          }
      }
  }