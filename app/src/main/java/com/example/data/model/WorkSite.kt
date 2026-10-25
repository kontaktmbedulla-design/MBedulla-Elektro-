package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_sites")
data class WorkSite(
    @PrimaryKey
    val id: String,
    val name: String,
    val address: String,
    val projectManager: String,
    val targetElectricians: Int,
    val assignedCount: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val geofenceRadiusMeters: Int = 100,
    val status: String = "Aktiv", // Aktiv, Përfunduar, Planifikuar
    val notes: String = ""
)
