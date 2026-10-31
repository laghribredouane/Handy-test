package com.example.data.firebase.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class DriveSurvey(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val networkType: String = "",
    val operator: String = "",
    val avgRsrp: Double = 0.0,
    val avgSinr: Double = 0.0,
    val pci: Int = 0,
    val band: String = "",
    val pointCount: Int = 0,
    val status: String = "completed",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
