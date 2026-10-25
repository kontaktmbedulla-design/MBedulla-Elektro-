package com.example.ui.model

enum class AlertType {
    OUT_OF_ZONE,     // When worker is out of geofence zone
    STATUS_CHANGED,  // When worker status changes (Në Punë, Pauzë, Pushim)
    CHECK_IN,        // When worker checks into a site
    CHECK_OUT        // When worker leaves site
}

data class ManagerAlert(
    val id: String,
    val type: AlertType,
    val title: String,
    val message: String,
    val workerName: String,
    val siteName: String? = null,
    val distanceMeters: Int? = null,
    val timestamp: String,
    val isCritical: Boolean = false
)
