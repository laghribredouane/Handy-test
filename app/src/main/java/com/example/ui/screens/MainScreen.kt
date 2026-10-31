package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.MainViewModel
import com.example.ui.components.NetworkLockDialog
import com.example.ui.components.RatBadge
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.RfGreen
import com.example.ui.theme.RfRed
import com.example.ui.theme.RfYellow

enum class HandyNavTab(val label: String, val icon: ImageVector) {
    CELL("Cellule", Icons.Default.CellTower),
    MAP("Carte", Icons.Default.Map),
    GRAPHS("Graphes", Icons.Default.ShowChart),
    SPEED("Débit", Icons.Default.Speed),
    SESSIONS("Sessions", Icons.Default.Folder)
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val servingCell by viewModel.servingCell.collectAsState()
    val neighbors by viewModel.neighbors.collectAsState()
    val gpsLocation by viewModel.gpsLocation.collectAsState()
    val throughput by viewModel.throughput.collectAsState()
    val isSimulationMode by viewModel.isSimulationMode.collectAsState()
    val rsrpHistory by viewModel.rsrpHistory.collectAsState()
    val sinrHistory by viewModel.sinrHistory.collectAsState()

    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDurationSeconds.collectAsState()
    val currentSamples by viewModel.currentSessionSamples.collectAsState()
    val lockSettings by viewModel.lockSettings.collectAsState()
    val diagnosticState by viewModel.diagnosticState.collectAsState()
    var showLockDialog by remember { mutableStateOf(false) }

    val savedSessions by viewModel.savedSessions.collectAsState()
    val selectedSession by viewModel.selectedSession.collectAsState()
    val selectedSamples by viewModel.selectedSessionSamples.collectAsState()

    // Permissions Request Logic
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fineLoc = perms[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        hasLocationPermission = fineLoc
        if (fineLoc) {
            viewModel.measurementManager.startRealHardwareMonitoring()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
                )
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CyberBlue.copy(alpha = 0.15f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CellTower,
                                    contentDescription = "NetField",
                                    tint = CyberBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "NETFIELD HANDY",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Mesure & Drive Test RF",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // Lock & Recording Action Buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { showLockDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lockSettings.isLockActive) RfYellow else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .testTag("top_lock_button")
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Verrouillage Réseau",
                                tint = if (lockSettings.isLockActive) Color(0xFF261900) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (lockSettings.isLockActive) lockSettings.ratLock.techName else "Verrou",
                                color = if (lockSettings.isLockActive) Color(0xFF261900) else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (isRecording) {
                            Surface(
                                color = RfRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .clickable { viewModel.stopRecording() }
                                    .padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(RfRed, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val mins = recordingDuration / 60
                                    val secs = recordingDuration % 60
                                    Text(
                                        text = "REC ${String.format("%02d:%02d", mins, secs)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = RfRed
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.Stop, contentDescription = "Arrêter", tint = RfRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.startRecording() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("top_rec_button")
                            ) {
                                Icon(Icons.Default.FiberManualRecord, contentDescription = "REC", tint = Color(0xFF00363F), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("REC", color = Color(0xFF00363F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Sub-Bar: Simulation Mode Toggle & Status
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isSimulationMode) "MODE SIMULATION TERRAIN (ACTIF)" else "CAPTEURS RÉELS TÉLÉPHONIE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isSimulationMode) RfYellow else RfGreen
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Simu",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = isSimulationMode,
                                onCheckedChange = { viewModel.toggleSimulation(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = RfYellow,
                                    checkedTrackColor = RfYellow.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.testTag("simulation_switch")
                            )
                        }
                    }
                }

                // Permission Warning Banner if not granted
                if (!hasLocationPermission && !isSimulationMode) {
                    Surface(
                        color = RfYellow.copy(alpha = 0.18f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Security, contentDescription = "Permission", tint = RfYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Activez la localisation pour la téléphonie et le GPS",
                                    style = MaterialTheme.typography.labelSmall.copy(color = RfYellow, fontSize = 11.sp)
                                )
                            }
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.READ_PHONE_STATE
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RfYellow),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Autoriser", color = Color(0xFF261900), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar")
            ) {
                HandyNavTab.values().forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberBlue,
                            selectedTextColor = CyberBlue,
                            indicatorColor = CyberBlue.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> CellTelemetryScreen(
                    serving = servingCell,
                    neighbors = neighbors,
                    throughput = throughput,
                    diagnosticState = diagnosticState,
                    lockSettings = lockSettings,
                    onStartDiagnostic = { viewModel.runNetworkDiagnostic(it) },
                    onCancelDiagnostic = { viewModel.cancelNetworkDiagnostic() },
                    onOpenLockDialog = { showLockDialog = true }
                )
                1 -> DriveMapScreen(
                    currentGps = gpsLocation,
                    servingCell = servingCell,
                    samples = if (isRecording) currentSamples else selectedSamples,
                    isRecording = isRecording,
                    recordingDurationSeconds = recordingDuration,
                    onStartRecording = { viewModel.startRecording() },
                    onStopRecording = { viewModel.stopRecording() }
                )
                2 -> RfGraphsScreen(
                    serving = servingCell,
                    rsrpHistory = rsrpHistory,
                    sinrHistory = sinrHistory
                )
                3 -> ThroughputTestScreen(
                    serving = servingCell,
                    throughput = throughput,
                    onRunPingTest = { viewModel.runPingTest() },
                    onRunSpeedTest = { viewModel.runSpeedTest() }
                )
                4 -> SessionsScreen(
                    sessions = savedSessions,
                    selectedSession = selectedSession,
                    selectedSamples = selectedSamples,
                    onSelectSession = { viewModel.selectSession(it) },
                    onDeleteSession = { viewModel.deleteSession(it) },
                    onExportCsv = { sessionId, onReady ->
                        viewModel.exportCsv(sessionId, onReady)
                    }
                )
            }
        }
    }

    if (showLockDialog) {
        NetworkLockDialog(
            currentSettings = lockSettings,
            onApplySettings = { viewModel.setNetworkLocks(it) },
            onDismiss = { showLockDialog = false }
        )
    }
}
