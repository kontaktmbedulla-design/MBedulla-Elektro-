package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WorkerStatus(val label: String) {
    WORKING("Në Punë"),
    BREAK("Pushim / Jashtë Orarit"),
    PAUSED("Në Pauzë")
}

enum class GpsStatus(val label: String) {
    VERIFIED("Verified"),
    OUT_OF_BOUNDS("Jashtë Zonës"),
    NO_SIGNAL("Pa Sinjal"),
    OFFLINE("-")
}

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey
    val id: String,
    val name: String,
    val role: String,
    val phone: String,
    val currentSiteId: String? = null,
    val currentSiteName: String? = null,
    val checkInTime: String? = null,
    val gpsStatus: GpsStatus = GpsStatus.VERIFIED,
    val gpsDistanceMeters: Int? = null,
    val status: WorkerStatus = WorkerStatus.WORKING,
    val hoursToday: Double = 0.0,
    val hourlyRateEur: Double = 10.0,
    val latitude: Double = 42.6629,
    val longitude: Double = 21.1655
)
