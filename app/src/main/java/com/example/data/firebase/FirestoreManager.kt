package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.GpsStatus
import com.example.data.model.TimesheetLog
import com.example.data.model.WorkSite
import com.example.data.model.Worker
import com.example.data.model.WorkerStatus
import com.example.ui.model.AlertType
import com.example.ui.model.ManagerAlert
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreManager(private val db: FirebaseFirestore) {

    companion object {
        private const val TAG = "FirestoreManager"

        fun getInstance(context: Context): FirestoreManager {
            val databaseId = context.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(databaseId)
            return FirestoreManager(db)
        }
    }

    val isUserAuthenticated: Boolean
        get() = Firebase.auth.currentUser != null

    val currentUserId: String?
        get() = Firebase.auth.currentUser?.uid

    // Workers Realtime Stream
    fun observeWorkers(): Flow<List<Worker>> {
        return db.collection("workers")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.getString("id") ?: doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val role = doc.getString("role") ?: "Elektricist"
                        val phone = doc.getString("phone") ?: "+383 44 000 000"
                        val currentSiteId = doc.getString("currentSiteId")
                        val currentSiteName = doc.getString("currentSiteName")
                        val checkInTime = doc.getString("checkInTime")
                        val statusStr = doc.getString("status") ?: WorkerStatus.BREAK.name
                        val status = try {
                            WorkerStatus.valueOf(statusStr)
                        } catch (e: Exception) {
                            WorkerStatus.BREAK
                        }
                        val gpsStatusStr = doc.getString("gpsStatus") ?: GpsStatus.OFFLINE.name
                        val gpsStatus = try {
                            GpsStatus.valueOf(gpsStatusStr)
                        } catch (e: Exception) {
                            GpsStatus.OFFLINE
                        }
                        val lat = doc.getDouble("latitude") ?: 42.6629
                        val lng = doc.getDouble("longitude") ?: 21.1655
                        val dist = doc.getLong("gpsDistanceMeters")?.toInt()
                        val hours = doc.getDouble("hoursToday") ?: 0.0
                        val rate = doc.getDouble("hourlyRateEur") ?: 10.0

                        Worker(
                            id = id,
                            name = name,
                            role = role,
                            phone = phone,
                            currentSiteId = currentSiteId,
                            currentSiteName = currentSiteName,
                            checkInTime = checkInTime,
                            gpsStatus = gpsStatus,
                            gpsDistanceMeters = dist,
                            status = status,
                            hoursToday = hours,
                            hourlyRateEur = rate,
                            latitude = lat,
                            longitude = lng
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping worker doc: ${doc.id}", e)
                        null
                    }
                }
            }
            .catch { e ->
                Log.e(TAG, "Error observing workers from Firestore", e)
                emit(emptyList())
            }
    }

    suspend fun saveWorker(worker: Worker) {
        val data = mutableMapOf<String, Any?>(
            "id" to worker.id,
            "name" to worker.name,
            "role" to worker.role,
            "phone" to worker.phone,
            "currentSiteId" to worker.currentSiteId,
            "currentSiteName" to worker.currentSiteName,
            "checkInTime" to worker.checkInTime,
            "status" to worker.status.name,
            "gpsStatus" to worker.gpsStatus.name,
            "latitude" to worker.latitude,
            "longitude" to worker.longitude,
            "gpsDistanceMeters" to worker.gpsDistanceMeters,
            "hoursToday" to worker.hoursToday,
            "hourlyRateEur" to worker.hourlyRateEur
        )
        try {
            db.collection("workers").document(worker.id).set(data.filterValues { it != null }).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save worker ${worker.id}", e)
        }
    }

    // Work Sites Realtime Stream
    fun observeSites(): Flow<List<WorkSite>> {
        return db.collection("sites")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.getString("id") ?: doc.id
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val address = doc.getString("address") ?: ""
                        val manager = doc.getString("projectManager") ?: "Ing. Valmir Kelmendi"
                        val target = (doc.getLong("targetElectricians") ?: 4L).toInt()
                        val assigned = (doc.getLong("assignedCount") ?: 0L).toInt()
                        val lat = doc.getDouble("latitude") ?: 42.6629
                        val lng = doc.getDouble("longitude") ?: 21.1655
                        val radius = (doc.getLong("geofenceRadiusMeters") ?: 100L).toInt()
                        val status = doc.getString("status") ?: "Aktiv"
                        val notes = doc.getString("notes") ?: ""

                        WorkSite(
                            id = id,
                            name = name,
                            address = address,
                            projectManager = manager,
                            targetElectricians = target,
                            assignedCount = assigned,
                            latitude = lat,
                            longitude = lng,
                            geofenceRadiusMeters = radius,
                            status = status,
                            notes = notes
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping site doc: ${doc.id}", e)
                        null
                    }
                }
            }
            .catch { e ->
                Log.e(TAG, "Error observing sites from Firestore", e)
                emit(emptyList())
            }
    }

    suspend fun saveSite(site: WorkSite) {
        val data = mapOf(
            "id" to site.id,
            "name" to site.name,
            "address" to site.address,
            "projectManager" to site.projectManager,
            "targetElectricians" to site.targetElectricians.toLong(),
            "assignedCount" to site.assignedCount.toLong(),
            "latitude" to site.latitude,
            "longitude" to site.longitude,
            "geofenceRadiusMeters" to site.geofenceRadiusMeters.toLong(),
            "status" to site.status,
            "notes" to site.notes
        )
        try {
            db.collection("sites").document(site.id).set(data).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save site ${site.id}", e)
        }
    }

    // Timesheets Realtime Stream
    fun observeTimesheets(): Flow<List<TimesheetLog>> {
        return db.collection("timesheets")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.getLong("id") ?: 0L
                        val workerId = doc.getString("workerId") ?: ""
                        val workerName = doc.getString("workerName") ?: ""
                        val siteName = doc.getString("siteName") ?: ""
                        val date = doc.getString("date") ?: ""
                        val checkInTime = doc.getString("checkInTime") ?: ""
                        val checkOutTime = doc.getString("checkOutTime")
                        val totalHours = doc.getDouble("totalHours") ?: 8.0
                        val gpsVerified = doc.getBoolean("gpsVerified") ?: true
                        val distanceMeters = doc.getLong("distanceMeters")?.toInt()
                        val notes = doc.getString("notes") ?: "Instalime të rregullta"

                        TimesheetLog(
                            id = id,
                            workerId = workerId,
                            workerName = workerName,
                            siteName = siteName,
                            date = date,
                            checkInTime = checkInTime,
                            checkOutTime = checkOutTime,
                            totalHours = totalHours,
                            gpsVerified = gpsVerified,
                            distanceMeters = distanceMeters,
                            notes = notes
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping timesheet doc: ${doc.id}", e)
                        null
                    }
                }
            }
            .catch { e ->
                Log.e(TAG, "Error observing timesheets from Firestore", e)
                emit(emptyList())
            }
    }

    suspend fun saveTimesheet(log: TimesheetLog) {
        val docId = if (log.id > 0) log.id.toString() else "ts_${System.currentTimeMillis()}"
        val data = mutableMapOf<String, Any?>(
            "id" to log.id,
            "workerId" to log.workerId,
            "workerName" to log.workerName,
            "siteName" to log.siteName,
            "date" to log.date,
            "checkInTime" to log.checkInTime,
            "checkOutTime" to log.checkOutTime,
            "totalHours" to log.totalHours,
            "gpsVerified" to log.gpsVerified,
            "distanceMeters" to log.distanceMeters,
            "notes" to log.notes
        )
        try {
            db.collection("timesheets").document(docId).set(data.filterValues { it != null }).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save timesheet $docId", e)
        }
    }

    // Alerts Realtime Stream
    fun observeAlerts(): Flow<List<ManagerAlert>> {
        return db.collection("alerts")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.getString("id") ?: doc.id
                        val workerName = doc.getString("workerName") ?: ""
                        val siteName = doc.getString("siteName")
                        val typeStr = doc.getString("type") ?: AlertType.OUT_OF_ZONE.name
                        val type = try {
                            AlertType.valueOf(typeStr)
                        } catch (e: Exception) {
                            AlertType.OUT_OF_ZONE
                        }
                        val title = doc.getString("title") ?: "Alarm"
                        val message = doc.getString("message") ?: ""
                        val distanceMeters = doc.getLong("distanceMeters")?.toInt()
                        val timestamp = doc.getString("timestamp") ?: ""
                        val isCritical = doc.getBoolean("isCritical") ?: false

                        ManagerAlert(
                            id = id,
                            type = type,
                            title = title,
                            message = message,
                            workerName = workerName,
                            siteName = siteName,
                            distanceMeters = distanceMeters,
                            timestamp = timestamp,
                            isCritical = isCritical
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping alert doc: ${doc.id}", e)
                        null
                    }
                }
            }
            .catch { e ->
                Log.e(TAG, "Error observing alerts from Firestore", e)
                emit(emptyList())
            }
    }

    suspend fun saveAlert(alert: ManagerAlert) {
        val data = mutableMapOf<String, Any?>(
            "id" to alert.id,
            "type" to alert.type.name,
            "title" to alert.title,
            "message" to alert.message,
            "workerName" to alert.workerName,
            "siteName" to alert.siteName,
            "distanceMeters" to alert.distanceMeters,
            "timestamp" to alert.timestamp,
            "isCritical" to alert.isCritical
        )
        try {
            db.collection("alerts").document(alert.id).set(data.filterValues { it != null }).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save alert ${alert.id}", e)
        }
    }

    // User Profile
    suspend fun saveUserProfile(uid: String, email: String, displayName: String, role: String, workerId: String? = null) {
        val data = mutableMapOf<String, Any>(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "role" to role,
            "lastLogin" to System.currentTimeMillis()
        )
        workerId?.let { data["workerId"] = it }
        try {
            db.collection("users").document(uid).set(data).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user profile $uid", e)
        }
    }

    suspend fun getUserProfile(uid: String): Map<String, Any>? {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) doc.data else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user profile $uid", e)
            null
        }
    }

    // Seed Initial Data to Firestore if collections are empty
    fun seedInitialDataIfEmpty(
        defaultWorkers: List<Worker>,
        defaultSites: List<WorkSite>,
        defaultTimesheets: List<TimesheetLog>,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val workersSnapshot = db.collection("workers").limit(1).get().await()
                if (workersSnapshot.isEmpty) {
                    Log.i(TAG, "Seeding initial workers to Firestore...")
                    for (worker in defaultWorkers) {
                        saveWorker(worker)
                    }
                }

                val sitesSnapshot = db.collection("sites").limit(1).get().await()
                if (sitesSnapshot.isEmpty) {
                    Log.i(TAG, "Seeding initial sites to Firestore...")
                    for (site in defaultSites) {
                        saveSite(site)
                    }
                }

                val timesheetsSnapshot = db.collection("timesheets").limit(1).get().await()
                if (timesheetsSnapshot.isEmpty) {
                    Log.i(TAG, "Seeding initial timesheets to Firestore...")
                    for (ts in defaultTimesheets) {
                        saveTimesheet(ts)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Note: Firestore seeding skipped or pending authentication: ${e.message}")
            }
        }
    }
}
