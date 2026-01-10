package com.example.plantscan.apiconnect.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.app.domain.utils.SupabaseConfig
import com.example.plantscan.Domain.Utils.NetworkUtils
import com.example.plantscan.apiconnect.model.PlantHistory
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class HistoryRepository(
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        engine { requestTimeout = 600_000 }
    }
) {

    private val TAG = "HistoryRepository"

    private fun compressImage(file: File, maxKB: Int = 800): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var quality = 90
        val outFile = File(file.parent, "compressed_${file.name}")
        while (quality > 10) {
            FileOutputStream(outFile).use { bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it) }
            if (outFile.length() / 1024 <= maxKB) break
            quality -= 10
        }
        return outFile
    }

    private suspend fun uploadPhotoToStorage(file: File, retries: Int = 3): String? {
        val compressed = compressImage(file)
        val name = "history_${UUID.randomUUID()}.jpg"
        repeat(retries) { attempt ->
            try {
                val response: HttpResponse = client.post("${SupabaseConfig.BASE_URL_STORAGE}/object/${SupabaseConfig.STORAGE_HISTORY_BUCKET}/$name") {
                    header("apikey", SupabaseConfig.API_KEY)
                    header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                    contentType(ContentType.Image.JPEG)
                    setBody(compressed.readBytes())
                }
                if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                    return "${SupabaseConfig.BASE_URL_STORAGE}/object/public/${SupabaseConfig.STORAGE_HISTORY_BUCKET}/$name"
                } else {
                    Log.e(TAG, "Storage upload failed status=${response.status}, body=${response.bodyAsText()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Upload attempt ${attempt + 1} error", e)
            }
            delay(1000L)
        }
        return null
    }

    suspend fun saveHistory(plant: PlantHistory, file: File?, context: android.content.Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkUtils.isInternetAvailable(context)) return@withContext false

                val photoUrl = file?.let { uploadPhotoToStorage(it) }
                val record = plant.copy(
                    id = UUID.randomUUID().toString(),
                    image_url = photoUrl ?: "",
                    created_at = null
                )

                val resp: HttpResponse = client.post("${SupabaseConfig.BASE_URL_REST}/plants_history") {
                    header("apikey", SupabaseConfig.API_KEY)
                    header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                    contentType(ContentType.Application.Json)
                    setBody(record)
                }

                if (resp.status == HttpStatusCode.Created || resp.status == HttpStatusCode.OK) {
                    true
                } else {
                    Log.e(TAG, "Save history failed: ${resp.status} ${resp.bodyAsText()}")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveHistory error", e)
                false
            }
        }
    }

    suspend fun getLastThree(): List<PlantHistory> = withContext(Dispatchers.IO) {
        try {
            val resp: HttpResponse = client.get("${SupabaseConfig.BASE_URL_REST}/plants_history") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                parameter("select", "*")
                parameter("order", "created_at.desc")
                parameter("limit", "3")
            }

            if (resp.status.isSuccess()) {
                val body = resp.bodyAsText()
                val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
                val jsonArray = json.parseToJsonElement(body).jsonArray

                jsonArray.map { elem ->
                    val obj = elem.jsonObject

                    fun safeList(key: String): List<String> =
                        obj[key]?.let {
                            if (it is kotlinx.serialization.json.JsonArray) {
                                it.mapNotNull { item -> item.jsonPrimitive.contentOrNull }
                            } else emptyList()
                        } ?: emptyList()

                    PlantHistory(
                        id = obj["id"]?.jsonPrimitive?.contentOrNull,
                        name = obj["name"]?.jsonPrimitive?.contentOrNull ?: "",
                        diagnosis = obj["diagnosis"]?.jsonPrimitive?.contentOrNull ?: "",
                        symptoms = safeList("symptoms"),
                        causes = safeList("causes"),
                        recommendations = safeList("recommendations"),
                        image_url = obj["image_url"]?.jsonPrimitive?.contentOrNull ?: "",
                        created_at = obj["created_at"]?.jsonPrimitive?.contentOrNull
                    )
                }
            } else {
                Log.e(TAG, "getLastThree failed: ${resp.status} ${resp.bodyAsText()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLastThree error", e)
            emptyList()
        }
    }
}
