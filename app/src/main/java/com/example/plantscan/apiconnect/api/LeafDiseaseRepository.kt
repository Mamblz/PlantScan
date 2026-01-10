package com.example.plantscan.apiconnect.api

import android.util.Base64
import android.util.Log
import com.example.app.domain.utils.GeminiApi.GEMINI_API_KEY
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.io.File
import kotlinx.coroutines.delay

class LeafDiseaseRepository {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        engine {
            requestTimeout = 120_000
        }
    }



    @Serializable
    data class DiseaseResponse(
        val plant_name: String = "",
        val diagnosis: String = "",
        val symptoms: List<String> = emptyList(),
        val causes: List<String> = emptyList(),
        val recommendations: List<String> = emptyList()
    )

    suspend fun analyzeLeaf(
        file: File,
        maxRetries: Int = 3,
        initialDelay: Long = 1500
    ): DiseaseResponse? {
        val bytes = file.readBytes()
        val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)

        val mimeType = when {
            file.name.endsWith(".png", true) -> "image/png"
            file.name.endsWith(".jpg", true) || file.name.endsWith(".jpeg", true) -> "image/jpeg"
            else -> "image/jpeg"
        }

        val requestBody = buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("parts") {
                        addJsonObject {
                            put(
                                "text",
                                """
                                Проанализируй фото листа растения. Определи:
                                - Название растения (научное или общепринятое)
                                - Состояние растения
                                - Если болеет — болезнь, симптомы, причины, рекомендации
                                
                                Верни JSON строго в формате на РУССКОМ ЯЗЫКЕ:
                                {
                                  "plant_name": "...",
                                  "diagnosis": "...",
                                  "symptoms": ["..."],
                                  "causes": ["..."],
                                  "recommendations": ["..."]
                                }
                                """.trimIndent()
                            )
                        }
                        addJsonObject {
                            putJsonObject("inline_data") {
                                put("mime_type", mimeType)
                                put("data", base64Image)
                            }
                        }
                    }
                }
            }
        }

        val url =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$GEMINI_API_KEY"

        var attempt = 0
        var delayTime = initialDelay

        while (attempt <= maxRetries) {
            try {
                Log.d("LeafDiseaseRepository", "Attempt ${attempt + 1} of ${maxRetries + 1}")
                val response: HttpResponse = client.post(url) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

                val bodyText = response.bodyAsText()
                Log.d("LeafDiseaseRepository", "Raw API Response: $bodyText")

                val jsonResponse = Json.parseToJsonElement(bodyText).jsonObject

                val error = jsonResponse["error"]?.jsonObject
                if (error != null) {
                    val code = error["code"]?.jsonPrimitive?.intOrNull
                    val message = error["message"]?.jsonPrimitive?.contentOrNull
                    Log.e("LeafDiseaseRepository", "API Error $code: $message")
                    if (code == 503) {
                        attempt++
                        if (attempt <= maxRetries) {
                            Log.d("LeafDiseaseRepository", "Retrying in $delayTime ms")
                            delay(delayTime)
                            delayTime *= 2
                            continue
                        }
                    }
                    return null
                }

                val text = jsonResponse["candidates"]?.jsonArray
                    ?.mapNotNull { candidate ->
                        candidate.jsonObject["content"]
                            ?.jsonObject?.get("parts")?.jsonArray
                            ?.mapNotNull { part ->
                                part.jsonObject["text"]?.jsonPrimitive?.contentOrNull
                            }?.firstOrNull()
                    }?.firstOrNull()

                if (text.isNullOrEmpty()) {
                    Log.e("LeafDiseaseRepository", "Gemini вернул пустой текст или candidates отсутствуют")
                    return null
                }

                val cleanedJson = text
                    .replace(Regex("^```json\\s*"), "")
                    .replace(Regex("\\s*```$"), "")
                    .trim()

                Log.d("LeafDiseaseRepository", "Cleaned JSON: $cleanedJson")

                return try {
                    Json.decodeFromString(DiseaseResponse.serializer(), cleanedJson)
                } catch (e: Exception) {
                    Log.e("LeafDiseaseRepository", "Ошибка декодирования JSON: ${e.message}", e)
                    null
                }

            } catch (e: Exception) {
                Log.e("LeafDiseaseRepository", "Ошибка при анализе листа: ${e.message}", e)
                attempt++
                if (attempt <= maxRetries) {
                    Log.d("LeafDiseaseRepository", "Retrying in $delayTime ms")
                    delay(delayTime)
                    delayTime *= 2
                    continue
                }
                return null
            }
        }
        return null
    }
}
