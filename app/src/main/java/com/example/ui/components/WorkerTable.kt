package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GpsStatus
import com.example.data.model.Worker
import com.example.data.model.WorkerStatus
import com.example.ui.theme.Amber600
import com.example.ui.theme.Cyan50
import com.example.ui.theme.Cyan800
import com.example.ui.theme.Green100
import com.example.ui.theme.Green800
import com.example.ui.theme.Red100
import com.example.ui.theme.Red800

@Composable
fun WorkerTableCard(
    workers: List<Worker>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    statusFilter: String,
    onFilterChange: (String) -> Unit,
    onExportReportClick: () -> Unit,
    onWorkerClick: (Worker) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("worker_table_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📍 Punëtorët dhe Vendndodhja Aktuale",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Monitorimi GPS dhe orari në kohë reale",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // PDF Export button matching HTML .btn-pdf (#d97706)
                Button(
                    onClick = onExportReportClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber600,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("export_report_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📄 Eksporto Raportin",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_workers_input"),
                placeholder = { Text("Kërko sipas elektricistit, objektit apo rolit...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == "ALL",
                    onClick = { onFilterChange("ALL") },
                    label = { Text("Të gjithë (${workers.size})") }
                )
                FilterChip(
                    selected = statusFilter == "WORKING",
                    onClick = { onFilterChange("WORKING") },
                    label = { Text("Në Punë") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Green100,
                        selectedLabelColor = Green800
                    )
                )
                FilterChip(
                    selected = statusFilter == "BREAK",
                    onClick = { onFilterChange("BREAK") },
                    label = { Text("Pushim") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Red100,
                        selectedLabelColor = Red800
                    )
                )
                FilterChip(
                    selected = statusFilter == "GPS_ALERT",
                    onClick = { onFilterChange("GPS_ALERT") },
                    label = { Text("Alert GPS") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Table / List of Workers
            if (workers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nuk u gjet asnjë elektricist me këtë filtër.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    workers.forEach { worker ->
                        WorkerRowCard(
                            worker = worker,
                            onClick = { onWorkerClick(worker) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerRowCard(
    worker: Worker,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .testTag("worker_row_${worker.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Row 1: Name and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = worker.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = worker.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = worker.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Objekti, Check-in, GPS Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Objekti info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = worker.currentSiteName ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                // Check-in info
                if (worker.checkInTime != null && worker.checkInTime != "-") {
                    Text(
                        text = "🕒 ${worker.checkInTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }

                // GPS Badge
                GpsStatusBadge(
                    status = worker.gpsStatus,
                    distanceMeters = worker.gpsDistanceMeters
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: WorkerStatus) {
    val (bg, fg, text) = when (status) {
        WorkerStatus.WORKING -> Triple(Green100, Green800, "Në Punë")
        WorkerStatus.BREAK -> Triple(Red100, Red800, "Pushim / Jashtë Orarit")
        WorkerStatus.PAUSED -> Triple(Color(0xFFFEF9C3), Color(0xFF854D0E), "Në Pauzë")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun GpsStatusBadge(
    status: GpsStatus,
    distanceMeters: Int?
) {
    val (bg, fg, label) = when (status) {
        GpsStatus.VERIFIED -> Triple(
            Cyan50,
            Cyan800,
            if (distanceMeters != null) "🟢 Verified (${distanceMeters}m)" else "🟢 Verified"
        )
        GpsStatus.OUT_OF_BOUNDS -> Triple(
            Color(0xFFFEF3C7),
            Color(0xFF92400E),
            if (distanceMeters != null) "🟡 Jashtë (${distanceMeters}m)" else "🟡 Jashtë Zonës"
        )
        GpsStatus.NO_SIGNAL -> Triple(
            Color(0xFFF1F5F9),
            Color(0xFF475569),
            "⚪ Pa Sinjal"
        )
        GpsStatus.OFFLINE -> Triple(
            Color(0xFFF1F5F9),
            Color(0xFF64748B),
            "-"
        )
    }

    if (label != "-") {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = fg,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
