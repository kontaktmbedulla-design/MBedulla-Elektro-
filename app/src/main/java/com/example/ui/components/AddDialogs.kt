package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, role: String, phone: String, rate: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+383 ") }
    var hourlyRate by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "⚡ Regjistro Elektricist të Ri",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Emri dhe Mbiemri") },
                    placeholder = { Text("p.sh. Besnik Morina") },
                    modifier = Modifier.fillMaxWidth().testTag("worker_name_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Pozita / Specializimi") },
                    placeholder = { Text("p.sh. Mjeshtër i Instalimeve") },
                    modifier = Modifier.fillMaxWidth().testTag("worker_role_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Numri i Telefonit") },
                    placeholder = { Text("+383 44 111 222") },
                    modifier = Modifier.fillMaxWidth().testTag("worker_phone_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it },
                    label = { Text("Çmimi për Orë (€/std)") },
                    placeholder = { Text("10.0") },
                    modifier = Modifier.fillMaxWidth().testTag("worker_rate_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val rate = hourlyRate.toDoubleOrNull() ?: 10.0
                        onConfirm(name.trim(), role.trim(), phone.trim(), rate)
                    }
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_add_worker_button")
            ) {
                Text("Ruaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anulo")
            }
        },
        modifier = modifier.testTag("add_worker_dialog")
    )
}

@Composable
fun AddSiteDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String, manager: String, targetWorkers: Int, lat: Double, lng: Double, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var manager by remember { mutableStateOf("") }
    var targetWorkers by remember { mutableStateOf("2") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🏗️ Shto Objekt të Ri me Punime",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Emri i Objektit") },
                    placeholder = { Text("p.sh. Kompleksi Banesor Panorama") },
                    modifier = Modifier.fillMaxWidth().testTag("site_name_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adresa / Qyteti") },
                    placeholder = { Text("p.sh. Rruga Muharrem Fejza, Prishtinë") },
                    modifier = Modifier.fillMaxWidth().testTag("site_address_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = manager,
                    onValueChange = { manager = it },
                    label = { Text("Përgjegjësi / Inxhinieri") },
                    placeholder = { Text("p.sh. Ing. Fatmir Krasniqi") },
                    modifier = Modifier.fillMaxWidth().testTag("site_manager_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetWorkers,
                    onValueChange = { targetWorkers = it },
                    label = { Text("Numri i planifikuar i punëtorëve") },
                    placeholder = { Text("2") },
                    modifier = Modifier.fillMaxWidth().testTag("site_target_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Përshkrimi i punimeve") },
                    placeholder = { Text("Instalimi i rrjetit elektrik...") },
                    modifier = Modifier.fillMaxWidth().testTag("site_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val target = targetWorkers.toIntOrNull() ?: 2
                        // Default Prishtina coordinates with slight random variance
                        val lat = 42.6629 + (Math.random() - 0.5) * 0.05
                        val lng = 21.1655 + (Math.random() - 0.5) * 0.05
                        onConfirm(name.trim(), address.trim(), manager.trim(), target, lat, lng, notes.trim())
                    }
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_add_site_button")
            ) {
                Text("Krijo Objektin")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anulo")
            }
        },
        modifier = modifier.testTag("add_site_dialog")
    )
}
