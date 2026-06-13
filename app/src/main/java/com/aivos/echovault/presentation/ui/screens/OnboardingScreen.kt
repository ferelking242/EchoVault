package com.aivos.echovault.presentation.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.aivos.echovault.presentation.ui.theme.*
import com.aivos.echovault.receiver.EchoVaultDeviceAdminReceiver
import com.aivos.echovault.util.PermissionHelper

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) }
    // Sub-step for accessibility on Android 13+ : 0 = unlock restricted, 1 = open accessibility
    var accessibilitySubStep by remember { mutableIntStateOf(0) }

    val steps = listOf(
        OnboardingStep.Welcome,
        OnboardingStep.Notification,
        OnboardingStep.Accessibility,
        OnboardingStep.BatteryOptimization,
        OnboardingStep.DeviceAdmin,
        OnboardingStep.Done
    )

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        currentStep++
    }
    val overlayLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        currentStep++
    }

    val step = steps[currentStep]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, EchoVioletDark.copy(alpha = 0.12f)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.dropLast(1).forEachIndexed { i, _ ->
                    val active = i <= currentStep
                    Box(
                        modifier = Modifier
                            .size(if (i == currentStep) 24.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (active) EchoViolet else MaterialTheme.colorScheme.outline.copy(0.3f))
                            .animateContentSize()
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Icon
            val pulse = rememberInfiniteTransition(label = "pulse")
            val scale by pulse.animateFloat(0.9f, 1.1f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")

            Box(
                modifier = Modifier
                    .size((90 * if (step == OnboardingStep.Welcome) scale else 1f).dp)
                    .clip(CircleShape)
                    .background(step.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(step.icon, null, modifier = Modifier.size(44.dp), tint = step.color)
            }

            Spacer(Modifier.height(32.dp))

            Text(step.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))

            // Special description for Accessibility step on Android 13+
            if (step == OnboardingStep.Accessibility && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Text(
                    "Sur Android 13+ et Samsung, il y a 2 étapes obligatoires :",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.65f)
                )
                Spacer(Modifier.height(16.dp))

                // Step 1 card
                AccessibilityStepCard(
                    number = "1",
                    title = "Débloquer les paramètres restreints",
                    description = "Paramètres → Apps → EchoVault\n→ menu ⋮ (3 points) → \"Autoriser les paramètres restreints\"",
                    done = accessibilitySubStep >= 1,
                    color = if (accessibilitySubStep >= 1) EchoGreen else EchoViolet
                )

                Spacer(Modifier.height(10.dp))

                // Step 2 card
                AccessibilityStepCard(
                    number = "2",
                    title = "Activer le service d'accessibilité",
                    description = "Accessibilité → Applications installées → EchoVault → Activer",
                    done = false,
                    color = if (accessibilitySubStep >= 1) EchoViolet else MaterialTheme.colorScheme.outline
                )
            } else {
                Text(
                    step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.65f),
                    lineHeight = 22.sp
                )
            }

            if (step.warning != null) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EchoAmber.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Info, null, tint = EchoAmber, modifier = Modifier.size(18.dp))
                        Text(step.warning, style = MaterialTheme.typography.bodySmall, color = EchoAmber)
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            // Action button(s)
            if (step == OnboardingStep.Accessibility && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Two-button flow for Android 13+
                if (accessibilitySubStep == 0) {
                    // Sub-step 1: Open App Info so user can allow restricted settings
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                            // Advance sub-step after launching
                            accessibilitySubStep = 1
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EchoViolet)
                    ) {
                        Icon(Icons.Filled.Settings, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Étape 1 : Ouvrir les infos de l'app", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { accessibilitySubStep = 1 },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Déjà débloqué → passer à l'étape 2", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    // Sub-step 2: Open Accessibility settings
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EchoViolet)
                    ) {
                        Icon(Icons.Filled.Accessibility, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Étape 2 : Ouvrir Accessibilité", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { accessibilitySubStep = 0 },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.ArrowBack, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Retour à l'étape 1", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { currentStep++ }) {
                    Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(16.dp), tint = EchoGreen)
                    Spacer(Modifier.width(6.dp))
                    Text("C'est activé, continuer", color = EchoGreen)
                }
            } else {
                // Standard button for other steps
                Button(
                    onClick = {
                        when (step) {
                            OnboardingStep.Welcome -> currentStep++
                            OnboardingStep.Notification -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    currentStep++
                                }
                            }
                            OnboardingStep.Accessibility -> {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            }
                            OnboardingStep.BatteryOptimization -> {
                                try {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:${context.packageName}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                }
                            }
                            OnboardingStep.DeviceAdmin -> {
                                val adminComponent = ComponentName(context, EchoVaultDeviceAdminReceiver::class.java)
                                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                    putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                        "Nécessaire pour protéger EchoVault contre les arrêts forcés et la désinstallation non autorisée.")
                                }
                                overlayLauncher.launch(intent)
                            }
                            OnboardingStep.Done -> onComplete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = step.color)
                ) {
                    Text(step.buttonText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                // Skip button for optional steps
                if (step.skippable && step != OnboardingStep.Done) {
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { currentStep++ }) {
                        Text("Passer cette étape", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                }

                // "Already done" button for battery step
                if (step == OnboardingStep.BatteryOptimization) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = { currentStep++ }) {
                        Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(16.dp), tint = EchoGreen)
                        Spacer(Modifier.width(6.dp))
                        Text("C'est déjà activé", color = EchoGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessibilityStepCard(
    number: String,
    title: String,
    description: String,
    done: Boolean,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                if (done) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = color)
                Spacer(Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f), lineHeight = 18.sp)
            }
        }
    }
}

