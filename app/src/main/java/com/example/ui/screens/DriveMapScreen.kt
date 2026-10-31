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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.data.entity.DriveTestSample
import com.example.model.GpsLocationInfo
import com.example.model.ServingCellInfo
import com.example.ui.components.DriveTestCanvasMap
import com.example.ui.components.RatBadge
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.RfGreen
import com.example.ui.theme.RfRed
import com.example.ui.theme.RfYellow

@Composable
fun DriveMapScreen(
    currentGps: GpsLocationInfo,
    servingCell: ServingCellInfo,
    samples: List<DriveTestSample>,
    isRecording: Boolean,
    recordingDurationSeconds: Long,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("drive_map_screen")
    ) {
        // High-Precision Vector Canvas Map
        DriveTestCanvasMap(
            currentGps = currentGps,
            servingCell = servingCell,
            samples = samples,
            modifier = Modifier.fillMaxSize()
        )

        // Top Floating HUD Card (Nemo Drive Info)
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("map_top_hud_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
            ),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatBadge(rat = servingCell.rat)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PCI ${servingCell.pci}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "eNB ${servingCell.enodebId}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    // RSRP live value
                    Surface(
                        color = servingCell.signalCategory.color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${servingCell.rsrp} dBm",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = servingCell.signalCategory.color
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom strip of HUD: Recording Status + Start/Stop action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecording) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(RfRed, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val mins = recordingDurationSeconds / 60
                            val secs = recordingDurationSeconds % 60
                            Text(
                                text = "REC: ${String.format("%02d:%02d", mins, secs)} (${samples.size} pts)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = RfRed
                                )
                            )
                        }

                        Button(
                            onClick = onStopRecording,
                            colors = ButtonDefaults.buttonColors(containerColor = RfRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("stop_recording_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Arrêter REC", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Arrêter", fontSize = 12.sp)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Prêt pour Drive Test",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Button(
                            onClick = onStartRecording,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("start_recording_button")
                        ) {
                            Icon(
                                Icons.Default.FiberManualRecord,
                                contentDescription = "Démarrer REC",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF00363F)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Démarrer REC", color = Color(0xFF00363F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
