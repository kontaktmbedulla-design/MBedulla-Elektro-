package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.model.WorkSite
import com.example.data.model.Worker
import com.example.ui.theme.Cyan50
import com.example.ui.theme.Cyan800
import com.example.ui.theme.Green100
import com.example.ui.theme.Green600
import com.example.ui.theme.Green800
import com.example.ui.theme.Red100
import com.example.ui.theme.Red800
import kotlin.math.cos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsSimulationSection(
    workers: List<Worker>,
    sites: List<WorkSite>,
    onSimulateCheckIn: (Worker, WorkSite, Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedWorkerIndex by remember { mutableStateOf(0) }
    var selectedSiteIndex by remember { mutableStateOf(0) }
    var workerDropdownExpanded by remember { mutableStateOf(false) }
    var siteDropdownExpanded by remember { mutableStateOf(false) }
    var distanceMeters by remember { mutableFloatStateOf(15f) }

    val currentWorker = workers.getOrNull(selectedWorkerIndex) ?: workers.firstOrNull()
    val currentSite = sites.getOrNull(selectedSiteIndex) ?: sites.firstOrNull()

    val geofenceRadius = currentSite?.geofenceRadiusMeters ?: 100
    val isInsideGeofence = distanceMeters <= geofenceRadius

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gps_simulation_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Cyan50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = Cyan800,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "🛰️ Simulimi i Verifikimit GPS në Terren",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Testoni sistemin automatik të Geofencing dhe Check-in",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Select Worker Dropdown
            Text(
                text = "Zgjidh Elektricistin:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = workerDropdownExpanded,
                onExpandedChange = { workerDropdownExpanded = !workerDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = currentWorker?.let { "${it.name} (${it.status.label})" } ?: "Zgjidh elektricistin",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workerDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = workerDropdownExpanded,
                    onDismissRequest = { workerDropdownExpanded = false }
                ) {
                    workers.forEachIndexed { index, worker ->
                        DropdownMenuItem(
                            text = { Text("${worker.name} - ${worker.role}") },
                            onClick = {
                                selectedWorkerIndex = index
                                workerDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Select Worksite Dropdown
            Text(
                text = "Zgjidh Objektin me Punime:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = siteDropdownExpanded,
                onExpandedChange = { siteDropdownExpanded = !siteDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = currentSite?.let { "${it.name} (Rrezja: ${it.geofenceRadiusMeters}m)" } ?: "Zgjidh objektin",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = siteDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = siteDropdownExpanded,
                    onDismissRequest = { siteDropdownExpanded = false }
                ) {
                    sites.forEachIndexed { index, site ->
                        DropdownMenuItem(
                            text = { Text("${site.name} (${site.address})") },
                            onClick = {
                                selectedSiteIndex = index
                                siteDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Distance Presets & Slider
            Text(
                text = "Largësia e Elektricistit nga Objekti: ${distanceMeters.toInt()} metra",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isInsideGeofence) Green800 else Red800
            )

            Slider(
                value = distanceMeters,
                onValueChange = { distanceMeters = it },
                valueRange = 5f..500f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )

            // Preset Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = distanceMeters == 12f,
                    onClick = { distanceMeters = 12f },
                    label = { Text("12m (Brenda)") }
                )
                FilterChip(
                    selected = distanceMeters == 45f,
                    onClick = { distanceMeters = 45f },
                    label = { Text("45m (Brenda)") }
                )
                FilterChip(
                    selected = distanceMeters == 250f,
                    onClick = { distanceMeters = 250f },
                    label = { Text("250m (Jashtë)") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Geofence result preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isInsideGeofence) Green100 else Red100)
                    .border(1.dp, if (isInsideGeofence) Green800 else Red800, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isInsideGeofence) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isInsideGeofence) Green800 else Red800,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isInsideGeofence)
                                "🟢 VERIFIKIM I SUKSESSHEM: Brenda rrezes së objektit (${distanceMeters.toInt()}m <= ${geofenceRadius}m)"
                            else
                                "🟡 PARALAJMERIM GEOFENCE: Jashtë rrezes së lejuar (${distanceMeters.toInt()}m > ${geofenceRadius}m)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isInsideGeofence) Green800 else Red800
                        )
                        Text(
                            text = if (isInsideGeofence)
                                "Statusi do të shënohet: 'Verified (${distanceMeters.toInt()}m)'"
                            else
                                "Statusi do të shënohet: 'Jashtë Zonës (${distanceMeters.toInt()}m)' me alarm tek menaxheri",
                            fontSize = 11.sp,
                            color = if (isInsideGeofence) Green800.copy(alpha = 0.8f) else Red800.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Button
            Button(
                onClick = {
                    if (currentWorker != null && currentSite != null) {
                        // Compute offset lat/lng matching distanceMeters
                        // Approx 1 meter latitude is ~ 1 / 111111 degrees
                        val deltaLat = (distanceMeters / 111111.0)
                        val simulatedLat = currentSite.latitude + deltaLat
                        val simulatedLng = currentSite.longitude
                        onSimulateCheckIn(currentWorker, currentSite, simulatedLat, simulatedLng)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("execute_simulated_checkin_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInsideGeofence) Green600 else Color(0xFFD97706)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.GpsFixed,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kryej Check-in me GPS (${distanceMeters.toInt()}m)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
