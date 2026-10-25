package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.TimesheetDao
import com.example.data.dao.WorkSiteDao
import com.example.data.dao.WorkerDao
import com.example.data.model.GpsStatus
import com.example.data.model.TimesheetLog
import com.example.data.model.WorkSite
import com.example.data.model.Worker
import com.example.data.model.WorkerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromWorkerStatus(value: WorkerStatus): String = value.name

    @TypeConverter
    fun toWorkerStatus(value: String): WorkerStatus = runCatching {
        WorkerStatus.valueOf(value)
    }.getOrDefault(WorkerStatus.WORKING)

    @TypeConverter
    fun fromGpsStatus(value: GpsStatus): String = value.name

    @TypeConverter
    fun toGpsStatus(value: String): GpsStatus = runCatching {
        GpsStatus.valueOf(value)
    }.getOrDefault(GpsStatus.VERIFIED)
}

@Database(
    entities = [Worker::class, WorkSite::class, TimesheetLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun workSiteDao(): WorkSiteDao
    abstract fun timesheetDao(): TimesheetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elektrotrack_db"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Seed Work Sites
            val sites = listOf(
                WorkSite(
                    id = "site_1",
                    name = "Objekti Banesor - Rruga B",
                    address = "Rruga B, Prishtinë",
                    projectManager = "Ing. Valmir Kelmendi",
                    targetElectricians = 2,
                    assignedCount = 1,
                    latitude = 42.6510,
                    longitude = 21.1710,
                    geofenceRadiusMeters = 100,
                    status = "Aktiv",
                    notes = "Instalimi i ormanëve elektrikë dhe kabllimi i kateve 4-7"
                ),
                WorkSite(
                    id = "site_2",
                    name = "Shtëpia e A. Berishës",
                    address = "Lagjja Marigona, Çagllavicë",
                    projectManager = "Agon Berisha",
                    targetElectricians = 1,
                    assignedCount = 1,
                    latitude = 42.6180,
                    longitude = 21.1490,
                    geofenceRadiusMeters = 80,
                    status = "Aktiv",
                    notes = "Ndriçimi arkitektural dhe sistemi Smart Home"
                ),
                WorkSite(
                    id = "site_3",
                    name = "Parku Fotovoltaik - Podujevë",
                    address = "Zona Industriale, Podujevë",
                    projectManager = "Ing. Dardan Shala",
                    targetElectricians = 3,
                    assignedCount = 1,
                    latitude = 42.9100,
                    longitude = 21.1900,
                    geofenceRadiusMeters = 200,
                    status = "Aktiv",
                    notes = "Montimi i invertorëve dhe lidhja e paneleve solare 250kW"
                ),
                WorkSite(
                    id = "site_4",
                    name = "Qendra Tregtare Prishtinë",
                    address = "Veternik, Prishtinë",
                    projectManager = "Ing. Lulzim Morina",
                    targetElectricians = 2,
                    assignedCount = 1,
                    latitude = 42.6390,
                    longitude = 21.1620,
                    geofenceRadiusMeters = 150,
                    status = "Aktiv",
                    notes = "Kontrolli i gjeneratorëve dhe ndriçimi i emergjencës"
                )
            )
            db.workSiteDao().insertSites(sites)

            // Seed Workers exactly matching the HTML table prompt
            val workers = listOf(
                Worker(
                    id = "w_1",
                    name = "Agon Berisha",
                    role = "Kryeelektricist & Mjeshtër",
                    phone = "+383 44 123 456",
                    currentSiteId = "site_1",
                    currentSiteName = "Objekti Banesor - Rruga B",
                    checkInTime = "07:45 AM",
                    gpsStatus = GpsStatus.VERIFIED,
                    gpsDistanceMeters = 12,
                    status = WorkerStatus.WORKING,
                    hoursToday = 8.5,
                    hourlyRateEur = 12.5,
                    latitude = 42.65108,
                    longitude = 21.17105
                ),
                Worker(
                    id = "w_2",
                    name = "Driton Gashi",
                    role = "Instalime Tension i Ulët",
                    phone = "+383 49 234 567",
                    currentSiteId = "site_2",
                    currentSiteName = "Shtëpia e A. Berishës",
                    checkInTime = "08:00 AM",
                    gpsStatus = GpsStatus.VERIFIED,
                    gpsDistanceMeters = 45,
                    status = WorkerStatus.WORKING,
                    hoursToday = 8.0,
                    hourlyRateEur = 10.0,
                    latitude = 42.6183,
                    longitude = 21.1493
                ),
                Worker(
                    id = "w_3",
                    name = "Valon Hoxha",
                    role = "Sisteme Fotovoltaike & Solare",
                    phone = "+383 45 345 678",
                    currentSiteId = "site_3",
                    currentSiteName = "Parku Fotovoltaik - Podujevë",
                    checkInTime = "08:15 AM",
                    gpsStatus = GpsStatus.VERIFIED,
                    gpsDistanceMeters = 8,
                    status = WorkerStatus.WORKING,
                    hoursToday = 8.0,
                    hourlyRateEur = 11.5,
                    latitude = 42.91005,
                    longitude = 21.19004
                ),
                Worker(
                    id = "w_4",
                    name = "Krenar Rama",
                    role = "Mirëmbajtje & Diagnostikim",
                    phone = "+383 44 456 789",
                    currentSiteId = null,
                    currentSiteName = "-",
                    checkInTime = "-",
                    gpsStatus = GpsStatus.OFFLINE,
                    gpsDistanceMeters = null,
                    status = WorkerStatus.BREAK,
                    hoursToday = 0.0,
                    hourlyRateEur = 9.5,
                    latitude = 42.6629,
                    longitude = 21.1655
                ),
                Worker(
                    id = "w_5",
                    name = "Artan Krasniqi",
                    role = "Instalime Banesore & Automatizim",
                    phone = "+383 49 567 890",
                    currentSiteId = "site_4",
                    currentSiteName = "Qendra Tregtare Prishtinë",
                    checkInTime = "08:30 AM",
                    gpsStatus = GpsStatus.VERIFIED,
                    gpsDistanceMeters = 25,
                    status = WorkerStatus.WORKING,
                    hoursToday = 8.0,
                    hourlyRateEur = 10.5,
                    latitude = 42.6391,
                    longitude = 21.1622
                )
            )
            db.workerDao().insertWorkers(workers)

            // Seed timesheet logs for reporting
            val timesheets = listOf(
                TimesheetLog(
                    workerId = "w_1",
                    workerName = "Agon Berisha",
                    siteName = "Objekti Banesor - Rruga B",
                    date = "14 Tetor 2026",
                    checkInTime = "07:45 AM",
                    checkOutTime = null,
                    totalHours = 8.5,
                    gpsVerified = true,
                    distanceMeters = 12,
                    notes = "Instalimi i ormanit kryesor të furnizimit"
                ),
                TimesheetLog(
                    workerId = "w_2",
                    workerName = "Driton Gashi",
                    siteName = "Shtëpia e A. Berishës",
                    date = "14 Tetor 2026",
                    checkInTime = "08:00 AM",
                    checkOutTime = null,
                    totalHours = 8.0,
                    gpsVerified = true,
                    distanceMeters = 45,
                    notes = "Kalimi i kabllove të rrjetit dhe ndriçimit të fshehur"
                ),
                TimesheetLog(
                    workerId = "w_3",
                    workerName = "Valon Hoxha",
                    siteName = "Parku Fotovoltaik - Podujevë",
                    date = "14 Tetor 2026",
                    checkInTime = "08:15 AM",
                    checkOutTime = null,
                    totalHours = 8.0,
                    gpsVerified = true,
                    distanceMeters = 8,
                    notes = "Testimi i kontinuitetit dhe testimi i izolimit"
                ),
                TimesheetLog(
                    workerId = "w_5",
                    workerName = "Artan Krasniqi",
                    siteName = "Qendra Tregtare Prishtinë",
                    date = "14 Tetor 2026",
                    checkInTime = "08:30 AM",
                    checkOutTime = null,
                    totalHours = 8.0,
                    gpsVerified = true,
                    distanceMeters = 25,
                    notes = "Lidhja e tabelave sekondare të energjisë"
                ),
                TimesheetLog(
                    workerId = "w_1",
                    workerName = "Agon Berisha",
                    siteName = "Objekti Banesor - Rruga B",
                    date = "13 Tetor 2026",
                    checkInTime = "08:00 AM",
                    checkOutTime = "04:30 PM",
                    totalHours = 8.5,
                    gpsVerified = true,
                    distanceMeters = 10,
                    notes = "Shpimi i trasave dhe montimi i kanaleve kabllore"
                ),
                TimesheetLog(
                    workerId = "w_2",
                    workerName = "Driton Gashi",
                    siteName = "Shtëpia e A. Berishës",
                    date = "13 Tetor 2026",
                    checkInTime = "08:00 AM",
                    checkOutTime = "04:00 PM",
                    totalHours = 8.0,
                    gpsVerified = true,
                    distanceMeters = 30,
                    notes = "Instalimi i çelësave dhe prizave"
                ),
                TimesheetLog(
                    workerId = "w_3",
                    workerName = "Valon Hoxha",
                    siteName = "Parku Fotovoltaik - Podujevë",
                    date = "13 Tetor 2026",
                    checkInTime = "08:30 AM",
                    checkOutTime = "04:30 PM",
                    totalHours = 8.0,
                    gpsVerified = true,
                    distanceMeters = 15,
                    notes = "Lidhja e kordonëve DC"
                ),
                TimesheetLog(
                    workerId = "w_4",
                    workerName = "Krenar Rama",
                    siteName = "Qendra Tregtare Prishtinë",
                    date = "13 Tetor 2026",
                    checkInTime = "08:00 AM",
                    checkOutTime = "04:00 PM",
                    totalHours = 8.0,
                    gpsVerified = true,
                    distanceMeters = 18,
                    notes = "Inspektimi i siguresave automatike"
                )
            )
            db.timesheetDao().insertTimesheets(timesheets)
        }
    }
}
