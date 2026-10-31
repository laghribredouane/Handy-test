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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.model.RfSignalCategory
import com.example.model.ServingCellInfo
import com.example.ui.components.RfStripChart
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.RfGreen
import com.example.ui.theme.RfPurple
import com.example.ui.theme.RfRed
import com.example.ui.theme.RfYellow

@Composable
fun RfGraphsScreen(
    serving: ServingCellInfo,
    rsrpHistory: List<Pair<Long, Int>>,
    sinrHistory: List<Pair<Long, Float>>,
    modifier: Modifier = Modifier
) {
    val rsrpPoints = rsrpHistory.map { it.second.toFloat() }
    val sinrPoints = sinrHistory.map { it.second }

    val minRsrp = if (rsrpPoints.isNotEmpty()) rsrpPoints.minOrNull()?.toInt() ?: -120 else -120
    val maxRsrp = if (rsrpPoints.isNotEmpty()) rsrpPoints.maxOrNull()?.toInt() ?: -70 else -70
    val avgRsrp = if (rsrpPoints.isNotEmpty()) rsrpPoints.average().toInt() else -88

    // Calculate distribution
    val totalSamples = rsrpPoints.size.coerceAtLeast(1)
    val excCount = rsrpPoints.count { it >= -80 }
    val goodCount = rsrpPoints.count { it in -95f..-80f }
    val fairCount = rsrpPoints.count { it in -105f..-95f }
    val poorCount = rsrpPoints.count { it < -105 }

    val excPct = (excCount * 100) / totalSamples
    val goodPct = (goodCount * 100) / totalSamples
    val fairPct = (fairCount * 100) / totalSamples
    val poorPct = (poorCount * 100) / totalSamples

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("rf_graphs_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Summary Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(label = "RSRP MIN", value = "$minRsrp", unit = "dBm", color = RfRed, modifier = Modifier.weight(1f))
                StatCard(label = "RSRP MOY", value = "$avgRsrp", unit = "dBm", color = CyberBlue, modifier = Modifier.weight(1f))
                StatCard(label = "RSRP MAX", value = "$maxRsrp", unit = "dBm", color = RfGreen, modifier = Modifier.weight(1f))
            }
        }

        // Live RSRP Strip Chart
        item {
            RfStripChart(
                title = "ÉVOLUTION TEMPORELLE RSRP (dBm)",
                currentValue = "${serving.rsrp}",
                dataPoints = rsrpPoints,
                minY = -130f,
                maxY = -60f,
                unit = "dBm",
                lineColor = serving.signalCategory.color,
                referenceLines = listOf(
                    Pair(-80f, RfGreen),
                    Pair(-95f, CyberBlue),
                    Pair(-105f, RfYellow)
                )
            )
        }

        // Live SINR Strip Chart
        item {
            RfStripChart(
                title = "RAPPORT SIGNAL/BRUIT SINR (dB)",
                currentValue = String.format("%.1f", serving.sinr),
                dataPoints = sinrPoints,
                minY = -5f,
                maxY = 30f,
                unit = "dB",
                lineColor = CyberBlue,
                referenceLines = listOf(
                    Pair(0f, RfRed),
                    Pair(10f, RfYellow),
                    Pair(20f, RfGreen)
                )
            )
        }

        // Signal Quality Distribution
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "RÉPARTITION DE LA QUALITÉ DU SIGNAL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Segmented Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                    ) {
                        if (excPct > 0) Box(modifier = Modifier.weight(excPct.toFloat()).fillMaxSize().background(RfGreen))
                        if (goodPct > 0) Box(modifier = Modifier.weight(goodPct.toFloat()).fillMaxSize().background(CyberBlue))
                        if (fairPct > 0) Box(modifier = Modifier.weight(fairPct.toFloat()).fillMaxSize().background(RfYellow))
                        if (poorPct > 0) Box(modifier = Modifier.weight(poorPct.toFloat()).fillMaxSize().background(RfRed))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend and percentages
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DistributionItem(label = "≥ -80 (Exc)", pct = excPct, color = RfGreen)
                        DistributionItem(label = "-80..-95 (Bon)", pct = goodPct, color = CyberBlue)
                        DistributionItem(label = "-95..-105 (Moy)", pct = fairPct, color = RfYellow)
                        DistributionItem(label = "< -105 (Faible)", pct = poorPct, color = RfRed)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                )
                Text(text = " $unit", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
            }
        }
    }
}

@Composable
private fun DistributionItem(label: String, pct: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(6.dp).height(6.dp).background(color, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "$pct%", style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = color))
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
    }
}
