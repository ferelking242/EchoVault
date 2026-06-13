package com.aivos.echovault.presentation.ui.screens

  import androidx.compose.animation.core.*
  import androidx.compose.foundation.*
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.shape.*
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.*
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.*
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.graphics.Brush
  import androidx.compose.ui.unit.dp
  import androidx.hilt.navigation.compose.hiltViewModel
  import com.aivos.echovault.presentation.ui.theme.*
  import com.aivos.echovault.presentation.viewmodel.StatsViewModel

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
      val state by viewModel.uiState.collectAsState()
      val maxActivity = (state.weekActivity.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)

      Scaffold(
          topBar = {
              TopAppBar(
                  title = { Text("Statistics", style = MaterialTheme.typography.titleLarge) },
                  colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
              )
          }
      ) { padding ->
          Column(
              modifier = Modifier.verticalScroll(rememberScrollState()).padding(padding).padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
              Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                  StatCard(Modifier.weight(1f), "Total Copies", state.totalEntries.toString(), Icons.Filled.ContentCopy, EchoViolet)
                  StatCard(Modifier.weight(1f), "Today", state.todayCount.toString(), Icons.Filled.Today, EchoTeal)
              }

              Card(
                  shape = RoundedCornerShape(20.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                  modifier = Modifier.fillMaxWidth()
              ) {
                  Column(Modifier.padding(20.dp)) {
                      Text("Weekly Activity", style = MaterialTheme.typography.titleMedium)
                      Spacer(Modifier.height(16.dp))
                      if (state.weekActivity.isNotEmpty()) {
                          Row(
                              modifier = Modifier.fillMaxWidth().height(120.dp),
                              horizontalArrangement = Arrangement.SpaceEvenly,
                              verticalAlignment = Alignment.Bottom
                          ) {
                              state.weekActivity.forEach { day ->
                                  val heightFraction = day.count.toFloat() / maxActivity
                                  val animHeight by animateFloatAsState(heightFraction, tween(600), label = "bar")
                                  Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                      if (day.count > 0) Text(day.count.toString(), style = MaterialTheme.typography.labelSmall, color = EchoViolet)
                                      Spacer(Modifier.height(4.dp))
                                      Box(
                                          Modifier
                                              .width(28.dp)
                                              .height((100 * animHeight).coerceAtLeast(4f).dp)
                                              .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                              .background(Brush.verticalGradient(listOf(EchoVioletLight, EchoViolet)))
                                      )
                                      Spacer(Modifier.height(6.dp))
                                      Text(day.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                                  }
                              }
                          }
                      } else {
                          Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                              Text("No data yet", color = MaterialTheme.colorScheme.onSurface.copy(0.3f))
                          }
                      }
                  }
              }

              Card(
                  shape = RoundedCornerShape(20.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                  modifier = Modifier.fillMaxWidth()
              ) {
                  Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                      Text("Quick Stats", style = MaterialTheme.typography.titleMedium)
                      InfoRow(Icons.Filled.Storage, "Entries stored", "${state.totalEntries} total")
                      InfoRow(Icons.Filled.CalendarToday, "Today's copies", "${state.todayCount}")
                  }
              }
          }
      }
  }

  @Composable
  fun StatCard(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color) {
      Card(
          modifier = modifier, shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
      ) {
          Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
              Text(value, style = MaterialTheme.typography.headlineMedium, color = color)
              Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
          }
      }
  }

  @Composable
  fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
          Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
          Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
      }
  }