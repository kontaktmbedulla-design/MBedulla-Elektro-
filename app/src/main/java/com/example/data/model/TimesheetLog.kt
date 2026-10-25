package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timesheets")
data class TimesheetLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workerId: String,
    val workerName: String,
    val siteName: String,
    val date: String, // e.g. "2026-10-14"
    val checkInTime: String,
    val checkOutTime: String? = null,
    val totalHours: Double = 8.0,
    val gpsVerified: Boolean = true,
    val distanceMeters: Int? = 15,
    val notes: String = "Instalime të rregullta"
)
