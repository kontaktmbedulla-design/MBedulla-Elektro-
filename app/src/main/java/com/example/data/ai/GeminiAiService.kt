package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AiGroundingMode(val label: String, val description: String) {
    BOTH("Harta & Kërkim Google", "Gjej dyqane materialeve & standarde elektrike"),
    MAPS_ONLY("Google Maps Vetëm", "Lokacione dyqanesh, furnizuesish & distancë"),
    SEARCH_ONLY("Google Search Vetëm", "Rregullore KEDS, kode IEC & specifikime")
}

data class GroundingSource(
    val title: String,
    val uri: String,
    val snippet: String? = null
)

data class GroundingPlace(
    val title: String,
    val address: String? = null,
    val uri: String? = null
)

data class GeminiAiResponse(
    val text: String,
    val searchSources: List<GroundingSource> = emptyList(),
    val mapPlaces: List<GroundingPlace> = emptyList(),
    val searchQueries: List<String> = emptyList(),
    val isError: Boolean = false
)

object GeminiAiService {
    private const val TAG = "GeminiAiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun queryGemini(
        prompt: String,
        groundingMode: AiGroundingMode = AiGroundingMode.BOTH,
        userLocation: String? = "Prishtinë, Kosovë"
    ): GeminiAiResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiAiResponse(
                text = "⚠️ Çelësi i Gemini API nuk është konfiguruar ende. Ju lutem vendosni GEMINI_API_KEY në panelin e Secrets në AI Studio.\n\nNdërkohë, pyetja juaj për instalimet elektrike: \"$prompt\" kërkon verifikim të standardeve teknike të KEDS dhe furnizuesve lokalë.",
                isError = false
            )
        }

        try {
            // Build Context Prompt for ElektroTrack
            val systemContext = "Ti je Asistenti Inteligjent i Inxhinierisë Elektrike dhe Terrenit për kompaninë 'ElektroTrack ⚡' në Kosovë. " +
                    "Ndihmo inxhinierët dhe elektricistët në terren me: " +
                    "1. Gjetjen e dyqaneve dhe distributorëve të materialeve elektrike (kabllo, automate, siguresa, transformatorë, inverterë fotovoltaikë) duke përdorur Google Maps. " +
                    "2. Dhënien e rregulloreve teknike të sakta të KEDS dhe standardeve evropiane IEC/EN duke përdorur Google Search. " +
                    "Lokacioni i terrenit: $userLocation. Përgjigju qartë, profesionalisht dhe në gjuhën shqipe."

            val fullPrompt = "$systemContext\n\nPyetja e elektricistit / menaxherit:\n$prompt"

            // Construct JSON payload
            val rootJson = JSONObject()

            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", fullPrompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            // Tools (Google Search & Google Maps Grounding)
            val toolsArray = JSONArray()
            val toolsObj = JSONObject()

            when (groundingMode) {
                AiGroundingMode.BOTH -> {
                    toolsObj.put("googleSearch", JSONObject())
                    toolsObj.put("googleMaps", JSONObject())
                    toolsArray.put(toolsObj)
                }
                AiGroundingMode.MAPS_ONLY -> {
                    toolsObj.put("googleMaps", JSONObject())
                    toolsArray.put(toolsObj)
                }
                AiGroundingMode.SEARCH_ONLY -> {
                    toolsObj.put("googleSearch", JSONObject())
                    toolsArray.put(toolsObj)
                }
            }
            rootJson.put("tools", toolsArray)

            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error: ${response.code} -> $responseString")
                return@withContext GeminiAiResponse(
                    text = "Gabim gjatë komunikimit me Gemini AI (${response.code}). Ju lutem provoni përsëri.",
                    isError = true
                )
            }

            parseGeminiResponse(responseString)
        } catch (e: Exception) {
            Log.e(TAG, "Exception querying Gemini AI", e)
            GeminiAiResponse(
                text = "Dështoi lidhja me shërbimin AI: ${e.localizedMessage ?: "Gabim i panjohur"}",
                isError = true
            )
        }
    }

    private fun parseGeminiResponse(jsonString: String): GeminiAiResponse {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return GeminiAiResponse(text = "Nuk u kthye asnjë përgjigje nga modeli.", isError = true)
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    if (p.has("text")) {
                        textBuilder.append(p.getString("text"))
                    }
                }
            }

            val searchSources = mutableListOf<GroundingSource>()
            val mapPlaces = mutableListOf<GroundingPlace>()
            val searchQueries = mutableListOf<String>()

            val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                // Search queries
                val webQueries = groundingMetadata.optJSONArray("webSearchQueries")
                if (webQueries != null) {
                    for (i in 0 until webQueries.length()) {
                        searchQueries.add(webQueries.getString(i))
                    }
                }

                // Grounding chunks
                val chunks = groundingMetadata.optJSONArray("groundingChunks")
                if (chunks != null) {
                    for (i in 0 until chunks.length()) {
                        val chunk = chunks.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        if (web != null) {
                            val uri = web.optString("uri", "")
                            val title = web.optString("title", "Burim Uebi")
                            searchSources.add(GroundingSource(title = title, uri = uri))
                        }
                        val maps = chunk.optJSONObject("maps")
                        if (maps != null) {
                            val title = maps.optString("title", "Lokacion në Google Maps")
                            val address = if (maps.has("address")) maps.getString("address") else null
                            val uri = if (maps.has("uri")) maps.getString("uri") else null
                            mapPlaces.add(GroundingPlace(title = title, address = address, uri = uri))
                        }
                    }
                }
            }

            GeminiAiResponse(
                text = if (textBuilder.isNotEmpty()) textBuilder.toString() else "Përgjigja u procesua.",
                searchSources = searchSources.distinctBy { it.uri },
                mapPlaces = mapPlaces.distinctBy { it.title },
                searchQueries = searchQueries,
                isError = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini response", e)
            GeminiAiResponse(text = "Gabim gjatë procesimit të përgjigjes.", isError = true)
        }
    }
}
