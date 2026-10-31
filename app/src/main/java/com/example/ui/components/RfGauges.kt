package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RfSignalCategory
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.RfGreen
import com.example.ui.theme.RfPurple
import com.example.ui.theme.RfRed
import com.example.ui.theme.RfYellow

@Composable
fun CircularRfGauge(
    value: Float,
    min: Float,
    max: Float,
    unit: String,
    label: String,
    gaugeColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    val progress = ((value - min) / (max - min)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400),
        label = "gauge_progress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size)
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = 8.dp.toPx()
                val arcSize = size.toPx() - strokeWidth
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                // Background Track
                drawArc(
                    color = Color(0xFF263238),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Value Arc
                drawArc(
                    color = gaugeColor,
                    startAngle = 135f,
                    sweepAngle = 270f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (value % 1 == 0f) "${value.toInt()}" else String.format("%.1f", value),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    fontSize = 18.sp
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    fontSize = 10.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun RfSignalQualityBar(
    rsrp: Int,
    modifier: Modifier = Modifier
) {
    val category = RfSignalCategory.fromRsrp(rsrp)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Qualité RF: ${category.label.uppercase()}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = category.color
                )
            )
            Text(
                text = "$rsrp dBm",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Segmented Progress Bar (4 segments: Poor, Fair, Good, Excellent)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val poorActive = rsrp < -105 || rsrp >= -105
            val fairActive = rsrp >= -105
            val goodActive = rsrp >= -95
            val excActive = rsrp >= -80

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(
                        if (poorActive) RfRed else Color(0xFF374151),
                        RoundedCornerShape(4.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(
                        if (fairActive) RfYellow else Color(0xFF374151),
                        RoundedCornerShape(4.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(
                        if (goodActive) CyberBlue else Color(0xFF374151),
                        RoundedCornerShape(4.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .background(
                        if (excActive) RfGreen else Color(0xFF374151),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
fun RatBadge(
    rat: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when {
        rat.contains("5G", ignoreCase = true) -> Pair(RfPurple.copy(alpha = 0.2f), RfPurple)
        rat.contains("LTE", ignoreCase = true) || rat.contains("4G", ignoreCase = true) -> Pair(CyberBlue.copy(alpha = 0.2f), CyberBlue)
        rat.contains("3G", ignoreCase = true) || rat.contains("WCDMA", ignoreCase = true) -> Pair(RfYellow.copy(alpha = 0.2f), RfYellow)
        else -> Pair(Color(0xFF374151), Color.White)
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = rat,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontFamily = FontFamily.Monospace
            )
        )
    }
}

@Composable
fun TelemetryParamItem(
    label: String,
    value: String,
    unit: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
