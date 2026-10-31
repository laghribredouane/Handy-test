package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.RfGreen
import com.example.ui.theme.RfYellow
import com.example.ui.theme.RfRed
import com.example.ui.theme.CyberBlue

enum class RfSignalCategory(val label: String, val color: Color) {
    EXCELLENT("Excellent", RfGreen),      // RSRP >= -80 dBm
    GOOD("Bon", CyberBlue),              // -80 > RSRP >= -95 dBm
    FAIR("Moyen", RfYellow),             // -95 > RSRP >= -105 dBm
    POOR("Faible", RfRed);               // RSRP < -105 dBm

    companion object {
        fun fromRsrp(rsrp: Int): RfSignalCategory = when {
            rsrp >= -80 -> EXCELLENT
            rsrp >= -95 -> GOOD
            rsrp >= -105 -> FAIR
            else -> POOR
        }
    }
}

enum class RatLockMode(val label: String, val techName: String) {
    AUTO("Automatique (Multi-RAT)", "Auto"),
    NR_ONLY("5G Only (NR)", "5G NR"),
    LTE_ONLY("4G Only (LTE)", "4G LTE"),
    WCDMA_ONLY("3G Only (WCDMA / UMTS)", "3G WCDMA"),
    GSM_ONLY("2G Only (GSM / EDGE)", "2G GSM")
}

data class NetworkLockSettings(
    val ratLock: RatLockMode = RatLockMode.AUTO,
    val bandLock: String? = null,
    val pciLock: Int? = null
) {
    val isLockActive: Boolean
        get() = ratLock != RatLockMode.AUTO || bandLock != null || pciLock != null

    val summaryText: String
        get() {
            val parts = mutableListOf<String>()
            if (ratLock != RatLockMode.AUTO) parts.add("RAT: ${ratLock.label.substringBefore(" ")}")
            if (bandLock != null) parts.add("Bande: $bandLock")
            if (pciLock != null) parts.add("PCI: $pciLock")
            return if (parts.isEmpty()) "Aucun verrou" else parts.joinToString(" | ")
        }
}

enum class MapStyle(val label: String) {
    STREETS("Plan Rues"),
    SATELLITE("Satellite"),
    HYBRID("Hybride / Relief")
}

data class DiagnosticServer(
    val name: String,
    val host: String,
    val port: Int = 53,
    val isCustom: Boolean = false
) {
    companion object {
        val DEFAULT_SERVERS = listOf(
            DiagnosticServer("Google DNS", "8.8.8.8", 53),
            DiagnosticServer("Cloudflare Core", "1.1.1.1", 53),
            DiagnosticServer("OpenDNS Europe", "208.67.222.222", 53),
            DiagnosticServer("Passerelle EPC/5GC", "10.0.0.1", 53)
        )
    }
}

enum class DiagnosticPhase {
    IDLE,
    PING,
    SPEED_TEST,
    COMPLETED,
    FAILED
}

data class NetworkDiagnosticState(
    val server: DiagnosticServer = DiagnosticServer.DEFAULT_SERVERS[0],
    val phase: DiagnosticPhase = DiagnosticPhase.IDLE,
    val progress: Float = 0f,
    val statusMessage: String = "Prêt pour le diagnostic",
    val pingMs: Int = 0,
    val minPingMs: Int = 0,
    val maxPingMs: Int = 0,
    val jitterMs: Int = 0,
    val packetLossPercent: Float = 0f,
    val downloadSpeedMbps: Float = 0f,
    val verdict: String = "Non testé",
    val isRunning: Boolean = false
)

data class ServingCellInfo(
    val rat: String = "4G LTE",
    val operatorName: String = "Opérateur Mobile",
    val mcc: String = "208",
    val mnc: String = "01",
    val ci: Long = 27481923,
    val enodebId: Int = 107351,
    val sectorId: Int = 3,
    val pci: Int = 184,
    val tac: Int = 4120,
    val earfcn: Int = 1650,
    val band: String = "B3 (1800 MHz)",
    val rsrp: Int = -86,
    val rsrq: Int = -10,
    val sinr: Float = 16.4f,
    val rssi: Int = -62,
    val cqi: Int = 12,
    val timingAdvance: Int = 2,
    val bandwidthMhz: Int = 20,
    val is5gNsa: Boolean = false
) {
    val signalCategory: RfSignalCategory
        get() = RfSignalCategory.fromRsrp(rsrp)

    val distanceMeters: Int
        get() = (timingAdvance * 78.12).toInt() // LTE TA step is ~78.12m
}

data class NeighborCellInfo(
    val id: String,
    val rat: String,
    val pci: Int,
    val earfcn: Int,
    val rsrp: Int,
    val rsrq: Int,
    val deltaRsrp: Int = 0
)

data class GpsLocationInfo(
    val latitude: Double = 48.8566,
    val longitude: Double = 2.3522,
    val altitude: Double = 35.0,
    val speedKmh: Float = 0.0f,
    val accuracyM: Float = 3.5f,
    val bearing: Float = 0.0f,
    val timestamp: Long = System.currentTimeMillis(),
    val hasFix: Boolean = true
)

data class ThroughputInfo(
    val dlSpeedMbps: Float = 0.0f,
    val ulSpeedMbps: Float = 0.0f,
    val pingMs: Int = 0,
    val jitterMs: Int = 0,
    val packetLossPercent: Float = 0.0f,
    val isTesting: Boolean = false,
    val testProgress: Float = 0.0f,
    val testStatusText: String = "Inactif"
)
