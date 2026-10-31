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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RfSignalCategory
import com.example.model.ServingCellInfo
import com.example.model.ThroughputInfo
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.RfGreen
import com.example.ui.theme.RfPurple
import com.example.ui.theme.RfRed
import com.example.ui.theme.RfYellow

/**
 * A professional, high-fidelity cellular signal dashboard UI displaying
 * real-time RF metrics (RSRP, RSRQ, SINR, RSSI, CQI, TA) in a clean grid format.
 */
@Composable
fun CellularMetricsDashboard(
    serving: ServingCellInfo,
    throughput: ThroughputInfo,
    modifier: Modifier = Modifier
) {
    var isDenseView by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cellular_metrics_dashboard"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Dashboard Header Card: Operator, Technology & Active Serving Cell
        Card(
            modifier = Modifier.fillMaxWidth().testTag("dashboard_header_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CyberBlue.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CellTower,
                                    contentDescription = "Cell Tower",
                                    tint = CyberBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = serving.operatorName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "PLMN ${serving.mcc}-${serving.mnc} • ${serving.band}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatBadge(rat = serving.rat)
                        Spacer(modifier = Modifier.width(8.dp))
                        // Toggle view density
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { isDenseView = !isDenseView }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isDenseView) Icons.Default.ViewAgenda else Icons.Default.GridView,
                                contentDescription = "Changer la vue",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp).padding(2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Overall RF Signal Quality Strip
                RfSignalQualityBar(rsrp = serving.rsrp)
            }
        }

        // Section Title: KPI Radio Principaux
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "INDICATEURS CLÉS RF (KPI)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = if (isDenseView) "Vue Compacte" else "Vue Cartes",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // --- Clean Professional 2x2 Grid for RSRP, RSRQ, SINR, RSSI ---
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Row 1: RSRP & RSRQ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricGridCard(
                    title = "RSRP",
                    subtitle = "Puissance Référence",
                    value = "${serving.rsrp}",
                    unit = "dBm",
                    progress = ((serving.rsrp - (-140f)) / (-44f - (-140f))).coerceIn(0f, 1f),
                    color = serving.signalCategory.color,
                    statusText = serving.signalCategory.label,
                    rangeText = "[-140 .. -44 dBm]",
                    modifier = Modifier.weight(1f)
                )

                val rsrqColor = when {
                    serving.rsrq >= -10 -> RfGreen
                    serving.rsrq >= -15 -> CyberBlue
                    serving.rsrq >= -18 -> RfYellow
                    else -> RfRed
                }
                val rsrqLabel = when {
                    serving.rsrq >= -10 -> "Excellente"
                    serving.rsrq >= -15 -> "Bonne"
                    serving.rsrq >= -18 -> "Moyenne"
                    else -> "Faible"
                }

                MetricGridCard(
                    title = "RSRQ",
                    subtitle = "Qualité Référence",
                    value = "${serving.rsrq}",
                    unit = "dB",
                    progress = ((serving.rsrq - (-22f)) / (-3f - (-22f))).coerceIn(0f, 1f),
                    color = rsrqColor,
                    statusText = rsrqLabel,
                    rangeText = "[-22 .. -3 dB]",
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: SINR & RSSI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val sinrColor = when {
                    serving.sinr >= 20f -> RfGreen
                    serving.sinr >= 13f -> CyberBlue
                    serving.sinr >= 0f -> RfYellow
                    else -> RfRed
                }
                val sinrLabel = when {
                    serving.sinr >= 20f -> "Pur (≥20dB)"
                    serving.sinr >= 13f -> "Bon (≥13dB)"
                    serving.sinr >= 0f -> "Acceptable"
                    else -> "Bruité (<0dB)"
                }

                MetricGridCard(
                    title = "SINR",
                    subtitle = "Signal / Bruit",
                    value = String.format("%.1f", serving.sinr),
                    unit = "dB",
                    progress = ((serving.sinr - (-5f)) / (30f - (-5f))).coerceIn(0f, 1f),
                    color = sinrColor,
                    statusText = sinrLabel,
                    rangeText = "[-5 .. +30 dB]",
                    modifier = Modifier.weight(1f)
                )

                MetricGridCard(
                    title = "RSSI",
                    subtitle = "Puissance Large Bande",
                    value = "${serving.rssi}",
                    unit = "dBm",
                    progress = ((serving.rssi - (-110f)) / (-40f - (-110f))).coerceIn(0f, 1f),
                    color = CyberBlue,
                    statusText = "Total Rx",
                    rangeText = "[-110 .. -40 dBm]",
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 3: CQI & Timing Advance (Distance)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val modulation = when {
                    serving.cqi >= 10 -> "256 QAM"
                    serving.cqi >= 7 -> "64 QAM"
                    serving.cqi >= 4 -> "16 QAM"
                    else -> "QPSK"
                }

                MetricGridCard(
                    title = "CQI",
                    subtitle = "Qualité Canal",
                    value = "${serving.cqi}",
                    unit = "/ 15",
                    progress = (serving.cqi / 15f).coerceIn(0f, 1f),
                    color = if (serving.cqi >= 10) RfGreen else if (serving.cqi >= 7) CyberBlue else RfYellow,
                    statusText = modulation,
                    rangeText = "Index Modulation",
                    modifier = Modifier.weight(1f)
                )

                MetricGridCard(
                    title = "TIMING ADV (TA)",
                    subtitle = "Distance Antenne",
                    value = "${serving.timingAdvance}",
                    unit = "steps",
                    progress = (serving.timingAdvance / 20f).coerceIn(0f, 1f),
                    color = CyberBlue,
                    statusText = "~${serving.distanceMeters} m",
                    rangeText = "1 step ≈ 78m",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section Title: Matrice Technique Cellulaire
        Text(
            text = "MATRICE D'IDENTIFICATION CELLULAIRE",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        )

        // Matrix Grid Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("telemetry_matrix_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryParamItem(
                        label = "Cell ID (CI)",
                        value = "${serving.ci}",
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryParamItem(
                        label = "eNB / Secteur",
                        value = "${serving.enodebId} / ${serving.sectorId}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryParamItem(
                        label = "PCI (Physique)",
                        value = "${serving.pci}",
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryParamItem(
                        label = "TAC (Tracking Area)",
                        value = "${serving.tac}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TelemetryParamItem(
                        label = "Canal (EARFCN)",
                        value = "${serving.earfcn}",
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryParamItem(
                        label = "Largeur Bande",
                        value = "${serving.bandwidthMhz}",
                        unit = "MHz",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Clean, professional KPI Grid Card with animated progress bar and status badge.
 */
@Composable
fun MetricGridCard(
    title: String,
    subtitle: String,
    value: String,
    unit: String,
    progress: Float,
    color: Color,
    statusText: String,
    rangeText: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400),
        label = "metric_progress"
    )

    Card(
        modifier = modifier.testTag("metric_card_${title.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Title & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                )

                Surface(
                    color = color.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = color,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Value Readout
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar meter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1E293B))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Range Scale Guide
            Text(
                text = rangeText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            )
        }
    }
}
