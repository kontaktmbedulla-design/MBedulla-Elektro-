package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.ai.AiGroundingMode
import com.example.data.ai.GeminiAiResponse
import com.example.data.ai.GeminiAiService
import com.example.ui.theme.Amber500
import com.example.ui.theme.Blue500
import com.example.ui.theme.Green500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAssistantSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var promptInput by remember { mutableStateOf("") }
    var selectedGroundingMode by remember { mutableStateOf(AiGroundingMode.BOTH) }
    var isLoading by remember { mutableStateOf(false) }
    var aiResponse by remember { mutableStateOf<GeminiAiResponse?>(null) }

    val quickPrompts = listOf(
        "📍 Dyqanet e materialeve elektrike afër meje" to AiGroundingMode.MAPS_ONLY,
        "⚡ Rregullat e KEDS për kyçje të re trifazore" to AiGroundingMode.SEARCH_ONLY,
        "☀️ Kërkesat e tokëzimit për panele fotovoltaike" to AiGroundingMode.BOTH,
        "🔌 Tabela e rënies së tensionit për kabllo bakri" to AiGroundingMode.SEARCH_ONLY
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_assistant_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with AI Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Amber500.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Amber500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Asistenti AI i Terrenit & Harta ⚡",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gemini 3.8 Flash me Google Search & Google Maps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grounding Tool Mode Filter Chips
            Text(
                text = "Burimet e Informacionit (Grounding):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedGroundingMode == AiGroundingMode.BOTH,
                    onClick = { selectedGroundingMode = AiGroundingMode.BOTH },
                    label = { Text("Harta & Kërkim", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Amber500.copy(alpha = 0.2f))
                )
                FilterChip(
                    selected = selectedGroundingMode == AiGroundingMode.MAPS_ONLY,
                    onClick = { selectedGroundingMode = AiGroundingMode.MAPS_ONLY },
                    label = { Text("Google Maps", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Blue500.copy(alpha = 0.2f))
                )
                FilterChip(
                    selected = selectedGroundingMode == AiGroundingMode.SEARCH_ONLY,
                    onClick = { selectedGroundingMode = AiGroundingMode.SEARCH_ONLY },
                    label = { Text("Google Search", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp)) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Green500.copy(alpha = 0.2f))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Prompt Suggestions
            Text(
                text = "Pyetje të shpeshta të terrenit:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPrompts.forEach { (prompt, mode) ->
                    SuggestionChip(
                        onClick = {
                            promptInput = prompt
                            selectedGroundingMode = mode
                        },
                        label = { Text(prompt, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Input TextField & Send Button
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("Pyesni p.sh. Gjej dyqan afër meje për siguresa 63A ose rregullat e KEDS...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_prompt_input"),
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber500,
                    cursorColor = Amber500
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (promptInput.isNotBlank() && !isLoading) {
                        isLoading = true
                        scope.launch {
                            aiResponse = GeminiAiService.queryGemini(
                                prompt = promptInput.trim(),
                                groundingMode = selectedGroundingMode
                            )
                            isLoading = false
                        }
                    }
                },
                enabled = promptInput.isNotBlank() && !isLoading,
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag("ai_send_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Slate900)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Amber500,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Duke kërkuar...", color = Amber500)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = Amber500,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pyet Gemini ⚡", color = Amber500, fontWeight = FontWeight.Bold)
                }
            }

            // Results Display
            aiResponse?.let { res ->
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.isError) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (res.isError) Color(0xFFEF4444) else Amber500.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (res.isError) Color(0xFFEF4444) else Amber500,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Përgjigja e Asistentit AI:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = res.text,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )

                        // Google Maps Places Found
                        if (res.mapPlaces.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "📍 Lokacione të Gjetura në Google Maps:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Blue500
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            res.mapPlaces.forEach { place ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable {
                                            val query = place.address ?: place.title
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(place.uri ?: "geo:0,0?q=${Uri.encode(query)}")
                                            )
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                // ignore
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = place.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        place.address?.let {
                                            Text(text = it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Hap në Hartë", tint = Blue500, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Google Search Sources Found
                        if (res.searchSources.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "🔍 Burime Zyrtare të Verifikuara:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Green500
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                res.searchSources.forEach { source ->
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .clickable {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.uri))
                                                try {
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    // ignore
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Language, contentDescription = null, tint = Green500, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = source.title, fontSize = 11.sp, color = Green500, fontWeight = FontWeight.Medium)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Green500, modifier = Modifier.size(10.dp))
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
