package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiagnosticPhase
import com.example.model.DiagnosticServer
import com.example.model.NetworkDiagnosticState
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.RfGreen
import com.example.ui.theme.RfPurple
import com.example.ui.theme.RfRed
import com.example.ui.theme.RfYellow

/**
 * Real-time network diagnostic card for the dashboard performing
 * speed test and ping measurement to a target server with live progress indicator.
 */
@Composable
fun NetworkDiagnosticCard(
    diagnosticState: NetworkDiagnosticState,
    onStartDiagnostic: (DiagnosticServer) -> Unit,
    onCancelDiagnostic: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedServer by remember { mutableStateOf(diagnosticState.server) }

    val animatedProgress by animateFloatAsState(
        targetValue = diagnosticState.progress,
        animationSpec = tween(durationMillis = 300),
        label = "diag_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("network_diagnostic_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = CyberBlue.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = "Diagnostic Réseau",
                                tint = CyberBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DIAGNOSTIC & TEST RÉSEAU",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "Ping, Gigue, Perte & Débit descendant",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                if (diagnosticState.isRunning) {
                    Surface(
                        color = RfYellow.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = RfYellow
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EN COURS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RfYellow,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Target Server Selection Pills
            Text(
                text = "SERVEUR CIBLE :",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DiagnosticServer.DEFAULT_SERVERS) { server ->
                    val isSelected = selectedServer.host == server.host
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) CyberBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) CyberBlue else Color.Transparent
                        ),
                        modifier = Modifier.clickable(enabled = !diagnosticState.isRunning) {
                            selectedServer = server
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = if (isSelected) CyberBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = server.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CyberBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = server.host,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Progress Bar & Phase Status
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = diagnosticState.statusMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (diagnosticState.isRunning) CyberBlue else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${(diagnosticState.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyberBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Animated Multi-Stage Progress Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    diagnosticState.progress >= 0.99f -> RfGreen
                                    diagnosticState.phase == DiagnosticPhase.SPEED_TEST -> CyberBlue
                                    else -> RfYellow
                                }
                            )
                    )
                }

                // Phase Markers
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "1. Ping & Gigue (0-50%)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = if (diagnosticState.progress in 0.01f..0.5f) CyberBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "2. Débit Réel (50-100%)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = if (diagnosticState.progress > 0.5f) CyberBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Results Grid (Ping, Jitter, Packet Loss, Speed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryParamItem(
                    label = "Latence RTT",
                    value = "${diagnosticState.pingMs}",
                    unit = "ms",
                    modifier = Modifier.weight(1f)
                )
                TelemetryParamItem(
                    label = "Gigue",
                    value = "${diagnosticState.jitterMs}",
                    unit = "ms",
                    modifier = Modifier.weight(1f)
                )
                TelemetryParamItem(
                    label = "Pertes",
                    value = "${diagnosticState.packetLossPercent.toInt()}",
                    unit = "%",
                    modifier = Modifier.weight(1f)
                )
                TelemetryParamItem(
                    label = "Débit Desc.",
                    value = String.format("%.1f", diagnosticState.downloadSpeedMbps),
                    unit = "Mbps",
                    modifier = Modifier.weight(1.2f)
                )
            }

            // Verdict Chip if test completed
            if (diagnosticState.phase == DiagnosticPhase.COMPLETED) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = RfGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verdict",
                            tint = RfGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "VERDICT QUALITÉ RÉSEAU :",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = RfGreen
                                )
                            )
                            Text(
                                text = diagnosticState.verdict,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (diagnosticState.isRunning) {
                    OutlinedButton(
                        onClick = onCancelDiagnostic,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("cancel_diagnostic_button")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Arrêter", tint = RfRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Arrêter le Test", color = RfRed)
                    }
                } else {
                    Button(
                        onClick = { onStartDiagnostic(selectedServer) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("run_diagnostic_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Lancer", tint = Color(0xFF00363F), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lancer le Diagnostic",
                            color = Color(0xFF00363F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
