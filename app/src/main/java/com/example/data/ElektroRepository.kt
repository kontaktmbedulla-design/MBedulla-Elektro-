package com.example.data

import com.example.data.dao.TimesheetDao
import com.example.data.dao.WorkSiteDao
import com.example.data.dao.WorkerDao
import com.example.data.model.GpsStatus
import com.example.data.model.TimesheetLog
import com.example.data.model.WorkSite
import com.example.data.model.Worker
import com.example.data.model.WorkerStatus
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class ElektroRepository(
    private val workerDao: WorkerDao,
    private val workSiteDao: WorkSiteDao,
    private val timesheetDao: TimesheetDao
) {
    val allWorkers: Flow<List<Worker>> = workerDao.getAllWorkers()
    val allSites: Flow<List<WorkSite>> = workSiteDao.getAllSites()
    val allTimesheets: Flow<List<TimesheetLog>> = timesheetDao.getAllTimesheets()

    suspend fun updateWorker(worker: Worker) {
        workerDao.updateWorker(worker)
    }

    suspend fun insertWorker(worker: Worker) {
        workerDao.insertWorker(worker)
    }

    suspend fun deleteWorker(workerId: String) {
        workerDao.deleteWorker(workerId)
    }

    suspend fun insertSite(site: WorkSite) {
        workSiteDao.insertSite(site)
    }

    suspend fun updateSite(site: WorkSite) {
        workSiteDao.updateSite(site)
    }

    suspend fun deleteSite(siteId: String) {
        workSiteDao.deleteSite(siteId)
    }

    suspend fun checkInWorker(
        worker: Worker,
        site: WorkSite,
        currentLat: Double,
        currentLng: Double
    ) {
        val distance = calculateDistanceInMeters(
            lat1 = currentLat,
            lon1 = currentLng,
            lat2 = site.latitude,
            lon2 = site.longitude
        ).toInt()

        val isWithinGeofence = distance <= site.geofenceRadiusMeters
        val gpsStatus = if (isWithinGeofence) GpsStatus.VERIFIED else GpsStatus.OUT_OF_BOUNDS

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("sq"))
        val currentTimeStr = timeFormat.format(Date())
        val currentDateStr = dateFormat.format(Date())

        val updatedWorker = worker.copy(
            currentSiteId = site.id,
            currentSiteName = site.name,
            checkInTime = currentTimeStr,
            gpsStatus = gpsStatus,
            gpsDistanceMeters = distance,
            status = WorkerStatus.WORKING,
            hoursToday = if (worker.hoursToday == 0.0) 8.0 else worker.hoursToday,
            latitude = currentLat,
            longitude = currentLng
        )
        workerDao.updateWorker(updatedWorker)

        // Log entry in timesheets
        val log = TimesheetLog(
            workerId = worker.id,
            workerName = worker.name,
            siteName = site.name,
            date = currentDateStr,
            checkInTime = currentTimeStr,
            checkOutTime = null,
            totalHours = 8.0,
            gpsVerified = isWithinGeofence,
            distanceMeters = distance,
            notes = if (isWithinGeofence) "Check-in i verifikuar në lokacion" else "Check-in jashtë rrezes së objektit ($distance m)"
        )
        timesheetDao.insertTimesheet(log)
    }

    suspend fun checkOutWorker(worker: Worker) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val checkOutStr = timeFormat.format(Date())

        val updatedWorker = worker.copy(
            status = WorkerStatus.BREAK,
            gpsStatus = GpsStatus.OFFLINE,
            gpsDistanceMeters = null,
            currentSiteId = null,
            currentSiteName = "-",
            checkInTime = "-"
        )
        workerDao.updateWorker(updatedWorker)
    }

    suspend fun changeWorkerStatus(worker: Worker, newStatus: WorkerStatus) {
        val updated = worker.copy(
            status = newStatus,
            gpsStatus = if (newStatus == WorkerStatus.BREAK) GpsStatus.OFFLINE else worker.gpsStatus
        )
        workerDao.updateWorker(updated)
    }

    companion object {
        // Haversine formula to calculate accurate ground distance in meters
        fun calculateDistanceInMeters(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double
        ): Double {
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
}
