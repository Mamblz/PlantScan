package com.example.plantscan.apiconnect.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.plantscan.apiconnect.model.FavoritePlant
import com.example.app.domain.utils.SupabaseConfig
import com.example.plantscan.Domain.Utils.NetworkUtils
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FavoritesRepository(
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
        engine {
            requestTimeout = 600_000
            endpoint {
                connectTimeout = 60_000
                socketTimeout = 60_000
            }
        }
    }
) {

    suspend fun getPlantById(id: String): FavoritePlant? = withContext(Dispatchers.IO) {
        try {
            val response = client.get("${SupabaseConfig.BASE_URL_REST}/favorites") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                parameter("id", "eq.$id")
                parameter("select", "*")
            }

            if (response.status.isSuccess()) {
                val plants = Json.decodeFromString<List<FavoritePlant>>(response.bodyAsText())
                plants.firstOrNull()
            } else null

        } catch (e: Exception) {
            Log.e("FavoritesRepo", "getPlantById error", e)
            null
        }
    }

    suspend fun saveFavoriteWithPhoto(
        plant: FavoritePlant,
        photoFile: File?,
        context: android.content.Context
    ): Boolean = withContext(Dispatchers.IO) {

        if (!NetworkUtils.isInternetAvailable(context)) return@withContext false

        val photoUrl = photoFile?.let { uploadAndCompressPhoto(it) }

        val plantWithUrl = plant.copy(
            id = UUID.randomUUID().toString(),
            created_at = null,
            photo_url = photoUrl
        )

        saveFavorite(plantWithUrl)
    }

    private suspend fun uploadAndCompressPhoto(file: File, retries: Int = 3): String? {
        val compressed = compressImage(file, 800)
        val uniqueName = "${UUID.randomUUID()}_${compressed.nameWithoutExtension}.jpg"

        repeat(retries) { attempt ->
            try {
                val response = client.post(
                    "${SupabaseConfig.BASE_URL_STORAGE}/object/${SupabaseConfig.STORAGE_BUCKET}/$uniqueName"
                ) {
                    header("apikey", SupabaseConfig.API_KEY)
                    header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                    contentType(ContentType.Image.JPEG)
                    setBody(compressed.readBytes())
                }

                if (response.status == HttpStatusCode.OK) {
                    return "${SupabaseConfig.BASE_URL_STORAGE}/object/public/${SupabaseConfig.STORAGE_BUCKET}/$uniqueName"
                }

            } catch (e: Exception) {
                Log.e("FavoritesRepo", "Upload error", e)
                delay(1000)
            }
        }

        return null
    }

    private fun compressImage(file: File, maxKB: Int): File {
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

    suspend fun saveFavorite(plant: FavoritePlant): Boolean {
        return try {
            val response = client.post("${SupabaseConfig.BASE_URL_REST}/favorites") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                contentType(ContentType.Application.Json)
                setBody(plant)
            }

            response.status == HttpStatusCode.Created

        } catch (e: Exception) {
            Log.e("FavoritesRepo", "Save error", e)
            false
        }
    }

    suspend fun getFavorites(): List<FavoritePlant> = withContext(Dispatchers.IO) {
        try {
            val response = client.get("${SupabaseConfig.BASE_URL_REST}/favorites") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                parameter("select", "*")
                parameter("order", "created_at.desc")
            }

            if (response.status.isSuccess())
                Json.decodeFromString(response.bodyAsText())
            else emptyList()

        } catch (e: Exception) {
            Log.e("FavoritesRepo", "Load error", e)
            emptyList()
        }
    }

    suspend fun deleteFavorite(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.delete("${SupabaseConfig.BASE_URL_REST}/favorites") {
                header("apikey", SupabaseConfig.API_KEY)
                header("Authorization", "Bearer ${SupabaseConfig.API_KEY}")
                parameter("id", "eq.$id")
            }

            response.status == HttpStatusCode.NoContent

        } catch (e: Exception) {
            Log.e("FavoritesRepo", "Delete error", e)
            false
        }
    }
}
