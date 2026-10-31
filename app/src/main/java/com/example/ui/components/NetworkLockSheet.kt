package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NetworkLockSettings
import com.example.model.RatLockMode
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.RfGreen
import com.example.ui.theme.RfPurple
import com.example.ui.theme.RfRed
import com.example.ui.theme.RfYellow

/**
 * Nemo Handy style Network Forced Functions dialog allowing:
 * - RAT Lock: 2G Lock, 3G Lock, 4G Lock, 5G Lock, Auto
 * - Band Lock: Lock par Bande LTE / 5G
 * - PCI Lock: Lock par Cellule Physique (Physical Cell ID)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NetworkLockDialog(
    currentSettings: NetworkLockSettings,
    onApplySettings: (NetworkLockSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRat by remember { mutableStateOf(currentSettings.ratLock) }
    var selectedBand by remember { mutableStateOf(currentSettings.bandLock) }
    var pciLockEnabled by remember { mutableStateOf(currentSettings.pciLock != null) }
    var pciText by remember { mutableStateOf(currentSettings.pciLock?.toString() ?: "184") }

    val availableBands = listOf(
        "Toutes (Auto)",
        "B1 (2100 MHz)",
        "B3 (1800 MHz)",
        "B7 (2600 MHz)",
        "B8 (900 MHz)",
        "B20 (800 MHz)",
        "B28 (700 MHz)",
        "B38 (TDD 2600)",
        "n78 (3.5 GHz)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Verrouillage",
                        tint = CyberBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verrouillage Réseau (Nemo Lock)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Forces les fonctions modem pour isoler une technologie, une bande ou une cellule spécifique (comme dans Nemo Handy).",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                // 1. RAT Lock (Verrouillage 2G, 3G, 4G, 5G)
                Column {
                    Text(
                        text = "1. VERROUILLAGE TECHNOLOGIE (RAT LOCK)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RatLockMode.values().forEach { mode ->
                            val isSelected = selectedRat == mode
                            val badgeColor = when (mode) {
                                RatLockMode.NR_ONLY -> RfPurple
                                RatLockMode.LTE_ONLY -> CyberBlue
                                RatLockMode.WCDMA_ONLY -> RfYellow
                                RatLockMode.GSM_ONLY -> Color(0xFFE11D48)
                                RatLockMode.AUTO -> RfGreen
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) badgeColor.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) badgeColor else Color.Transparent
                                ),
                                modifier = Modifier
                                    .clickable { selectedRat = mode }
                                    .testTag("rat_lock_${mode.name.lowercase()}")
                            ) {
                                Text(
                                    text = mode.label,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) badgeColor else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. Band Lock (Verrouillage par Bande)
                Column {
                    Text(
                        text = "2. VERROUILLAGE PAR BANDE (BAND LOCK)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableBands.forEach { bandOption ->
                            val isSelected = if (bandOption == "Toutes (Auto)") selectedBand == null else selectedBand == bandOption
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CyberBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) CyberBlue else Color.Transparent
                                ),
                                modifier = Modifier.clickable {
                                    selectedBand = if (bandOption == "Toutes (Auto)") null else bandOption
                                }
                            ) {
                                Text(
                                    text = bandOption,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CyberBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. PCI Lock (Verrouillage par Cellule / PCI)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "3. VERROUILLAGE PAR PCI (PCI LOCK)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "Bloque le handover et isole la cellule",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Switch(
                            checked = pciLockEnabled,
                            onCheckedChange = { pciLockEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberBlue,
                                checkedTrackColor = CyberBlue.copy(alpha = 0.3f)
                            )
                        )
                    }

                    if (pciLockEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = pciText,
                            onValueChange = { pciText = it.filter { char -> char.isDigit() } },
                            label = { Text("Numéro PCI à forcer (ex: 184)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pciVal = if (pciLockEnabled) pciText.toIntOrNull() else null
                    val newSettings = NetworkLockSettings(
                        ratLock = selectedRat,
                        bandLock = selectedBand,
                        pciLock = pciVal
                    )
                    onApplySettings(newSettings)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                modifier = Modifier.testTag("apply_network_locks_button")
            ) {
                Text("Appliquer Verrous", color = Color(0xFF00363F), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    // Reset to Auto
                    onApplySettings(NetworkLockSettings())
                    onDismiss()
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Réinitialiser", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Réinitialiser (Auto)")
            }
        }
    )
}
