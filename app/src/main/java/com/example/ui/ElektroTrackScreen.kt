package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddSiteDialog
import com.example.ui.components.AddWorkerDialog
import com.example.ui.components.AiAssistantSection
import com.example.ui.components.AlertsHistoryDialog
import com.example.ui.components.ElectriciansMapView
import com.example.ui.components.ElektroHeader
import com.example.ui.components.ExportReportDialog
import com.example.ui.components.GpsSimulationSection
import com.example.ui.components.ManagerToastBanner
import com.example.ui.components.ReportsSection
import com.example.ui.components.SitesSection
import com.example.ui.components.StatsGrid
import com.example.ui.components.WorkerDetailsBottomSheet
import com.example.ui.components.WorkerTableCard
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue600
import com.example.ui.theme.Slate900

@Composable
fun ElektroTrackScreen(
    viewModel: ElektroViewModel,
    modifier: Modifier = Modifier
) {
    val workers by viewModel.filteredWorkers.collectAsState()
    val allWorkersList by viewModel.allWorkers.collectAsState()
    val sites by viewModel.allSites.collectAsState()
    val timesheets by viewModel.allTimesheets.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val activeWorkersCount by viewModel.activeWorkersCount.collectAsState()
    val totalWorkersCount by viewModel.totalWorkersCount.collectAsState()
    val activeSitesCount by viewModel.activeSitesCount.collectAsState()
    val totalHoursToday by viewModel.totalHoursToday.collectAsState()

    val selectedWorker by viewModel.selectedWorkerForDetails.collectAsState()
    val showAddWorkerDialog by viewModel.showAddWorkerDialog.collectAsState()
    val showAddSiteDialog by viewModel.showAddSiteDialog.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val showAlertsHistoryDialog by viewModel.showAlertsHistoryDialog.collectAsState()
    val notificationMessage by viewModel.userNotificationMessage.collectAsState()

    val currentToastAlert by viewModel.currentToastAlert.collectAsState()
    val managerAlerts by viewModel.managerAlerts.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var dashboardViewMode by remember { mutableStateOf("BOTH") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(notificationMessage) {
        notificationMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val items = listOf(
                    Triple("Paneli", Icons.Default.Dashboard, 0),
                    Triple("Objektet", Icons.Default.Business, 1),
                    Triple("Raportet", Icons.Default.Description, 2),
                    Triple("Simulim GPS", Icons.Default.MyLocation, 3),
                    Triple("AI Terreni ⚡", Icons.Default.AutoAwesome, 4)
                )

                items.forEach { (label, icon, tabIndex) ->
                    val isSelected = selectedTab == tabIndex
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTab(tabIndex) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) Amber500 else Color(0xFF94A3B8)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Amber500 else Color(0xFF94A3B8)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Slate900.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("nav_tab_$tabIndex")
                    )
                }
            }
        },
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
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header (ElektroTrack ⚡ | Paneli i Kontrollit, Date, Notification Bell, Quick Actions, Logout)
                item {
                    ElektroHeader(
                        onAddWorkerClick = { viewModel.showAddWorkerDialog.value = true },
                        onAddSiteClick = { viewModel.showAddSiteDialog.value = true },
                        alertCount = managerAlerts.size,
                        onAlertsClick = { viewModel.showAlertsHistoryDialog.value = true },
                        onLogoutClick = { showLogoutDialog = true }
                    )
                }

                // Stats Grid matching HTML (Punëtorë në Terren: 4/5, Objekte me Punime: 3, Gjithsej Orë Sot: 32.5)
                item {
                    StatsGrid(
                        activeWorkers = activeWorkersCount,
                        totalWorkers = totalWorkersCount,
                        activeSites = activeSitesCount,
                        totalHours = totalHoursToday
                    )
                }

                // Content depending on selected tab
                item {
                    when (selectedTab) {
                        0 -> {
                            // Dashboard Tab: View mode switcher (Table / Map / Both)
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                // View Mode Segmented Controls
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val modes = listOf(
                                        Triple("BOTH", "⚡ Pamje e Kombinuar", Icons.Default.Dashboard),
                                        Triple("MAP", "🗺️ Harta Live", Icons.Default.MyLocation),
                                        Triple("TABLE", "📋 Tabela e Punëtorëve", Icons.Default.Business)
                                    )

                                    modes.forEach { (modeKey, modeTitle, modeIcon) ->
                                        val isSelected = dashboardViewMode == modeKey
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) Slate900 else Color.Transparent)
                                                .clickable { dashboardViewMode = modeKey }
                                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                                .testTag("dashboard_view_mode_$modeKey"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = modeTitle,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Amber500 else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // 1. Map View (when BOTH or MAP)
                                if (dashboardViewMode == "BOTH" || dashboardViewMode == "MAP") {
                                    ElectriciansMapView(
                                        workers = workers,
                                        sites = sites,
                                        onWorkerClick = { viewModel.openWorkerDetails(it) }
                                    )
                                }

                                // 2. Worker Table Card (when BOTH or TABLE)
                                if (dashboardViewMode == "BOTH" || dashboardViewMode == "TABLE") {
                                    WorkerTableCard(
                                        workers = workers,
                                        searchQuery = searchQuery,
                                        onSearchChange = { viewModel.setSearchQuery(it) },
                                        statusFilter = statusFilter,
                                        onFilterChange = { viewModel.setStatusFilter(it) },
                                        onExportReportClick = { viewModel.showExportDialog.value = true },
                                        onWorkerClick = { viewModel.openWorkerDetails(it) }
                                    )
                                }
                            }
                        }
                        1 -> {
                            // Sites Tab: List of Work Sites & Geofencing
                            SitesSection(
                                sites = sites,
                                workers = allWorkersList,
                                onAddSiteClick = { viewModel.showAddSiteDialog.value = true }
                            )
                        }
                        2 -> {
                            // Reports Tab: Monthly PDF & Timesheets
                            ReportsSection(
                                timesheets = timesheets,
                                onExportClick = { viewModel.showExportDialog.value = true }
                            )
                        }
                        3 -> {
                            // GPS Simulation Tab: Interactive Geofence Testing
                            GpsSimulationSection(
                                workers = allWorkersList,
                                sites = sites,
                                onSimulateCheckIn = { worker, site, lat, lng ->
                                    viewModel.checkInWorker(worker, site, lat, lng)
                                }
                            )
                        }
                        4 -> {
                            // AI Assistant Tab: Google Search and Google Maps Grounding
                            AiAssistantSection()
                        }
                    }
                }
            }

            // Floating in-app manager toast banner at the top
            ManagerToastBanner(
                alert = currentToastAlert,
                onDismiss = { viewModel.dismissToastAlert() }
            )
        }
    }

    // Modal dialogs & bottom sheets
    selectedWorker?.let { worker ->
        WorkerDetailsBottomSheet(
            worker = worker,
            sites = sites,
            onDismiss = { viewModel.closeWorkerDetails() },
            onStatusChange = { newStatus -> viewModel.updateWorkerStatus(worker, newStatus) },
            onCheckIn = { w, site, lat, lng -> viewModel.checkInWorker(w, site, lat, lng) },
            onCheckOut = { w -> viewModel.checkOutWorker(w) }
        )
    }

    if (showAddWorkerDialog) {
        AddWorkerDialog(
            onDismiss = { viewModel.showAddWorkerDialog.value = false },
            onConfirm = { name, role, phone, rate ->
                viewModel.addNewWorker(name, role, phone, rate)
            }
        )
    }

    if (showAddSiteDialog) {
        AddSiteDialog(
            onDismiss = { viewModel.showAddSiteDialog.value = false },
            onConfirm = { name, address, manager, target, lat, lng, notes ->
                viewModel.addNewSite(name, address, manager, target, lat, lng, notes)
            }
        )
    }

    if (showExportDialog) {
        ExportReportDialog(
            reportText = viewModel.generateMonthlyReportText(),
            onDismiss = { viewModel.showExportDialog.value = false }
        )
    }

    if (showAlertsHistoryDialog) {
        AlertsHistoryDialog(
            alerts = managerAlerts,
            onClearAll = { viewModel.clearAllAlerts() },
            onDismiss = { viewModel.showAlertsHistoryDialog.value = false }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Dalje nga Paneli i Menaxherit", fontWeight = FontWeight.Bold) },
            text = { Text("A jeni i sigurt që dëshironi të dilni nga llogaria e menaxherit?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
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
