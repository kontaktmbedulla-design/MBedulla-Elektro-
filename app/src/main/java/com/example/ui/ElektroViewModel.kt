package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ElektroRepository
import com.example.data.model.GpsStatus
import com.example.data.model.TimesheetLog
import com.example.data.model.WorkSite
import com.example.data.model.Worker
import com.example.data.model.WorkerStatus
import com.example.ui.model.AlertType
import com.example.ui.model.ManagerAlert
import com.example.ui.model.UserRole
import com.example.ui.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.example.data.firebase.FirestoreManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class ElektroViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = ElektroRepository(
        workerDao = database.workerDao(),
        workSiteDao = database.workSiteDao(),
        timesheetDao = database.timesheetDao()
    )

    private val firestoreManager = FirestoreManager.getInstance(application)
    private val auth = Firebase.auth
    val firebaseUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)

    init {
        // Observe Firebase Auth changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            firebaseUser.value = user
        }

        // Seed initial data to Firestore if online & empty
        viewModelScope.launch {
            try {
                val defaultWorkers = repository.allWorkers.first()
                val defaultSites = repository.allSites.first()
                val defaultTimesheets = repository.allTimesheets.first()
                firestoreManager.seedInitialDataIfEmpty(
                    defaultWorkers = defaultWorkers,
                    defaultSites = defaultSites,
                    defaultTimesheets = defaultTimesheets,
                    scope = viewModelScope
                )
            } catch (e: Exception) {
                // Ignore flow collection failures during setup
            }
        }
    }

    val allWorkers: StateFlow<List<Worker>> = repository.allWorkers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSites: StateFlow<List<WorkSite>> = repository.allSites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTimesheets: StateFlow<List<TimesheetLog>> = repository.allTimesheets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow("ALL") // "ALL", "WORKING", "BREAK", "GPS_ALERT"
    val selectedTab = MutableStateFlow(0) // 0: Dashboard, 1: Sites, 2: Reports, 3: Simulation, 4: AI Terreni

    val selectedWorkerForDetails = MutableStateFlow<Worker?>(null)
    val showAddWorkerDialog = MutableStateFlow(false)
    val showAddSiteDialog = MutableStateFlow(false)
    val showExportDialog = MutableStateFlow(false)
    val showAlertsHistoryDialog = MutableStateFlow(false)
    val userNotificationMessage = MutableStateFlow<String?>(null)

    // User Authentication Session (Role Division: Manager / Electrician)
    val currentUserSession = MutableStateFlow<UserSession?>(
        UserSession(
            id = "mgr_1",
            name = "Ing. Valmir Kelmendi",
            email = "menaxher@elektrotrack.com",
            role = UserRole.MANAGER,
            roleDescription = "Kryeinxhinier & Menaxher Projekti"
        )
    )

    fun signInWithGoogle(idToken: String, chosenRole: UserRole = UserRole.MANAGER, workerId: String? = null) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user
                firebaseUser.value = user
                if (user != null) {
                    val displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Përdorues Google"
                    val email = user.email ?: ""

                    firestoreManager.saveUserProfile(
                        uid = user.uid,
                        email = email,
                        displayName = displayName,
                        role = chosenRole.name,
                        workerId = workerId
                    )

                    currentUserSession.value = UserSession(
                        id = user.uid,
                        name = displayName,
                        email = email,
                        role = chosenRole,
                        workerId = workerId,
                        roleDescription = if (chosenRole == UserRole.MANAGER) "Menaxher Projekti (Google Auth)" else "Elektricist në Terren (Google Auth)"
                    )
                    userNotificationMessage.value = "Mirë se vini, $displayName!"
                }
            } catch (e: Exception) {
                userNotificationMessage.value = "Dështoi kyçja me Google: ${e.localizedMessage ?: "Gabim"}"
            }
        }
    }

    fun loginAsManager(email: String) {
        currentUserSession.value = UserSession(
            id = "mgr_1",
            name = "Ing. Valmir Kelmendi",
            email = email,
            role = UserRole.MANAGER,
            roleDescription = "Kryeinxhinier & Menaxher Projekti"
        )
        userNotificationMessage.value = "Mirë se vini në Panelin e Menaxherit!"
    }

    fun loginAsElectrician(worker: Worker) {
        currentUserSession.value = UserSession(
            id = "session_${worker.id}",
            name = worker.name,
            email = "${worker.name.lowercase().replace(" ", ".")}@elektrotrack.com",
            role = UserRole.ELECTRICIAN,
            workerId = worker.id,
            roleDescription = worker.role
        )
        userNotificationMessage.value = "Mirë se vini, ${worker.name}!"
    }

    fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // ignore
        }
        firebaseUser.value = null
        currentUserSession.value = null
        userNotificationMessage.value = "Keni dalë me sukses nga llogaria."
    }

    // Notification system for manager alerts
    val currentToastAlert = MutableStateFlow<ManagerAlert?>(null)
    val managerAlerts = MutableStateFlow<List<ManagerAlert>>(
        listOf(
            ManagerAlert(
                id = "init_1",
                type = AlertType.CHECK_IN,
                title = "🟢 Check-in i Verifikuar",
                message = "Agon Berisha u verifikua në 'Objekti Banesor - Rruga B' (12m nga qendra).",
                workerName = "Agon Berisha",
                siteName = "Objekti Banesor - Rruga B",
                distanceMeters = 12,
                timestamp = "07:45 AM",
                isCritical = false
            ),
            ManagerAlert(
                id = "init_2",
                type = AlertType.CHECK_IN,
                title = "🟢 Check-in i Verifikuar",
                message = "Valon Hoxha u verifikua në 'Parku Fotovoltaik - Podujevë' (8m).",
                workerName = "Valon Hoxha",
                siteName = "Parku Fotovoltaik - Podujevë",
                distanceMeters = 8,
                timestamp = "08:15 AM",
                isCritical = false
            )
        )
    )

    // Filtered workers list
    val filteredWorkers: StateFlow<List<Worker>> = combine(
        allWorkers,
        searchQuery,
        statusFilter
    ) { workers, query, filter ->
        workers.filter { worker ->
            val matchesQuery = query.isBlank() ||
                    worker.name.contains(query, ignoreCase = true) ||
                    (worker.currentSiteName?.contains(query, ignoreCase = true) == true) ||
                    worker.role.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "WORKING" -> worker.status == WorkerStatus.WORKING
                "BREAK" -> worker.status == WorkerStatus.BREAK
                "GPS_ALERT" -> worker.gpsStatus == GpsStatus.OUT_OF_BOUNDS || worker.gpsStatus == GpsStatus.NO_SIGNAL
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics
    val activeWorkersCount: StateFlow<Int> = allWorkers.combine(allWorkers) { workers, _ ->
        workers.count { it.status == WorkerStatus.WORKING }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4)

    val totalWorkersCount: StateFlow<Int> = allWorkers.combine(allWorkers) { workers, _ ->
        workers.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    val activeSitesCount: StateFlow<Int> = allSites.combine(allWorkers) { sites, workers ->
        val sitesWithWorkers = workers.filter { it.status == WorkerStatus.WORKING && it.currentSiteId != null }
            .mapNotNull { it.currentSiteId }
            .distinct()
            .size
        if (sitesWithWorkers > 0) sitesWithWorkers else sites.count { it.status == "Aktiv" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    val totalHoursToday: StateFlow<Double> = allWorkers.combine(allWorkers) { workers, _ ->
        workers.filter { it.status == WorkerStatus.WORKING }.sumOf { it.hoursToday }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 32.5)

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        statusFilter.value = filter
    }

    fun setSelectedTab(tab: Int) {
        selectedTab.value = tab
    }

    fun openWorkerDetails(worker: Worker) {
        selectedWorkerForDetails.value = worker
    }

    fun closeWorkerDetails() {
        selectedWorkerForDetails.value = null
    }

    fun triggerAlert(alert: ManagerAlert) {
        currentToastAlert.value = alert
        managerAlerts.value = listOf(alert) + managerAlerts.value
    }

    fun dismissToastAlert() {
        currentToastAlert.value = null
    }

    fun clearAllAlerts() {
        managerAlerts.value = emptyList()
    }

    fun checkInWorker(worker: Worker, site: WorkSite, lat: Double, lng: Double) {
        viewModelScope.launch {
            val distance = ElektroRepository.calculateDistanceInMeters(
                lat1 = lat,
                lon1 = lng,
                lat2 = site.latitude,
                lon2 = site.longitude
            ).toInt()

            val isOutOfZone = distance > site.geofenceRadiusMeters
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val currentTime = timeFormat.format(Date())

            repository.checkInWorker(worker, site, lat, lng)
            val updatedWorker = worker.copy(
                currentSiteId = site.id,
                currentSiteName = site.name,
                status = WorkerStatus.WORKING,
                gpsStatus = if (isOutOfZone) GpsStatus.OUT_OF_BOUNDS else GpsStatus.VERIFIED,
                gpsDistanceMeters = distance,
                checkInTime = currentTime,
                latitude = lat,
                longitude = lng
            )
            firestoreManager.saveWorker(updatedWorker)

            // Trigger notification system
            val alert = if (isOutOfZone) {
                ManagerAlert(
                    id = UUID.randomUUID().toString(),
                    type = AlertType.OUT_OF_ZONE,
                    title = "⚠️ ALARM: Jashtë Zonës së Objektit!",
                    message = "${worker.name} doli jashtë zonës së '${site.name}' ($distance m larg, rrezja e lejuar ${site.geofenceRadiusMeters} m)!",
                    workerName = worker.name,
                    siteName = site.name,
                    distanceMeters = distance,
                    timestamp = currentTime,
                    isCritical = true
                )
            } else {
                ManagerAlert(
                    id = UUID.randomUUID().toString(),
                    type = AlertType.CHECK_IN,
                    title = "🟢 Check-in i Verifikuar",
                    message = "${worker.name} hyri në punë te '${site.name}' (distanca e verifikuar: $distance m).",
                    workerName = worker.name,
                    siteName = site.name,
                    distanceMeters = distance,
                    timestamp = currentTime,
                    isCritical = false
                )
            }
            triggerAlert(alert)
            firestoreManager.saveAlert(alert)

            userNotificationMessage.value = if (isOutOfZone) {
                "⚠️ Paralajmërim: ${worker.name} ndodhet jashtë zonës së objektit ($distance m)!"
            } else {
                "Check-in i suksesshëm për ${worker.name} në ${site.name}"
            }

            // Update selectedWorker if opened
            selectedWorkerForDetails.value?.let {
                if (it.id == worker.id) {
                    selectedWorkerForDetails.value = updatedWorker
                }
            }
        }
    }

    fun checkOutWorker(worker: Worker) {
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val currentTime = timeFormat.format(Date())

            repository.checkOutWorker(worker)
            val updatedWorker = worker.copy(
                status = WorkerStatus.BREAK,
                currentSiteName = "-",
                checkInTime = "-"
            )
            firestoreManager.saveWorker(updatedWorker)

            val alert = ManagerAlert(
                id = UUID.randomUUID().toString(),
                type = AlertType.CHECK_OUT,
                title = "🛑 Çregjistrim nga Objekti",
                message = "${worker.name} përfundoi turnin dhe u çregjistrua nga objekti.",
                workerName = worker.name,
                siteName = worker.currentSiteName,
                timestamp = currentTime,
                isCritical = false
            )
            triggerAlert(alert)
            firestoreManager.saveAlert(alert)

            userNotificationMessage.value = "U kry check-out për ${worker.name}"
            selectedWorkerForDetails.value?.let {
                if (it.id == worker.id) {
                    selectedWorkerForDetails.value = updatedWorker
                }
            }
        }
    }

    fun updateWorkerStatus(worker: Worker, newStatus: WorkerStatus) {
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val currentTime = timeFormat.format(Date())

            repository.changeWorkerStatus(worker, newStatus)
            val updatedWorker = worker.copy(status = newStatus)
            firestoreManager.saveWorker(updatedWorker)

            val alert = ManagerAlert(
                id = UUID.randomUUID().toString(),
                type = AlertType.STATUS_CHANGED,
                title = "⚡ Ndryshim Statusi i Elektricistit",
                message = "${worker.name} ndryshoi statusin nga '${worker.status.label}' në '${newStatus.label}'.",
                workerName = worker.name,
                siteName = worker.currentSiteName,
                timestamp = currentTime,
                isCritical = newStatus == WorkerStatus.BREAK
            )
            triggerAlert(alert)
            firestoreManager.saveAlert(alert)

            selectedWorkerForDetails.value?.let {
                if (it.id == worker.id) {
                    selectedWorkerForDetails.value = updatedWorker
                }
            }
        }
    }

    fun addNewSite(
        name: String,
        address: String,
        projectManager: String,
        targetElectricians: Int,
        lat: Double,
        lng: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val newSite = WorkSite(
                id = "site_" + UUID.randomUUID().toString().take(6),
                name = name,
                address = address,
                projectManager = projectManager.ifBlank { "Ing. Menaxher" },
                targetElectricians = targetElectricians,
                latitude = lat,
                longitude = lng,
                geofenceRadiusMeters = 100,
                status = "Aktiv",
                notes = notes
            )
            repository.insertSite(newSite)
            firestoreManager.saveSite(newSite)
            showAddSiteDialog.value = false
            userNotificationMessage.value = "Objekti '$name' u shtua me sukses!"
        }
    }

    fun addNewWorker(name: String, role: String, phone: String, hourlyRate: Double) {
        viewModelScope.launch {
            val newWorker = Worker(
                id = "w_" + UUID.randomUUID().toString().take(6),
                name = name,
                role = role.ifBlank { "Elektricist" },
                phone = phone.ifBlank { "+383 44 000 000" },
                currentSiteId = null,
                currentSiteName = "-",
                checkInTime = "-",
                gpsStatus = GpsStatus.OFFLINE,
                gpsDistanceMeters = null,
                status = WorkerStatus.BREAK,
                hoursToday = 0.0,
                hourlyRateEur = hourlyRate
            )
            repository.insertWorker(newWorker)
            firestoreManager.saveWorker(newWorker)
            showAddWorkerDialog.value = false
            userNotificationMessage.value = "Elektricisti '$name' u regjistrua me sukses!"
        }
    }

    fun generateMonthlyReportText(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val generatedAt = dateFormat.format(Date())
        val workersList = allWorkers.value
        val sitesList = allSites.value
        val timesheetsList = allTimesheets.value

        val totalHours = workersList.sumOf { it.hoursToday }
        val activeCount = workersList.count { it.status == WorkerStatus.WORKING }

        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("⚡ ELEKTROTRACK - RAPORTI MUJOR & DITOR ⚡\n")
        sb.append("=========================================\n")
        sb.append("Gjeneruar më: $generatedAt\n")
        sb.append("Data e Raportit: 14 Tetor 2026\n\n")

        sb.append("📊 PËRMBLEDHJE STATISTIKORE:\n")
        sb.append("• Punëtorë Aktivë në Terren: $activeCount / ${workersList.size}\n")
        sb.append("• Objekte me Punime: ${sitesList.size} Objekte\n")
        sb.append("• Gjithsej Orë Pune Sot: ${String.format(Locale.US, "%.1f", totalHours)} std\n\n")

        sb.append("📍 STATUSI I ELEKTRICISTËVE (NË KOHË REALE):\n")
        sb.append("-----------------------------------------\n")
        workersList.forEach { w ->
            sb.append("• ${w.name} (${w.role})\n")
            sb.append("  Objekti: ${w.currentSiteName ?: '-'}\n")
            sb.append("  Check-in: ${w.checkInTime ?: '-'}\n")
            val gpsStr = if (w.gpsDistanceMeters != null) "Verified (${w.gpsDistanceMeters}m)" else w.gpsStatus.label
            sb.append("  GPS Status: $gpsStr\n")
            sb.append("  Statusi: ${w.status.label} | Orë sot: ${w.hoursToday} std\n\n")
        }

        sb.append("🏗️ OBJEKTET DHE LOKACIONET:\n")
        sb.append("-----------------------------------------\n")
        sitesList.forEach { s ->
            sb.append("• ${s.name}\n")
            sb.append("  Lokacioni: ${s.address}\n")
            sb.append("  Përgjegjës: ${s.projectManager}\n")
            sb.append("  Rrezi Geofence: ${s.geofenceRadiusMeters}m | Statusi: ${s.status}\n\n")
        }

        sb.append("🕒 HISTORIKU I REGJISTRIMEVE TË FUNDIT:\n")
        sb.append("-----------------------------------------\n")
        timesheetsList.take(6).forEach { t ->
            sb.append("• ${t.date} | ${t.workerName} - ${t.siteName}\n")
            sb.append("  Hyrja: ${t.checkInTime} | Orë: ${t.totalHours} std | GPS: ${if (t.gpsVerified) "✓ Verifikuar (${t.distanceMeters ?: 0}m)" else "✗ Jashtë rrezes"}\n")
        }

        sb.append("\n=========================================\n")
        sb.append("ElektroTrack ⚡ - Sistemi i Menaxhimit të Terrenit\n")
        return sb.toString()
    }

    fun clearNotification() {
        userNotificationMessage.value = null
    }
}
