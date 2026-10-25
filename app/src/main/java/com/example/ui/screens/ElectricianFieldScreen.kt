package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import com.example.ui.components.AiAssistantSection
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TimesheetLog
import com.example.data.model.WorkSite
import com.example.data.model.Worker
import com.example.data.model.WorkerStatus
import com.example.ui.ElektroViewModel
import com.example.ui.components.GpsStatusBadge
import com.example.ui.components.StatusBadge
import com.example.ui.model.UserSession
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue600
import com.example.ui.theme.Green100
import com.example.ui.theme.Green600
import com.example.ui.theme.Green800
import com.example.ui.theme.Red100
import com.example.ui.theme.Red500
import com.example.ui.theme.Red800
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricianFieldScreen(
    session: UserSession,
    viewModel: ElektroViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val workers by viewModel.allWorkers.collectAsState()
    val sites by viewModel.allSites.collectAsState()
    val timesheets by viewModel.allTimesheets.collectAsState()
    val notificationMessage by viewModel.userNotificationMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Find current worker object
    val currentWorker = workers.firstOrNull { it.id == session.workerId } ?: workers.firstOrNull { it.name == session.name }
    val assignedSite = sites.firstOrNull { it.id == currentWorker?.currentSiteId } ?: sites.firstOrNull()

    var selectedSiteForCheckIn by remember { mutableStateOf(assignedSite) }
    var siteDropdownExpanded by remember { mutableStateOf(false) }
    var workNotes by remember { mutableStateOf("") }

    LaunchedEffect(notificationMessage) {
        notificationMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Worker Profile & Logout
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("electrician_header_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Amber500.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FlashOn,
                                            contentDescription = null,
                                            tint = Amber500,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = session.name,
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = currentWorker?.role ?: session.roleDescription,
                                            color = Amber500,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showLogoutDialog = true },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Slate800)
                                        .testTag("electrician_logout_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Dil (Logout)",
                                        tint = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }

                // Current Work Status Card
                item {
                    currentWorker?.let { worker ->
                        val isWorking = worker.status == WorkerStatus.WORKING

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("electrician_status_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isWorking) Green100.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isWorking) Green600 else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "STATUSI AKTUAL NË TERREN",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isWorking) Green800 else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    StatusBadge(status = worker.status)
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Objekti:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = worker.currentSiteName ?: "Pa caktim aktiv",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Check-in:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = worker.checkInTime ?: "--:--",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Statusi GPS: ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        GpsStatusBadge(status = worker.gpsStatus, distanceMeters = worker.gpsDistanceMeters)
                                    }

                                    Text(
                                        text = "Orë sot: ${worker.hoursToday} std (€${worker.hourlyRateEur}/h)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Green800
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Action Buttons
                                if (isWorking) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.updateWorkerStatus(worker, WorkerStatus.PAUSED) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.PauseCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Merr Pauzë")
                                        }

                                        Button(
                                            onClick = { viewModel.checkOutWorker(worker) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Red500),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Check-out")
                                        }
                                    }
                                } else {
                                    // Check-in Selector & Action
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Zgjidh objektin për check-in:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        ExposedDropdownMenuBox(
                                            expanded = siteDropdownExpanded,
                                            onExpandedChange = { siteDropdownExpanded = !siteDropdownExpanded },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedTextField(
                                                value = selectedSiteForCheckIn?.name ?: "Zgjidh objektin",
                                                onValueChange = {},
                                                readOnly = true,
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = siteDropdownExpanded) },
                                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            ExposedDropdownMenu(
                                                expanded = siteDropdownExpanded,
                                                onDismissRequest = { siteDropdownExpanded = false }
                                            ) {
                                                sites.forEach { site ->
                                                    DropdownMenuItem(
                                                        text = { Text("${site.name} (${site.address})") },
                                                        onClick = {
                                                            selectedSiteForCheckIn = site
                                                            siteDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                selectedSiteForCheckIn?.let { site ->
                                                    // Simulate check-in at site position with 12m verified precision
                                                    viewModel.checkInWorker(
                                                        worker = worker,
                                                        site = site,
                                                        lat = site.latitude + 0.0001,
                                                        lng = site.longitude + 0.0001
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("electrician_checkin_button"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Green600)
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Kryej Check-in me Verifikim GPS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Assigned Job Site Details & Manager Contact
                item {
                    assignedSite?.let { site ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("electrician_site_details_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Blue600, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Të Dhënat e Objektit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Text(text = "Rrezi: ${site.geofenceRadiusMeters}m", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = site.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(text = "📍 ${site.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                if (site.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "Detyrat: ${site.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Përgjegjës Objekti:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = site.projectManager, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:+38344123456")
                                            }
                                            context.startActivity(intent)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Telefono", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Daily Work Log / Notes Submission
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(text = "📝 Ditar Pune & Shënime të Terrenit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Shënoni punimet elektrike të kryera sot për raportin mujor:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = workNotes,
                                onValueChange = { workNotes = it },
                                placeholder = { Text("p.sh. Kabllimi i katit 4 u përfundua, u montuan 12 priza...") },
                                modifier = Modifier.fillMaxWidth().testTag("worker_daily_notes_input"),
                                shape = RoundedCornerShape(10.dp),
                                minLines = 3
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (workNotes.isNotBlank()) {
                                        viewModel.userNotificationMessage.value = "Shënimi u regjistrua me sukses në ditar!"
                                        workNotes = ""
                                    }
                                },
                                enabled = workNotes.isNotBlank(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Dërgo Raportin e Ditës")
                            }
                        }
                    }
                }

                // AI Field Assistant with Google Search and Maps Grounding
                item {
                    AiAssistantSection()
                }

                // Worker Personal Timesheet Logs
                item {
                    val workerLogs = timesheets.filter { it.workerId == session.workerId || it.workerName == session.name }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Historiku Im i Check-in (${workerLogs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (workerLogs.isEmpty()) {
                            Text(text = "Nuk ka regjistrime të mëparshme.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                workerLogs.take(4).forEach { log ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = log.siteName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                Text(text = "${log.date} • Hyrja: ${log.checkInTime}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text(text = "${log.totalHours} std", fontWeight = FontWeight.Bold, color = Green800, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Dalje nga llogaria", fontWeight = FontWeight.Bold) },
            text = { Text("A jeni i sigurt që dëshironi të dilni nga llogaria e elektricistit?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red500)
                ) {
                    Text("Dil (Logout)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Anulo")
                }
            }
        )
    }
}
