package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.firebase.models.DiagnosticLog
import com.example.data.firebase.models.DriveSurvey
import com.example.data.firebase.repository.DiagnosticRepository
import com.example.data.firebase.repository.SurveyRepository
import com.example.model.NetworkDiagnosticState
import com.example.model.ServingCellInfo
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.SignInScreen
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@Composable
fun CloudSyncDialog(
    authViewModel: AuthViewModel,
    currentUser: FirebaseUser?,
    currentSignal: ServingCellInfo?,
    diagnosticState: NetworkDiagnosticState,
    onDismiss: () -> Unit
) {
    if (currentUser == null) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val uiState by produceState<AuthUiState>(initialValue = AuthUiState.Idle) {
                authViewModel.uiState.collect { value = it }
            }
            SignInScreen(
                authViewModel = authViewModel,
                uiState = uiState,
                onDismissOrContinueGuest = onDismiss
            )
        }
    } else {
        CloudSyncDashboardDialog(
            currentUser = currentUser,
            currentSignal = currentSignal,
            diagnosticState = diagnosticState,
            onSignOut = {
                authViewModel.signOut()
            },
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun CloudSyncDashboardDialog(
    currentUser: FirebaseUser,
    currentSignal: ServingCellInfo?,
    diagnosticState: NetworkDiagnosticState,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val surveyRepository = remember { SurveyRepository(context) }
    val diagnosticRepository = remember { DiagnosticRepository(context) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val cloudSurveys by produceState<List<DriveSurvey>>(initialValue = emptyList(), key1 = currentUser.uid) {
        surveyRepository.observeSurveys(currentUser.uid).collect { value = it }
    }

    val cloudDiagnostics by produceState<List<DiagnosticLog>>(initialValue = emptyList(), key1 = currentUser.uid) {
        diagnosticRepository.observeDiagnostics(currentUser.uid).collect { value = it }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("cloud_sync_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header with user info & sign out
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentUser.displayName ?: "Ingénieur RF",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = currentUser.email ?: "Compte Google vérifié",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Déconnexion",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Text(text = "✕", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Upload Actions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sauvegarder la session active",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            if (isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Réseau: ${currentSignal?.rat ?: "Inconnu"} • RSRP: ${currentSignal?.rsrp ?: -1} dBm • SINR: ${currentSignal?.sinr?.toInt() ?: -1} dB • PCI: ${currentSignal?.pci ?: -1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isUploading = true
                                        try {
                                            val survey = DriveSurvey(
                                                name = "Survey ${currentSignal?.rat ?: "Cellular"} - ${currentSignal?.operatorName ?: "Auto"}",
                                                networkType = currentSignal?.rat ?: "4G/5G",
                                                operator = currentSignal?.operatorName ?: "Opérateur",
                                                avgRsrp = currentSignal?.rsrp?.toDouble() ?: -95.0,
                                                avgSinr = currentSignal?.sinr?.toDouble() ?: 12.0,
                                                pci = currentSignal?.pci ?: 0,
                                                band = currentSignal?.band ?: "Auto",
                                                pointCount = 1,
                                                status = "completed"
                                            )
                                            surveyRepository.createSurvey(currentUser.uid, survey)
                                            uploadStatusMessage = "Session de mesure sauvegardée dans Firestore !"
                                        } catch (e: Exception) {
                                            uploadStatusMessage = "Erreur: ${e.localizedMessage}"
                                        } finally {
                                            isUploading = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upload_survey_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Sauvegarder Survey", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isUploading = true
                                        try {
                                            val diag = DiagnosticLog(
                                                targetServer = diagnosticState.server.host,
                                                latencyMs = diagnosticState.pingMs.toDouble(),
                                                downloadSpeedMbps = diagnosticState.downloadSpeedMbps.toDouble(),
                                                jitterMs = diagnosticState.jitterMs.toDouble(),
                                                packetLossPercent = diagnosticState.packetLossPercent.toDouble(),
                                                rfVerdict = diagnosticState.verdict
                                            )
                                            diagnosticRepository.saveDiagnostic(currentUser.uid, diag)
                                            uploadStatusMessage = "Résultat diagnostic sauvegardé dans Firestore !"
                                        } catch (e: Exception) {
                                            uploadStatusMessage = "Erreur: ${e.localizedMessage}"
                                        } finally {
                                            isUploading = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upload_diag_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Sauvegarder Diagnostic", fontSize = 12.sp)
                            }
                        }

                        if (uploadStatusMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = uploadStatusMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs for Surveys and Diagnostics
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Surveys (${cloudSurveys.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Diagnostics (${cloudDiagnostics.size})") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List content
                if (selectedTab == 0) {
                    if (cloudSurveys.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucun drive test sauvegardé dans le Cloud.\nCliquez sur 'Sauvegarder Survey' ci-dessus.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cloudSurveys, key = { it.id }) { survey ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = survey.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Réseau: ${survey.networkType} • ${survey.operator} • Band: ${survey.band}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "RSRP: ${survey.avgRsrp.toInt()} dBm • SINR: ${survey.avgSinr.toInt()} dB • PCI: ${survey.pci}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        surveyRepository.deleteSurvey(currentUser.uid, survey.id)
                                                    } catch (e: Exception) {
                                                        uploadStatusMessage = "Erreur suppression: ${e.localizedMessage}"
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Supprimer",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (cloudDiagnostics.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aucun test diagnostic sauvegardé.\nLancez un test sur le tableau de bord puis sauvegardez-le.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cloudDiagnostics, key = { it.id }) { diag ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Serveur: ${diag.targetServer}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "${diag.downloadSpeedMbps.toInt()} Mbps",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Ping: ${diag.latencyMs.toInt()} ms • Jitter: ${diag.jitterMs.toInt()} ms • Perte: ${diag.packetLossPercent.toInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (diag.rfVerdict.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = diag.rfVerdict,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Fermer")
                }
            }
        }
    }
}
