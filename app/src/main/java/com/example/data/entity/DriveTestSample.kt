package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drive_test_samples",
    indices = [Index(value = ["sessionId"])]
)
data class DriveTestSample(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Float,
    val accuracy: Float,
    val rat: String,
    val operator: String,
    val ci: Long,
    val pci: Int,
    val tac: Int,
    val earfcn: Int,
    val band: String,
    val rsrp: Int,
    val rsrq: Int,
    val sinr: Float,
    val cqi: Int,
    val dlSpeedMbps: Float,
    val pingMs: Int
)