sealed class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val buttonText: String,
    val warning: String? = null,
    val skippable: Boolean = false
) {
    object Welcome : OnboardingStep(
        title = "Bienvenue sur EchoVault",
        description = "Ton historique clipboard intelligent. EchoVault capture automatiquement tout ce que tu copies, même quand l'app est fermée.",
        icon = Icons.Filled.ContentPaste,
        color = EchoViolet,
        buttonText = "Commencer"
    )
    object Notification : OnboardingStep(
        title = "Notifications",
        description = "Autorise les notifications pour que le service de fond reste actif et visible. Sans ça, Android peut tuer l'app.",
        icon = Icons.Filled.NotificationsActive,
        color = EchoTeal,
        buttonText = "Autoriser les notifications"
    )
    object Accessibility : OnboardingStep(
        title = "Service d'accessibilité",
        description = "C'est la clé pour capturer le clipboard en arrière-plan sur Android 10+.\n\nDans la page qui s'ouvre : cherche EchoVault → active le service.",
        icon = Icons.Filled.Accessibility,
        color = EchoViolet,
        buttonText = "Ouvrir les paramètres",
        warning = "EchoVault ne lit que le contenu copié. Il ne surveille pas l'écran, ne lit pas tes mots de passe ni tes données bancaires."
    )
    object BatteryOptimization : OnboardingStep(
        title = "Optimisation batterie",
        description = "Désactive l'optimisation batterie pour EchoVault afin qu'Android ne le tue jamais en arrière-plan.",
        icon = Icons.Filled.BatteryFull,
        color = EchoAmber,
        buttonText = "Désactiver l'optimisation",
        skippable = true
    )
    object DeviceAdmin : OnboardingStep(
        title = "Administration du téléphone",
        description = "Optionnel mais recommandé : protège EchoVault contre la désinstallation forcée. L'app ne peut pas accéder à tes données personnelles avec ce droit.",
        icon = Icons.Filled.AdminPanelSettings,
        color = EchoRed,
        buttonText = "Activer l'administration",
        warning = "Ce droit empêche uniquement la désinstallation sans révocation manuelle. Il ne donne aucun accès supplémentaire à tes données.",
        skippable = true
    )
    object Done : OnboardingStep(
        title = "Tout est prêt !",
        description = "EchoVault est configuré et capture ton clipboard en permanence. Tu ne perdras plus jamais rien.",
        icon = Icons.Filled.CheckCircle,
        color = EchoGreen,
        buttonText = "Accéder à EchoVault"
    )
}
