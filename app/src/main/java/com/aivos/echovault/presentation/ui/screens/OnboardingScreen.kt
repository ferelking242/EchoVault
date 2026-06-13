package com.aivos.echovault.presentation.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.aivos.echovault.presentation.ui.theme.*
import com.aivos.echovault.receiver.EchoVaultDeviceAdminReceiver
import com.aivos.echovault.util.PermissionHelper

private const val ADB_COMMAND =
    "settings put secure enabled_accessibility_services com.aivos.echovault/.service.EchoVaultAccessibilityService && settings put secure accessibility_enabled 1"

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) }
    var adbMethodSelected by remember { mutableStateOf(false) }

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
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
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

            Spacer(Modifier.height(36.dp))

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

            Spacer(Modifier.height(28.dp))

            Text(step.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))

            if (step == OnboardingStep.Accessibility) {
                AccessibilityStepContent(
                    context = context,
                    adbMethodSelected = adbMethodSelected,
                    onSwitchMethod = { adbMethodSelected = it },
                    onDone = { currentStep++ }
                )
            } else {
                Text(
                    step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.65f),
                    lineHeight = 22.sp
                )

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

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        when (step) {
                            OnboardingStep.Welcome -> currentStep++
                            OnboardingStep.Notification -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else currentStep++
                            }
                            OnboardingStep.BatteryOptimization -> {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:${context.packageName}")))
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
                            else -> {}
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = step.color)
                ) {
                    Text(step.buttonText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }

                if (step.skippable && step != OnboardingStep.Done) {
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { currentStep++ }) {
                        Text("Passer cette étape", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    }
                }

                if (step == OnboardingStep.BatteryOptimization) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = { currentStep++ }) {
                        Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(16.dp), tint = EchoGreen)
                        Spacer(Modifier.width(6.dp))
                        Text("C'est déjà fait", color = EchoGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessibilityStepContent(
    context: Context,
    adbMethodSelected: Boolean,
    onSwitchMethod: (Boolean) -> Unit,
    onDone: () -> Unit
) {
    var copiedCommand by remember { mutableStateOf(false) }
    var copiedLadb by remember { mutableStateOf(false) }

    // Method tabs
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf("Standard" to false, "Via ADB ✦" to true).forEach { (label, isAdb) ->
            val selected = adbMethodSelected == isAdb
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selected) EchoViolet else Color.Transparent)
                    .clickable { onSwitchMethod(isAdb) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }
        }
    }

    Spacer(Modifier.height(20.dp))

    if (!adbMethodSelected) {
        // ── MÉTHODE STANDARD ──────────────────────────────────────
        Text(
            "Fonctionne sur la plupart des appareils Android",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        NumberedStep(1, "Ouvre les paramètres d'accessibilité", EchoViolet)
        Spacer(Modifier.height(8.dp))
        NumberedStep(2, "Touche « Applications installées »", EchoViolet)
        Spacer(Modifier.height(8.dp))
        NumberedStep(3, "Cherche EchoVault → Active le service", EchoViolet)
        Spacer(Modifier.height(8.dp))
        NumberedStep(4, "Si bloqué par « paramètres restreints » → bascule sur l'onglet ADB ✦", EchoAmber)

        Spacer(Modifier.height(4.dp))
        Surface(shape = RoundedCornerShape(12.dp), color = EchoAmber.copy(0.10f), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Info, null, tint = EchoAmber, modifier = Modifier.size(16.dp))
                Text("EchoVault ne lit que le contenu copié. Il ne surveille pas l'écran ni tes mots de passe.", style = MaterialTheme.typography.bodySmall, color = EchoAmber)
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EchoViolet)
        ) {
            Icon(Icons.Filled.Accessibility, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ouvrir l'accessibilité", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

    } else {
        // ── MÉTHODE ADB SANS FIL (Samsung / paramètres restreints) ──
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = EchoViolet.copy(0.10f),
            border = BorderStroke(1.dp, EchoViolet.copy(0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Terminal, null, tint = EchoViolet, modifier = Modifier.size(18.dp))
                    Text("Pourquoi ADB ?", fontWeight = FontWeight.SemiBold, color = EchoViolet, fontSize = 13.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Samsung et certains constructeurs bloquent l'activation de l'accessibilité pour les APK sideloadés. ADB contourne ce blocage directement, sans root, depuis ton téléphone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Steps
        AdbStep(
            number = "1",
            title = "Active les options développeur",
            detail = "Paramètres → À propos du téléphone → Numéro de build → tape 7 fois rapidement",
            color = EchoTeal
        )
        Spacer(Modifier.height(10.dp))
        AdbStep(
            number = "2",
            title = "Active le débogage sans fil",
            detail = "Paramètres → Options développeurs → Débogage sans fil → Active",
            color = EchoTeal
        )
        Spacer(Modifier.height(10.dp))
        AdbStep(
            number = "3",
            title = "Installe LADB (Local ADB Shell)",
            detail = "Une app gratuite sur le Play Store qui permet de lancer des commandes ADB directement depuis ton téléphone, sans PC.",
            color = EchoTeal,
            actionLabel = "Ouvrir Play Store",
            onAction = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.draco.ladb")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                } catch (e: Exception) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.draco.ladb")).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                }
            }
        )
        Spacer(Modifier.height(10.dp))
        AdbStep(
            number = "4",
            title = "Copie et colle cette commande dans LADB",
            detail = null,
            color = EchoTeal
        )

        Spacer(Modifier.height(8.dp))

        // Command box
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Commande ADB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    if (copiedCommand) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = EchoGreen, modifier = Modifier.size(14.dp))
                            Text("Copié !", style = MaterialTheme.typography.labelSmall, color = EchoGreen)
                        }
                    }
                }
                Divider(color = MaterialTheme.colorScheme.outline.copy(0.2f))
                Text(
                    ADB_COMMAND,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
                Divider(color = MaterialTheme.colorScheme.outline.copy(0.2f))
                TextButton(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("adb_cmd", ADB_COMMAND))
                        copiedCommand = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copier la commande")
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // LADB download link
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.onSurface.copy(0.5f), modifier = Modifier.size(16.dp))
                Text(
                    "LADB est open source (github.com/tytydraco/LADB), gratuit et sans publicité.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                    lineHeight = 17.sp
                )
            }
        }
    }

    Spacer(Modifier.height(24.dp))

    // Done button
    TextButton(
        onClick = onDone,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(18.dp), tint = EchoGreen)
        Spacer(Modifier.width(8.dp))
        Text("C'est activé, continuer →", color = EchoGreen, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
private fun NumberedStep(number: Int, text: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(26.dp).clip(CircleShape).background(color.copy(0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("$number", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
        }
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.8f), lineHeight = 20.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AdbStep(
    number: String,
    title: String,
    detail: String?,
    color: Color,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(26.dp).clip(CircleShape).background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                if (detail != null) {
                    Spacer(Modifier.height(3.dp))
                    Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.65f), lineHeight = 17.sp)
                }
                if (actionLabel != null && onAction != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onAction,
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Filled.OpenInNew, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(actionLabel, fontSize = 12.sp)
                    }
                }
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
        description = "",
        icon = Icons.Filled.Accessibility,
        color = EchoViolet,
        buttonText = ""
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
        description = "Optionnel mais recommandé : protège EchoVault contre la désinstallation forcée.",
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
