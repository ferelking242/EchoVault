package com.aivos.echovault.presentation.ui.screens

  import androidx.compose.animation.*
  import androidx.compose.animation.core.*
  import androidx.compose.foundation.background
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.shape.CircleShape
  import androidx.compose.material.icons.Icons
  import androidx.compose.material.icons.filled.Fingerprint
  import androidx.compose.material3.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.graphics.Brush
  import androidx.compose.ui.platform.LocalContext
  import androidx.compose.ui.text.style.TextAlign
  import androidx.compose.ui.unit.dp
  import androidx.fragment.app.FragmentActivity
  import com.aivos.echovault.domain.usecase.BiometricAuthUseCase
  import com.aivos.echovault.presentation.ui.theme.EchoViolet
  import com.aivos.echovault.presentation.ui.theme.EchoVioletDark
  import com.aivos.echovault.presentation.ui.theme.EchoVioletLight

  @Composable
  fun LockScreen(onUnlock: () -> Unit) {
      val context = LocalContext.current
      var errorMsg by remember { mutableStateOf("") }

      val infiniteTransition = rememberInfiniteTransition(label = "pulse")
      val pulseScale by infiniteTransition.animateFloat(
          initialValue = 0.95f, targetValue = 1.05f, label = "pulse",
          animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse)
      )

      LaunchedEffect(Unit) {
          triggerBiometric(context, onUnlock, { errorMsg = it })
      }

      Box(
          modifier = Modifier
              .fillMaxSize()
              .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, EchoVioletDark.copy(alpha = 0.15f)))),
          contentAlignment = Alignment.Center
      ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
              Text("EchoVault", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
              Text("Never lose a copy again.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
              Spacer(Modifier.height(24.dp))
              Box(
                  modifier = Modifier
                      .size((80 * pulseScale).dp)
                      .clip(CircleShape)
                      .background(EchoViolet.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
              ) {
                  IconButton(
                      onClick = { triggerBiometric(context, onUnlock, { errorMsg = it }) },
                      modifier = Modifier.size(64.dp)
                  ) {
                      Icon(Icons.Filled.Fingerprint, contentDescription = "Unlock", modifier = Modifier.size(48.dp), tint = EchoViolet)
                  }
              }
              Text("Tap to unlock with biometrics", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
              if (errorMsg.isNotEmpty()) {
                  Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
              }
          }
      }
  }

  private fun triggerBiometric(context: android.content.Context, onUnlock: () -> Unit, onError: (String) -> Unit) {
      val activity = context as? FragmentActivity ?: return
      val useCase = BiometricAuthUseCase(context)
      useCase.authenticate(activity, onSuccess = onUnlock, onError = { onError(it) }, onFailed = { onError("Authentication failed") })
  }