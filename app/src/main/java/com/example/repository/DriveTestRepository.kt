package com.example.repository

import com.example.data.dao.DriveTestDao
import com.example.data.entity.DriveTestSample
import com.example.data.entity.DriveTestSession
import com.example.model.GpsLocationInfo
import com.example.model.ServingCellInfo
import com.example.model.ThroughputInfo
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DriveTestRepository(private val dao: DriveTestDao) {

    val allSessions: Flow<List<DriveTestSession>> = dao.getAllSessions()

    fun getSamplesForSession(sessionId: Long): Flow<List<DriveTestSample>> {
        return dao.getSamplesForSession(sessionId)
    }

    suspend fun getSamplesListForSession(sessionId: Long): List<DriveTestSample> {
        return dao.getSamplesListForSession(sessionId)
    }

    suspend fun startSession(customName: String? = null): Long {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val name = customName ?: "DT_${dateFormat.format(Date())}"
        val session = DriveTestSession(
            name = name,
            startTime = System.currentTimeMillis()
        )
        return dao.insertSession(session)
    }

    suspend fun logSample(
        sessionId: Long,
        serving: ServingCellInfo,
        gps: GpsLocationInfo,
        throughput: ThroughputInfo
    ): Long {
        val sample = DriveTestSample(
            sessionId = sessionId,
            timestamp = System.currentTimeMillis(),
            latitude = gps.latitude,
            longitude = gps.longitude,
            speedKmh = gps.speedKmh,
            accuracy = gps.accuracyM,
            rat = serving.rat,
            operator = serving.operatorName,
            ci = serving.ci,
            pci = serving.pci,
            tac = serving.tac,
            earfcn = serving.earfcn,
            band = serving.band,
            rsrp = serving.rsrp,
            rsrq = serving.rsrq,
            sinr = serving.sinr,
            cqi = serving.cqi,
            dlSpeedMbps = throughput.dlSpeedMbps,
            pingMs = throughput.pingMs
        )
        return dao.insertSample(sample)
    }

    suspend fun stopSession(sessionId: Long) {
        val session = dao.getSessionById(sessionId) ?: return
        val samples = dao.getSamplesListForSession(sessionId)
        if (samples.isEmpty()) {
            dao.updateSession(
                session.copy(
                    endTime = System.currentTimeMillis(),
                    sampleCount = 0
                )
            )
            return
        }

        var totalRsrp = 0
        var minRsrp = Int.MAX_VALUE
        var maxRsrp = Int.MIN_VALUE
        var totalDist = 0.0

        for (i in samples.indices) {
            val s = samples[i]
            totalRsrp += s.rsrp
            if (s.rsrp < minRsrp) minRsrp = s.rsrp
            if (s.rsrp > maxRsrp) maxRsrp = s.rsrp

            if (i > 0) {
                val prev = samples[i - 1]
                totalDist += calculateDistanceMeters(prev.latitude, prev.longitude, s.latitude, s.longitude)
            }
        }

        val avgRsrp = totalRsrp.toDouble() / samples.size
        val primaryRat = samples.groupBy { it.rat }.maxByOrNull { it.value.size }?.key ?: "4G LTE"
        val operator = samples.firstOrNull()?.operator ?: "Unknown"

        dao.updateSession(
            session.copy(
                endTime = System.currentTimeMillis(),
                sampleCount = samples.size,
                avgRsrp = avgRsrp,
                minRsrp = if (minRsrp != Int.MAX_VALUE) minRsrp else -100,
                maxRsrp = if (maxRsrp != Int.MIN_VALUE) maxRsrp else -70,
                distanceMeters = totalDist,
                primaryRat = primaryRat,
                operator = operator
            )
        )
    }

    suspend fun deleteSession(session: DriveTestSession) {
        dao.deleteSamplesForSession(session.id)
        dao.deleteSession(session)
    }

    suspend fun generateCsvExport(sessionId: Long): String {
        val session = dao.getSessionById(sessionId) ?: return ""
        val samples = dao.getSamplesListForSession(sessionId)

        val sb = StringBuilder()
        sb.append("# NetField Handy Drive Test Export\n")
        sb.append("# Session: ${session.name}\n")
        sb.append("# Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(session.startTime))}\n")
        sb.append("# Total Samples: ${samples.size}\n\n")

        sb.append("Timestamp,DateTime,Latitude,Longitude,Speed_kmh,Accuracy_m,RAT,Operator,CellID,PCI,TAC,EARFCN,Band,RSRP_dBm,RSRQ_dB,SINR_dB,CQI,DL_Mbps,Ping_ms\n")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        for (s in samples) {
            sb.append("${s.timestamp},")
            sb.append("${sdf.format(Date(s.timestamp))},")
            sb.append("${s.latitude},")
            sb.append("${s.longitude},")
            sb.append("${String.format(Locale.US, "%.1f", s.speedKmh)},")
            sb.append("${String.format(Locale.US, "%.1f", s.accuracy)},")
            sb.append("\"${s.rat}\",")
            sb.append("\"${s.operator}\",")
            sb.append("${s.ci},")
            sb.append("${s.pci},")
            sb.append("${s.tac},")
            sb.append("${s.earfcn},")
            sb.append("\"${s.band}\",")
            sb.append("${s.rsrp},")
            sb.append("${s.rsrq},")
            sb.append("${String.format(Locale.US, "%.1f", s.sinr)},")
            sb.append("${s.cqi},")
            sb.append("${String.format(Locale.US, "%.1f", s.dlSpeedMbps)},")
            sb.append("${s.pingMs}\n")
        }

        return sb.toString()
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
