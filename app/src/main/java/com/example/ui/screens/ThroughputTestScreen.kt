package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ServingCellInfo
import com.example.model.ThroughputInfo
import com.example.ui.components.CircularRfGauge
import com.example.ui.components.TelemetryParamItem
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.RfGreen
import com.example.ui.theme.RfPurple
import com.example.ui.theme.RfRed
import com.example.ui.theme.RfYellow

@Composable
fun ThroughputTestScreen(
    serving: ServingCellInfo,
    throughput: ThroughputInfo,
    onRunPingTest: () -> Unit,
    onRunSpeedTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("throughput_test_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Active Speed Gauge Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("speed_gauge_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MESURE DE DÉBIT RÉSEAU",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CircularRfGauge(
                        value = throughput.dlSpeedMbps,
                        min = 0f,
                        max = 150f,
                        unit = "Mbps",
                        label = "TÉLÉCHARGEMENT (DL)",
                        gaugeColor = CyberBlue,
                        size = 140.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (throughput.isTesting) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = throughput.testStatusText,
                                style = MaterialTheme.typography.bodySmall.copy(color = CyberBlue)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { throughput.testProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = CyberBlue,
                            )
                        }
                    } else {
                        Button(
                            onClick = onRunSpeedTest,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("start_speed_test_button")
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = "Démarrer Test", tint = Color(0xFF00363F))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Démarrer Test Débit HTTP", color = Color(0xFF00363F), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Latency & Ping Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ping_test_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LATENCE & GIGUE (PING)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        )

                        Button(
                            onClick = onRunPingTest,
                            enabled = !throughput.isTesting,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("run_ping_button")
                        ) {
                            Icon(Icons.Default.NetworkCheck, contentDescription = "Ping", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Ping", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TelemetryParamItem(
                            label = "Latence (RTT)",
                            value = "${throughput.pingMs}",
                            unit = "ms",
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryParamItem(
                            label = "Gigue (Jitter)",
                            value = "${throughput.jitterMs}",
                            unit = "ms",
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryParamItem(
                            label = "Pertes Paquets",
                            value = "${throughput.packetLossPercent.toInt()}",
                            unit = "%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Network Transport Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CAPACITÉS DU RÉSEAU & TRANSPORT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TelemetryParamItem(
                            label = "Technologie Active",
                            value = serving.rat,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryParamItem(
                            label = "Opérateur",
                            value = serving.operatorName,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TelemetryParamItem(
                            label = "Bande Passante Théorique",
                            value = "${serving.bandwidthMhz}",
                            unit = "MHz",
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryParamItem(
                            label = "CQI Index",
                            value = "${serving.cqi}",
                            unit = "/ 15",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
