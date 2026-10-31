package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DriveTestSample
import com.example.model.GpsLocationInfo
import com.example.model.MapStyle
import com.example.model.RfSignalCategory
import com.example.model.ServingCellInfo
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.RfGreen
import com.example.ui.theme.RfPurple
import com.example.ui.theme.RfRed
import com.example.ui.theme.RfYellow

@Composable
fun DriveTestCanvasMap(
    currentGps: GpsLocationInfo,
    servingCell: ServingCellInfo,
    samples: List<DriveTestSample>,
    modifier: Modifier = Modifier
) {
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var zoomLevel by remember { mutableFloatStateOf(28000f) } // Meters to pixels scaling factor
    var mapStyle by remember { mutableStateOf(MapStyle.STREETS) }

    val mapBgColor = when (mapStyle) {
        MapStyle.SATELLITE -> Color(0xFF070B12)
        MapStyle.HYBRID -> Color(0xFF0F172A)
        MapStyle.STREETS -> Color(0xFF131926)
    }

    Box(modifier = modifier.background(mapBgColor).testTag("drive_test_canvas_map")) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffsetX += dragAmount.x
                        panOffsetY += dragAmount.y
                    }
                }
        ) {
            val center = Offset(size.width / 2 + panOffsetX, size.height / 2 + panOffsetY)

            // 1. Google Maps Cartographic Background Layers
            if (mapStyle == MapStyle.STREETS || mapStyle == MapStyle.HYBRID) {
                // Draw Google Maps style road networks & urban blocks
                val primaryRoadColor = if (mapStyle == MapStyle.HYBRID) Color(0xFF334155).copy(alpha = 0.7f) else Color(0xFF1E293B)
                val secondaryRoadColor = if (mapStyle == MapStyle.HYBRID) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFF0F172A)

                // Secondary street grid
                val spacing = 80.dp.toPx()
                var x = (center.x % spacing)
                while (x < size.width) {
                    drawLine(
                        color = secondaryRoadColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 3.dp.toPx()
                    )
                    x += spacing
                }
                var y = (center.y % spacing)
                while (y < size.height) {
                    drawLine(
                        color = secondaryRoadColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 3.dp.toPx()
                    )
                    y += spacing
                }

                // Primary Boulevards / Avenues
                val mainSpacing = 240.dp.toPx()
                var mx = (center.x % mainSpacing)
                while (mx < size.width) {
                    drawLine(
                        color = primaryRoadColor,
                        start = Offset(mx, 0f),
                        end = Offset(mx, size.height),
                        strokeWidth = 7.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF475569),
                        start = Offset(mx, 0f),
                        end = Offset(mx, size.height),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                    mx += mainSpacing
                }

                var my = (center.y % mainSpacing)
                while (my < size.height) {
                    drawLine(
                        color = primaryRoadColor,
                        start = Offset(0f, my),
                        end = Offset(size.width, my),
                        strokeWidth = 7.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF475569),
                        start = Offset(0f, my),
                        end = Offset(size.width, my),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                    my += mainSpacing
                }
            } else {
                // SATELLITE mode: Satellite imagery topography & terrain contours
                val contourColor = Color(0xFF0E1A2B)
                for (r in listOf(80.dp.toPx(), 160.dp.toPx(), 260.dp.toPx(), 380.dp.toPx())) {
                    drawCircle(
                        color = contourColor,
                        radius = r,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Radar concentric rings for distance scale
            val gridColor = Color(0xFF38BDF8).copy(alpha = 0.15f)
            val strokeGrid = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
            for (r in listOf(60.dp.toPx(), 120.dp.toPx(), 200.dp.toPx())) {
                drawCircle(
                    color = gridColor,
                    radius = r,
                    center = center,
                    style = strokeGrid
                )
            }

            // Cell Tower Location (calculated based on current serving cell distance & estimated bearing)
            val towerDistPx = (servingCell.distanceMeters.coerceIn(100, 3000) / 1000f) * (zoomLevel / 350f)
            val towerOffset = Offset(
                center.x - towerDistPx * 0.7f,
                center.y - towerDistPx * 0.7f
            )

            // Tower Sector beam
            val beamPath = Path().apply {
                moveTo(towerOffset.x, towerOffset.y)
                lineTo(towerOffset.x + 130f, towerOffset.y + 65f)
                lineTo(towerOffset.x + 65f, towerOffset.y + 130f)
                close()
            }
            drawPath(beamPath, color = RfPurple.copy(alpha = 0.16f))

            // Direct line of sight (LOS) from Tower to current GPS position
            drawLine(
                color = CyberBlue.copy(alpha = 0.45f),
                start = towerOffset,
                end = center,
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            // Tower Icon
            drawCircle(color = RfPurple, radius = 8.dp.toPx(), center = towerOffset)
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = towerOffset)

            // Draw Breadcrumbs of previous Drive Test Samples
            val refLat = currentGps.latitude
            val refLon = currentGps.longitude

            // Draw path lines between samples
            if (samples.size > 1) {
                var prevPoint: Offset? = null
                for (sample in samples) {
                    val dLon = (sample.longitude - refLon) * zoomLevel
                    val dLat = -(sample.latitude - refLat) * zoomLevel * 1.5f // Aspect correction for lat/lon
                    val pt = Offset(center.x + dLon.toFloat(), center.y + dLat.toFloat())

                    if (prevPoint != null) {
                        drawLine(
                            color = Color(0xFF64748B),
                            start = prevPoint,
                            end = pt,
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }
                    prevPoint = pt
                }
            }

            // Draw Breadcrumb Dots
            for (sample in samples) {
                val dLon = (sample.longitude - refLon) * zoomLevel
                val dLat = -(sample.latitude - refLat) * zoomLevel * 1.5f
                val pt = Offset(center.x + dLon.toFloat(), center.y + dLat.toFloat())

                val cat = RfSignalCategory.fromRsrp(sample.rsrp)
                drawCircle(
                    color = cat.color,
                    radius = 5.5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = Color.Black,
                    radius = 5.5.dp.toPx(),
                    center = pt,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Draw Current Vehicle Position at Center with Orientation Cone
            drawCircle(
                color = CyberBlue.copy(alpha = 0.28f),
                radius = 20.dp.toPx(),
                center = center
            )

            rotate(currentGps.bearing, pivot = center) {
                val conePath = Path().apply {
                    moveTo(center.x, center.y - 22.dp.toPx())
                    lineTo(center.x - 9.dp.toPx(), center.y + 11.dp.toPx())
                    lineTo(center.x, center.y + 5.dp.toPx())
                    lineTo(center.x + 9.dp.toPx(), center.y + 11.dp.toPx())
                    close()
                }
                drawPath(conePath, color = CyberBlue)
            }

            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = center
            )
        }

        // Top Right Controls (Zoom +, Zoom -, Center, Layer Switcher)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { zoomLevel = (zoomLevel * 1.3f).coerceAtMost(90000f) },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }
            SmallFloatingActionButton(
                onClick = { zoomLevel = (zoomLevel / 1.3f).coerceAtLeast(8000f) },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }
            SmallFloatingActionButton(
                onClick = {
                    panOffsetX = 0f
                    panOffsetY = 0f
                },
                containerColor = CyberBlue,
                contentColor = Color(0xFF00363F)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recentrer")
            }
            // Google Maps Layer Style Switcher Button
            SmallFloatingActionButton(
                onClick = {
                    mapStyle = when (mapStyle) {
                        MapStyle.STREETS -> MapStyle.SATELLITE
                        MapStyle.SATELLITE -> MapStyle.HYBRID
                        MapStyle.HYBRID -> MapStyle.STREETS
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Layers, contentDescription = "Changer Style Carte")
            }
        }

        // Top Left: Map Legend & Google Maps Badge
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Google Maps Style Mode Badge
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "Google Maps",
                        tint = Color(0xFFEA4335), // Google Red
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Google Maps • ${mapStyle.label}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // Signal Legend
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "LÉGENDE RSRP (dBm)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(RfGreen, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("≥ -80", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(8.dp).background(CyberBlue, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("-80..-95", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(RfYellow, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("-95..-105", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(8.dp).background(RfRed, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("< -105", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                    }
                }
            }
        }

        // Bottom Telemetry Overlay Banner
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GPS: ${String.format("%.5f", currentGps.latitude)}, ${String.format("%.5f", currentGps.longitude)}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Alt: ${currentGps.altitude.toInt()} m | Précision: ±${currentGps.accuracyM.toInt()} m",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${currentGps.speedKmh.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyberBlue
                            )
                        )
                        Text(
                            text = " km/h",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    Text(
                        text = "Cap: ${currentGps.bearing.toInt()}°",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
