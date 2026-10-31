package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drive_test_sessions")
data class DriveTestSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startTime: Long,
    val endTime: Long? = null,
    val sampleCount: Int = 0,
    val avgRsrp: Double = 0.0,
    val minRsrp: Int = 0,
    val maxRsrp: Int = 0,
    val distanceMeters: Double = 0.0,
    val primaryRat: String = "4G LTE",
    val operator: String = "Opérateur"
)
