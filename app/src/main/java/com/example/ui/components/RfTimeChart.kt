package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.RfGreen
import com.example.ui.theme.RfRed
import com.example.ui.theme.RfYellow

@Composable
fun RfStripChart(
    title: String,
    currentValue: String,
    dataPoints: List<Float>,
    minY: Float,
    maxY: Float,
    unit: String,
    lineColor: Color,
    modifier: Modifier = Modifier,
    referenceLines: List<Pair<Float, Color>> = emptyList()
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currentValue,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = lineColor
                    )
                )
                Text(
                    text = " $unit",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                val w = size.width
                val h = size.height

                // Draw reference threshold lines
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                for ((refVal, refColor) in referenceLines) {
                    val yNorm = 1f - ((refVal - minY) / (maxY - minY)).coerceIn(0f, 1f)
                    val yPx = yNorm * h
                    drawLine(
                        color = refColor.copy(alpha = 0.4f),
                        start = Offset(0f, yPx),
                        end = Offset(w, yPx),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                // Grid bounding lines
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(0f, h),
                    end = Offset(w, h),
                    strokeWidth = 1.dp.toPx()
                )

                if (dataPoints.size >= 2) {
                    val stepX = w / (dataPoints.size - 1).coerceAtLeast(1)
                    val path = Path()
                    val fillPath = Path()

                    dataPoints.forEachIndexed { index, value ->
                        val x = index * stepX
                        val yNorm = 1f - ((value - minY) / (maxY - minY)).coerceIn(0f, 1f)
                        val y = yNorm * h

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }

                    fillPath.lineTo((dataPoints.size - 1) * stepX, h)
                    fillPath.close()

                    // Gradient fill underneath
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Line stroke
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Last point marker
                    val lastX = (dataPoints.size - 1) * stepX
                    val lastNorm = 1f - ((dataPoints.last() - minY) / (maxY - minY)).coerceIn(0f, 1f)
                    val lastY = lastNorm * h
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Axis limits labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Min: ${minY.toInt()} $unit",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
            )
            Text(
                text = "Points: ${dataPoints.size}",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
            )
            Text(
                text = "Max: ${maxY.toInt()} $unit",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
            )
        }
    }
}
