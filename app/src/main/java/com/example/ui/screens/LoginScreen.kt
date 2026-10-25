package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import android.util.Log
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.example.ui.model.UserSession
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Worker
import com.example.ui.ElektroViewModel
import com.example.ui.model.UserRole
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Blue600
import com.example.ui.theme.Green100
import com.example.ui.theme.Green600
import com.example.ui.theme.Green800
import com.example.ui.theme.Red500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: ElektroViewModel,
    modifier: Modifier = Modifier
) {
    val workers by viewModel.allWorkers.collectAsState()
    val firebaseUser by viewModel.firebaseUser.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var selectedRole by remember { mutableStateOf(UserRole.MANAGER) }

    // Manager Form State
    var managerEmail by remember { mutableStateOf("menaxher@elektrotrack.com") }
    var managerPassword by remember { mutableStateOf("123456") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Electrician Form State
    var selectedWorkerIndex by remember { mutableStateOf(0) }
    var workerDropdownExpanded by remember { mutableStateOf(false) }
    var electricianPin by remember { mutableStateOf("1234") }
    var pinVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentWorker = workers.getOrNull(selectedWorkerIndex) ?: workers.firstOrNull()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("login_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Logo & Title
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Slate900),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Amber500,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ElektroTrack ⚡",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Sistemi i Menaxhimit dhe Kyçjes në Terren",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Role Switcher Tabs (Menaxher vs Elektricist)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        // Manager tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedRole == UserRole.MANAGER) Slate900 else Color.Transparent)
                                .clickable {
                                    selectedRole = UserRole.MANAGER
                                    errorMessage = null
                                }
                                .padding(vertical = 10.dp)
                                .testTag("select_manager_role_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (selectedRole == UserRole.MANAGER) Amber500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Menaxher",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedRole == UserRole.MANAGER) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedRole == UserRole.MANAGER) Amber500 else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Electrician tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedRole == UserRole.ELECTRICIAN) Slate900 else Color.Transparent)
                                .clickable {
                                    selectedRole = UserRole.ELECTRICIAN
                                    errorMessage = null
                                }
                                .padding(vertical = 10.dp)
                                .testTag("select_electrician_role_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (selectedRole == UserRole.ELECTRICIAN) Amber500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Elektricist",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedRole == UserRole.ELECTRICIAN) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedRole == UserRole.ELECTRICIAN) Amber500 else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google & Firebase Auth Card
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("google_auth_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Blue600,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Autentikim i Sigurt me Google & Firebase",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lidhni llogarinë tuaj për sinkronizim të të dhënave në re me Firestore.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (firebaseUser != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Green100.copy(alpha = 0.5f))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "✓ I Kyçur me Google:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Green800
                                        )
                                        Text(
                                            text = firebaseUser?.email ?: firebaseUser?.displayName ?: "Google User",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Green800
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            val selectedWorker = if (selectedRole == UserRole.ELECTRICIAN) currentWorker else null
                                            viewModel.currentUserSession.value = UserSession(
                                                id = firebaseUser!!.uid,
                                                name = firebaseUser?.displayName ?: firebaseUser?.email?.substringBefore("@") ?: "Përdorues Google",
                                                email = firebaseUser?.email ?: "",
                                                role = selectedRole,
                                                workerId = selectedWorker?.id,
                                                roleDescription = if (selectedRole == UserRole.MANAGER) "Menaxher Projekti (Google)" else "Elektricist në Terren (Google)"
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                                        modifier = Modifier.testTag("continue_with_existing_google_session_button")
                                    ) {
                                        Text("Hyr si ${if (selectedRole == UserRole.MANAGER) "Menaxher" else "Elektricist"}", fontSize = 11.sp, color = Amber500)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                val serverClientId = try {
                                                    context.getString(R.string.default_web_client_id)
                                                } catch (e: Exception) {
                                                    "95339977661-g51vche473e370b1atsoks9semh2qusm.apps.googleusercontent.com"
                                                }
                                                val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId)
                                                    .build()
                                                val request = GetCredentialRequest.Builder()
                                                    .addCredentialOption(googleIdOption)
                                                    .build()
                                                val result = credentialManager.getCredential(context = context, request = request)
                                                val credential = result.credential
                                                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                                    val idToken = googleIdTokenCredential.idToken
                                                    val selectedWorker = if (selectedRole == UserRole.ELECTRICIAN) currentWorker else null
                                                    viewModel.signInWithGoogle(idToken, selectedRole, selectedWorker?.id)
                                                }
                                            } catch (e: Exception) {
                                                Log.e("LoginScreen", "Google Sign-In error", e)
                                                errorMessage = "Nuk u arrit kyçja me Google: ${e.localizedMessage ?: "Përdoruesi anuloi ose nuk ka Google Play Services"}"
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("google_sign_in_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Google Logo",
                                            tint = Blue600,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Vazhdo me Google (Sign In)",
                                            color = Color(0xFF1E293B),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = "  ose me llogari lokale  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Form according to selected role
                    if (selectedRole == UserRole.MANAGER) {
                        // Manager Login Form
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Email i Menaxherit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = managerEmail,
                                onValueChange = { managerEmail = it },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Email, contentDescription = null)
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("manager_email_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Fjalëkalimi (Password)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = managerPassword,
                                onValueChange = { managerPassword = it },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth().testTag("manager_password_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    if (managerEmail.isNotBlank()) {
                                        viewModel.loginAsManager(managerEmail.trim())
                                    } else {
                                        errorMessage = "Ju lutem plotësoni email-in."
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_manager_login_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                            ) {
                                Text("Kyçu si Menaxher", fontWeight = FontWeight.Bold, color = Amber500, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Amber500, modifier = Modifier.size(16.dp))
                            }
                        }
                    } else {
                        // Electrician Login Form
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Zgjidh Elektricistin",
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
                                    value = currentWorker?.name ?: "Zgjidh elektricistin",
                                    onValueChange = {},
                                    readOnly = true,
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null)
                                    },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workerDropdownExpanded) },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().testTag("electrician_select_dropdown"),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = workerDropdownExpanded,
                                    onDismissRequest = { workerDropdownExpanded = false }
                                ) {
                                    workers.forEachIndexed { index, worker ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(worker.name, fontWeight = FontWeight.Bold)
                                                    Text(worker.role, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                selectedWorkerIndex = index
                                                workerDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Kodi PIN i Sigurisë",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = electricianPin,
                                onValueChange = { electricianPin = it },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { pinVisible = !pinVisible }) {
                                        Icon(
                                            imageVector = if (pinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth().testTag("electrician_pin_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = {
                                    currentWorker?.let { worker ->
                                        viewModel.loginAsElectrician(worker)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_electrician_login_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                            ) {
                                Text("Kyçu si Elektricist", fontWeight = FontWeight.Bold, color = Amber500, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Amber500, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Error Message
                    errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = err, color = Red500, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Demo / Fast Switching Section
                    Text(
                        text = "🚀 Kyçje e Menjëhershme për Demonstrim:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.loginAsManager("menaxher@elektrotrack.com") },
                            label = { Text("🛡️ Menaxher") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Amber500.copy(alpha = 0.15f),
                                labelColor = Amber600
                            )
                        )

                        workers.forEach { worker ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.loginAsElectrician(worker) },
                                label = { Text("⚡ ${worker.name}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Green100,
                                    labelColor = Green800
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
