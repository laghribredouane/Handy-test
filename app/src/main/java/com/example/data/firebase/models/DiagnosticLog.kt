package com.example.data.firebase.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class DiagnosticLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val targetServer: String = "",
    val latencyMs: Double = 0.0,
    val downloadSpeedMbps: Double = 0.0,
    val jitterMs: Double = 0.0,
    val packetLossPercent: Double = 0.0,
    val rfVerdict: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
